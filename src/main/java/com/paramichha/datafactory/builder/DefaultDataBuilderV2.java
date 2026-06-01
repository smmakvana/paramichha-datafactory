package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.generation.DataMachine;
import com.paramichha.datafactory.generation.DataMachines;
import com.paramichha.datafactory.instantiation.ObjectAssembler;
import com.paramichha.datafactory.constraint.ConstraintChecker;
import com.paramichha.utils.Seeds;
import jakarta.validation.Valid;
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
            T value = ObjectAssembler.instantiate(type, fields, resolveAllValid(fields, machine()));
            return DataFactoryResult.success(value);
        } catch (DataFactoryException e) {
            return DataFactoryResult.failure(classify(e), e.getMessage());
        } catch (Exception e) {
            return DataFactoryResult.failure(ErrorType.INTERNAL_ERROR,
                    "Failed to build valid " + type.getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public DataFactoryResult<T> invalid() {
        DataFactoryResults<T> all = invalidList();
        if (all.isFailure()) return all.failed().get(0);
        List<DataFactoryResult<T>> succeeded = all.succeeded();
        if (succeeded.isEmpty()) return DataFactoryResult.failure(ErrorType.INVALID_CONFIGURATION,
                "No constraints found to violate on " + type.getSimpleName());
        return succeeded.get(0);
    }

    @Override
    public DataFactoryResult<T> invalidFor(String fieldName) {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            boolean fieldExists = fields.stream().anyMatch(f -> f.name().equals(fieldName));
            if (!fieldExists) return DataFactoryResult.failure(ErrorType.INTERNAL_ERROR,
                    "Field '" + fieldName + "' does not exist on " + type.getSimpleName());
            Map<String, Object> values = resolveAllValid(fields, m);
            fields.stream()
                    .filter(f -> f.name().equals(fieldName))
                    .findFirst()
                    .ifPresent(f -> {
                        List<Object> inv = ValueBuilderFactory.create(f, m).invalidList();
                        if (!inv.isEmpty()) values.put(fieldName, inv.get(0));
                    });
            return DataFactoryResult.success(ObjectAssembler.instantiate(type, fields, values));
        } catch (DataFactoryException e) {
            return DataFactoryResult.failure(classify(e), e.getMessage());
        } catch (Exception e) {
            return DataFactoryResult.failure(ErrorType.INTERNAL_ERROR,
                    "Failed to build invalidFor('" + fieldName + "') on " + type.getSimpleName() + ": " + e.getMessage());
        }
    }

    // ── Bulk result ───────────────────────────────────────────────────────────

    @Override
    public DataFactoryResults<T> validList(int count) {
        List<DataFactoryResult<T>> results = IntStream.range(0, count)
                .mapToObj(i -> valid())
                .toList();
        return DataFactoryResults.of(results);
    }

    @Override
    public DataFactoryResults<T> invalidList() {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> validValues = resolveAllValid(fields, m);
            List<DataFactoryResult<T>> results = new ArrayList<>();
            for (FieldDescriptor field : fields) {
                for (Object invalidValue : ValueBuilderFactory.create(field, m).invalidList()) {
                    try {
                        Map<String, Object> values = new LinkedHashMap<>(validValues);
                        values.put(field.name(), invalidValue);
                        T built = ObjectAssembler.instantiate(type, fields, values);
                        results.add(DataFactoryResult.success(built));
                    } catch (DataFactoryException e) {
                        results.add(DataFactoryResult.failure(classify(e), e.getMessage()));
                    } catch (Exception e) {
                        results.add(DataFactoryResult.failure(ErrorType.INTERNAL_ERROR, e.getMessage()));
                    }
                }
            }
            if (results.isEmpty()) {
                return DataFactoryResults.of(List.of(
                        DataFactoryResult.failure(ErrorType.INVALID_CONFIGURATION,
                                "No constraints found to violate on " + type.getSimpleName())));
            }
            return DataFactoryResults.of(results);
        } catch (DataFactoryException e) {
            return DataFactoryResults.of(List.of(DataFactoryResult.failure(classify(e), e.getMessage())));
        } catch (Exception e) {
            return DataFactoryResults.of(List.of(DataFactoryResult.failure(ErrorType.INTERNAL_ERROR,
                    "Failed to build invalidList for " + type.getSimpleName() + ": " + e.getMessage())));
        }
    }

    @Override
    public Map<String, DataFactoryResults<T>> validPerField() {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> semanticValues = resolveAllValid(fields, m);
            Map<String, DataFactoryResults<T>> result = new LinkedHashMap<>();
            for (FieldDescriptor field : fields) {
                if (overrides.containsKey(field.name())) continue;
                if (field.rawType() == Boolean.class || field.rawType() == boolean.class) continue;
                List<Object> allValidValues = ValueBuilderFactory.create(field, m).validList();
                if (allValidValues.size() <= 1) continue;
                List<DataFactoryResult<T>> variants = new ArrayList<>();
                for (Object value : allValidValues) {
                    try {
                        Map<String, Object> variantValues = new LinkedHashMap<>(semanticValues);
                        variantValues.put(field.name(), value);
                        T built = ObjectAssembler.instantiate(type, fields, variantValues);
                        variants.add(DataFactoryResult.success(built));
                    } catch (DataFactoryException e) {
                        variants.add(DataFactoryResult.failure(classify(e), e.getMessage()));
                    } catch (Exception e) {
                        variants.add(DataFactoryResult.failure(ErrorType.INTERNAL_ERROR, e.getMessage()));
                    }
                }
                result.put(field.name(), DataFactoryResults.of(variants));
            }
            return Collections.unmodifiableMap(result);
        } catch (DataFactoryException e) {
            return Map.of("_error", DataFactoryResults.of(
                    List.of(DataFactoryResult.failure(classify(e), e.getMessage()))));
        } catch (Exception e) {
            return Map.of("_error", DataFactoryResults.of(
                    List.of(DataFactoryResult.failure(ErrorType.INTERNAL_ERROR,
                            "Failed to build validPerField for " + type.getSimpleName() + ": " + e.getMessage()))));
        }
    }

    // ── Classification ────────────────────────────────────────────────────────

    private static ErrorType classify(DataFactoryException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("No DataCreator found"))                   return ErrorType.UNSUPPORTED_CLASS_TYPE;
        if (msg.contains("not supported"))                          return ErrorType.UNSUPPORTED_ANNOTATION;
        if (msg.contains("impossible range") || msg.contains("conflict")) return ErrorType.INVALID_CONFIGURATION;
        if (msg.contains("No instantiation strategy") || msg.contains("Cannot instantiate")) return ErrorType.NOT_INSTANTIABLE;
        return ErrorType.INTERNAL_ERROR;
    }

    // ── Internal resolution ───────────────────────────────────────────────────

    private void surfaceProductionWarnings(List<FieldDescriptor> fields) {
        for (FieldDescriptor field : fields) ConstraintChecker.warn(field.name(), field.rawType(), field.validations());
    }

    private static final ThreadLocal<Set<Class<?>>> BUILDING = ThreadLocal.withInitial(HashSet::new);

    private Map<String, Object> resolveAllValid(List<FieldDescriptor> fields, DataMachine m) throws Exception {
        Map<String, Object> values = new LinkedHashMap<>();
        for (FieldDescriptor field : fields) {
            if (overrides.containsKey(field.name())) { values.put(field.name(), overrides.get(field.name())); continue; }
            if (field.rawType() == java.util.Optional.class) { values.put(field.name(), resolveOptional(field, m)); continue; }
            if (field.isNested() && field.hasAnnotation(Valid.class))   { values.put(field.name(), resolveNested(field)); continue; }
            if (field.isNested() && !field.isList() && field.hasAnnotation(NotNull.class)) { values.put(field.name(), resolveNested(field)); continue; }
            if (field.isNested() && !field.isList() && mode == GenerationMode.PRODUCTION)  { values.put(field.name(), resolveNested(field)); continue; }
            if (field.isList())  { values.put(field.name(), resolveList(field, m));  continue; }
            if (field.isSet())   { values.put(field.name(), resolveSet(field, m));   continue; }
            if (field.isMap())   { values.put(field.name(), resolveMap(field, m));   continue; }
            if (field.isArray()) { values.put(field.name(), resolveArray(field, m)); continue; }
            if (field.isQueue()) { values.put(field.name(), resolveQueue(field, m)); continue; }
            List<Object> validList = ValueBuilderFactory.create(field, m).validList();
            values.put(field.name(), validList.isEmpty() ? null : validList.get(0));
        }
        return values;
    }

    private Object resolveNested(FieldDescriptor field) {
        if (BUILDING.get().add(field.rawType())) {
            try {
                DefaultDataBuilderV2<?> nested = new DefaultDataBuilderV2<>(field.rawType()).mode(mode);
                if (seed != null) nested.seed(seed + Seeds.stableHash(field.name()));
                DataFactoryResult<?> r = nested.valid();
                if (r.isFailure()) throw new DataFactoryException(r.reason());
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

    private Object resolveList(FieldDescriptor field, DataMachine m)  { Object e = resolveElement(field.listElementType(), field.name(), m); return e != null ? List.of(e) : List.of(); }
    private Object resolveSet(FieldDescriptor field, DataMachine m)   { Object e = resolveElement(field.listElementType(), field.name(), m); return e != null ? new LinkedHashSet<>(List.of(e)) : new LinkedHashSet<>(); }
    private Object resolveQueue(FieldDescriptor field, DataMachine m) { Object e = resolveElement(field.listElementType(), field.name(), m); ArrayDeque<Object> q = new ArrayDeque<>(); if (e != null) q.add(e); return q; }

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
            if (r.isFailure()) throw new DataFactoryException(
                    "Failed to build element of type '" + elemType.getName()
                    + "' for field '" + fieldContext + "': " + r.reason());
            return r.get();
        }
        List<?> values = ValueBuilderFactory.create(elemType, m).validList();
        return values.isEmpty() ? null : values.get(0);
    }
}
