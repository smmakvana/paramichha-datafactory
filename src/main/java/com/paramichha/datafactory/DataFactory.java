package com.paramichha.datafactory;

import com.paramichha.datafactory.builder.*;
import com.paramichha.datafactory.builder.FieldBuilderFactory;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
 * to read the annotations and generate correct data automatically:
 * <pre>
 * UserRequest valid   = DataFactory.of(UserRequest.class).valid();
 * UserRequest noEmail = DataFactory.of(UserRequest.class).withNull("email").valid();
 *
 * List&lt;UserRequest&gt; bad  = DataFactory.of(UserRequest.class).invalidList();
 * List&lt;UserRequest&gt; bulk = DataFactory.of(UserRequest.class).stream(1000);
 * </pre>
 *
 * @see DataBuilder
 * @see FieldBuilder
 * @see GenerationMode
 */
public final class DataFactory {

    private DataFactory() {}

    // =========================================================================
    // Level 1 — instant values
    // =========================================================================

    /** A realistic email address. Example: {@code alice.johnson@example.com} */
    public static String email()       { return string("email").valid(); }

    /** A realistic full name. Example: {@code Alice Johnson} */
    public static String name()        { return string("name").valid(); }

    /** A realistic first name. Example: {@code Alice} */
    public static String firstName()   { return string("firstName").valid(); }

    /** A realistic last name. Example: {@code Johnson} */
    public static String lastName()    { return string("lastName").valid(); }

    /** A realistic phone number. Example: {@code +44 7700 900123} */
    public static String phone()       { return string("phone").valid(); }

    /** A realistic city name. Example: {@code London} */
    public static String city()        { return string("city").valid(); }

    /** A realistic country name. Example: {@code United Kingdom} */
    public static String country()     { return string("country").valid(); }

    /** A realistic postcode. Example: {@code SW1A 1AA} */
    public static String postcode()    { return string("postcode").valid(); }

    /** A realistic URL. Example: {@code https://example.com} */
    public static String url()         { return string("url").valid(); }

    /** A realistic company name. Example: {@code Acme Corp} */
    public static String company()     { return string("company").valid(); }

    /** A realistic sentence or description. */
    public static String description() { return string("description").valid(); }

    /** A random UUID string. Example: {@code 550e8400-e29b-41d4-a716-446655440000} */
    public static String uuid()        { return UUID.randomUUID().toString(); }

    /** {@code n} realistic email addresses. */
    public static List<String> emails(int n)    { return string("email").stream(n); }

    /** {@code n} realistic full names. */
    public static List<String> names(int n)     { return string("name").stream(n); }

    /** {@code n} realistic phone numbers. */
    public static List<String> phones(int n)    { return string("phone").stream(n); }

    /** {@code n} realistic city names. */
    public static List<String> cities(int n)    { return string("city").stream(n); }

    /** {@code n} realistic company names. */
    public static List<String> companies(int n) { return string("company").stream(n); }

    /** {@code n} random UUID strings. */
    public static List<String> uuids(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());
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
    public static StringField string() {
        return new DefaultStringField(FieldBuilderFactory.create(String.class));
    }

    /**
     * Returns a {@link StringField} pre-seeded with a semantic hint.
     * Shorthand for {@code DataFactory.string().hint(hint)}.
     * <pre>
     * String email = DataFactory.string("email").valid();
     * String iban  = DataFactory.string("iban").valid();
     *
     * List&lt;String&gt; phones = DataFactory.string("phone").stream(50);
     * </pre>
     */
    public static StringField string(String hint) {
        return new DefaultStringField(FieldBuilderFactory.create(String.class, hint));
    }

    /**
     * Returns a builder for {@code Integer} / {@code int} values.
     * <pre>
     * int age   = DataFactory.integer().range(18, 65).valid();
     * int score = DataFactory.integer().positive().toInt();
     * </pre>
     */
    public static IntegerField integer() {
        return new DefaultIntegerField(FieldBuilderFactory.create(Integer.class));
    }

