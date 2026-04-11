package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.FieldBuilder;
import com.paramichha.datafactory.constraint.*;
import com.paramichha.datafactory.generation.BoundaryPlanner;
import com.paramichha.datafactory.generation.ValueGenerator;

import net.datafaker.Faker;
import com.paramichha.datafactory.generation.FakerProvider;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

final public class DefaultFieldBuilder<T> implements FieldBuilder<T> {

    private final FieldConstraints constraints;
    private final List<Annotation> extraAnnotations;
    private final Faker faker;
    private final Long seed;

    DefaultFieldBuilder(FieldConstraints constraints) {
        this(constraints, List.of(), null, null);
    }

    DefaultFieldBuilder(FieldConstraints constraints, Faker faker) {
        this(constraints, List.of(), faker, null);
    }

    DefaultFieldBuilder(FieldConstraints constraints, Faker faker, Long seed) {
        this(constraints, List.of(), faker, seed);
    }

    private DefaultFieldBuilder(FieldConstraints constraints, List<Annotation> extraAnnotations, Faker faker, Long seed) {
        this.constraints = constraints;
        this.extraAnnotations = extraAnnotations;
        this.faker = faker;
        this.seed = seed;
    }

    private Faker resolvedFaker() {
        return faker != null ? faker : FakerProvider.random();
    }

    private void setSeededRandom() {
        if (seed != null) {
            FakerProvider.SEEDED_RANDOM.set(FakerProvider.randomForSeed(seed));
        }
    }

    @Override
    public DefaultFieldBuilder<T> with(Class<? extends Annotation> annotationClass) {
        Annotation instance = DefaultAnnotationParser.INSTANCE
                .parse("@" + annotationClass.getSimpleName());
        if (instance == null) return this;
        List<Annotation> next = new ArrayList<>(extraAnnotations);
        next.add(instance);
        return new DefaultFieldBuilder<>(constraints, List.copyOf(next), faker, seed);
    }

    @Override
    public DefaultFieldBuilder<T> with(String annotationString) {
        Annotation instance = DefaultAnnotationParser.INSTANCE.parse(annotationString);
        if (instance == null) return this;
        List<Annotation> next = new ArrayList<>(extraAnnotations);
        next.add(instance);
        return new DefaultFieldBuilder<>(constraints, List.copyOf(next), faker, seed);
    }

