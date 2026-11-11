package com.paramichha.datafactory;

import com.paramichha.datafactory.utils.JavaNames;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Level 1 shortcuts, the static object shortcuts, the result types and JavaNames. */
class ShortcutsAndResultsTest {

    enum Status { NEW, ACTIVE, CLOSED }

    enum Empty { }

    record Person(@NotBlank String name, @NotNull @Email String email, @Min(18) @Max(99) int age) {}

    static class Holder {
        @NotBlank @jakarta.validation.constraints.Size(max = 5) String code;
    }

    @Nested
    @DisplayName("Level 1 shortcuts")
    class Level1 {
        @Test
        void singleValues() {
            assertThat(DataFactory.email()).matches("[\\w.+-]+@[\\w.-]+\\.\\w+");
            assertThat(List.of(DataFactory.name(), DataFactory.firstName(), DataFactory.lastName(), DataFactory.phone(),
                    DataFactory.city(), DataFactory.country(), DataFactory.postcode(), DataFactory.company(),
                    DataFactory.description())).allSatisfy(v -> assertThat(v).isNotBlank());
            assertThat(DataFactory.url()).startsWith("http");
            assertThat(UUID.fromString(DataFactory.uuid())).isNotNull();
        }

        @Test
        void lists() {
            assertThat(DataFactory.emails(3)).hasSize(3).allSatisfy(v -> assertThat(v).contains("@"));
            assertThat(DataFactory.names(2)).hasSize(2);
            assertThat(DataFactory.phones(2)).hasSize(2);
            assertThat(DataFactory.cities(2)).hasSize(2);
            assertThat(DataFactory.companies(2)).hasSize(2);
            List<String> ids = DataFactory.uuids(20);
            assertThat(new HashSet<>(ids)).hasSize(20);
        }

        @Test
        void enums() {
            assertThat(DataFactory.enumOf(Status.class)).isIn((Object[]) Status.values());
            assertThat(DataFactory.enumOf(Status.class, 5)).hasSize(5).allSatisfy(s -> assertThat(s).isIn((Object[]) Status.values()));
            assertThat(DataFactory.enumOf(Status.class, 7L)).isEqualTo(DataFactory.enumOf(Status.class, 7L));
            assertThat(DataFactory.enumOf(Status.class, 4, 7L)).isEqualTo(DataFactory.enumOf(Status.class, 4, 7L)).hasSize(4);
            assertThatThrownBy(() -> DataFactory.enumOf(Empty.class)).hasMessageContaining("Empty has no constants");
            assertThatThrownBy(() -> DataFactory.enumOf(Empty.class, 2)).hasMessageContaining("no constants");
            assertThatThrownBy(() -> DataFactory.enumOf(Empty.class, 1L)).hasMessageContaining("no constants");
            assertThatThrownBy(() -> DataFactory.enumOf(Empty.class, 2, 1L)).hasMessageContaining("no constants");
        }

        @Test
        void otherTypes() {
            assertThat(DataFactory.bigInteger().valid()).isInstanceOf(BigInteger.class);
            assertThat(DataFactory.character().valid()).isInstanceOf(Character.class);
        }
    }

    @Nested
    @DisplayName("field(...) entry points")
    class Fields {
        @Test
        void fromReflectedFieldReadsItsAnnotations() throws Exception {
            FieldValue<String> code = DataFactory.field(Holder.class.getDeclaredField("code"));
            assertThat(code.validList()).allSatisfy(v -> assertThat(v).isNotBlank().hasSizeLessThanOrEqualTo(5));
            assertThat(code.invalidList()).contains("");
        }

