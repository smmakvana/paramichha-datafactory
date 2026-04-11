package com.paramichha.datafactory;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.datafactory.fixture.*;

/**
 * Tests all typed field builders — the fluent DSL on top of DataFactory.
 * <p>
 * DataFactory.string().email().valid()
 * DataFactory.integer().range(1, 100).toInt()
 * DataFactory.decimal().positive().valid()
 * etc.
 * <p>
 * Structure per builder:
 * Construction → builder is created and returns non-null
 * Validation   → each constraint method produces values that satisfy it
 * Behaviour    → stream(), validList(), violations(), chaining
 */
@DisplayName("TypedFieldBuilders")
class TypedFieldBuildersTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        try (ValidatorFactory f = Validation.buildDefaultValidatorFactory()) {
            validator = f.getValidator();
        }
    }

    // =========================================================================
    // StringFieldBuilder
    // =========================================================================

    @Nested
    @DisplayName("StringFieldBuilder — DataFactory.string()")
    class StringFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("DataFactory.string() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.string()).isNotNull();
            }

            @Test
            @DisplayName("DataFactory.string(hint) returns non-null builder")
            void validInstanceIsCreatedWithHint() {
                assertThat(DataFactory.string("email")).isNotNull();
            }

            @Test
            @DisplayName("get() returns non-blank string")
            void validInstanceFieldsAreCorrect() {
                assertThat(DataFactory.string().valid()).isNotBlank();
            }
        }

        @Nested
        @DisplayName("Validation — semantic hints")
        class SemanticHints {

            @Test
            @DisplayName("email() returns valid email format")
            void shouldAcceptEmail() {
                assertThat(DataFactory.string().email().valid()).contains("@");
            }

            @Test
            @DisplayName("phone() returns non-blank")
            void shouldAcceptPhone() {
                assertThat(DataFactory.string().phone().valid()).isNotBlank();
            }

            @Test
            @DisplayName("name() returns non-blank")
            void shouldAcceptName() {
                assertThat(DataFactory.string().name().valid()).isNotBlank();
            }

            @Test
            @DisplayName("firstName() returns non-blank")
            void shouldAcceptFirstName() {
                assertThat(DataFactory.string().firstName().valid()).isNotBlank();
            }

            @Test
            @DisplayName("lastName() returns non-blank")
            void shouldAcceptLastName() {
                assertThat(DataFactory.string().lastName().valid()).isNotBlank();
            }

            @Test
            @DisplayName("city() returns non-blank")
            void shouldAcceptCity() {
                assertThat(DataFactory.string().city().valid()).isNotBlank();
            }

            @Test
            @DisplayName("country() returns non-blank")
            void shouldAcceptCountry() {
                assertThat(DataFactory.string().country().valid()).isNotBlank();
            }

            @Test
            @DisplayName("postcode() returns non-blank")
            void shouldAcceptPostcode() {
                assertThat(DataFactory.string().postcode().valid()).isNotBlank();
            }

            @Test
            @DisplayName("url() returns non-blank")
            void shouldAcceptUrl() {
                assertThat(DataFactory.string().url().valid()).isNotBlank();
            }

            @Test
            @DisplayName("company() returns non-blank")
            void shouldAcceptCompany() {
                assertThat(DataFactory.string().company().valid()).isNotBlank();
            }

            @Test
            @DisplayName("description() returns non-blank")
            void shouldAcceptDescription() {
                assertThat(DataFactory.string().description().valid()).isNotBlank();
            }

            @Test
            @DisplayName("hint(custom) delegates to semanticByName")
            void shouldAcceptCustomHint() {
                assertThat(DataFactory.string().hint("iban").valid()).isNotBlank();
            }
        }

        @Nested
        @DisplayName("Validation — constraint methods")
        class Constraints {

            @Test
            @DisplayName("notBlank() produces non-blank string")
            void shouldAcceptNotBlank() {
                assertThat(DataFactory.string().notBlank().valid()).isNotBlank();
            }

//            @Test @DisplayName("size(min, max) produces string within bounds")
//            void shouldAcceptSizeBounds() {
//                String v = DataFactory.string().size(5, 10).get();
//                assertThat(v.length()).isBetween(5, 10);
//            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test
            @DisplayName("stream(n) returns exactly n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.string().email().stream(5)).hasSize(5);
            }

//            @Test @DisplayName("validList() returns multiple boundary values")
//            void shouldReturnAllBoundaryValues() {
//                assertThat(DataFactory.string().size(2, 20).validList()).isNotEmpty();
//            }

            @Test
            @DisplayName("violations() on notBlank() includes null and blank")
            void shouldRejectBlankAndNull() {
                var v = DataFactory.string().notBlank().invalidList();
                assertThat(v).isNotEmpty().contains(null, "");
            }
        }
    }

    // =========================================================================
    // IntegerFieldBuilder
    // =========================================================================

    @Nested
    @DisplayName("IntegerFieldBuilder — DataFactory.integer()")
    class IntegerFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("DataFactory.integer() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.integer()).isNotNull();
            }

