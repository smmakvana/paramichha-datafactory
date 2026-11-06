package com.paramichha.datafactory;

import com.paramichha.datafactory.regression.missingtype.HoldsMissingType;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Paths of the object builder and the field builders that ordinary fixtures don't reach. */
class BuilderEdgeCasesTest {

    record Person(String name) {}
    record Letter(char c) {}
    record Node(String name, Node next) {}
    record WithOptional(Optional<Person> person) {}
    record Team(@Size(min = 2, max = 2) List<Person> members) {}
    interface Shape {}
    record Drawing(List<Shape> shapes) {}

    /** Needs an enclosing instance, so DataFactory can't create it. */
    @SuppressWarnings("unused")
    class Inner { String label; }

    /** No constructor matches its fields, and there is no no-args constructor. */
    @SuppressWarnings("unused")
    static class OddConstructor {
        String label;
        OddConstructor(int a, int b) {}
    }

    /** A hand-written builder whose setters don't take the field's exact type. */
    public static final class HandBuilt {
        final int count;
        final Integer level;
        final long size;
        final String note;

        HandBuilt(int count, Integer level, long size, String note) {
            this.count = count;
            this.level = level;
            this.size = size;
            this.note = note;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private Integer count = 0;
            private Number level;
            private long size;

            public Builder count(Integer count) { this.count = count; return this; }   // field is int
            public Builder level(Number level) { this.level = level; return this; }     // field is Integer
            public Builder size(Number size) { this.size = size.longValue(); return this; } // field is long
            // no setter for note
            public HandBuilt build() {
                return new HandBuilt(count, level == null ? null : level.intValue(), size, null);
            }
        }
    }

    @Nested
    @DisplayName("object builder")
    class ObjectBuilder {

        @Test
        void withNullSetsTheFieldToNull() {
            assertThat(DataFactory.of(Person.class).withNull("name").valid().get().name()).isNull();
        }

        @Test
        void aFieldWithNothingToViolateIsAConfigurationFailure() {
            assertThat(DataFactory.of(Letter.class).invalidFor("c").errorType())
                    .isEqualTo(ErrorType.INVALID_CONFIGURATION);
            DataFactoryResults<Letter> all = DataFactory.of(Letter.class).invalidList();
            assertThat(all.total()).isEqualTo(1);
            assertThat(all.failed().getFirst().errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
        }

        @Test
        void fieldsUsesTheOverrideAsTheOnlyValidValue() {
            FieldCases cases = DataFactory.of(Person.class).with("name", "Ann").fields().get("name").get();
            assertThat(cases.validValues()).containsExactly("Ann");
            assertThat(cases.invalidCases()).isNotEmpty();
        }

        @Test
        void aSelfReferenceStopsAfterOneLevel() {
            Node node = DataFactory.of(Node.class).valid().get();
            assertThat(node.next()).isNotNull();
            assertThat(node.next().next()).isNull();
        }

        @Test
        void optionalOfAnObjectIsFilled() {
            assertThat(DataFactory.of(WithOptional.class).valid().get().person()).isPresent();
        }

        @Test
        void aCollectionOfObjectsIsFilledToItsSize() {
            assertThat(DataFactory.of(Team.class).valid().get().members()).hasSize(2);
        }

        @Test
        void anElementThatCannotBeBuiltFailsTheObject() {
            DataFactoryResult<Drawing> r = DataFactory.of(Drawing.class).valid();
            assertThat(r.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE);
            assertThat(r.reason()).contains("Failed to build element of type").contains("Shape");
        }

        @Test
        void typesThatCannotBeCreatedAreReported() {
            DataFactoryResult<Inner> inner = DataFactory.of(Inner.class).valid();
            assertThat(inner.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE);
            assertThat(inner.reason()).contains("non-static inner class");

            DataFactoryResult<OddConstructor> odd = DataFactory.of(OddConstructor.class).valid();
            assertThat(odd.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE);
            assertThat(odd.reason()).contains("No instantiation strategy");
        }

        @Test
        void aBuilderSetterIsFoundByWrapperPrimitiveOrName() {
            HandBuilt built = DataFactory.of(HandBuilt.class).with("count", 7).with("level", 3).with("size", 9L).valid().get();
            assertThat(built.count).isEqualTo(7);
            assertThat(built.level).isEqualTo(3);
            assertThat(built.size).isEqualTo(9L);
            assertThat(built.note).isNull();
        }

        @Test
        void getOnAFailureCarriesTheErrorType() {
            DataFactoryResult<Inner> r = DataFactory.of(Inner.class).valid();
            assertThatThrownBy(r::get).isInstanceOfSatisfying(DataFactoryException.class,
                    e -> assertThat(e.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE));
        }
    }

    @Nested
    @DisplayName("a field type missing at runtime (classpath mismatch)")
    class MissingType {

