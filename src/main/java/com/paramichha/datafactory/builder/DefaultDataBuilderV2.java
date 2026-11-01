package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import com.paramichha.datafactory.constraint.AnnotationResolver;
import com.paramichha.datafactory.constraint.ConstraintConflicts;
import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.generation.DataMachine;
import com.paramichha.datafactory.generation.DataMachines;
import com.paramichha.datafactory.instantiation.ObjectAssembler;
import com.paramichha.datafactory.constraint.ConstraintChecker;
import com.paramichha.utils.Seeds;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;

/**
 * V2 implementation of {@link DataBuilderV2}.
 * Single results return {@link DataFactoryResult}, bulk results return {@link DataFactoryResults}.
 * Neither ever throws.
 */
public final class DefaultDataBuilderV2<T> implements DataBuilderV2<T> {

    private final Class<T> type;
    private final Map<String, Object> overrides = new LinkedHashMap<>();
    private GenerationMode mode = GenerationMode.PRODUCTION;
    private Long seed = null;
    private DataMachine machine;

    public DefaultDataBuilderV2(Class<T> type) {
        this.type = type;
    }

    // ── Configuration ─────────────────────────────────────────────────────────

    @Override public DefaultDataBuilderV2<T> with(String fieldName, Object value) { overrides.put(fieldName, value); return this; }
    @Override public DefaultDataBuilderV2<T> withNull(String fieldName)           { return with(fieldName, null); }
    @Override public DefaultDataBuilderV2<T> mode(GenerationMode mode)            { this.mode = Objects.requireNonNull(mode); return this; }
    @Override public DefaultDataBuilderV2<T> seed(long seed)                      { this.seed = seed; this.machine = null; return this; }

    private DataMachine machine() {
        if (machine == null) machine = seed != null ? DataMachines.seeded(seed) : DataMachines.random();
        return machine;
    }

    // ── Single result ─────────────────────────────────────────────────────────