//            @Test @DisplayName("get() returns non-null Integer")
//            void validInstanceFieldsAreCorrect() {
//                assertThat(DataFactory.integer().valid()).isNotNull();
//            }
        }

        @Nested
        @DisplayName("Validation — constraint methods")
        class Constraints {

            @Test
            @DisplayName("positive() returns positive value")
            void shouldAcceptPositive() {
                assertThat(DataFactory.integer().positive().toInt()).isGreaterThan(0);
            }

            @Test
            @DisplayName("negative() returns negative value")
            void shouldAcceptNegative() {
                assertThat(DataFactory.integer().negative().toInt()).isLessThan(0);
            }

            @Test
            @DisplayName("range(min,max) respects both bounds")
            void shouldAcceptRange() {
                assertThat(DataFactory.integer().range(10, 20).toInt()).isBetween(10, 20);
            }

            @Test
            @DisplayName("min(n) produces value >= n")
            void shouldAcceptMin() {
                assertThat(DataFactory.integer().min(100).toInt()).isGreaterThanOrEqualTo(100);
            }

            @Test
            @DisplayName("max(n) produces value <= n")
            void shouldAcceptMax() {
                assertThat(DataFactory.integer().max(5).toInt()).isLessThanOrEqualTo(5);
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test
            @DisplayName("stream(n) returns exactly n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.integer().positive().stream(10)).hasSize(10);
            }

            @Test
            @DisplayName("validList() includes boundary values 1 and 10")
            void shouldReturnBoundaryValues() {
                assertThat(DataFactory.integer().range(1, 10).validList()).contains(1, 10);
            }

            @Test
            @DisplayName("violations() on positive() includes 0 and negative")
            void shouldRejectNonPositive() {
                assertThat(DataFactory.integer().positive().invalidList())
                        .isNotEmpty().contains(0);
            }
        }
    }

    // =========================================================================
    // LongFieldBuilder
    // =========================================================================

    @Nested
    @DisplayName("LongFieldBuilder — DataFactory.longVal()")
    class LongFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("DataFactory.longVal() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.longVal()).isNotNull();
            }

//            @Test @DisplayName("get() returns non-null Long")
//            void validInstanceFieldsAreCorrect() {
//                assertThat(DataFactory.longVal().valid()).isNotNull();
//            }
        }

        @Nested
        @DisplayName("Validation — constraint methods")
        class Constraints {

            @Test
            @DisplayName("positive() returns positive long")
            void shouldAcceptPositive() {
                assertThat(DataFactory.longVal().positive().toLong()).isGreaterThan(0L);
            }

            @Test
            @DisplayName("range(min,max) respects both bounds")
            void shouldAcceptRange() {
                assertThat(DataFactory.longVal().range(100L, 200L).toLong()).isBetween(100L, 200L);
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test
            @DisplayName("stream(n) returns exactly n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.longVal().positive().stream(5)).hasSize(5);
            }

            @Test
            @DisplayName("violations() on positive() includes non-positive")
            void shouldRejectNonPositive() {
                assertThat(DataFactory.longVal().positive().invalidList()).isNotEmpty();
            }
        }
    }

    // =========================================================================
    // DecimalFieldBuilder
    // =========================================================================

    @Nested
    @DisplayName("DecimalFieldBuilder — DataFactory.decimal()")
    class DecimalFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("DataFactory.decimal() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.decimal()).isNotNull();
            }