        @Test
        void fromNameTypeAndAnnotations() throws Exception {
            var annotations = List.of(Holder.class.getDeclaredField("code").getAnnotations());
            FieldValue<String> code = DataFactory.field("code", String.class, annotations);
            assertThat(code.valid()).hasSizeLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("static object shortcuts")
    class ObjectShortcuts {
        @Test
        void validAndInvalid() {
            Person p = DataFactory.valid(Person.class).get();
            assertThat(p.email()).contains("@");
            assertThat(p.age()).isBetween(18, 99);
            assertThat(DataFactory.invalid(Person.class).isSuccess()).isTrue();
            int badAge = DataFactory.invalidFor(Person.class, "age").get().age();
            assertThat(badAge < 18 || badAge > 99).as("age %s breaks @Min(18) @Max(99)", badAge).isTrue();
            assertThat(DataFactory.invalidFor(Person.class, "nope").isFailure()).isTrue();
            assertThat(DataFactory.validList(Person.class, 3).getAll()).hasSize(3);
            assertThat(DataFactory.invalidList(Person.class).getAll()).isNotEmpty();
            // fields with a single valid value (name, email) have nothing to vary and are left out
            assertThat(DataFactory.validPerField(Person.class)).containsOnlyKeys("age");
            assertThat(DataFactory.fields(Person.class)).containsKeys("name", "email", "age");
        }

        @Test
        void capabilities() {
            assertThat(DataFactory.supportedTypes()).contains(String.class, Integer.class);
            assertThat(DataFactory.supportedAnnotations()).contains("NotNull", "Size", "Email");
            assertThat(DataFactory.canHandle(String.class)).isTrue();
            assertThat(DataFactory.canHandle(Status.class)).isTrue();
            assertThat(DataFactory.canHandle(String[].class)).isTrue();
            assertThat(DataFactory.canHandle(List.class)).isTrue();
            assertThat(DataFactory.canHandle(java.util.Map.class)).isTrue();
            assertThat(DataFactory.canHandle(Thread.class)).isFalse();
            assertThat(DataFactory.canHandle(null)).isFalse();
            assertThat(DataFactory.VERSION).matches("\\d+\\.\\d+\\.\\d+.*");
        }
    }

    @Nested
    @DisplayName("DataFactoryResult")
    class Result {
        private final DataFactoryResult<String> ok = DataFactoryResult.success("x");
        private final DataFactoryResult<String> bad = DataFactoryResult.failure(ErrorType.NOT_INSTANTIABLE, "no ctor");

        @Test
        void success() {
            assertThat(ok.isSuccess()).isTrue();
            assertThat(ok.isFailure()).isFalse();
            assertThat(ok.get()).isEqualTo("x");
            assertThat(ok.getOrNull()).isEqualTo("x");
            assertThat(ok.getOrElse("d")).isEqualTo("x");
            assertThat(ok.toOptional()).contains("x");
            assertThat(ok.map(String::length).get()).isEqualTo(1);
            assertThat(ok.map(s -> null).errorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
            assertThat(ok.status()).isEqualTo(ResultStatus.SUCCESS);
        }

        @Test
        void failure() {
            assertThat(bad.isFailure()).isTrue();
            assertThatThrownBy(bad::get).isInstanceOf(DataFactoryException.class).hasMessageContaining("NOT_INSTANTIABLE")
                    .hasMessageContaining("no ctor");
            assertThat(bad.getOrNull()).isNull();
            assertThat(bad.getOrElse("d")).isEqualTo("d");
            assertThat(bad.toOptional()).isEmpty();
            assertThat(bad.map(String::length).reason()).isEqualTo("no ctor");
            assertThat(bad.errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE);
            assertThat(bad.status()).isEqualTo(ResultStatus.FAILED);
            assertThatThrownBy(() -> bad.getOrElse(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ok.map(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void equalityAndText() {
            assertThat(ok).isEqualTo(DataFactoryResult.success("x")).hasSameHashCodeAs(DataFactoryResult.success("x"))
                    .isNotEqualTo(bad).isNotEqualTo("x");
            assertThat(bad).isEqualTo(DataFactoryResult.failure(ErrorType.NOT_INSTANTIABLE, "no ctor"));
            assertThat(ok.toString()).contains("x");
            assertThat(bad.toString()).contains("no ctor");
        }
    }

    @Nested
    @DisplayName("DataFactoryResults")
    class Results {
        @Test
        void statusFollowsTheParts() {
            var ok = DataFactoryResult.success("a");
            var bad = DataFactoryResult.<String>failure(ErrorType.INTERNAL_ERROR, "boom");
            DataFactoryResults<String> partial = DataFactoryResults.of(List.of(ok, bad));
            assertThat(partial.isPartial()).isTrue();
            assertThat(partial.succeeded()).containsExactly(ok);
            assertThat(partial.failed()).containsExactly(bad);
            assertThatThrownBy(partial::getAll).isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("boom").hasMessageContaining("1 of 2 results failed");
            assertThat(DataFactoryResults.of(List.of(ok)).getAll()).containsExactly("a");
            assertThat(partial.successCount()).isEqualTo(1);
            assertThat(partial.failureCount()).isEqualTo(1);
            assertThat(partial.total()).isEqualTo(2);
            assertThat(partial.results()).hasSize(2);
            assertThat(partial.toString()).contains("PARTIAL").contains("total=2");
            assertThat(DataFactoryResults.of(List.of(ok)).isSuccess()).isTrue();
            assertThat(DataFactoryResults.of(List.of(bad)).isFailure()).isTrue();
            assertThat(DataFactoryResults.<String>of(List.of()).status()).isEqualTo(ResultStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("JavaNames")
    class Names {
        @Test
        void accessors() {
            assertThat(JavaNames.getterName("active", boolean.class)).isEqualTo("isActive");
            assertThat(JavaNames.getterName("active", Boolean.class)).isEqualTo("isActive");
            assertThat(JavaNames.getterName("name", String.class)).isEqualTo("getName");
            assertThat(JavaNames.setterName("name")).isEqualTo("setName");
            assertThat(JavaNames.witherName("name")).isEqualTo("withName");
            assertThat(JavaNames.adderName("item")).isEqualTo("addItem");
            assertThat(JavaNames.removerName("item")).isEqualTo("removeItem");
        }

        @Test
        void caseStyles() {
            assertThat(JavaNames.className("order line-item")).isEqualTo("OrderLineItem");
            assertThat(JavaNames.variableName("Order Line")).isEqualTo("orderLine");
            assertThat(JavaNames.variableName(String.class)).isEqualTo("string");
            assertThat(JavaNames.fieldName("first name")).isEqualTo("firstName");
            assertThat(JavaNames.parameterName("first name")).isEqualTo("firstName");
            assertThat(JavaNames.parameterName(Integer.class)).isEqualTo("integer");
            assertThat(JavaNames.methodName("get value")).isEqualTo("getValue");
            assertThat(JavaNames.constantName("maxRetryCount")).isEqualTo("MAX_RETRY_COUNT");
            assertThat(JavaNames.packageName("Com Example-App!")).isEqualTo("com.example.app");
            assertThat(JavaNames.className(" ")).isEqualTo(" ");
            assertThat(JavaNames.variableName((String) null)).isNull();
            assertThat(JavaNames.constantName(null)).isNull();
            assertThat(JavaNames.packageName("")).isEmpty();
        }

        @Test
        void identifiers() {
            assertThat(JavaNames.isValidIdentifier("orderId")).isTrue();
            assertThat(JavaNames.isValidIdentifier("1st")).isFalse();
            assertThat(JavaNames.isValidIdentifier("class")).isFalse();
            assertThat(JavaNames.isValidIdentifier(" ")).isFalse();
            assertThat(JavaNames.isKeyword("record")).isTrue();
            assertThat(JavaNames.isKeyword("order")).isFalse();
            assertThat(JavaNames.safeIdentifier("1st place")).isEqualTo("_1stplace");
            assertThat(JavaNames.safeIdentifier("class")).isEqualTo("classValue");
            assertThat(JavaNames.safeIdentifier("!!!")).isEqualTo("_");
            assertThat(JavaNames.safeIdentifier(null)).isEqualTo("_");
        }
    }
}