    /**
     * Returns a builder for {@code Long} / {@code long} values.
     * <pre>
     * long id    = DataFactory.longVal().positive().toLong();
     * long price = DataFactory.longVal().range(1L, 9999L).toLong();
     * </pre>
     */
    public static LongField longVal() {
        return new DefaultLongField(FieldBuilderFactory.create(Long.class));
    }

    /**
     * Returns a builder for {@code BigDecimal} values.
     * <pre>
     * BigDecimal price = DataFactory.decimal().positive().valid();
     * BigDecimal gbp   = DataFactory.decimal().range(0.01, 999.99).valid();
     * </pre>
     */
    public static DecimalField decimal() {
        return new DefaultDecimalField(FieldBuilderFactory.create(BigDecimal.class));
    }

    /**
     * Returns a builder for {@code Boolean} / {@code boolean} values.
     * <pre>
     * boolean active  = DataFactory.bool().assertTrue().valid();
     * boolean deleted = DataFactory.bool().assertFalse().valid();
     * </pre>
     */
    public static BoolField bool() {
        return new DefaultBoolField(FieldBuilderFactory.create(Boolean.class));
    }

    /**
     * Returns a builder for {@code LocalDate} values.
     * <pre>
     * LocalDate dob    = DataFactory.date().past().valid();
     * LocalDate expiry = DataFactory.date().future().valid();
     * </pre>
     */
    public static DateField date() {
        return new DefaultDateField(FieldBuilderFactory.create(LocalDate.class));
    }

    /**
     * Returns a builder for {@code LocalDateTime} values.
     * <pre>
     * LocalDateTime created  = DataFactory.dateTime().past().valid();
     * LocalDateTime deadline = DataFactory.dateTime().future().valid();
     * </pre>
     */
    public static DateTimeField dateTime() {
        return new DefaultDateTimeField(FieldBuilderFactory.create(LocalDateTime.class));
    }

    /**
     * Returns a {@link DoubleField} for {@code Double} / {@code double} values.
     * <pre>
     * double price = DataFactory.doubles().positive().toDouble();
     * </pre>
     */
    public static DoubleField doubles() {
        return new DefaultDoubleField(FieldBuilderFactory.create(Double.class));
    }

    /**
     * Returns a {@link FloatField} for {@code Float} / {@code float} values.
     * <pre>
     * float rate = DataFactory.floats().positive().toFloat();
     * </pre>
     */
    public static FloatField floats() {
        return new DefaultFloatField(FieldBuilderFactory.create(Float.class));
    }

    /**
     * Returns a {@link ShortField} for {@code Short} / {@code short} values.
     * <pre>
     * short flags = DataFactory.shorts().positive().toShort();
     * </pre>
     */
    public static ShortField shorts() {
        return new DefaultShortField(FieldBuilderFactory.create(Short.class));
    }

    /**
     * Returns a {@link ByteField} for {@code Byte} / {@code byte} values.
     * <pre>
     * byte n = DataFactory.bytes().positive().toByte();
     * </pre>
     */
    public static ByteField bytes() {
        return new DefaultByteField(FieldBuilderFactory.create(Byte.class));
    }

    /**
     * Returns a {@link FieldBuilder} for {@code BigInteger} values.
     * <pre>
     * java.math.BigInteger n = DataFactory.bigInteger().valid();
     * </pre>
     */
    public static FieldBuilder<java.math.BigInteger> bigInteger() {
        return FieldBuilderFactory.create(java.math.BigInteger.class);
    }

