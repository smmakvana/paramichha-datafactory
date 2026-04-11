package com.paramichha.datafactory.examples;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

/**
 * Validation Testing — the most powerful use case for DataFactory.
 *
 * <p>One annotated class. One call to invalidList(). Every constraint tested.
 *
 * <p>In a real test this becomes:
 * <pre>
 * {@literal @}ParameterizedTest
 * {@literal @}MethodSource("invalidRequests")
 * void each_constraint_is_enforced(RegistrationRequest req) {
 *     assertThat(validator.validate(req)).isNotEmpty();
 * }
 *
 * static List&lt;RegistrationRequest&gt; invalidRequests() {
 *     return DataFactory.of(RegistrationRequest.class).invalidList();
 * }
 * </pre>
 */
public class ValidationTesting {

    @Value @Builder
    static class RegistrationRequest {
        @NotBlank @Email @Size(max = 100) String email;
        @NotBlank @Size(min = 8, max = 50) String password;
        @NotBlank @Size(min = 2, max = 50) String fullName;
        @NotNull @Min(18) @Max(120)        Integer age;
    }

    public static void main(String[] args) {
        Validator validator = buildValidator();
        validObject(validator);
        allViolations(validator);
        targetOneField(validator);
    }

    static void validObject(Validator validator) {
        section("Valid object — zero violations");
        RegistrationRequest r = DataFactory.of(RegistrationRequest.class).valid();
        System.out.println("  email:      " + r.getEmail());
        System.out.println("  name:       " + r.getFullName());
        System.out.println("  age:        " + r.getAge());
        System.out.println("  violations: " + validator.validate(r).size() + "  ✓");
    }

    static void allViolations(Validator validator) {
        section("Each constraint tested — one invalid object per constraint");
        List<RegistrationRequest> bad = DataFactory.of(RegistrationRequest.class).invalidList();
        System.out.printf("  %-12s %-28s %-20s %s%n", "field", "value", "constraint", "violations");
        System.out.println("  " + "-".repeat(75));
        for (RegistrationRequest req : bad) {
            Set<ConstraintViolation<RegistrationRequest>> v = validator.validate(req);
            if (v.isEmpty()) continue;
            String field      = v.iterator().next().getPropertyPath().toString();
            String constraint = v.iterator().next().getConstraintDescriptor()
                    .getAnnotation().annotationType().getSimpleName();
            String value      = fieldValue(req, field);
            System.out.printf("  %-12s %-28s %-20s %d%n",
                    field, shorten(value, 26), constraint, v.size());
        }
        System.out.println();
        System.out.println("  Every constraint enforced. Zero manual setup.");
    }

    static void targetOneField(Validator validator) {
        section("Target one field — useful for controller tests");
        RegistrationRequest r = DataFactory.of(RegistrationRequest.class).invalidFor("email");
        Set<ConstraintViolation<RegistrationRequest>> v = validator.validate(r);
        System.out.println("  email:      " + r.getEmail() + "  ← invalid");
        System.out.println("  name:       " + r.getFullName() + "  ← valid");
        System.out.println("  age:        " + r.getAge() + "  ← valid");
        System.out.println("  violations: " + v.size() + " (email only)");
    }

    static String fieldValue(RegistrationRequest r, String field) {
        return switch (field) {
            case "email"    -> r.getEmail() == null ? "null" : r.getEmail();
            case "password" -> r.getPassword() == null ? "null" : r.getPassword();
            case "fullName" -> r.getFullName() == null ? "null" : r.getFullName();
            case "age"      -> r.getAge() == null ? "null" : String.valueOf(r.getAge());
            default         -> "?";
        };
    }

    static Validator buildValidator() {
        try (ValidatorFactory f = Validation.buildDefaultValidatorFactory()) {
            return f.getValidator();
        }
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }

    static String shorten(String s, int max) {
        if (s == null) return "null";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
