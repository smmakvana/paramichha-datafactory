package com.paramichha.datafactory;

import com.paramichha.datafactory.builder.DefaultBigDecimalValue;
import com.paramichha.datafactory.builder.DefaultBooleanValue;
import com.paramichha.datafactory.builder.DefaultByteValue;
import com.paramichha.datafactory.builder.DefaultDataBuilder;
import com.paramichha.datafactory.builder.DefaultDoubleValue;
import com.paramichha.datafactory.builder.DefaultFloatValue;
import com.paramichha.datafactory.builder.DefaultIntegerValue;
import com.paramichha.datafactory.builder.DefaultLocalDateTimeValue;
import com.paramichha.datafactory.builder.DefaultLocalDateValue;
import com.paramichha.datafactory.builder.DefaultLongValue;
import com.paramichha.datafactory.builder.DefaultShortValue;
import com.paramichha.datafactory.builder.DefaultStringValue;
import com.paramichha.datafactory.builder.ValueBuilderFactory;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The single entry point for all test data generation.
 *
 * <p>DataFactory works at three levels depending on what you need:
 *
 * <h2>Level 1 — instant values, no setup</h2>
 * <p>When you just need a realistic string right now:
 * <pre>
 * String email   = DataFactory.email();
 * String name    = DataFactory.name();
 * String phone   = DataFactory.phone();
 * String uuid    = DataFactory.uuid();
 * </pre>
 *
 * <h2>Level 2 — typed builders, one chain</h2>
 * <p>When you need a value with specific constraints or format:
 * <pre>
 * String      email  = DataFactory.string().email().valid();
 * int         age    = DataFactory.integer().range(18, 65).valid();
 * BigDecimal  price  = DataFactory.decimal().positive().valid();
 * LocalDate   dob    = DataFactory.date().past().valid();
 * Status      status = DataFactory.enumOf(Status.class);
 *
 * List&lt;String&gt; emails = DataFactory.string().email().stream(100);
 * </pre>
 *
 * <h2>Level 3 — object builders, full annotation-awareness</h2>
 * <p>When you have a Jakarta-annotated class and want DataFactory
 * to read the annotations and generate correct data automatically.
 * Results are never thrown — check {@code isSuccess()} or call {@code get()}:
 * <pre>
 * UserRequest valid   = DataFactory.of(UserRequest.class).valid().get();
 * UserRequest noEmail = DataFactory.of(UserRequest.class).withNull("email").valid().get();
 *
 * DataFactoryResults&lt;UserRequest&gt; bad  = DataFactory.of(UserRequest.class).invalidList();
 * DataFactoryResults&lt;UserRequest&gt; bulk = DataFactory.of(UserRequest.class).validList(1000);
 *
 * // Per field, without building the object
 * Map&lt;String, DataFactoryResult&lt;FieldCases&gt;&gt; f = DataFactory.of(UserRequest.class).fields();
 * </pre>
 *
 * @see DataBuilder
 * @see FieldValue
 */
public final class DataFactory {

    private DataFactory() {}

    // =========================================================================
    // Level 1 — instant values
    // =========================================================================

    /** A realistic email address. Example: {@code alice.johnson@example.com} */
    public static String email() {
        return string("email").valid();
    }

    /** A realistic full name. Example: {@code Alice Johnson} */
    public static String name() {
        return string("name").valid();
    }

    /** A realistic first name. Example: {@code Alice} */
    public static String firstName() {
        return string("firstName").valid();
    }

    /** A realistic last name. Example: {@code Johnson} */
    public static String lastName() {
        return string("lastName").valid();
    }

    /** A realistic phone number. Example: {@code +44 7700 900123} */
    public static String phone() {
        return string("phone").valid();
    }

    /** A realistic city name. Example: {@code London} */
    public static String city() {
        return string("city").valid();
    }

    /** A realistic country name. Example: {@code United Kingdom} */
    public static String country() {
        return string("country").valid();
    }

    /** A realistic postcode. Example: {@code SW1A 1AA} */
    public static String postcode() {
        return string("postcode").valid();
    }

    /** A realistic URL. Example: {@code https://example.com} */
    public static String url() {
        return string("url").valid();
    }

    /** A realistic company name. Example: {@code Acme Corp} */
    public static String company() {
        return string("company").valid();
    }

    /** A realistic sentence or description. */
    public static String description() {
        return string("description").valid();
    }

    /** A random UUID string. Example: {@code 550e8400-e29b-41d4-a716-446655440000} */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /** {@code n} realistic email addresses. */
    public static List<String> emails(int n) {
        return string("email").stream(n);
    }

    /** {@code n} realistic full names. */
    public static List<String> names(int n) {
        return string("name").stream(n);
    }

