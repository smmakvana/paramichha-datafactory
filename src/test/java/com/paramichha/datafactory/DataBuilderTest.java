package com.paramichha.datafactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    class Inner {
        String label;
    }

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

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private Integer count = 0;
            private Number level;
            private long size;

            public Builder count(Integer count) {
                this.count = count;
                return this;
            } // field is int

            public Builder level(Number level) {
                this.level = level;
                return this;
            } // field is Integer

            public Builder size(Number size) {
                this.size = size.longValue();
                return this;
            } // field is long
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
            assertThat(DataFactory.of(Person.class)
                            .withNull("name")
                            .valid()
                            .get()
                            .name())
                    .isNull();
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
            FieldCases cases = DataFactory.of(Person.class)
                    .with("name", "Ann")
                    .fields()
                    .get("name")
                    .get();
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
            assertThat(DataFactory.of(WithOptional.class).valid().get().person())
                    .isPresent();
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

            DataFactoryResult<OddConstructor> odd =
                    DataFactory.of(OddConstructor.class).valid();
            assertThat(odd.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE);
            assertThat(odd.reason()).contains("No instantiation strategy");
        }

        @Test
        void aBuilderSetterIsFoundByWrapperPrimitiveOrName() {
            HandBuilt built = DataFactory.of(HandBuilt.class)
                    .with("count", 7)
                    .with("level", 3)
                    .with("size", 9L)
                    .valid()
                    .get();
            assertThat(built.count).isEqualTo(7);
            assertThat(built.level).isEqualTo(3);
            assertThat(built.size).isEqualTo(9L);
            assertThat(built.note).isNull();
        }

        @Test
        void getOnAFailureCarriesTheErrorType() {
            DataFactoryResult<Inner> r = DataFactory.of(Inner.class).valid();
            assertThatThrownBy(r::get)
                    .isInstanceOfSatisfying(
                            DataFactoryException.class,
                            e -> assertThat(e.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE));
        }
    }

    record Address(String city) {}

    record Customer(@NotNull Address address) {}

    record EmptyBag(@Size(max = 0) List<String> items) {}

    record Framed(@Valid Shape shape) {}

    record Named(@NotBlank String name, @Min(1) int age) {}

    record WithOptionalAddress(Optional<Address> address) {}

    record ObjectPair(@Size(min = 2, max = 2) List<Object> things) {}

    record ThreeFlags(@Size(min = 3, max = 3) Set<Boolean> flags) {}

    record TwoAddresses(@Size(min = 2, max = 2) Set<Address> addresses) {}

    record ObjectKeys(@Size(min = 1, max = 1) Map<Object, String> entries) {}

    record ShapeValues(@Size(min = 1, max = 1) Map<String, Shape> shapes) {}

    record ObjectValues(@Size(max = 1) Map<String, Object> entries) {}

    record ObjectKeysAtMostOne(@Size(max = 1) Map<Object, String> entries) {}

    enum NoConstants {}

    record LooseBag(List<Object> things) {}

    record TwoEntries(@Size(min = 2, max = 2) Map<String, Integer> entries) {}

    record ThreeFlagMap(@Size(min = 3, max = 3) Map<Boolean, String> entries) {}

    record NoChoices(@Size(min = 2, max = 2) List<NoConstants> choices) {}

    @Nested
    @DisplayName("nested objects, lists and seeds")
    class NestedObjectsListsAndSeeds {

        @Test
        @DisplayName("a @NotNull nested object is built, without @Valid")
        void notNullNestedObjectIsBuilt() {
            assertThat(DataFactory.of(Customer.class).valid().get().address().city())
                    .isNotBlank();
        }

        @Test
        @DisplayName("a nested object that cannot be built fails the whole object, naming the field")
        void unbuildableNestedObjectFails() {
            DataFactoryResult<Framed> result = DataFactory.of(Framed.class).valid();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE);
            assertThat(result.reason()).contains("field 'shape'");
        }

        @Test
        @DisplayName("the seed reaches an Optional's value")
        void seedReachesAnOptionalValue() {
            WithOptionalAddress first =
                    DataFactory.of(WithOptionalAddress.class).seed(5).valid().get();
            WithOptionalAddress second =
                    DataFactory.of(WithOptionalAddress.class).seed(5).valid().get();
            assertThat(first.address()).isPresent();
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("validList(0) has no results")
        void validListOfZeroIsEmpty() {
            assertThat(DataFactory.of(Named.class).validList(0).total()).isZero();
        }

        @Test
        @DisplayName("invalidList leaves an overridden field alone")
        void invalidListSkipsOverriddenFields() {
            List<Named> invalid = DataFactory.of(Named.class)
                    .with("name", "fixed")
                    .invalidList()
                    .getAll();
            assertThat(invalid)
                    .isNotEmpty()
                    .allSatisfy(n -> assertThat(n.name()).isEqualTo("fixed"));
        }
    }

    @Nested
    @DisplayName("@Size on collections and maps")
    class SizedCollectionsAndMaps {

        @Test
        @DisplayName("@Size(max = 0) gives an empty collection")
        void maxZeroGivesAnEmptyCollection() {
            assertThat(DataFactory.of(EmptyBag.class).valid().get().items()).isEmpty();
        }

        @Test
        @DisplayName("a @Size that needs elements of an undeclared type fails, naming the field")
        void undeclaredElementTypeFails() {
            DataFactoryResult<ObjectPair> list =
                    DataFactory.of(ObjectPair.class).valid();
            assertThat(list.errorType()).isEqualTo(ErrorType.UNSUPPORTED_CLASS_TYPE);
            assertThat(list.reason()).contains("field 'things': needs 2 element(s)");

            DataFactoryResult<ObjectKeys> map = DataFactory.of(ObjectKeys.class).valid();
            assertThat(map.errorType()).isEqualTo(ErrorType.UNSUPPORTED_CLASS_TYPE);
            assertThat(map.reason()).contains("field 'entries': needs 1 element(s)");
        }

        @Test
        @DisplayName("a @Size that needs more distinct values than exist fails, naming the field")
        void moreDistinctValuesThanExistFails() {
            DataFactoryResult<ThreeFlags> flags =
                    DataFactory.of(ThreeFlags.class).valid();
            assertThat(flags.errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
            assertThat(flags.reason()).contains("field 'flags': needs 3 distinct Boolean value(s)");

            assertThat(DataFactory.of(TwoAddresses.class).valid().errorType())
                    .isEqualTo(ErrorType.INVALID_CONFIGURATION);
            assertThat(DataFactory.of(NoChoices.class).valid().errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
        }

        @Test
        @DisplayName("an unsized collection of an undeclared type is empty, and a sized map gets that many entries")
        void unsizedStaysEmptyAndSizedMapsAreFilled() {
            assertThat(DataFactory.of(LooseBag.class).valid().get().things()).isEmpty();
            assertThat(DataFactory.of(TwoEntries.class).valid().get().entries()).hasSize(2);
            assertThat(DataFactory.of(ThreeFlagMap.class).valid().errorType())
                    .isEqualTo(ErrorType.INVALID_CONFIGURATION);
        }

        @Test
        @DisplayName("a map with untyped keys or values still gets a too-large invalid case")
        void untypedMapsStillGetAnOversizedCase() {
            assertThat(DataFactory.of(ObjectKeysAtMostOne.class).invalidList().getAll())
                    .anySatisfy(o -> assertThat(o.entries()).hasSize(2));
            assertThat(DataFactory.of(ObjectValues.class).invalidList().getAll())
                    .anySatisfy(o -> assertThat(o.entries()).hasSize(2));
        }

        @Test
        @DisplayName("a map whose value type cannot be built fails the object")
        void unbuildableMapValuesFail() {
            DataFactoryResult<ShapeValues> result =
                    DataFactory.of(ShapeValues.class).valid();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.reason()).contains("'shapes.value'");
        }
    }
}