    @Override
    public DataFactoryResult<T> valid() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            if (mode == GenerationMode.PRODUCTION) surfaceProductionWarnings(fields);
            Baseline baseline = baseline(fields, machine());
            if (baseline.failure() != null) return baseline.failure();
            return DataFactoryResult.success(ObjectAssembler.instantiate(type, fields, baseline.values()));
        } catch (Exception e) {
            return failure(e, "Failed to build valid " + type.getSimpleName());
        }
    }

    @Override
    public DataFactoryResult<T> invalid() {
        DataFactoryResults<T> all = invalidList();
        List<DataFactoryResult<T>> succeeded = all.succeeded();
        return succeeded.isEmpty() ? all.failed().get(0) : succeeded.get(0);
    }

    @Override
    public DataFactoryResult<T> invalidFor(String fieldName) {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            FieldDescriptor target = fields.stream()
                    .filter(f -> f.name().equals(fieldName)).findFirst().orElse(null);
            if (target == null) return DataFactoryResult.failure(ErrorType.UNKNOWN_FIELD,
                    "Field '" + fieldName + "' does not exist on " + type.getSimpleName());
            Baseline baseline = baseline(fields, m);
            if (baseline.failure() != null) return baseline.failure();
            List<Object> inv = ValueBuilderFactory.create(target, m).invalidList();
            if (inv.isEmpty()) return DataFactoryResult.failure(ErrorType.INVALID_CONFIGURATION,
                    "Field '" + fieldName + "' on " + type.getSimpleName() + " has no constraint to violate");
            Map<String, Object> values = new LinkedHashMap<>(baseline.values());
            values.put(fieldName, inv.get(0));
            return DataFactoryResult.success(ObjectAssembler.instantiate(type, fields, values));
        } catch (Exception e) {
            return failure(e, "Failed to build invalidFor('" + fieldName + "') on " + type.getSimpleName());
        }
    }

    // ── Bulk result ───────────────────────────────────────────────────────────

    @Override
    public DataFactoryResults<T> validList(int count) {
        if (count <= 0) return DataFactoryResults.of(List.of());
        DataFactoryResult<T> first = valid();
        // A configuration or type failure is the same on every attempt — don't rebuild it n times.
        if (first.isFailure()) return DataFactoryResults.of(Collections.nCopies(count, first));
        List<DataFactoryResult<T>> results = new ArrayList<>(count);
        results.add(first);
        for (int i = 1; i < count; i++) results.add(valid());
        return DataFactoryResults.of(results);
    }

    @Override
    public DataFactoryResults<T> invalidList() {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Baseline baseline = baseline(fields, m);
            if (baseline.failure() != null) return DataFactoryResults.of(List.of(baseline.failure()));
            List<DataFactoryResult<T>> results = new ArrayList<>();
            for (FieldDescriptor field : fields) {
                if (overrides.containsKey(field.name())) continue;
                for (Object invalidValue : invalidCasesFor(field, m).stream().map(ConstraintCase::value).toList()) {
                    Map<String, Object> values = new LinkedHashMap<>(baseline.values());
                    values.put(field.name(), invalidValue);
                    results.add(instantiate(fields, values, "invalid case for field '" + field.name() + "'"));
                }
            }
            if (results.isEmpty()) {
                return DataFactoryResults.of(List.of(DataFactoryResult.failure(ErrorType.INVALID_CONFIGURATION,
                        "No constraints found to violate on " + type.getSimpleName())));
            }
            return DataFactoryResults.of(results);
        } catch (Exception e) {
            return DataFactoryResults.of(List.of(failure(e, "Failed to build invalidList for " + type.getSimpleName())));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>When the class itself cannot be built, every field maps to that same failure.
     * Only when field extraction itself fails is the single key {@code "_error"} used.
     */
    @Override
    public Map<String, DataFactoryResults<T>> validPerField() {
        List<FieldDescriptor> fields;
        try {
            fields = FieldDescriptor.extract(type);
        } catch (Exception e) {
            return Map.of("_error", DataFactoryResults.of(List.of(
                    failure(e, "Failed to read fields of " + type.getSimpleName()))));
        }
        try {
            DataMachine m = machine();
            Baseline baseline = baseline(fields, m);
            Map<String, DataFactoryResults<T>> result = new LinkedHashMap<>();
            for (FieldDescriptor field : fields) {
                if (overrides.containsKey(field.name())) continue;
                if (field.rawType() == Boolean.class || field.rawType() == boolean.class) continue;
                if (field.isComposite()) continue;
                if (baseline.failure() != null) {
                    result.put(field.name(), DataFactoryResults.of(List.of(baseline.failure())));
                    continue;
                }
                List<Object> allValidValues = ValueBuilderFactory.create(field, m).validList();
                if (allValidValues.size() <= 1) continue;
                List<DataFactoryResult<T>> variants = new ArrayList<>();
                for (Object value : allValidValues) {
                    Map<String, Object> variantValues = new LinkedHashMap<>(baseline.values());
                    variantValues.put(field.name(), value);
                    variants.add(instantiate(fields, variantValues, "variant for field '" + field.name() + "'"));
                }
                result.put(field.name(), DataFactoryResults.of(variants));
            }
            return Collections.unmodifiableMap(result);
        } catch (Exception e) {
            DataFactoryResults<T> f = DataFactoryResults.of(List.of(
                    failure(e, "Failed to build validPerField for " + type.getSimpleName())));
            Map<String, DataFactoryResults<T>> result = new LinkedHashMap<>();
            for (FieldDescriptor field : fields) result.put(field.name(), f);
            return Collections.unmodifiableMap(result);
        }
    }

    // ── Per-field ─────────────────────────────────────────────────────────────

    @Override
    public Map<String, DataFactoryResult<FieldCases>> fields() {
        List<FieldDescriptor> fields;
        try {
            fields = FieldDescriptor.extract(type);
        } catch (Exception e) {
            return Map.of("_error", failure(e, "Failed to read fields of " + type.getSimpleName()));
        }
        Map<String, DataFactoryResult<FieldCases>> result = new LinkedHashMap<>();
        for (FieldDescriptor field : fields) result.put(field.name(), fieldCases(field));
        return Collections.unmodifiableMap(result);
    }

    private DataFactoryResult<FieldCases> fieldCases(FieldDescriptor field) {
        try {
            if (!overrides.containsKey(field.name())) {
                List<String> conflicts = ConstraintConflicts.find(analyze(field));
                if (!conflicts.isEmpty()) {
                    return DataFactoryResult.failure(ErrorType.INVALID_CONFIGURATION, String.join("; ", conflicts));
                }
            }
            // Each field gets its own machine so its values don't depend on the other fields.
            DataMachine m = seed != null ? DataMachines.seeded(seed + Seeds.stableHash(field.name())) : DataMachines.random();
            List<Object> valid;
            if (overrides.containsKey(field.name())) {
                valid = Collections.singletonList(overrides.get(field.name()));
            } else if (isSizedCollection(field)) {
                valid = collectionBoundaries(field, m);
            } else if (field.isComposite() || field.isArray() || field.rawType() == Optional.class) {
                valid = Collections.singletonList(resolveField(field, m));
            } else {
                valid = ValueBuilderFactory.create(field, m).validList();
            }
            FieldValueBuilder<?> fvb = (FieldValueBuilder<?>) ValueBuilderFactory.create(field, m);
            List<ConstraintCase> invalid = overrides.containsKey(field.name())
                    ? new ArrayList<>(fvb.invalidCases())
                    : invalidCasesFor(field, m);
            // A nested object's inner rules belong to its own fields(); keep only the
            // field-level null case here (e.g. @NotNull on the reference itself).
            if (field.isNested()) invalid.removeIf(c -> c.value() != null);
            return DataFactoryResult.success(new FieldCases(field.name(), field.rawType(), valid, invalid));
        } catch (Exception e) {
            return failure(e, "Failed to generate field '" + field.name() + "' on " + type.getSimpleName());
        }
    }

    // ── Classification ────────────────────────────────────────────────────────

    private static <R> DataFactoryResult<R> failure(Exception e, String context) {
        ErrorType typed = DataFactoryException.errorTypeInChain(e);
        ErrorType errorType = typed != null ? typed
                : e instanceof DataFactoryException dfe ? classifyByMessage(dfe) : ErrorType.INTERNAL_ERROR;
        String detail = rootMessage(e);
        return DataFactoryResult.failure(errorType, detail == null ? context : context + ": " + detail);
    }

    private static String rootMessage(Throwable t) {
        String msg = t.getMessage();
        for (Throwable c = t.getCause(); c != null && c != c.getCause(); c = c.getCause()) {
            if (c.getMessage() != null) msg = c.getMessage();
        }
        return msg;
    }

    /** Fallback for untyped exceptions — kept until every throw site carries an ErrorType. */
    private static ErrorType classifyByMessage(DataFactoryException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("No DataCreator found"))                   return ErrorType.UNSUPPORTED_CLASS_TYPE;
        if (msg.contains("not supported"))                          return ErrorType.UNSUPPORTED_ANNOTATION;
        if (msg.contains("No instantiation strategy") || msg.contains("Cannot instantiate")) return ErrorType.NOT_INSTANTIABLE;
        return ErrorType.INTERNAL_ERROR;
    }

    // ── Internal resolution ───────────────────────────────────────────────────

    private void surfaceProductionWarnings(List<FieldDescriptor> fields) {
        for (FieldDescriptor field : fields) ConstraintChecker.warn(field.name(), field.rawType(), field.validations());
    }

    private static final ThreadLocal<Set<Class<?>>> BUILDING = ThreadLocal.withInitial(HashSet::new);

    /**
     * Valid values for every field, or the failure that prevents a valid instance.
     * Conflicts are checked first for all fields; then each field is resolved on its
     * own so the failure names every field that blocks the object, not just the first.
     */
    private record Baseline(Map<String, Object> values, DataFactoryResult failure) {}

    @SuppressWarnings("unchecked")
    private Baseline baseline(List<FieldDescriptor> fields, DataMachine m) {
        List<String> conflicts = new ArrayList<>();
        for (FieldDescriptor field : fields) {
            if (overrides.containsKey(field.name())) continue;
            conflicts.addAll(ConstraintConflicts.find(analyze(field)));
        }
        if (!conflicts.isEmpty()) {
            return new Baseline(Map.of(), DataFactoryResult.failure(ErrorType.INVALID_CONFIGURATION,
                    type.getSimpleName() + " has impossible constraints — " + String.join("; ", conflicts)));
        }
        Map<String, Object> values = new LinkedHashMap<>();
        List<String> failedFields = new ArrayList<>();
        DataFactoryResult firstFailure = null;
        for (FieldDescriptor field : fields) {
            try {
                values.put(field.name(), resolveField(field, m));
            } catch (Exception e) {
                DataFactoryResult f = failure(e, "field '" + field.name() + "'");
                if (firstFailure == null) firstFailure = f;
                failedFields.add(f.reason());
            }
        }
        if (firstFailure != null) {
            return new Baseline(values, DataFactoryResult.failure(firstFailure.errorType(),
                    "Cannot build " + type.getSimpleName() + " — " + String.join("; ", failedFields)));
        }
        return new Baseline(values, null);
    }

    private static FieldConstraints analyze(FieldDescriptor field) {
        return AnnotationResolver.analyze(field.name(), field.rawType(), field.validations());
    }

    private DataFactoryResult<T> instantiate(List<FieldDescriptor> fields, Map<String, Object> values, String what) {
        try {
            return DataFactoryResult.success(ObjectAssembler.instantiate(type, fields, values));
        } catch (Exception e) {
            return failure(e, "Failed to build " + what + " on " + type.getSimpleName());
        }
    }

    private Object resolveField(FieldDescriptor field, DataMachine m) {
        if (overrides.containsKey(field.name())) return overrides.get(field.name());
        if (field.rawType() == java.util.Optional.class) return resolveOptional(field, m);
        if (field.isNested() && field.hasAnnotation(Valid.class))   return resolveNested(field);
        if (field.isNested() && !field.isList() && field.hasAnnotation(NotNull.class)) return resolveNested(field);
        if (field.isNested() && !field.isList() && mode == GenerationMode.PRODUCTION)  return resolveNested(field);
        if (field.isList())  return resolveList(field, m);
        if (field.isSet())   return resolveSet(field, m);
        if (field.isMap())   return resolveMap(field, m);
        if (field.isArray()) return resolveArray(field, m);
        if (field.isQueue()) return resolveQueue(field, m);
        List<Object> validList = ValueBuilderFactory.create(field, m).validList();
        return validList.isEmpty() ? null : validList.get(0);
    }

    private Object resolveNested(FieldDescriptor field) {
        if (BUILDING.get().add(field.rawType())) {
            try {
                DefaultDataBuilderV2<?> nested = new DefaultDataBuilderV2<>(field.rawType()).mode(mode);
                if (seed != null) nested.seed(seed + Seeds.stableHash(field.name()));
                DataFactoryResult<?> r = nested.valid();
                if (r.isFailure()) throw new DataFactoryException(r.errorType(), r.reason());
                return r.get();
            } finally {
                BUILDING.get().remove(field.rawType());
            }
        }
        return null;
    }

    private Object resolveOptional(FieldDescriptor field, DataMachine m) {
        Class<?> elemType = field.listElementType();
        if (!elemType.getPackageName().startsWith("java.")) {
            if (BUILDING.get().contains(elemType)) return Optional.empty();
            DefaultDataBuilderV2<?> ob = new DefaultDataBuilderV2<>(elemType).mode(mode);
            if (seed != null) ob.seed(seed);
            DataFactoryResult<?> r = ob.valid();
            return r.isSuccess() ? Optional.of(r.get()) : Optional.empty();
        }
        List<?> elemValid = ValueBuilderFactory.create(elemType, m).validList();
        Object elem = elemValid.isEmpty() ? null : elemValid.get(0);
        return elem != null ? Optional.of(elem) : Optional.empty();
    }

    private Object resolveList(FieldDescriptor field, DataMachine m)  { return resolveCollection(field, m); }
    private Object resolveSet(FieldDescriptor field, DataMachine m)   { return resolveCollection(field, m); }
    private Object resolveQueue(FieldDescriptor field, DataMachine m) { return resolveCollection(field, m); }

    // ── Collection sizing ─────────────────────────────────────────────────────

    /** Largest collection DataFactory will generate for a size boundary. */
    private static final int MAX_GENERATED_SIZE = 100;

    private static boolean isSizedCollection(FieldDescriptor field) {
        return field.isList() || field.isSet() || field.isQueue();
    }

    private static Long sizeMin(FieldDescriptor field) {
        Long lo = analyze(field).bounds().min();
        boolean notEmpty = field.hasAnnotation(NotEmpty.class);
        if (notEmpty && (lo == null || lo < 1)) return 1L;
        return lo;
    }

    private static Long sizeMax(FieldDescriptor field) {
        return analyze(field).bounds().max();
    }

    /** Size used for the canonical valid value: one element, moved inside any @Size range. */
    private static int typicalSize(FieldDescriptor field) {
        long size = 1;
        Long lo = sizeMin(field), hi = sizeMax(field);
        if (lo != null && size < lo) size = lo;
        if (hi != null && size > hi) size = hi;
        return (int) Math.min(Math.max(size, 0), MAX_GENERATED_SIZE);
    }

    /** Canonical valid collection — always the first entry of {@link #collectionBoundaries}. */
    private Object resolveCollection(FieldDescriptor field, DataMachine m) {
        Object c = buildCollection(field, typicalSize(field), m);
        // Element type can't be generated (e.g. Object) — fall back to an empty container.
        return c != null ? c : buildCollection(field, 0, m);
    }

    /** Valid collections at the typical size and at each declared @Size / @NotEmpty boundary. */
    private List<Object> collectionBoundaries(FieldDescriptor field, DataMachine m) {
        LinkedHashSet<Integer> sizes = new LinkedHashSet<>();
        sizes.add(typicalSize(field));
        Long lo = sizeMin(field), hi = sizeMax(field);
        if (lo != null && lo <= MAX_GENERATED_SIZE) sizes.add(lo.intValue());
        if (hi != null && hi <= MAX_GENERATED_SIZE) sizes.add(hi.intValue());
        List<Object> values = new ArrayList<>();
        for (int size : sizes) {
            Object c = size == typicalSize(field) ? resolveCollection(field, m) : buildCollection(field, size, m);
            if (c != null) values.add(c);
        }
        return values;
    }

    /**
     * Invalid cases for a field: one per annotation, plus collection @Size/@NotEmpty
     * cases. Type-default probes (e.g. null for an unannotated field) are used only
     * when the field has no annotation-driven case at all.
     */
    private List<ConstraintCase> invalidCasesFor(FieldDescriptor field, DataMachine m) {
        FieldValueBuilder<?> fvb = (FieldValueBuilder<?>) ValueBuilderFactory.create(field, m);
        List<ConstraintCase> collection = collectionInvalidCases(field, m);
        if (collection.isEmpty()) return new ArrayList<>(fvb.invalidCases());
        List<ConstraintCase> cases = new ArrayList<>(fvb.annotationCases());
        cases.addAll(collection);
        return cases;
    }

    /** Invalid collections: one element too few / too many for @Size, and empty for @NotEmpty. */
    private List<ConstraintCase> collectionInvalidCases(FieldDescriptor field, DataMachine m) {
        if (!isSizedCollection(field)) return List.of();
        List<ConstraintCase> cases = new ArrayList<>();
        Long lo = analyze(field).bounds().min(), hi = sizeMax(field);
        if (lo != null && lo > 0 && lo - 1 <= MAX_GENERATED_SIZE) {
            Object c = buildCollection(field, (int) (lo - 1), m);
            if (c != null) cases.add(ConstraintCase.of(field.name(), "Size", c));
        }
        if (hi != null && hi + 1 <= MAX_GENERATED_SIZE) {
            Object c = buildCollection(field, (int) (hi + 1), m);
            if (c != null) cases.add(ConstraintCase.of(field.name(), "Size", c));
        }
        if (field.hasAnnotation(NotEmpty.class)) {
            cases.add(ConstraintCase.of(field.name(), "NotEmpty", buildCollection(field, 0, m)));
        }
        return cases;
    }

    /**
     * A collection of the field's container type with {@code size} valid elements,
     * or {@code null} when that many elements can't be produced (e.g. a Set that needs
     * more distinct values than the element type offers, or an ungeneratable element).
     */
    private Object buildCollection(FieldDescriptor field, int size, DataMachine m) {
        List<Object> elements = new ArrayList<>(size);
        Class<?> elemType = field.listElementType();
        if (size == 1) {
            Object e = resolveElement(elemType, field.name(), m);
            if (e == null) return null;
            elements.add(e);
        } else if (size > 1) {
            boolean scalar = elemType.getPackageName().startsWith("java.") || elemType.isEnum();
            if (scalar) {
                if (elemType == Object.class) return null;
                LinkedHashSet<Object> distinct = new LinkedHashSet<>();
                for (int attempt = 0; attempt < size * 10 && distinct.size() < size; attempt++) {
                    for (Object v : ValueBuilderFactory.create(elemType, m).stream(1)) {
                        if (v == null) continue;
                        if (field.isSet()) distinct.add(v); else elements.add(v);
                    }
                    if (!field.isSet() && elements.size() >= size) break;
                }
                if (field.isSet()) elements.addAll(distinct);
                if (elements.size() < size) return null;
                elements = new ArrayList<>(elements.subList(0, size));
            } else {
                Object e = resolveElement(elemType, field.name(), m);
                if (e == null || field.isSet()) return null; // equal nested elements collapse in a Set
                for (int i = 0; i < size; i++) elements.add(e);
            }
        }
        if (field.isSet())   return new LinkedHashSet<>(elements);
        if (field.isQueue()) return new ArrayDeque<>(elements);
        return Collections.unmodifiableList(elements);
    }

    private Object resolveMap(FieldDescriptor field, DataMachine m) {
        Object key   = resolveElement(field.mapKeyType(),   field.name() + ".key",   m);
        Object value = resolveElement(field.mapValueType(), field.name() + ".value", m);
        if (key == null || value == null) return new LinkedHashMap<>();
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private Object resolveArray(FieldDescriptor field, DataMachine m) {
        Class<?> elemType = field.rawType().getComponentType();
        Object elem = resolveElement(elemType, field.name(), m);
        Object arr = Array.newInstance(elemType, 1);
        if (elem != null) {
            try { Array.set(arr, 0, elem); }
            catch (IllegalArgumentException e) {
                throw new DataFactoryException("Failed to set element of type '" + elem.getClass().getName()
                        + "' into array '" + field.name() + "' of component type '" + elemType.getName() + "'", e);
            }
        }
        return arr;
    }

    private Object resolveElement(Class<?> elemType, String fieldContext, DataMachine m) {
        if (!elemType.getPackageName().startsWith("java.") && !elemType.isEnum()) {
            if (BUILDING.get().contains(elemType)) return null;
            DefaultDataBuilderV2<?> eb = new DefaultDataBuilderV2<>(elemType).mode(mode);
            if (seed != null) eb.seed(seed);
            DataFactoryResult<?> r = eb.valid();
            if (r.isFailure()) throw new DataFactoryException(r.errorType(),
                    "Failed to build element of type '" + elemType.getName()
                    + "' for field '" + fieldContext + "': " + r.reason());
            return r.get();
        }
        if (elemType == Object.class) return null; // no generator for an untyped element
        List<?> values = ValueBuilderFactory.create(elemType, m).validList();
        return values.isEmpty() ? null : values.get(0);
    }
}
