package com.paramichha.testkit.examples;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SampleService")
class SampleServiceTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // DataFactory generated — valid instance
    SampleService sampleService =
            new SampleService("John Smith", 42, new BigDecimal("99.99"));

    // =========================================================================
    // SampleService object creation
    // =========================================================================

    @Nested
    @DisplayName("SampleService object creation")
    class SampleServiceCreationTest {

        @Test
        @DisplayName("sampleService can be created")
        void testCanBeCreated() {
            assertThat(sampleService).isNotNull();
        }

        @Test
        @DisplayName("sampleService holds expected values")
        void testHoldsExpectedValues() {
            assertThat(sampleService.getName()).isEqualTo("John Smith");
            assertThat(sampleService.getQuantity()).isEqualTo(42);
            assertThat(sampleService.getPrice()).isEqualTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("sampleService is valid")
        void testIsValid() {
            assertThat(validator.validate(sampleService)).isEmpty();
        }
    }

    // =========================================================================
    // reset in SampleService — COMMAND
    // =========================================================================

    @Nested
    @DisplayName("reset in SampleService")
    class SampleServiceMethodResetTest {

        @Test
        @DisplayName("executes without exception")
        void testExecutesWithoutException() {
            assertThatCode(() -> sampleService.reset()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("object remains valid after execution")
        void testObjectRemainsValidAfterExecution() {
            sampleService.reset();
            assertThat(validator.validate(sampleService)).isEmpty();
        }
    }

    // =========================================================================
    // getQuantity in SampleService — QUERY primitive annotated
    // =========================================================================

    @Nested
    @DisplayName("getQuantity in SampleService")
    class SampleServiceMethodGetQuantityTest {

        @Test
        @DisplayName("getQuantity returns a result")
        void testReturnsResult() {
            int result = sampleService.getQuantity();
            assertThat(result).isGreaterThanOrEqualTo(1);
            assertThat(result).isLessThanOrEqualTo(100);
        }

        @Test
        @DisplayName("getQuantity returns consistent result")
        void testReturnsConsistentResult() {
            assertThat(sampleService.getQuantity()).isEqualTo(sampleService.getQuantity());
        }
    }

    // =========================================================================
    // getRawCount in SampleService — QUERY primitive no annotation
    // =========================================================================

    @Nested
    @DisplayName("getRawCount in SampleService")
    class SampleServiceMethodGetRawCountTest {

        @Test
        @DisplayName("getRawCount returns a result")
        void testReturnsResult() {
            assertThatCode(() -> sampleService.getRawCount()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getRawCount returns consistent result")
        void testReturnsConsistentResult() {
            assertThat(sampleService.getRawCount()).isEqualTo(sampleService.getRawCount());
        }
    }

    // =========================================================================
    // isActive in SampleService — QUERY boolean no annotation
    // =========================================================================

    @Nested
    @DisplayName("isActive in SampleService")
    class SampleServiceMethodIsActiveTest {

        @Test
        @DisplayName("isActive returns a result")
        void testReturnsResult() {
            assertThatCode(() -> sampleService.isActive()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("isActive returns consistent result")
        void testReturnsConsistentResult() {
            assertThat(sampleService.isActive()).isEqualTo(sampleService.isActive());
        }
    }

    // =========================================================================
    // getName in SampleService — QUERY String annotated
    // =========================================================================

    @Nested
    @DisplayName("getName in SampleService")
    class SampleServiceMethodGetNameTest {

        @Test
        @DisplayName("getName returns a result")
        void testReturnsResult() {
            String result = sampleService.getName();
            assertThat(result).isNotBlank();
        }

        @Test
        @DisplayName("getName returns consistent result")
        void testReturnsConsistentResult() {
            assertThat(sampleService.getName()).isEqualTo(sampleService.getName());
        }
    }

    // =========================================================================
    // getDisplayName in SampleService — QUERY String no annotation
    // =========================================================================

    @Nested
    @DisplayName("getDisplayName in SampleService")
    class SampleServiceMethodGetDisplayNameTest {

        @Test
        @DisplayName("getDisplayName returns a result")
        void testReturnsResult() {
            String result = sampleService.getDisplayName();
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getDisplayName returns consistent result")
        void testReturnsConsistentResult() {
            assertThat(sampleService.getDisplayName()).isEqualTo(sampleService.getDisplayName());
        }
    }

    // =========================================================================
    // getPrice in SampleService — QUERY Object annotated fields
    // =========================================================================

    @Nested
    @DisplayName("getPrice in SampleService")
    class SampleServiceMethodGetPriceTest {

        @Test
        @DisplayName("getPrice returns a result")
        void testReturnsResult() {
            BigDecimal result = sampleService.getPrice();
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getPrice returns consistent result")
        void testReturnsConsistentResult() {
            assertThat(sampleService.getPrice()).isEqualTo(sampleService.getPrice());
        }
    }

    // =========================================================================
    // getMetadata in SampleService — QUERY plain Object no annotation
    // =========================================================================

    @Nested
    @DisplayName("getMetadata in SampleService")
    class SampleServiceMethodGetMetadataTest {

        @Test
        @DisplayName("getMetadata returns a result")
        void testReturnsResult() {
            assertThatCode(() -> sampleService.getMetadata()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getMetadata returns consistent result")
        void testReturnsConsistentResult() {
            assertThat(sampleService.getMetadata()).isEqualTo(sampleService.getMetadata());
        }
    }

    // =========================================================================
    // setName in SampleService — MUTATOR constrained String param
    // =========================================================================

    @Nested
    @DisplayName("setName in SampleService")
    class SampleServiceMethodSetNameTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validSetNameInputs")
        @DisplayName("setName accepts valid input")
        void testAcceptsValidInput(String description, String name) {
            assertThatCode(() -> sampleService.setName(name)).doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidSetNameInputs")
        @DisplayName("setName rejects invalid input")
        void testRejectsInvalidInput(String description, String name) {
            assertThatThrownBy(() -> sampleService.setName(name))
                    .isInstanceOf(Exception.class);
        }

        static Stream<Arguments> validSetNameInputs() {
            return Stream.of(
                    Arguments.of("valid name", "John Smith")   // DataFactory generated
            );
        }

        static Stream<Arguments> invalidSetNameInputs() {
            return Stream.of(
                    Arguments.of("null name", (String) null),
                    Arguments.of("blank name", ""),
                    Arguments.of("blank name", "   ")
            );
        }
    }

    // =========================================================================
    // setQuantity in SampleService — MUTATOR constrained primitive param
    // =========================================================================

    @Nested
    @DisplayName("setQuantity in SampleService")
    class SampleServiceMethodSetQuantityTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validSetQuantityInputs")
        @DisplayName("setQuantity accepts valid input")
        void testAcceptsValidInput(String description, int quantity) {
            assertThatCode(() -> sampleService.setQuantity(quantity)).doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidSetQuantityInputs")
        @DisplayName("setQuantity rejects invalid input")
        void testRejectsInvalidInput(String description, int quantity) {
            assertThatThrownBy(() -> sampleService.setQuantity(quantity))
                    .isInstanceOf(Exception.class);
        }

        static Stream<Arguments> validSetQuantityInputs() {
            return Stream.of(
                    Arguments.of("valid quantity", 42)    // DataFactory generated — @Min(1) @Max(100)
            );
        }

        static Stream<Arguments> invalidSetQuantityInputs() {
            return Stream.of(
                    Arguments.of("below min", 0),
                    Arguments.of("below min", -1),
                    Arguments.of("above max", 101)
            );
        }
    }

    // =========================================================================
    // setMetadata in SampleService — MUTATOR unannotated param
    // =========================================================================

    @Nested
    @DisplayName("setMetadata in SampleService")
    class SampleServiceMethodSetMetadataTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validSetMetadataInputs")
        @DisplayName("setMetadata accepts valid input")
        void testAcceptsValidInput(String description, String metadata) {
            assertThatCode(() -> sampleService.setMetadata(metadata)).doesNotThrowAnyException();
        }

        static Stream<Arguments> validSetMetadataInputs() {
            return Stream.of(
                    Arguments.of("valid metadata", "some-value")  // DataFactory generated
            );
        }
        // No reject test — param has no constraints
    }

    // =========================================================================
    // applyDiscount in SampleService — TRANSFORMER annotated param + annotated Object return
    // =========================================================================

    @Nested
    @DisplayName("applyDiscount in SampleService")
    class SampleServiceMethodApplyDiscountTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validApplyDiscountInputs")
        @DisplayName("applyDiscount accepts valid input")
        void testAcceptsValidInput(String description, BigDecimal rate) {
            BigDecimal result = sampleService.applyDiscount(rate);
            assertThat(result).isNotNull();
            assertThat(result).isGreaterThan(BigDecimal.ZERO);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidApplyDiscountInputs")
        @DisplayName("applyDiscount rejects invalid input")
        void testRejectsInvalidInput(String description, BigDecimal rate) {
            assertThatThrownBy(() -> sampleService.applyDiscount(rate))
                    .isInstanceOf(Exception.class);
        }

        static Stream<Arguments> validApplyDiscountInputs() {
            return Stream.of(
                    Arguments.of("valid rate", new BigDecimal("0.10"))  // DataFactory generated
            );
        }

        static Stream<Arguments> invalidApplyDiscountInputs() {
            return Stream.of(
                    Arguments.of("null rate", (BigDecimal) null),
                    Arguments.of("negative rate", new BigDecimal("-0.10")),  // DataFactory invalidList
                    Arguments.of("above max", new BigDecimal("1.10"))    // DataFactory invalidList
            );
        }
    }

    // =========================================================================
    // formatName in SampleService — TRANSFORMER annotated param + plain String return
    // =========================================================================

    @Nested
    @DisplayName("formatName in SampleService")
    class SampleServiceMethodFormatNameTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validFormatNameInputs")
        @DisplayName("formatName accepts valid input")
        void testAcceptsValidInput(String description, String prefix) {
            String result = sampleService.formatName(prefix);
            assertThat(result).isNotNull();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidFormatNameInputs")
        @DisplayName("formatName rejects invalid input")
        void testRejectsInvalidInput(String description, String prefix) {
            assertThatThrownBy(() -> sampleService.formatName(prefix))
                    .isInstanceOf(Exception.class);
        }

        static Stream<Arguments> validFormatNameInputs() {
            return Stream.of(
                    Arguments.of("valid prefix", "Mr")   // DataFactory generated
            );
        }

        static Stream<Arguments> invalidFormatNameInputs() {
            return Stream.of(
                    Arguments.of("null prefix", (String) null),
                    Arguments.of("blank prefix", ""),
                    Arguments.of("blank prefix", "   ")
            );
        }
    }

    // =========================================================================
    // describe in SampleService — TRANSFORMER unannotated param + annotated String return
    // =========================================================================

    @Nested
    @DisplayName("describe in SampleService")
    class SampleServiceMethodDescribeTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validDescribeInputs")
        @DisplayName("describe accepts valid input")
        void testAcceptsValidInput(String description, String separator) {
            String result = sampleService.describe(separator);
            assertThat(result).isNotBlank();  // return has @NotBlank
        }

        static Stream<Arguments> validDescribeInputs() {
            return Stream.of(
                    Arguments.of("valid separator", " - ")  // DataFactory generated
            );
        }
        // No reject test — param has no constraints
    }

    // =========================================================================
    // encode in SampleService — TRANSFORMER unannotated param + plain String return
    // =========================================================================

    @Nested
    @DisplayName("encode in SampleService")
    class SampleServiceMethodEncodeTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validEncodeInputs")
        @DisplayName("encode accepts valid input")
        void testAcceptsValidInput(String description, String format) {
            String result = sampleService.encode(format);
            assertThat(result).isNotNull();
        }

        static Stream<Arguments> validEncodeInputs() {
            return Stream.of(
                    Arguments.of("valid format", "base64")  // DataFactory generated
            );
        }
        // No reject test — param has no constraints
    }

    // =========================================================================
    // multiply in SampleService — TRANSFORMER unannotated param + primitive return
    // =========================================================================

    @Nested
    @DisplayName("multiply in SampleService")
    class SampleServiceMethodMultiplyTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validMultiplyInputs")
        @DisplayName("multiply accepts valid input")
        void testAcceptsValidInput(String description, int factor) {
            assertThatCode(() -> sampleService.multiply(factor)).doesNotThrowAnyException();
        }

        static Stream<Arguments> validMultiplyInputs() {
            return Stream.of(
                    Arguments.of("valid factor", 2)   // DataFactory generated
            );
        }
        // No reject test — param has no constraints
    }

    // =========================================================================
    // merge in SampleService — OPERATION all constrained params + annotated Object return
    // =========================================================================

    @Nested
    @DisplayName("merge in SampleService")
    class SampleServiceMethodMergeTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validMergeInputs")
        @DisplayName("merge accepts valid input")
        void testAcceptsValidInput(String description, SampleService other, String newName) {
            SampleService result = sampleService.merge(other, newName);
            assertThat(validator.validate(result)).isEmpty();  // return has @Valid
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidMergeInputs")
        @DisplayName("merge rejects invalid input")
        void testRejectsInvalidInput(String description, SampleService other, String newName) {
            assertThatThrownBy(() -> sampleService.merge(other, newName))
                    .isInstanceOf(Exception.class);
        }

        static Stream<Arguments> validMergeInputs() {
            return Stream.of(
                    Arguments.of("valid merge",
                            new SampleService("Jane Doe", 10, new BigDecimal("50.00")),  // DataFactory
                            "Combined")  // DataFactory
            );
        }

        static Stream<Arguments> invalidMergeInputs() {
            return Stream.of(
                    // null other — @NotNull constrained
                    Arguments.of("null other",
                            (SampleService) null,
                            "Combined"),
                    // null newName — @NotBlank constrained
                    Arguments.of("null newName",
                            new SampleService("Jane Doe", 10, new BigDecimal("50.00")),
                            (String) null),
                    // blank newName — @NotBlank constrained
                    Arguments.of("blank newName",
                            new SampleService("Jane Doe", 10, new BigDecimal("50.00")),
                            "")
            );
        }
    }

    // =========================================================================
    // transferTo in SampleService — OPERATION some constrained + void return
    // =========================================================================

    @Nested
    @DisplayName("transferTo in SampleService")
    class SampleServiceMethodTransferToTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validTransferToInputs")
        @DisplayName("transferTo accepts valid input")
        void testAcceptsValidInput(String description, SampleService target, int amount) {
            assertThatCode(() -> sampleService.transferTo(target, amount)).doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidTransferToInputs")
        @DisplayName("transferTo rejects invalid input")
        void testRejectsInvalidInput(String description, SampleService target, int amount) {
            assertThatThrownBy(() -> sampleService.transferTo(target, amount))
                    .isInstanceOf(Exception.class);
        }

        static Stream<Arguments> validTransferToInputs() {
            return Stream.of(
                    Arguments.of("valid transfer",
                            new SampleService("Jane", 5, new BigDecimal("10.00")),  // DataFactory
                            10)   // DataFactory
            );
        }

        static Stream<Arguments> invalidTransferToInputs() {
            return Stream.of(
                    // Only target is constrained (@NotNull) — amount has no annotation
                    Arguments.of("null target", (SampleService) null, 10)
            );
        }
    }

    // =========================================================================
    // combine in SampleService — OPERATION no constrained params + primitive return
    // =========================================================================

    @Nested
    @DisplayName("combine in SampleService")
    class SampleServiceMethodCombineTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validCombineInputs")
        @DisplayName("combine accepts valid input")
        void testAcceptsValidInput(String description, int a, int b) {
            assertThatCode(() -> sampleService.combine(a, b)).doesNotThrowAnyException();
        }

        static Stream<Arguments> validCombineInputs() {
            return Stream.of(
                    Arguments.of("valid combine", 3, 4)  // DataFactory generated
            );
        }
        // No reject test — no params are constrained
    }

    // =========================================================================
    // swap in SampleService — OPERATION no constrained params + void return
    // =========================================================================

    @Nested
    @DisplayName("swap in SampleService")
    class SampleServiceMethodSwapTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("validSwapInputs")
        @DisplayName("swap accepts valid input")
        void testAcceptsValidInput(String description, String a, String b) {
            assertThatCode(() -> sampleService.swap(a, b)).doesNotThrowAnyException();
        }

        static Stream<Arguments> validSwapInputs() {
            return Stream.of(
                    Arguments.of("valid swap", "first", "second")  // DataFactory generated
            );
        }
        // No reject test — no params are constrained
    }
}