    /** {@code n} realistic phone numbers. */
    public static List<String> phones(int n) {
        return string("phone").stream(n);
    }

    /** {@code n} realistic city names. */
    public static List<String> cities(int n) {
        return string("city").stream(n);
    }

    /** {@code n} realistic company names. */
    public static List<String> companies(int n) {
        return string("company").stream(n);
    }

    /** {@code n} random UUID strings. */
    public static List<String> uuids(int n) {
        return IntStream.range(0, n).mapToObj(i -> UUID.randomUUID().toString()).collect(Collectors.toList());
    }

    // =========================================================================
    // Level 2 — typed fluent builders
    // =========================================================================

    /**
     * Returns a builder for {@code String} values.
     * <pre>
     * String email = DataFactory.string().email().valid();
     * String iban  = DataFactory.string().iban().valid();
     * String name  = DataFactory.string().name().valid();
     * </pre>
     */
    public static StringValue string() {
        return new DefaultStringValue(ValueBuilderFactory.create(String.class));
    }

    /**
     * Returns a {@link StringValue} pre-seeded with a semantic hint.
     * Shorthand for {@code DataFactory.string().hint(hint)}.
     * <pre>
     * String email = DataFactory.string("email").valid();
     * String iban  = DataFactory.string("iban").valid();
     *
     * List&lt;String&gt; phones = DataFactory.string("phone").stream(50);
     * </pre>
     */
    public static StringValue string(String hint) {
        return new DefaultStringValue(ValueBuilderFactory.create(String.class, hint));
    }

    /**
     * Returns a builder for {@code Integer} / {@code int} values.
     * <pre>
     * int age   = DataFactory.integer().range(18, 65).valid();
     * int score = DataFactory.integer().positive().toInt();
     * </pre>
     */
    public static IntegerValue integer() {
        return new DefaultIntegerValue(ValueBuilderFactory.create(Integer.class));
    }

    /**
     * Returns a builder for {@code Long} / {@code long} values.
     * <pre>
     * long id    = DataFactory.longVal().positive().toLong();
     * long price = DataFactory.longVal().range(1L, 9999L).toLong();
     * </pre>
     */
    public static LongValue longVal() {
        return new DefaultLongValue(ValueBuilderFactory.create(Long.class));
    }

    /**
     * Returns a builder for {@code BigDecimal} values.
     * <pre>
     * BigDecimal price = DataFactory.decimal().positive().valid();
     * BigDecimal gbp   = DataFactory.decimal().range(0.01, 999.99).valid();
     * </pre>
     */
    public static BigDecimalValue decimal() {
        return new DefaultBigDecimalValue(ValueBuilderFactory.create(BigDecimal.class));
    }

    /**
     * Returns a builder for {@code Boolean} / {@code boolean} values.
     * <pre>
     * boolean active  = DataFactory.bool().assertTrue().valid();
     * boolean deleted = DataFactory.bool().assertFalse().valid();
     * </pre>
     */
    public static BooleanValue bool() {
        return new DefaultBooleanValue(ValueBuilderFactory.create(Boolean.class));
    }

    /**
     * Returns a builder for {@code LocalDate} values.
     * <pre>
     * LocalDate dob    = DataFactory.date().past().valid();
     * LocalDate expiry = DataFactory.date().future().valid();
     * </pre>
     */
    public static LocalDateValue date() {
        return new DefaultLocalDateValue(ValueBuilderFactory.create(LocalDate.class));
    }

    /**
     * Returns a builder for {@code LocalDateTime} values.
     * <pre>
     * LocalDateTime created  = DataFactory.dateTime().past().valid();
     * LocalDateTime deadline = DataFactory.dateTime().future().valid();
     * </pre>
     */
    public static LocalDateTimeValue dateTime() {
        return new DefaultLocalDateTimeValue(ValueBuilderFactory.create(LocalDateTime.class));
    }

    /**
     * Returns a {@link DoubleValue} for {@code Double} / {@code double} values.
     * <pre>
     * double price = DataFactory.doubles().positive().toDouble();
     * </pre>
     */
    public static DoubleValue doubles() {
        return new DefaultDoubleValue(ValueBuilderFactory.create(Double.class));
    }

    /**
     * Returns a {@link FloatValue} for {@code Float} / {@code float} values.
     * <pre>
     * float rate = DataFactory.floats().positive().toFloat();
     * </pre>
     */
    public static FloatValue floats() {
        return new DefaultFloatValue(ValueBuilderFactory.create(Float.class));
    }

