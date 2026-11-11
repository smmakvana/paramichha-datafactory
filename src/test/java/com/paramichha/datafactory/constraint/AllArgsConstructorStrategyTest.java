package com.paramichha.datafactory.constraint;

import java.util.Map;
import com.paramichha.datafactory.instantiation.ConstructorObjectCreation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/** All-arguments constructor strategy: a constructor with another argument count is not used. */
@DisplayName("All-arguments constructor strategy")
class AllArgsConstructorStrategyTest {

    @Nested
    @DisplayName("AllArgsConstructorStrategy — count mismatch → canHandle false")
    class ConstructorWithAnotherArgumentCount {

        @Test
        void constructorWithDifferentArgumentCountIsNotUsed() {
            // TwoArgsClass only has a 2-arg constructor, we pass 3 fields
            var fields = List.of(
                    new FieldDescriptor("a", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("b", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("c", String.class, String.class, Collections.emptyList())
            );
            assertThat(ConstructorObjectCreation.INSTANCE.canCreate(TwoArgsClass.class, fields))
                    .isFalse();
        }

        static class TwoArgsClass {
            TwoArgsClass(String a, String b) {
            }
        }
    }

    @Nested
    @DisplayName("AllArgsConstructorStrategy — canHandle false")
    class NoUsableConstructor {

        @Test
        void interfaceIsNotCreatable() {
            assertThat(ConstructorObjectCreation.INSTANCE
                    .canCreate(NoConstructorInterface.class, List.of()))
                    .isFalse();
        }

        @Test
        void zeroFieldsWithoutMatchingConstructorIsNotCreatable() {
            // Class with only a 2-arg constructor, but we pass 0 fields — count mismatch, null returned
            assertThat(ConstructorObjectCreation.INSTANCE
                    .canCreate(NoConstructorInterface.class, List.of()))
                    .isFalse();
        }

        interface NoConstructorInterface {
        }

        abstract static class AbstractNoConstructor {
            abstract void doSomething();
        }
    }

    @Nested
    @DisplayName("AllArgsConstructorStrategy — count-match and canHandle false")
    class CountMatchAndNoConstructor {

        @Test
        void countMatchIsUsedWhenTypesDiffer() throws Exception {
            // getDeclaredConstructor(String.class, String.class) throws NoSuchMethodException
            // falls back to count-match: constructor with 2 params
            var fields = List.of(
                    new FieldDescriptor("a", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("b", String.class, String.class, Collections.emptyList())
            );
            assertThat(ConstructorObjectCreation.INSTANCE.canCreate(ObjectArgs.class, fields)).isTrue();
            var values = Map.of("a", (Object) "x", "b", (Object) "y");
            var result = ConstructorObjectCreation.INSTANCE.create(ObjectArgs.class, fields, values);
            assertThat(result.a).isEqualTo("x");
        }

        @Test
        void noConstructorIsNotCreatable() {
            assertThat(ConstructorObjectCreation.INSTANCE
                    .canCreate(OnlyAnInterface.class, List.of()))
                    .isFalse();
        }

        interface OnlyAnInterface {
        }

        static class ObjectArgs {
            final Object a;
            final Object b;

            ObjectArgs(Object a, Object b) {
                this.a = a;
                this.b = b;
            }
        }
    }
}