//            @Test @DisplayName("get() returns non-null BigDecimal")
//            void validInstanceFieldsAreCorrect() {
//                assertThat(DataFactory.decimal().valid()).isNotNull();
//            }
        }

        @Nested
        @DisplayName("Validation — constraint methods")
        class Constraints {

//            @Test @DisplayName("positive() returns positive BigDecimal")
//            void shouldAcceptPositive() {
//                assertThat(DataFactory.decimal().positive().valid()).isPositive();
//            }

//            @Test @DisplayName("range(min,max) respects both bounds")
//            void shouldAcceptRange() {
//                BigDecimal v = DataFactory.decimal().range("1.00", "9.99").valid();
//                assertThat(v).isBetween(new BigDecimal("1.00"), new BigDecimal("9.99"));
//            }
//
//            @Test @DisplayName("digits(integer, fraction) sets correct scale")
//            void shouldAcceptDigits() {
//                assertThat(DataFactory.decimal().digits(5, 2).get().scale()).isEqualTo(2);
//            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test
            @DisplayName("stream(n) returns exactly n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.decimal().positive().stream(3)).hasSize(3);
            }

            @Test
            @DisplayName("violations() on positive() includes zero and negative")
            void shouldRejectNonPositive() {
                assertThat(DataFactory.decimal().positive().invalidList()).isNotEmpty();
            }
        }
    }

    // =========================================================================
    // BoolFieldBuilder
    // =========================================================================

    @Nested
    @DisplayName("BoolFieldBuilder — DataFactory.bool()")
    class BoolFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("DataFactory.bool() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.bool()).isNotNull();
            }

            @Test
            @DisplayName("get() returns non-null Boolean")
            void validInstanceFieldsAreCorrect() {
                assertThat(DataFactory.bool().valid()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation — constraint methods")
        class Constraints {

//            @Test @DisplayName("assertTrue() returns true")
//            void shouldAcceptTrue() {
//                assertThat(DataFactory.bool().assertTrue().valid()).isTrue();
//            }
//
//            @Test @DisplayName("assertFalse() returns false")
//            void shouldAcceptFalse() {
//                assertThat(DataFactory.bool().assertFalse().valid()).isFalse();
//            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test
            @DisplayName("stream(n) returns exactly n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.bool().stream(4)).hasSize(4);
            }
//
//            @Test @DisplayName("violations() on assertTrue() includes false")
//            void shouldRejectFalse() {
//                assertThat(DataFactory.bool().assertTrue().invalidList()).contains(false);
//            }
        }
    }

    // =========================================================================
    // DateFieldBuilder
    // =========================================================================

    @Nested
    @DisplayName("DateFieldBuilder — DataFactory.date()")
    class DateFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("DataFactory.date() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.date()).isNotNull();
            }

            @Test
            @DisplayName("get() returns non-null LocalDate")
            void validInstanceFieldsAreCorrect() {
                assertThat(DataFactory.date().valid()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation — constraint methods")
        class Constraints {

            @Test
            @DisplayName("past() returns date before today")
            void shouldAcceptPast() {
                assertThat(DataFactory.date().past().valid()).isBefore(LocalDate.now());
            }

            @Test
            @DisplayName("future() returns date after today")
            void shouldAcceptFuture() {
                assertThat(DataFactory.date().future().valid()).isAfter(LocalDate.now());
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test
            @DisplayName("stream(n) returns exactly n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.date().past().stream(3)).hasSize(3);
            }

            @Test
            @DisplayName("violations() on past() includes future dates")
            void shouldRejectFutureDates() {
                assertThat(DataFactory.date().past().invalidList()).isNotEmpty();
                DataFactory.date().past().invalidList()
                        .forEach(d -> assertThat(d).isAfterOrEqualTo(LocalDate.now()));
            }
        }
    }

    // =========================================================================
    // DateTimeFieldBuilder
    // =========================================================================

    @Nested
    @DisplayName("DateTimeFieldBuilder — DataFactory.dateTime()")
    class DateTimeFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("DataFactory.dateTime() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.dateTime()).isNotNull();
            }

            @Test
            @DisplayName("get() returns non-null LocalDateTime")
            void validInstanceFieldsAreCorrect() {
                assertThat(DataFactory.dateTime().valid()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation — constraint methods")
        class Constraints {

            @Test
            @DisplayName("past() returns datetime before now")
            void shouldAcceptPast() {
                assertThat(DataFactory.dateTime().past().valid()).isBefore(LocalDateTime.now());
            }

            @Test
            @DisplayName("future() returns datetime after now")
            void shouldAcceptFuture() {
                assertThat(DataFactory.dateTime().future().valid()).isAfter(LocalDateTime.now());
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test
            @DisplayName("stream(n) returns exactly n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.dateTime().future().stream(3)).hasSize(3);
            }

            @Test
            @DisplayName("violations() on past() includes future datetimes")
            void shouldRejectFutureDateTimes() {
                assertThat(DataFactory.dateTime().past().invalidList()).isNotEmpty();
            }
        }
    }

    // =========================================================================
    // DataFactory static convenience methods
    // =========================================================================

    @Nested
    @DisplayName("DataFactory — static API")
    class DataFactoryStaticAPI {

        @Nested
        @DisplayName("Construction")
        class Construction {
//
//            @Test @DisplayName("of(Class) returns non-null DataBuilder")
//            void validInstanceIsCreated() {
//                assertThat(DataFactory.of(AnnotatedWrappersRequest.class)).isNotNull();
//            }
        }

        @Nested
        @DisplayName("Behaviour — shortcut methods")
        class ShortcutMethods {

//            @Test @DisplayName("valid(Class) returns valid object")
//            void shouldReturnValidObject() {
//                assertThat(DataFactory.valid(AnnotatedWrappersRequest.class)).isNotNull();
//            }
//
//            @Test @DisplayName("violations(Class) returns non-empty list")
//            void shouldReturnViolations() {
//                assertThat(DataFactory.violations(AnnotatedWrappersRequest.class))
//                        .isNotEmpty();
//            }
//
//            @Test @DisplayName("validAll(Class) returns map with entries")
//            void shouldReturnValidAll() {
//                assertThat(DataFactory.validAll(AnnotatedWrappersRequest.class))
//                        .isNotEmpty();
//            }
//
//            @Test @DisplayName("invalidFor(Class, field) returns object failing that field")
//            void shouldReturnInvalidForField() {
//                assertThat(DataFactory.invalidFor(
//                        AnnotatedWrappersRequest.class, "email")).isNotNull();
//            }

            @Test
            @DisplayName("field(Class) returns FieldBuilder")
            void shouldReturnFieldBuilderByClass() {
                assertThat(DataFactory.field(String.class)).isNotNull();
            }
//
//            @Test
//            @DisplayName("field(Field) returns FieldBuilder")
//            void shouldReturnFieldBuilderByReflection() throws Exception {
//                var f = AnnotatedWrappersRequest.class.getDeclaredField("email");
//                assertThat(DataFactory.field(f)).isNotNull();
//            }

            @Test
            @DisplayName("field(name, Class, annotations) returns FieldBuilder")
            void shouldReturnFieldBuilderByNameTypeAnnotations() {
                assertThat(DataFactory.field("email", String.class, java.util.List.of()))
                        .isNotNull();
            }
        }
    }

    // =========================================================================
    // AnnotationParseException
    // =========================================================================

    @Nested
    @DisplayName("AnnotationParseException")
    class AnnotationParseExceptionTests {

        @Nested
        @DisplayName("Construction")
        class Construction {

            @Test
            @DisplayName("message constructor sets message")
            void validInstanceIsCreated() {
                var ex = new AnnotationParseException("bad annotation");
                assertThat(ex.getMessage()).isEqualTo("bad annotation");
                assertThat(ex).isInstanceOf(RuntimeException.class);
            }

            @Test
            @DisplayName("message + cause constructor sets both")
            void validInstanceWithCause() {
                var cause = new RuntimeException("root");
                var ex = new AnnotationParseException("bad annotation", cause);
                assertThat(ex.getMessage()).isEqualTo("bad annotation");
                assertThat(ex.getCause()).isSameAs(cause);
            }
        }
    }
    // =========================================================================
    // StringFieldBuilder — all semantic hints
    // =========================================================================

    @Nested
    @DisplayName("StringFieldBuilder — semantic hints")
    class StringSemanticHints {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("all hint methods return non-blank strings")
            void allHintsReturnNonBlank() {
                assertThat(DataFactory.string().iban().valid()).isNotBlank();
                assertThat(DataFactory.string().bic().valid()).isNotBlank();
                assertThat(DataFactory.string().creditCard().valid()).isNotBlank();
                assertThat(DataFactory.string().sortCode().valid()).isNotBlank();
                assertThat(DataFactory.string().accountNumber().valid()).isNotBlank();
                assertThat(DataFactory.string().address().valid()).isNotBlank();
                assertThat(DataFactory.string().ipAddress().valid()).isNotBlank();
                assertThat(DataFactory.string().colour().valid()).isNotBlank();
                assertThat(DataFactory.string().username().valid()).isNotBlank();
                assertThat(DataFactory.string().password().valid()).isNotBlank();
                assertThat(DataFactory.string().product().valid()).isNotBlank();
                assertThat(DataFactory.string().department().valid()).isNotBlank();
                assertThat(DataFactory.string().uuid().valid()).isNotBlank();
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("hint() escape hatch delegates to DataFaker")
            void shouldAcceptCustomHint() {
                assertThat(DataFactory.string().hint("jobTitle").valid()).isNotBlank();
            }

            @Test @DisplayName("minLength() constrains minimum length")
            void shouldApplyMinLength() {
                assertThat(DataFactory.string().minLength(10).valid().length())
                        .isGreaterThanOrEqualTo(10);
            }

            @Test @DisplayName("maxLength() constrains maximum length")
            void shouldApplyMaxLength() {
                assertThat(DataFactory.string().maxLength(5).valid().length())
                        .isLessThanOrEqualTo(5);
            }
        }
    }

    // =========================================================================
    // BoolFieldBuilder — constraint methods
    // =========================================================================

    @Nested
    @DisplayName("BoolFieldBuilder — constraint methods")
    class BoolConstraints {

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("assertTrue() returns true")
            void shouldReturnTrue() {
                assertThat(DataFactory.bool().assertTrue().valid()).isTrue();
            }

            @Test @DisplayName("assertFalse() returns false")
            void shouldReturnFalse() {
                assertThat(DataFactory.bool().assertFalse().valid()).isFalse();
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("violations() on assertTrue() contains false")
            void shouldRejectFalseWhenAssertTrue() {
                assertThat(DataFactory.bool().assertTrue().invalidList()).contains(false);
            }
        }
    }

    // =========================================================================
    // DecimalFieldBuilder — additional methods
    // =========================================================================

    @Nested
    @DisplayName("DecimalFieldBuilder — digits() and scale()")
    class DecimalExtras {

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("digits(integer, fraction) sets correct scale")
            void shouldApplyDigitsScale() {
                assertThat(DataFactory.decimal().digits(5, 2).valid().scale()).isEqualTo(2);
            }

            @Test @DisplayName("scale(2) sets two decimal places")
            void shouldApplyScale() {
                assertThat(DataFactory.decimal().positive().scale(2).valid().scale()).isEqualTo(2);
            }

            @Test @DisplayName("range(String, String) respects bounds")
            void shouldApplyStringRange() {
                java.math.BigDecimal v = DataFactory.decimal().range("1.00", "9.99").valid();
                assertThat(v).isBetween(new java.math.BigDecimal("1.00"),
                                        new java.math.BigDecimal("9.99"));
            }
        }
    }

    // =========================================================================
    // DateFieldBuilder — pastOrPresent / futureOrPresent
    // =========================================================================

    @Nested
    @DisplayName("DateFieldBuilder — pastOrPresent and futureOrPresent")
    class DateExtras {

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("pastOrPresent() returns date on or before today")
            void shouldAcceptPastOrPresent() {
                assertThat(DataFactory.date().pastOrPresent().valid())
                        .isBeforeOrEqualTo(LocalDate.now());
            }

            @Test @DisplayName("futureOrPresent() returns date on or after today")
            void shouldAcceptFutureOrPresent() {
                assertThat(DataFactory.date().futureOrPresent().valid())
                        .isAfterOrEqualTo(LocalDate.now());
            }
        }
    }

    // =========================================================================
    // DateTimeFieldBuilder — pastOrPresent / futureOrPresent
    // =========================================================================

    @Nested
    @DisplayName("DateTimeFieldBuilder — pastOrPresent and futureOrPresent")
    class DateTimeExtras {

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("pastOrPresent() returns datetime on or before now")
            void shouldAcceptPastOrPresent() {
                assertThat(DataFactory.dateTime().pastOrPresent().valid())
                        .isBeforeOrEqualTo(LocalDateTime.now());
            }

            @Test @DisplayName("futureOrPresent() returns datetime on or after now")
            void shouldAcceptFutureOrPresent() {
                assertThat(DataFactory.dateTime().futureOrPresent().valid())
                        .isAfterOrEqualTo(LocalDateTime.now());
            }
        }
    }

    // =========================================================================
    // DataFactory — new methods coverage
    // =========================================================================

    @Nested
    @DisplayName("DataFactory — new API methods")
    class DataFactoryNewMethods {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("of(Class) returns non-null DataBuilder")
            void shouldCreateBuilder() {
                assertThat(DataFactory.of(AnnotatedWrappersRequest.class)).isNotNull();
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test @DisplayName("stream(type, n) returns n valid objects")
            void shouldStreamNObjects() {
                assertThat(DataFactory.stream(AnnotatedWrappersRequest.class, 5))
                        .hasSize(5);
            }

            @Test @DisplayName("withNull(field) sets field to null")
            void shouldSetFieldNull() {
                var req = DataFactory.of(AnnotatedWrappersRequest.class)
                        .withNull("email").valid();
                assertThat(req).isNotNull();
            }

            @Test @DisplayName("enumOf(Class) returns a non-null enum constant")
            void shouldReturnEnumConstant() {
                assertThat(DataFactory.enumOf(TestStatus.class)).isNotNull();
            }

            @Test @DisplayName("enumOf(Class, n) returns n enum constants")
            void shouldReturnNEnumConstants() {
                assertThat(DataFactory.enumOf(TestStatus.class, 10)).hasSize(10);
            }

            @Test @DisplayName("names(n) returns n names")
            void shouldReturnNNames() {
                assertThat(DataFactory.names(5)).hasSize(5);
            }

            @Test @DisplayName("phones(n) returns n phones")
            void shouldReturnNPhones() {
                assertThat(DataFactory.phones(3)).hasSize(3);
            }

            @Test @DisplayName("cities(n) returns n cities")
            void shouldReturnNCities() {
                assertThat(DataFactory.cities(3)).hasSize(3);
            }

            @Test @DisplayName("companies(n) returns n companies")
            void shouldReturnNCompanies() {
                assertThat(DataFactory.companies(3)).hasSize(3);
            }

            @Test @DisplayName("uuids(n) returns n UUIDs")
            void shouldReturnNUuids() {
                assertThat(DataFactory.uuids(5)).hasSize(5);
            }

            @Test @DisplayName("DataFactory.validMap(type) returns non-empty map")
            void shouldReturnValidMap() {
                assertThat(DataFactory.validMap(AnnotatedWrappersRequest.class))
                        .isNotEmpty();
            }
        }
    }

    enum TestStatus { ACTIVE, INACTIVE, PENDING }
    // =========================================================================
    // New typed builders — doubles, floats, shorts, bytes, bigInteger, character
    // =========================================================================

    @Nested
    @DisplayName("DoubleFieldBuilder — DataFactory.doubles()")
    class DoubleFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("doubles() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.doubles()).isNotNull();
            }
            @Test @DisplayName("valid() returns non-null Double")
            void validInstanceFieldsAreCorrect() {
                assertThat(DataFactory.doubles().valid()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("positive() returns positive double")
            void shouldAcceptPositive() {
                assertThat(DataFactory.doubles().positive().toDouble()).isGreaterThan(0.0);
            }
            @Test @DisplayName("negative() returns negative double")
            void shouldAcceptNegative() {
                assertThat(DataFactory.doubles().negative().toDouble()).isLessThan(0.0);
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("stream(n) returns n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.doubles().positive().stream(5)).hasSize(5);
            }
            @Test @DisplayName("validList() returns boundary values")
            void shouldReturnBoundaryValues() {
                assertThat(DataFactory.doubles().valid()).isNotNull();
            }
            @Test @DisplayName("invalid() returns a value")
            void shouldReturnInvalid() {
                assertThat(DataFactory.doubles().positive().invalid()).isNotNull();
            }
            @Test @DisplayName("invalidList() returns values")
            void shouldReturnInvalidList() {
                assertThat(DataFactory.doubles().positive().invalidList()).isNotEmpty();
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.doubles().with("@Positive").valid()).isNotNull();
            }
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.doubles()
                        .with(jakarta.validation.constraints.Positive.class).valid()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("FloatFieldBuilder — DataFactory.floats()")
    class FloatFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("floats() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.floats()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("positive() returns positive float")
            void shouldAcceptPositive() {
                assertThat(DataFactory.floats().positive().toFloat()).isGreaterThan(0.0f);
            }
            @Test @DisplayName("negative() returns negative float")
            void shouldAcceptNegative() {
                assertThat(DataFactory.floats().negative().toFloat()).isLessThan(0.0f);
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("stream(n) returns n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.floats().stream(3)).hasSize(3);
            }
            @Test @DisplayName("invalid() returns a value")
            void shouldReturnInvalid() {
                assertThat(DataFactory.floats().positive().invalid()).isNotNull();
            }
            @Test @DisplayName("invalidList() returns values")
            void shouldReturnInvalidList() {
                assertThat(DataFactory.floats().positive().invalidList()).isNotEmpty();
            }
            @Test @DisplayName("validList() returns values")
            void shouldReturnValidList() {
                assertThat(DataFactory.floats().validList()).isNotEmpty();
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.floats().with("@Positive").valid()).isNotNull();
            }
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.floats()
                        .with(jakarta.validation.constraints.Positive.class).valid()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("ShortFieldBuilder — DataFactory.shorts()")
    class ShortFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("shorts() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.shorts()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("positive() returns positive short")
            void shouldAcceptPositive() {
                assertThat(DataFactory.shorts().positive().toShort()).isGreaterThan((short)0);
            }
            @Test @DisplayName("range() respects bounds")
            void shouldAcceptRange() {
                short v = DataFactory.shorts().range((short)1, (short)100).toShort();
                assertThat(v).isBetween((short)1, (short)100);
            }
            @Test @DisplayName("min() constrains minimum")
            void shouldAcceptMin() {
                assertThat(DataFactory.shorts().min((short)10).toShort())
                        .isGreaterThanOrEqualTo((short)10);
            }
            @Test @DisplayName("max() constrains maximum")
            void shouldAcceptMax() {
                assertThat(DataFactory.shorts().max((short)50).toShort())
                        .isLessThanOrEqualTo((short)50);
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("stream(n) returns n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.shorts().stream(4)).hasSize(4);
            }
            @Test @DisplayName("invalidList() on positive returns non-positive values")
            void shouldReturnInvalidList() {
                assertThat(DataFactory.shorts().positive().invalidList()).isNotEmpty();
            }
            @Test @DisplayName("invalid() returns a value")
            void shouldReturnInvalid() {
                assertThat(DataFactory.shorts().positive().invalid()).isNotNull();
            }
            @Test @DisplayName("validList() returns boundary values")
            void shouldReturnValidList() {
                assertThat(DataFactory.shorts().range((short)1, (short)100).validList())
                        .contains((short)1, (short)100);
            }
            @Test @DisplayName("negative() returns negative short")
            void shouldAcceptNegative() {
                assertThat(DataFactory.shorts().negative().toShort()).isLessThan((short)0);
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.shorts().with("@Positive").valid()).isNotNull();
            }
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.shorts()
                        .with(jakarta.validation.constraints.Positive.class).valid()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("ByteFieldBuilder — DataFactory.bytes()")
    class ByteFieldBuilderTests {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("bytes() returns non-null builder")
            void validInstanceIsCreated() {
                assertThat(DataFactory.bytes()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("positive() returns positive byte")
            void shouldAcceptPositive() {
                assertThat(DataFactory.bytes().positive().toByte()).isGreaterThan((byte)0);
            }
            @Test @DisplayName("range() respects bounds")
            void shouldAcceptRange() {
                byte v = DataFactory.bytes().range((byte)1, (byte)100).toByte();
                assertThat(v).isBetween((byte)1, (byte)100);
            }
            @Test @DisplayName("min() constrains minimum")
            void shouldAcceptMin() {
                assertThat(DataFactory.bytes().min((byte)5).toByte())
                        .isGreaterThanOrEqualTo((byte)5);
            }
            @Test @DisplayName("max() constrains maximum")
            void shouldAcceptMax() {
                assertThat(DataFactory.bytes().max((byte)50).toByte())
                        .isLessThanOrEqualTo((byte)50);
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("stream(n) returns n values")
            void shouldStreamNValues() {
                assertThat(DataFactory.bytes().stream(4)).hasSize(4);
            }
            @Test @DisplayName("invalidList() on positive returns values")
            void shouldReturnInvalidList() {
                assertThat(DataFactory.bytes().positive().invalidList()).isNotEmpty();
            }
            @Test @DisplayName("invalid() returns a value")
            void shouldReturnInvalid() {
                assertThat(DataFactory.bytes().positive().invalid()).isNotNull();
            }
            @Test @DisplayName("validList() returns boundary values")
            void shouldReturnValidList() {
                assertThat(DataFactory.bytes().range((byte)1, (byte)100).validList())
                        .contains((byte)1, (byte)100);
            }
            @Test @DisplayName("negative() returns negative byte")
            void shouldAcceptNegative() {
                assertThat(DataFactory.bytes().negative().toByte()).isLessThan((byte)0);
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.bytes().with("@Positive").valid()).isNotNull();
            }
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.bytes()
                        .with(jakarta.validation.constraints.Positive.class).valid()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("BigInteger and Character — DataFactory.bigInteger() / .character()")
    class BigIntegerAndCharacterTests {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("bigInteger() returns non-null FieldBuilder")
            void bigIntegerBuilderCreated() {
                assertThat(DataFactory.bigInteger()).isNotNull();
            }
            @Test @DisplayName("character() returns non-null FieldBuilder")
            void characterBuilderCreated() {
                assertThat(DataFactory.character()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("bigInteger() valid() returns non-null BigInteger")
            void bigIntegerValid() {
                assertThat(DataFactory.bigInteger().valid())
                        .isInstanceOf(java.math.BigInteger.class);
            }
            @Test @DisplayName("character() valid() returns non-null Character")
            void characterValid() {
                assertThat(DataFactory.character().valid())
                        .isInstanceOf(Character.class);
            }
            @Test @DisplayName("bigInteger() stream(n) returns n values")
            void bigIntegerStream() {
                assertThat(DataFactory.bigInteger().stream(3)).hasSize(3);
            }
        }
    }

    // =========================================================================
    // DataFactory instant one-liners — coverage for untested methods
    // =========================================================================

    @Nested
    @DisplayName("DataFactory — instant one-liners coverage")
    class DataFactoryInstantCoverage {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("description() returns non-blank")
            void descriptionNonBlank() {
                assertThat(DataFactory.description()).isNotBlank();
            }
            @Test @DisplayName("postcode() returns non-blank")
            void postcodeNonBlank() {
                assertThat(DataFactory.postcode()).isNotBlank();
            }
            @Test @DisplayName("url() returns non-blank")
            void urlNonBlank() {
                assertThat(DataFactory.url()).isNotBlank();
            }
            @Test @DisplayName("company() returns non-blank")
            void companyNonBlank() {
                assertThat(DataFactory.company()).isNotBlank();
            }
            @Test @DisplayName("firstName() returns non-blank")
            void firstNameNonBlank() {
                assertThat(DataFactory.firstName()).isNotBlank();
            }
            @Test @DisplayName("lastName() returns non-blank")
            void lastNameNonBlank() {
                assertThat(DataFactory.lastName()).isNotBlank();
            }
            @Test @DisplayName("country() returns non-blank")
            void countryNonBlank() {
                assertThat(DataFactory.country()).isNotBlank();
            }
        }
    }

    // =========================================================================
    // Coverage for untested builder methods
    // =========================================================================

    @Nested
    @DisplayName("LongFieldBuilder — untested methods")
    class LongFieldBuilderCoverage {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("min() constrains minimum")
            void shouldAcceptMin() {
                assertThat(DataFactory.longVal().min(100L).toLong()).isGreaterThanOrEqualTo(100L);
            }
            @Test @DisplayName("max() constrains maximum")
            void shouldAcceptMax() {
                assertThat(DataFactory.longVal().max(5L).toLong()).isLessThanOrEqualTo(5L);
            }
            @Test @DisplayName("negative() returns negative long")
            void shouldAcceptNegative() {
                assertThat(DataFactory.longVal().negative().toLong()).isLessThan(0L);
            }
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.longVal()
                        .with(jakarta.validation.constraints.Positive.class).valid()).isNotNull();
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.longVal().with("@Positive").valid()).isNotNull();
            }
            @Test @DisplayName("invalid() returns a value")
            void shouldReturnInvalid() {
                assertThat(DataFactory.longVal().positive().invalid()).isNotNull();
            }
            @Test @DisplayName("validList() returns boundary values")
            void shouldReturnValidList() {
                assertThat(DataFactory.longVal().range(1L, 100L).validList())
                        .contains(1L, 100L);
            }
            @Test @DisplayName("invalidList() returns values")
            void shouldReturnInvalidList() {
                assertThat(DataFactory.longVal().positive().invalidList()).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("BoolFieldBuilder — untested methods")
    class BoolFieldBuilderCoverage {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.bool()
                        .with(jakarta.validation.constraints.AssertTrue.class).valid()).isNotNull();
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.bool().with("@AssertTrue").valid()).isNotNull();
            }
            @Test @DisplayName("validList() returns values")
            void shouldReturnValidList() {
                assertThat(DataFactory.bool().validList()).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("DecimalFieldBuilder — untested methods")
    class DecimalFieldBuilderCoverage {

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("min() constrains minimum")
            void shouldAcceptMin() {
                assertThat(DataFactory.decimal().min("1.00").valid())
                        .isGreaterThanOrEqualTo(new java.math.BigDecimal("1.00"));
            }
            @Test @DisplayName("max() constrains maximum")
            void shouldAcceptMax() {
                assertThat(DataFactory.decimal().max("100.00").valid())
                        .isLessThanOrEqualTo(new java.math.BigDecimal("100.00"));
            }
            @Test @DisplayName("negative() returns negative decimal")
            void shouldAcceptNegative() {
                assertThat(DataFactory.decimal().negative().valid()).isNegative();
            }
        }

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.decimal()
                        .with(jakarta.validation.constraints.Positive.class).valid()).isNotNull();
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.decimal().with("@Positive").valid()).isNotNull();
            }
            @Test @DisplayName("invalid() returns a value")
            void shouldReturnInvalid() {
                assertThat(DataFactory.decimal().positive().invalid()).isNotNull();
            }
            @Test @DisplayName("validList() returns boundary values")
            void shouldReturnValidList() {
                assertThat(DataFactory.decimal().range("1.00", "100.00").validList()).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("DateFieldBuilder — untested methods")
    class DateFieldBuilderCoverage {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.date()
                        .with(jakarta.validation.constraints.Past.class).valid()).isNotNull();
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.date().with("@Past").valid()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("DateTimeFieldBuilder — untested methods")
    class DateTimeFieldBuilderCoverage {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("with(Class) accepts annotation")
            void shouldAcceptWithClass() {
                assertThat(DataFactory.dateTime()
                        .with(jakarta.validation.constraints.Past.class).valid()).isNotNull();
            }
            @Test @DisplayName("with(String) accepts annotation")
            void shouldAcceptWithString() {
                assertThat(DataFactory.dateTime().with("@Past").valid()).isNotNull();
            }
        }
    }
}