    /**
     * Returns a {@link ShortValue} for {@code Short} / {@code short} values.
     * <pre>
     * short flags = DataFactory.shorts().positive().toShort();
     * </pre>
     */
    public static ShortValue shorts() {
        return new DefaultShortValue(ValueBuilderFactory.create(Short.class));
    }

    /**
     * Returns a {@link ByteValue} for {@code Byte} / {@code byte} values.
     * <pre>
     * byte n = DataFactory.bytes().positive().toByte();
     * </pre>
     */
    public static ByteValue bytes() {
        return new DefaultByteValue(ValueBuilderFactory.create(Byte.class));
    }

    /**
     * Returns a {@link FieldValue} for {@code BigInteger} values.
     * <pre>
     * java.math.BigInteger n = DataFactory.bigInteger().valid();
     * </pre>
     */
    public static FieldValue<BigInteger> bigInteger() {
        return ValueBuilderFactory.create(java.math.BigInteger.class);
    }

    /**
     * Returns a {@link FieldValue} for {@code Character} / {@code char} values.
     * <pre>
     * char c = DataFactory.character().valid();
     * </pre>
     */
    public static FieldValue<Character> character() {
        return ValueBuilderFactory.create(Character.class);
    }

    /**
     * Returns a random constant from the given enum type.
     * <pre>
     * Status   status   = DataFactory.enumOf(Status.class);
     * Priority priority = DataFactory.enumOf(Priority.class);
     * </pre>
     *
     * @throws DataFactoryException if the enum type has no constants
     */
    public static <E extends Enum<E>> E enumOf(Class<E> enumType) {
        E[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new DataFactoryException(
                    "Cannot generate enum value — " + enumType.getSimpleName() + " has no constants");
        }
        return constants[ThreadLocalRandom.current().nextInt(constants.length)];
    }

    /**
     * Returns {@code n} random constants from the given enum type.
     * Constants may repeat — this is random sampling, not a permutation.
     * <pre>
     * List&lt;Status&gt; statuses = DataFactory.enumOf(Status.class, 10);
     * </pre>
     *
     * @throws DataFactoryException if the enum type has no constants
     */
    public static <E extends Enum<E>> List<E> enumOf(Class<E> enumType, int n) {
        E[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new DataFactoryException(
                    "Cannot generate enum values — " + enumType.getSimpleName() + " has no constants");
        }
        return IntStream.range(0, n)
                .mapToObj(i -> constants[ThreadLocalRandom.current().nextInt(constants.length)])
                .collect(Collectors.toList());
    }

    /**
     * Returns a deterministic constant from the enum type for the given seed.
     * Same seed always returns the same constant — useful for test data generation.
     * <pre>
     * Status status = DataFactory.enumOf(Status.class, 42L);
     * </pre>
     */
    public static <E extends Enum<E>> E enumOf(Class<E> enumType, long seed) {
        E[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new DataFactoryException(
                    "Cannot generate enum value — " + enumType.getSimpleName() + " has no constants");
        }
        return constants[(int) (Math.abs(seed) % constants.length)];
    }

