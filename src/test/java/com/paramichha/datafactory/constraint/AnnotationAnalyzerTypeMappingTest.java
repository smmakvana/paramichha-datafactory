package com.paramichha.datafactory.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** How AnnotationAnalyzer maps field types to their value kinds. */
@DisplayName("AnnotationAnalyzer type mappings")
class AnnotationAnalyzerTypeMappingTest {

    @Nested
    @DisplayName("AnnotationAnalyzer.resolveType all mappings")
    class AllMappings {

        private FieldConstraints make(String type) {
            return AnnotationResolver.analyzeFromStrings("f", type, List.of());
        }

        @Test
        void string() {
            assertThat(make("String").fieldType()).isEqualTo(String.class);
        }

        @Test
        void integer() {
            assertThat(make("Integer").fieldType()).isEqualTo(Integer.class);
        }

        @Test
        void intPrimitive() {
            assertThat(make("int").fieldType()).isEqualTo(Integer.class);
        }

        @Test
        void longType() {
            assertThat(make("Long").fieldType()).isEqualTo(Long.class);
        }

        @Test
        void longPrimitive() {
            assertThat(make("long").fieldType()).isEqualTo(Long.class);
        }

        @Test
        void doubleType() {
            assertThat(make("Double").fieldType()).isEqualTo(Double.class);
        }

        @Test
        void doublePrimitive() {
            assertThat(make("double").fieldType()).isEqualTo(Double.class);
        }

        @Test
        void floatType() {
            assertThat(make("Float").fieldType()).isEqualTo(Float.class);
        }

        @Test
        void floatPrimitive() {
            assertThat(make("float").fieldType()).isEqualTo(Float.class);
        }

        @Test
        void booleanType() {
            assertThat(make("Boolean").fieldType()).isEqualTo(Boolean.class);
        }

        @Test
        void booleanPrimitive() {
            assertThat(make("boolean").fieldType()).isEqualTo(Boolean.class);
        }

        @Test
        void charType() {
            assertThat(make("Character").fieldType()).isEqualTo(Character.class);
        }

        @Test
        void charPrimitive() {
            assertThat(make("char").fieldType()).isEqualTo(Character.class);
        }

        @Test
        void bigDecimal() {
            assertThat(make("BigDecimal").fieldType()).isEqualTo(java.math.BigDecimal.class);
        }

        @Test
        void bigInteger() {
            assertThat(make("BigInteger").fieldType()).isEqualTo(java.math.BigInteger.class);
        }

        @Test
        void instant() {
            assertThat(make("Instant").fieldType()).isEqualTo(java.time.Instant.class);
        }

        @Test
        void localDate() {
            assertThat(make("LocalDate").fieldType()).isEqualTo(java.time.LocalDate.class);
        }

        @Test
        void localDateTime() {
            assertThat(make("LocalDateTime").fieldType()).isEqualTo(java.time.LocalDateTime.class);
        }

        @Test
        void localTime() {
            assertThat(make("LocalTime").fieldType()).isEqualTo(java.time.LocalTime.class);
        }

        @Test
        void zonedDateTime() {
            assertThat(make("ZonedDateTime").fieldType()).isEqualTo(java.time.ZonedDateTime.class);
        }

        @Test
        void offsetDateTime() {
            assertThat(make("OffsetDateTime").fieldType()).isEqualTo(java.time.OffsetDateTime.class);
        }

        @Test
        void uuid() {
            assertThat(make("UUID").fieldType()).isEqualTo(java.util.UUID.class);
        }

        @Test
        void shortType() {
            assertThat(make("Short").fieldType()).isEqualTo(Short.class);
        }

        @Test
        void shortPrimitive() {
            assertThat(make("short").fieldType()).isEqualTo(Short.class);
        }

        @Test
        void byteType() {
            assertThat(make("Byte").fieldType()).isEqualTo(Byte.class);
        }

        @Test
        void bytePrimitive() {
            assertThat(make("byte").fieldType()).isEqualTo(Byte.class);
        }

        @Test
        void date() {
            assertThat(make("Date").fieldType()).isEqualTo(java.util.Date.class);
        }

        @Test
        void year() {
            assertThat(make("Year").fieldType()).isEqualTo(java.time.Year.class);
        }

        @Test
        void yearMonth() {
            assertThat(make("YearMonth").fieldType()).isEqualTo(java.time.YearMonth.class);
        }

        @Test
        void list() {
            assertThat(make("List").fieldType()).isEqualTo(java.util.List.class);
        }

        @Test
        void collection() {
            assertThat(make("Collection").fieldType()).isEqualTo(java.util.Collection.class);
        }

        @Test
        void unknownDefaultsToString() {
            assertThat(make("UnknownType").fieldType()).isEqualTo(String.class);
        }
    }
}
