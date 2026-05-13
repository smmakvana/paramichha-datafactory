package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.DataBuilder;
import com.paramichha.datafactory.DataFactoryException;
import com.paramichha.datafactory.GenerationMode;
import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.generation.DataMachine;
import com.paramichha.datafactory.generation.DataMachines;
import com.paramichha.datafactory.instantiation.ObjectAssembler;
import com.paramichha.datafactory.constraint.ConstraintChecker;
import com.paramichha.utils.Seeds;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.*;
import java.lang.reflect.Array;
import java.util.stream.IntStream;

public class DefaultDataBuilder<T> implements DataBuilder<T> {

    private final Class<T> type;
    private final Map<String, Object> overrides = new LinkedHashMap<>();
    private GenerationMode mode = GenerationMode.PRODUCTION;
    private Long seed = null;

    // One machine per builder — created once, reused across all build calls.
    // Advances internal state with each call so successive objects vary naturally.
    private DataMachine machine;

    public DefaultDataBuilder(Class<T> type) {
        this.type = type;
    }

    // ── Configuration ─────────────────────────────────────────────────────

    @Override
    public DefaultDataBuilder<T> with(String fieldName, Object value) {
        overrides.put(fieldName, value);
        return this;
    }

    @Override
    public DefaultDataBuilder<T> withNull(String fieldName) {
        return with(fieldName, null);
    }

    @Override
    public DefaultDataBuilder<T> mode(GenerationMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        return this;
    }

    @Override
    public DefaultDataBuilder<T> seed(long seed) {
        this.seed = seed;
        this.machine = null; // reset so machine() recreates with new seed
        return this;
    }

    // ── Machine ───────────────────────────────────────────────────────────

    private DataMachine machine() {
        if (machine == null) {
            machine = seed != null ? DataMachines.seeded(seed) : DataMachines.random();
        }
        return machine;
    }

    // ── Public API ────────────────────────────────────────────────────────