    /**
     * Returns {@code n} deterministic constants from the enum type for the given seed.
     * Same seed always returns the same sequence.
     */
    public static <E extends Enum<E>> List<E> enumOf(Class<E> enumType, int n, long seed) {
        E[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new DataFactoryException(
                    "Cannot generate enum values — " + enumType.getSimpleName() + " has no constants");
        }
        Random rng = new Random(seed);
        return IntStream.range(0, n)
                .mapToObj(i -> constants[rng.nextInt(constants.length)])
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Level 2 — generic field builder
    // =========================================================================

    /**
     * Returns a {@link FieldValue} for the given type with no constraints.
     * Use this when you want to add constraints manually via {@code .with()}.
     * <pre>
     * String email = DataFactory.field(String.class)
     *                           .with(Email.class)
     *                           .with("@Size(max=100)")
     *                           .valid();
     * </pre>
     */
    public static <T> FieldValue<T> field(Class<T> type) {
        return ValueBuilderFactory.create(type);
    }

    /**
     * Returns a {@link FieldValue} from a reflected {@code Field}.
     * All Jakarta validation annotations declared on the field are read automatically.
     * <pre>
     * Field f     = UserRequest.class.getDeclaredField("email");
     * String good = DataFactory.field(f).valid();
     * String bad  = DataFactory.field(f).invalid();
     * </pre>
     */
    public static <T> FieldValue<T> field(java.lang.reflect.Field field) {
        return ValueBuilderFactory.create(field);
    }

    /**
     * Returns a {@link FieldValue} from an explicit name, type, and annotation list.
     *
     * <p>Use this for record components where Jakarta annotations sit on the
     * constructor parameter rather than the backing field, meaning
     * {@code field.getAnnotations()} returns empty.
     * <pre>
     * FieldBuilder&lt;String&gt; fb = DataFactory.field("email", String.class, annotations);
     * List&lt;String&gt; validEmails = fb.validList();
     * </pre>
     */
    public static <T> FieldValue<T> field(String name, Class<T> type, List<Annotation> annotations) {
        return ValueBuilderFactory.create(name, type, annotations);
    }

    // =========================================================================
    // Level 3 — object builder
    // =========================================================================

    /**
     * The DataFactory artifact version, e.g. {@code "0.2.0"}. Read from the jar at build time
     * (Maven resource filtering), so it always matches the published artifact.
     * {@code "unknown"} if the version resource is missing.
     */
    public static final String VERSION =
            readVersion(DataFactory.class.getResourceAsStream("datafactory-version.properties"));

    static String readVersion(java.io.InputStream resource) {
        try (java.io.InputStream in = resource) {
            if (in == null) {
                return "unknown";
            }
            Properties p = new Properties();
            p.load(in);
            String v = p.getProperty("version", "unknown").trim();
            return v.isEmpty() || v.startsWith("${") ? "unknown" : v;
        } catch (java.io.IOException e) {
            return "unknown";
        }
    }

    /**
     * Entry point for annotation-aware object generation. Every operation returns a
     * {@link DataFactoryResult} or {@link DataFactoryResults} and never throws.
     *
     * <pre>
     * DataFactoryResult&lt;UserRequest&gt; r = DataFactory.of(UserRequest.class).valid();
     * if (r.isSuccess()) use(r.get());
     * else log(r.errorType() + ": " + r.reason());
     * </pre>
     */
    public static <T> DataBuilder<T> of(Class<T> type) {
        return new DefaultDataBuilder<>(type);
    }

    // ── Object-level static shortcuts ────────────────────────────────────────

    public static <T> DataFactoryResult<T> valid(Class<T> type) {
        return of(type).valid();
    }

    public static <T> DataFactoryResult<T> invalid(Class<T> type) {
        return of(type).invalid();
    }

    public static <T> DataFactoryResult<T> invalidFor(Class<T> type, String field) {
        return of(type).invalidFor(field);
    }

    public static <T> DataFactoryResults<T> validList(Class<T> type, int count) {
        return of(type).validList(count);
    }

    public static <T> DataFactoryResults<T> invalidList(Class<T> type) {
        return of(type).invalidList();
    }

    public static <T> Map<String, DataFactoryResults<T>> validPerField(Class<T> type) {
        return of(type).validPerField();
    }

    public static Map<String, DataFactoryResult<FieldCases>> fields(Class<?> type) {
        return of(type).fields();
    }

    // ── Discovery ────────────────────────────────────────────────────────────

    /** Scalar types DataFactory can generate values for. */
    public static Set<Class<?>> supportedTypes() {
        return SUPPORTED_TYPES;
    }

    /** Simple names of the constraint annotations DataFactory understands. */
    public static Set<String> supportedAnnotations() {
        return SUPPORTED_ANNOTATIONS;
    }

    /** Whether DataFactory can generate a value of {@code type}. */
    public static boolean canHandle(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (type.isEnum()) {
            return true;
        }
        if (type.isArray()) {
            return canHandle(type.getComponentType());
        }
        if (Collection.class.isAssignableFrom(type)) {
            return true;
        }
        if (Map.class.isAssignableFrom(type)) {
            return true;
        }
        return SUPPORTED_TYPES.contains(type);
    }

    // ── Registries ────────────────────────────────────────────────────────────

    private static final Set<Class<?>> SUPPORTED_TYPES = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            boolean.class,
            char.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class,
            BigDecimal.class,
            BigInteger.class,
            UUID.class,
            Instant.class,
            LocalDate.class,
            LocalDateTime.class,
            LocalTime.class,
            ZonedDateTime.class,
            OffsetDateTime.class,
            Date.class,
            Year.class,
            YearMonth.class)));

    private static final Set<String> SUPPORTED_ANNOTATIONS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    "NotNull",
                    "Null",
                    "NotBlank",
                    "NotEmpty",
                    "Size",
                    "Min",
                    "Max",
                    "DecimalMin",
                    "DecimalMax",
                    "Digits",
                    "Positive",
                    "PositiveOrZero",
                    "Negative",
                    "NegativeOrZero",
                    "Pattern",
                    "Email",
                    "AssertTrue",
                    "AssertFalse",
                    "Past",
                    "PastOrPresent",
                    "Future",
                    "FutureOrPresent",
                    "Valid",
                    "Length",
                    "Range",
                    "URL",
                    "CreditCardNumber",
                    "ISBN",
                    "EAN",
                    "UUID")));
}
