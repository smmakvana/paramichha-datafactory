package com.paramichha.datafactory;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The object builder: nulls, overrides, self references, optionals and collections. */
@DisplayName("DataBuilder — object building")
class DataBuilderTest {

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
}