    @Override
    public T valid() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            if (mode == GenerationMode.PRODUCTION) surfaceProductionWarnings(fields);
            return ObjectAssembler.instantiate(type, fields, resolveAllValid(fields, machine()));
        } catch (Exception e) {
            throw new DataFactoryException("Failed to build valid " + type.getSimpleName(), e);
        }
    }

    @Override
    public Optional<T> invalid() {
        List<T> all = invalidList();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    @Override
    public List<T> validList(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> valid())
                .toList();
    }

    @Override
    public List<T> invalidList() {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> validValues = resolveAllValid(fields, m);
            List<T> result = new ArrayList<>();
            for (FieldDescriptor field : fields) {
                for (Object invalidValue : ValueBuilderFactory.create(field, m).invalidList()) {
                    Map<String, Object> values = new LinkedHashMap<>(validValues);
                    values.put(field.name(), invalidValue);
                    result.add(ObjectAssembler.instantiate(type, fields, values));
                }
            }
            return List.copyOf(result);
        } catch (Exception e) {
            throw new DataFactoryException("Failed to build invalidList for " + type.getSimpleName(), e);
        }
    }

    @Override
    public T invalidFor(String fieldName) {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> values = resolveAllValid(fields, m);
            fields.stream()
                    .filter(f -> f.name().equals(fieldName))
                    .findFirst()
                    .ifPresent(f -> {
                        List<Object> inv = ValueBuilderFactory.create(f, m).invalidList();
                        if (!inv.isEmpty()) values.put(fieldName, inv.get(0));
                    });
            return ObjectAssembler.instantiate(type, fields, values);
        } catch (Exception e) {
            throw new DataFactoryException(
                    "Failed to build invalidFor('" + fieldName + "') on " + type.getSimpleName(), e);
        }
    }

    @Override
    public Map<String, List<T>> validPerField() {
        try {
            DataMachine m = machine();
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> semanticValues = resolveAllValid(fields, m);
            Map<String, List<T>> result = new LinkedHashMap<>();
            for (FieldDescriptor field : fields) {
                if (overrides.containsKey(field.name())) continue;
                if (field.rawType() == Boolean.class || field.rawType() == boolean.class) continue;
                List<Object> allValidValues = ValueBuilderFactory.create(field, m).validList();
                if (allValidValues.size() <= 1) continue;
                List<T> variants = new ArrayList<>();
                for (Object value : allValidValues) {
                    Map<String, Object> variantValues = new LinkedHashMap<>(semanticValues);
                    variantValues.put(field.name(), value);
                    variants.add(ObjectAssembler.instantiate(type, fields, variantValues));
                }
                result.put(field.name(), List.copyOf(variants));
            }
            return Collections.unmodifiableMap(result);
        } catch (Exception e) {
            throw new DataFactoryException("Failed to build validPerField for " + type.getSimpleName(), e);
        }
    }

    // ── Internal resolution ───────────────────────────────────────────────

    private void surfaceProductionWarnings(List<FieldDescriptor> fields) {
        for (FieldDescriptor field : fields) {
            ConstraintChecker.warn(field.name(), field.rawType(), field.validations());
        }
    }

    private static final ThreadLocal<Set<Class<?>>> BUILDING =
            ThreadLocal.withInitial(HashSet::new);

    private Map<String, Object> resolveAllValid(List<FieldDescriptor> fields, DataMachine m) throws Exception {
        Map<String, Object> values = new LinkedHashMap<>();
        for (FieldDescriptor field : fields) {
            if (overrides.containsKey(field.name())) {
                values.put(field.name(), overrides.get(field.name()));
                continue;
            }
            if (field.rawType() == java.util.Optional.class) {
                values.put(field.name(), resolveOptional(field, m));
                continue;
            }
            if (field.isNested() && field.hasAnnotation(Valid.class)) {
                values.put(field.name(), resolveNested(field));
                continue;
            }
            if (field.isNested() && !field.isList() && field.hasAnnotation(NotNull.class)) {
                values.put(field.name(), resolveNested(field));
                continue;
            }
            if (field.isNested() && !field.isList() && mode == GenerationMode.PRODUCTION) {
                values.put(field.name(), resolveNested(field));
                continue;
            }
            if (field.isList()) {
                values.put(field.name(), resolveList(field, m));
                continue;
            }
            if (field.isSet()) {
                values.put(field.name(), resolveSet(field, m));
                continue;
            }
            if (field.isMap()) {
                values.put(field.name(), resolveMap(field, m));
                continue;
            }
            if (field.isArray()) {
                values.put(field.name(), resolveArray(field, m));
                continue;
            }
            if (field.isQueue()) {
                values.put(field.name(), resolveQueue(field, m));
                continue;
            }
            List<Object> validList = ValueBuilderFactory.create(field, m).validList();
            values.put(field.name(), validList.isEmpty() ? null : validList.get(0));
        }
        return values;
    }

    private Object resolveNested(FieldDescriptor field) {
        if (BUILDING.get().add(field.rawType())) {
            try {
                DefaultDataBuilder<?> nested = new DefaultDataBuilder<>(field.rawType()).mode(mode);
                if (seed != null) nested.seed(seed + Seeds.stableHash(field.name()));
                return nested.valid();
            } catch (Exception e) {
                return null;
            } finally {
                BUILDING.get().remove(field.rawType());
            }
        }
        return null;
    }

    private Object resolveOptional(FieldDescriptor field, DataMachine m) {
        try {
            Class<?> elemType = field.listElementType();
            if (!elemType.getPackageName().startsWith("java.")) {
                if (BUILDING.get().contains(elemType)) return Optional.empty();
                DefaultDataBuilder<?> ob = new DefaultDataBuilder<>(elemType).mode(mode);
                if (seed != null) ob.seed(seed);
                return Optional.of(ob.valid());
            }
            List<?> elemValid = ValueBuilderFactory.create(elemType, m).validList();
            Object elem = elemValid.isEmpty() ? null : elemValid.get(0);
            return elem != null ? Optional.of(elem) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    private Object resolveSet(FieldDescriptor field, DataMachine m) {
        try {
            Class<?> elemType = field.listElementType();
            Object elem = resolveElement(elemType, m);
            return elem != null ? new LinkedHashSet<>(List.of(elem)) : new LinkedHashSet<>();
        } catch (Exception e) {
            return new LinkedHashSet<>();
        }
    }

    private Object resolveMap(FieldDescriptor field, DataMachine m) {
        try {
            Class<?> keyType   = field.mapKeyType();
            Class<?> valueType = field.mapValueType();
            Object key   = resolveElement(keyType, m);
            Object value = resolveElement(valueType, m);
            if (key == null || value == null) return new LinkedHashMap<>();
            LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
            map.put(key, value);
            return map;
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Object resolveArray(FieldDescriptor field, DataMachine m) {
        try {
            Class<?> elemType = field.rawType().getComponentType();
            Object elem = resolveElement(elemType, m);
            Object arr = Array.newInstance(elemType, 1);
            if (elem != null) Array.set(arr, 0, elem);
            return arr;
        } catch (Exception e) {
            return Array.newInstance(field.rawType().getComponentType(), 0);
        }
    }

    private Object resolveQueue(FieldDescriptor field, DataMachine m) {
        try {
            Class<?> elemType = field.listElementType();
            Object elem = resolveElement(elemType, m);
            ArrayDeque<Object> queue = new ArrayDeque<>();
            if (elem != null) queue.add(elem);
            return queue;
        } catch (Exception e) {
            return new ArrayDeque<>();
        }
    }

    /** Resolves a single element — handles domain types, java types, and enums. */
    private Object resolveElement(Class<?> elemType, DataMachine m) {
        try {
            if (!elemType.getPackageName().startsWith("java.") && !elemType.isEnum()) {
                if (BUILDING.get().contains(elemType)) return null;
                DefaultDataBuilder<?> eb = new DefaultDataBuilder<>(elemType).mode(mode);
                if (seed != null) eb.seed(seed);
                return eb.valid();
            }
            List<?> values = ValueBuilderFactory.create(elemType, m).validList();
            return values.isEmpty() ? null : values.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private Object resolveList(FieldDescriptor field, DataMachine m) {
        try {
            Object elem = resolveElement(field.listElementType(), m);
            return elem != null ? List.of(elem) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
}