    /**
     * Returns a {@link FieldBuilder} for {@code Character} / {@code char} values.
     * <pre>
     * char c = DataFactory.character().valid();
     * </pre>
     */
    public static FieldBuilder<Character> character() {
        return FieldBuilderFactory.create(Character.class);
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
        if (constants == null || constants.length == 0)
            throw new DataFactoryException(
                    "Cannot generate enum value — " + enumType.getSimpleName() + " has no constants");
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
        if (constants == null || constants.length == 0)
            throw new DataFactoryException(
                    "Cannot generate enum values — " + enumType.getSimpleName() + " has no constants");
        return IntStream.range(0, n)
                .mapToObj(i -> constants[ThreadLocalRandom.current().nextInt(constants.length)])
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Level 2 — generic field builder
    // =========================================================================

    /**
     * Returns a {@link FieldBuilder} for the given type with no constraints.
     * Use this when you want to add constraints manually via {@code .with()}.
     * <pre>
     * String email = DataFactory.field(String.class)
     *                           .with(Email.class)
     *                           .with("@Size(max=100)")
     *                           .valid();
     * </pre>
     */
    public static <T> FieldBuilder<T> field(Class<T> type) {
        return FieldBuilderFactory.create(type);
    }

    /**
     * Returns a {@link FieldBuilder} from a reflected {@code Field}.
     * All Jakarta validation annotations declared on the field are read automatically.
     * <pre>
     * Field f     = UserRequest.class.getDeclaredField("email");
     * String good = DataFactory.field(f).valid();
     * String bad  = DataFactory.field(f).invalid();
     * </pre>
     */
    public static <T> FieldBuilder<T> field(java.lang.reflect.Field field) {
        return FieldBuilderFactory.create(field);
    }

    /**
     * Returns a {@link FieldBuilder} from an explicit name, type, and annotation list.
     *
     * <p>Use this for record components where Jakarta annotations sit on the
     * constructor parameter rather than the backing field, meaning
     * {@code field.getAnnotations()} returns empty.
     * <pre>
     * FieldBuilder&lt;String&gt; fb = DataFactory.field("email", String.class, annotations);
     * List&lt;String&gt; validEmails = fb.validList();
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <T> FieldBuilder<T> field(String name, Class<T> type,
                                             List<Annotation> annotations) {
        return FieldBuilderFactory.create(name, type, annotations);
    }

    // =========================================================================
    // Level 3 — object builder
    // =========================================================================

    /**
     * Returns a {@link DataBuilder} for the given Jakarta-annotated class.
     *
     * <p>DataFactory reads all field annotations and generates values that
     * satisfy them — or violate each one individually.
     * <pre>
     * UserRequest valid   = DataFactory.of(UserRequest.class).valid();
     * UserRequest noEmail = DataFactory.of(UserRequest.class).withNull("email").valid();
     *
     * List&lt;UserRequest&gt; bad  = DataFactory.of(UserRequest.class).invalidList();
     * List&lt;UserRequest&gt; bulk = DataFactory.of(UserRequest.class).stream(1000);
     * </pre>
     *
     * @see DataBuilder
     */
    public static <T> DataBuilder<T> of(Class<T> type) {
        return new DefaultDataBuilder<>(type);
    }

    // ── Object-level static shortcuts ────────────────────────────────────────
    // Convenience wrappers for the most common single-call operations.

    /**
     * Builds one valid instance of the given type.
     * Equivalent to {@code DataFactory.of(type).valid()}.
     */
    public static <T> T valid(Class<T> type) {
        return of(type).valid();
    }

    /**
     * Builds one invalid instance per constraint per field.
     * Each object in the returned list violates exactly one constraint.
     * Equivalent to {@code DataFactory.of(type).invalidList()}.
     */
    public static <T> List<T> invalidList(Class<T> type) {
        return of(type).invalidList();
    }

    /**
     * Builds one invalid instance with the named field set to an invalid value.
     * All other fields are valid.
     * Equivalent to {@code DataFactory.of(type).invalidFor(fieldName)}.
     */
    public static <T> T invalidFor(Class<T> type, String fieldName) {
        return of(type).invalidFor(fieldName);
    }

    /**
     * Returns boundary-covering valid instances per field, keyed by field name.
     * Equivalent to {@code DataFactory.of(type).validMap()}.
     *
     * <p>Each map entry is a field name mapped to a list of valid objects where
     * that field covers its boundary values — minimum, midpoint, maximum, semantic.
     * All other fields hold their canonical valid value.
     */
    public static <T> Map<String, List<T>> validMap(Class<T> type) {
        return of(type).validMap();
    }

    /**
     * Builds {@code n} valid instances of the given type.
     * Equivalent to {@code DataFactory.of(type).stream(n)}.
     * <pre>
     * List&lt;UserRequest&gt; users = DataFactory.stream(UserRequest.class, 1000);
     * </pre>
     */
    public static <T> List<T> stream(Class<T> type, int n) {
        return of(type).stream(n);
    }
}