        /** Loads HoldsMissingType in a class loader that can't find MissingAtRuntime. */
        private Class<?> holderWithoutItsElementType() throws Exception {
            String holder = HoldsMissingType.class.getName();
            String hidden = holder.replace("HoldsMissingType", "MissingAtRuntime");
            URL root = HoldsMissingType.class.getProtectionDomain().getCodeSource().getLocation();
            @SuppressWarnings("resource") // the loader lives as long as the class it loaded
            ClassLoader loader = new URLClassLoader(new URL[]{root}, getClass().getClassLoader()) {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    if (name.equals(hidden)) throw new ClassNotFoundException(name);
                    if (!name.equals(holder)) return super.loadClass(name, resolve);
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> c = findLoadedClass(name);
                        return c != null ? c : findClass(name);
                    }
                }
            };
            return loader.loadClass(holder);
        }

        @Test
        void everyCallReturnsAFailureInsteadOfThrowing() throws Exception {
            DataBuilder<?> b = DataFactory.of(holderWithoutItsElementType());
            assertThat(b.valid().errorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
            assertThat(b.invalidList().failed()).singleElement()
                    .satisfies(r -> assertThat(r.reason()).contains("Failed to build invalidList"));
            assertThat(b.validPerField()).containsOnlyKeys("_error");
            Map<String, ? extends DataFactoryResult<?>> fields = b.fields();
            assertThat(fields).containsOnlyKeys("_error");
            assertThat(fields.get("_error").reason()).contains("MissingAtRuntime");
        }
    }

    @Nested
    @DisplayName("field builders")
    class FieldBuilders {

        @Test
        void typedBuildersPassEveryCallThrough() {
            assertThat(DataFactory.doubles().with("@Positive").validList()).allSatisfy(v -> assertThat(v).isPositive());
            assertThat(DataFactory.doubles().positive().invalid()).isLessThanOrEqualTo(0d);
            assertThat(DataFactory.floats().positive().invalid()).isLessThanOrEqualTo(0f);
            assertThat(DataFactory.decimal().positive().invalid()).isLessThanOrEqualTo(BigDecimal.ZERO);
            assertThat(DataFactory.string().notBlank().invalid()).isNullOrEmpty();
            assertThat(DataFactory.date().future().invalidList()).allSatisfy(d -> assertThat(d).isBefore(LocalDate.now().plusDays(1)));
            assertThat(DataFactory.dateTime().with("@Future").valid()).isAfter(LocalDateTime.now());
            assertThat(DataFactory.dateTime().future().invalid()).isBefore(LocalDateTime.now());
        }

        @Test
        void uuidStrings() {
            FieldValue<String> uuid = DataFactory.field(String.class).with(org.hibernate.validator.constraints.UUID.class);
            assertThat(java.util.UUID.fromString(uuid.valid())).isNotNull();
            assertThat(uuid.invalidList()).contains("not-a-uuid");
        }

        @Test
        void digitsOnAString() {
            FieldValue<String> digits = DataFactory.field(String.class).with("@Digits(integer=3, fraction=2)");
            assertThat(digits.validList()).allSatisfy(v -> assertThat(v).matches("\\d{1,3}\\.\\d{2}"));
            assertThat(digits.invalidList()).contains("11111");
        }

        @Test
        void decimalMaxOnAString() {
            assertThat(DataFactory.field(String.class).with("@DecimalMax(\"10\")").invalidList()).contains("20");
        }

        @Test
        void instantsOutsideTheirTimeRule() {
            Instant now = Instant.now();
            assertThat(DataFactory.field(Instant.class).with("@Future").invalidList())
                    .anySatisfy(i -> assertThat(i).isBefore(now));
            assertThat(DataFactory.field(Instant.class).with("@Past").invalidList())
                    .anySatisfy(i -> assertThat(i).isAfter(now));
        }
    }

    @Nested
    @DisplayName("string generation")
    class Strings {

        @Test
        void aPatternKeepsItsShapeAtSizeBoundaries() {
            assertThat(DataFactory.field(String.class).with("@Pattern(regexp=\"[a-z]{3}\")").with("@Size(min=2, max=5)")
                    .validList()).allSatisfy(v -> assertThat(v).matches("[a-z]{3}"));
        }

        @Test
        void aUrlIsPaddedToItsMinimumSize() {
            assertThat(DataFactory.field(String.class).with(org.hibernate.validator.constraints.URL.class)
                    .with("@Size(min=80)").valid()).hasSizeGreaterThanOrEqualTo(80);
        }

        @Test
        void namesPickTheirValue() {
            assertThat(DataFactory.string("price").valid()).matches("[0-9.,]+");
            assertThat(DataFactory.string("paragraph").valid()).contains(" ");
            assertThat(DataFactory.string("mac_address").valid()).matches("[0-9.]+");
        }
    }

    @Test
    void anUnreadableVersionResourceIsUnknown() {
        InputStream broken = new InputStream() {
            @Override public int read() throws IOException { throw new IOException("disk error"); }
        };
        assertThat(DataFactory.readVersion(broken)).isEqualTo("unknown");
    }
}
