package com.paramichha.datafactory;

import com.paramichha.datafactory.builder.DefaultDataBuilderV2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

/**
 * V2 entry point for DataFactory.
 *
 * <p>Single results return {@link DataFactoryResult} — never throws.
 * Bulk results return {@link DataFactoryResults} — supports SUCCESS, PARTIAL, FAILED.
 * V1 ({@link DataFactory}) is untouched. Delete it when V2 is stable.
 *
 * <h2>Usage</h2>
 * <pre>
 * // Single
 * DataFactoryResult&lt;UserRequest&gt; result = DataFactoryV2.of(UserRequest.class).valid();
 * UserRequest req = result.get();
 *
 * // Bulk — same processing every time
 * DataFactoryResults&lt;UserRequest&gt; bulk = DataFactoryV2.of(UserRequest.class).validList(1000);
 * bulk.succeeded().forEach(r -> use(r.get()));
 * bulk.failed().forEach(r -> log(r.errorType() + ": " + r.reason()));
 *
 * // Discovery
 * DataFactoryV2.canHandle(MyType.class)
 * DataFactoryV2.supportedTypes()
 * DataFactoryV2.supportedAnnotations()
 * </pre>
 */
public final class DataFactoryV2 {

    public static final String VERSION = "2.0.0";

    private DataFactoryV2() {}

    // ── Builder ───────────────────────────────────────────────────────────────

    public static <T> DataBuilderV2<T> of(Class<T> type) {
        return new DefaultDataBuilderV2<>(type);
    }

    // ── Static shortcuts ──────────────────────────────────────────────────────

    public static <T> DataFactoryResult<T>              valid(Class<T> type)                      { return of(type).valid(); }
    public static <T> DataFactoryResult<T>              invalid(Class<T> type)                    { return of(type).invalid(); }
    public static <T> DataFactoryResult<T>              invalidFor(Class<T> type, String field)   { return of(type).invalidFor(field); }
    public static <T> DataFactoryResults<T>             validList(Class<T> type, int count)       { return of(type).validList(count); }
    public static <T> DataFactoryResults<T>             invalidList(Class<T> type)                { return of(type).invalidList(); }
    public static <T> Map<String, DataFactoryResults<T>> validPerField(Class<T> type)             { return of(type).validPerField(); }
    public static Map<String, DataFactoryResult<FieldCases>> fields(Class<?> type)                { return of(type).fields(); }

    // ── Discovery ─────────────────────────────────────────────────────────────

    public static Set<Class<?>> supportedTypes()         { return SUPPORTED_TYPES; }
    public static Set<String>   supportedAnnotations()   { return SUPPORTED_ANNOTATIONS; }

    public static boolean canHandle(Class<?> type) {
        if (type == null)                                         return false;
        if (type.isEnum())                                        return true;
        if (type.isArray())                                       return canHandle(type.getComponentType());
        if (Collection.class.isAssignableFrom(type))              return true;
        if (Map.class.isAssignableFrom(type))                     return true;
        return SUPPORTED_TYPES.contains(type);
    }

    // ── Registries ────────────────────────────────────────────────────────────

    private static final Set<Class<?>> SUPPORTED_TYPES = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class,
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            String.class, BigDecimal.class, BigInteger.class, UUID.class,
            Instant.class, LocalDate.class, LocalDateTime.class, LocalTime.class,
            ZonedDateTime.class, OffsetDateTime.class, Date.class, Year.class, YearMonth.class
    )));

    private static final Set<String> SUPPORTED_ANNOTATIONS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "NotNull", "Null", "NotBlank", "NotEmpty", "Size", "Min", "Max",
            "DecimalMin", "DecimalMax", "Digits", "Positive", "PositiveOrZero",
            "Negative", "NegativeOrZero", "Pattern", "Email", "AssertTrue", "AssertFalse",
            "Past", "PastOrPresent", "Future", "FutureOrPresent", "Valid",
            "Length", "Range", "URL", "CreditCardNumber", "ISBN", "EAN", "UUID"
    )));
}