    private FieldConstraints resolvedConstraints() {
        if (extraAnnotations.isEmpty()) return constraints;
        // Re-analyze merging base field annotations with extras.
        // Base annotations come from the descriptor's validations (held inside the original
        // FieldConstraints build chain) — we reconstruct from the extra list merged on top.
        // Since FieldConstraints no longer carries the raw annotation list, we keep the base
        // constraints as-is and layer extra annotations by re-running analysis on extras only,
        // then merging the two FieldConstraints snapshots is complex. Simpler: require callers
        // using .with() to supply the full annotation list via FieldBuilderFactory.create(name, type, list).
        // For the common .with(annotationString) path, merge into a fresh analysis.
        List<Annotation> merged = new ArrayList<>(extraAnnotations);
        return AnnotationAnalyzer.analyze(
                constraints.fieldName(), constraints.fieldType(), merged);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T valid() {
        FieldConstraints rc = resolvedConstraints();
        setSeededRandom();
        try {
            List<Object> values = ValueGenerator.shapeAll(rc, BoundaryPlanner.plan(rc), resolvedFaker());
            return (T) (values.isEmpty() ? null : values.getFirst());
        } finally {
            FakerProvider.SEEDED_RANDOM.remove();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> validList() {
        FieldConstraints rc = resolvedConstraints();
        setSeededRandom();
        try {
            return (List<T>) ValueGenerator.shapeAll(rc, BoundaryPlanner.plan(rc), resolvedFaker());
        } finally {
            FakerProvider.SEEDED_RANDOM.remove();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T invalid() {
        FieldConstraints rc = resolvedConstraints();
        List<ConstraintCase> cases = annotationCases(rc);
        if (cases.isEmpty()) cases = constraintCases(rc);
        return (T) (cases.isEmpty() ? null : cases.getFirst().value());
    }

    /**
     * Returns the first invalid value wrapped in Optional, or empty if no invalid cases exist.
     * Distinguishes between "null is the violation" (Optional.of(null) via wrapper) and "no cases".
     */
    public Optional<Object> firstInvalidValue() {
        FieldConstraints rc = resolvedConstraints();
        List<ConstraintCase> cases = annotationCases(rc);
        if (cases.isEmpty()) cases = constraintCases(rc);
        if (cases.isEmpty()) return Optional.empty();
        // Use a holder since Optional cannot wrap null
        return Optional.of(new Object[]{cases.getFirst().value()});
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> invalidList() {
        FieldConstraints rc = resolvedConstraints();
        List<ConstraintCase> cases = annotationCases(rc);
        if (cases.isEmpty()) {
            cases = constraintCases(rc).stream()
                    .filter(c -> !c.annotationDriven())
                    .toList();
        }
        return (List<T>) cases.stream().map(ConstraintCase::value).toList();
    }

    @Override
    public List<T> stream(int count) {
        List<T> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) result.add(valid());
        return List.copyOf(result);
    }

    public List<ConstraintCase> annotationCases() {
        return annotationCases(resolvedConstraints());
    }

    public List<ConstraintCase> constraintCases() {
        return constraintCases(resolvedConstraints());
    }

    // ── constraint case generation ────────────────────────────────────────

    private static List<ConstraintCase> annotationCases(FieldConstraints c) {
        return constraintCases(c).stream()
                .filter(ConstraintCase::annotationDriven).toList();
    }

    private static List<ConstraintCase> constraintCases(FieldConstraints c) {
        List<ConstraintCase> cases = new ArrayList<>();
        String fn = c.fieldName();
        Class<?> type = c.fieldType();

        // Type-default invalid cases — only when field has no binding constraint
        boolean skipNull = c.cascadeValid() && c.nullable();
        if (!type.isPrimitive() && !c.mustBeNull() && c.nullable() && !skipNull)
            cases.add(ConstraintCase.typeDefault(fn, "Null", null));
        if (type == String.class && c.nullable())
            cases.add(ConstraintCase.typeDefault(fn, "Blank", ""));
        if (isNumeric(type) && c.nullable() && !boundsHasMin(c.bounds()))
            cases.add(ConstraintCase.typeDefault(fn, "Negative", castNumeric(type, -1)));
        if (isBoolean(type) && c.nullable() && !c.assertTrueRequired() && !c.assertFalseRequired())
            cases.add(ConstraintCase.typeDefault(fn, "False", false));
        if (isList(type) && c.nullable() && intMin(c.bounds()) == null)
            cases.add(ConstraintCase.typeDefault(fn, "Empty", List.of()));

        // Annotation-driven invalid cases — from declared constraints
        if (!c.nullable())
            cases.add(ConstraintCase.of(fn, "NotNull", null));
        if (c.hasNotBlank() && type == String.class)
            cases.add(ConstraintCase.of(fn, "NotBlank", ""));
        switch (c.format()) {
            case EMAIL -> cases.add(ConstraintCase.of(fn, "Email", "not-an-email"));
            case URL -> cases.add(ConstraintCase.of(fn, "URL", "not-a-url"));
            case CREDIT_CARD -> cases.add(ConstraintCase.of(fn, "CreditCardNumber", "1234-bad"));
            case ISBN -> cases.add(ConstraintCase.of(fn, "ISBN", "000-bad-isbn"));
            case EAN -> cases.add(ConstraintCase.of(fn, "EAN", "000-bad-ean"));
            case UUID_STRING -> cases.add(ConstraintCase.of(fn, "UUID", "not-a-uuid"));
            case PATTERN -> cases.add(ConstraintCase.of(fn, "Pattern", "!!!INVALID!!!"));
            default -> {
            }
        }
        if (type == String.class && c.maxIntegerDigits() < Integer.MAX_VALUE) {
            cases.add(ConstraintCase.of(fn, "Digits", "1".repeat(c.maxIntegerDigits() + 2)));
        }
        if (type == BigDecimal.class && c.maxIntegerDigits() < Integer.MAX_VALUE) {
            long tooMany = (long) Math.pow(10, c.maxIntegerDigits() + 1);
            cases.add(ConstraintCase.of(fn, "Digits", BigDecimal.valueOf(tooMany)));
        }
        if (type == String.class && c.bounds() instanceof QuantityBounds.DecimalBounds db) {
            if (db.decMax() != null)
                cases.add(ConstraintCase.of(fn, "DecimalMax",
                        db.decMax().add(BigDecimal.TEN).toPlainString()));
            else if (db.decMin() != null)
                cases.add(ConstraintCase.of(fn, "DecimalMin",
                        db.decMin().subtract(BigDecimal.TEN).toPlainString()));
        }
        Long intMin = intMin(c.bounds());
        Long intMax = intMax(c.bounds());
        if (intMin != null && type == String.class && intMin > 0)
            cases.add(ConstraintCase.of(fn, "Size", "x".repeat((int) (intMin - 1))));
        if (intMax != null && type == String.class)
            cases.add(ConstraintCase.of(fn, "Size", "x".repeat((int) (intMax + 1))));
        if (intMin != null && isNumeric(type))
            cases.add(ConstraintCase.of(fn, "Min", castNumeric(type, intMin - 1)));
        if (intMax != null && isNumeric(type))
            cases.add(ConstraintCase.of(fn, "Max", castNumeric(type, intMax + 1)));
        if (c.isPast())
            cases.add(ConstraintCase.of(fn, "Past", futureTemporal(type)));
        if (c.isFuture())
            cases.add(ConstraintCase.of(fn, "Future", pastTemporal(type)));
        if (c.assertTrueRequired())
            cases.add(ConstraintCase.of(fn, "AssertTrue", false));
        if (c.assertFalseRequired())
            cases.add(ConstraintCase.of(fn, "AssertFalse", true));

        return List.copyOf(deduplicate(cases));
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static List<ConstraintCase> deduplicate(List<ConstraintCase> cases) {
        List<ConstraintCase> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (ConstraintCase c : cases)
            if (seen.add(c.constraint() + ":" + c.value())) result.add(c);
        return result;
    }

    private static boolean isNumeric(Class<?> t) {
        return t == Integer.class || t == int.class
                || t == Long.class || t == long.class
                || t == Short.class || t == short.class
                || t == Byte.class || t == byte.class
                || t == Double.class || t == double.class
                || t == Float.class || t == float.class
                || t == BigInteger.class || t == BigDecimal.class;
    }

    private static boolean isList(Class<?> t) {
        return Collection.class.isAssignableFrom(t);
    }

    private static boolean isBoolean(Class<?> t) {
        return t == Boolean.class || t == boolean.class;
    }

    private static Object castNumeric(Class<?> type, long value) {
        if (type == Integer.class || type == int.class) return (int) value;
        if (type == Long.class || type == long.class) return value;
        if (type == Short.class || type == short.class) return (short) value;
        if (type == Byte.class || type == byte.class) return (byte) value;
        if (type == Double.class || type == double.class) return (double) value;
        if (type == Float.class || type == float.class) return (float) value;
        if (type == BigDecimal.class) return BigDecimal.valueOf(value);
        if (type == BigInteger.class) return BigInteger.valueOf(value);
        return value;
    }

    private static Object futureTemporal(Class<?> type) {
        if (type == LocalDate.class)      return com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_DATE;
        if (type == LocalDateTime.class)  return com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_DATE_TIME;
        if (type == ZonedDateTime.class)  return com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_DATE_TIME.atZone(com.paramichha.datafactory.generation.TemporalAnchor.ZONE);
        if (type == OffsetDateTime.class) return com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_DATE_TIME.atOffset(java.time.ZoneOffset.UTC);
        if (type == Date.class)           return Date.from(com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_INSTANT);
        if (type == Year.class)           return com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_YEAR;
        if (type == YearMonth.class)      return com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_YEARMONTH;
        return com.paramichha.datafactory.generation.TemporalAnchor.FUTURE_INSTANT;
    }

    private static Object pastTemporal(Class<?> type) {
        if (type == LocalDate.class)      return com.paramichha.datafactory.generation.TemporalAnchor.PAST_DATE;
        if (type == LocalDateTime.class)  return com.paramichha.datafactory.generation.TemporalAnchor.PAST_DATE_TIME;
        if (type == ZonedDateTime.class)  return com.paramichha.datafactory.generation.TemporalAnchor.PAST_DATE_TIME.atZone(com.paramichha.datafactory.generation.TemporalAnchor.ZONE);
        if (type == OffsetDateTime.class) return com.paramichha.datafactory.generation.TemporalAnchor.PAST_DATE_TIME.atOffset(java.time.ZoneOffset.UTC);
        if (type == Date.class)           return Date.from(com.paramichha.datafactory.generation.TemporalAnchor.PAST_INSTANT);
        if (type == Year.class)           return com.paramichha.datafactory.generation.TemporalAnchor.PAST_YEAR;
        if (type == YearMonth.class)      return com.paramichha.datafactory.generation.TemporalAnchor.PAST_YEARMONTH;
        return com.paramichha.datafactory.generation.TemporalAnchor.PAST_INSTANT;
    }

    private static boolean boundsHasMin(QuantityBounds b) {
        return switch (b) {
            case QuantityBounds.IntegerBounds i -> i.hasMin();
            case QuantityBounds.DecimalBounds d -> d.hasMin();
            case QuantityBounds.Unbounded u -> false;
        };
    }

    private static Long intMin(QuantityBounds b) {
        return switch (b) {
            case QuantityBounds.IntegerBounds i -> i.min();
            case QuantityBounds.DecimalBounds d -> d.decMin() != null ? d.decMin().longValue() : null;
            case QuantityBounds.Unbounded u -> null;
        };
    }

    private static Long intMax(QuantityBounds b) {
        return switch (b) {
            case QuantityBounds.IntegerBounds i -> i.max();
            case QuantityBounds.DecimalBounds d -> d.decMax() != null ? d.decMax().longValue() : null;
            case QuantityBounds.Unbounded u -> null;
        };
    }
}