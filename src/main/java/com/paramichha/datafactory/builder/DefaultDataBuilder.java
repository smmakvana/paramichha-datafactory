package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.DataBuilder;
import com.paramichha.datafactory.DataFactoryException;
import com.paramichha.datafactory.GenerationMode;
import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.generation.FakerProvider;
import net.datafaker.Faker;
import com.paramichha.datafactory.instantiation.ObjectAssembler;
import com.paramichha.datafactory.constraint.ProductionDefaults;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultDataBuilder<T> implements DataBuilder<T> {

    private final Class<T> type;
    private final Map<String, Object> overrides = new LinkedHashMap<>();
    private GenerationMode mode = GenerationMode.PRODUCTION;
    private Long seed = null;

    public DefaultDataBuilder(Class<T> type) {
        this.type = type;
    }

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
        return this;
    }

    private Faker faker() {
        return seed != null ? FakerProvider.forSeed(seed) : FakerProvider.random();
    }

    @Override
    public List<T> stream(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> valid())
                .collect(Collectors.toList());
    }

    @Override
    public T valid() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            if (mode == GenerationMode.PRODUCTION) surfaceProductionWarnings(fields);
            return ObjectAssembler.instantiate(type, fields, resolveAllValid(fields, faker()));
        } catch (Exception e) {
            throw new DataFactoryException("Failed to build valid " + type.getSimpleName(), e);
        }
    }

    private void surfaceProductionWarnings(List<FieldDescriptor> fields) {
        for (FieldDescriptor field : fields) {
            ProductionDefaults.warn(field.name(), field.rawType(), field.validations());
        }
    }

    @Override
    public List<T> invalidList() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Faker f = faker();
            Map<String, Object> validValues = resolveAllValid(fields, f);
            List<T> result = new ArrayList<>();
            for (FieldDescriptor field : fields) {
                for (Object invalidValue : FieldBuilderFactory.create(field, f, seed != null ? seed : 0L).invalidList()) {
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
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Faker fkr = faker();
            Map<String, Object> values = resolveAllValid(fields, fkr);
            fields.stream()
                    .filter(f -> f.name().equals(fieldName))
                    .findFirst()
                    .ifPresent(f -> {
                        DefaultFieldBuilder<?> fb =
                                (DefaultFieldBuilder<?>) FieldBuilderFactory.create(f, fkr, seed != null ? seed : 0L);
                        fb.firstInvalidValue().ifPresent(holder ->
                                values.put(fieldName, ((Object[]) holder)[0]));
                    });
            return ObjectAssembler.instantiate(type, fields, values);
        } catch (Exception e) {
            throw new DataFactoryException(
                    "Failed to build invalidFor('" + fieldName + "') on " + type.getSimpleName(), e);
        }
    }

    @Override
    public Map<String, List<T>> validMap() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Faker f = faker();
            Map<String, Object> semanticValues = resolveAllValid(fields, f);
            Map<String, List<T>> result = new LinkedHashMap<>();
            for (FieldDescriptor field : fields) {
                if (overrides.containsKey(field.name())) continue;
                if (field.rawType() == Boolean.class || field.rawType() == boolean.class) continue;
                List<Object> allValidValues = FieldBuilderFactory.create(field, f, seed != null ? seed : 0L).validList();
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
            throw new DataFactoryException("Failed to build validMap for " + type.getSimpleName(), e);
        }
    }

    private static final ThreadLocal<Set<Class<?>>> BUILDING =
            ThreadLocal.withInitial(HashSet::new);

    private Map<String, Object> resolveAllValid(List<FieldDescriptor> fields, Faker f) throws Exception {
        Map<String, Object> values = new LinkedHashMap<>();
        for (FieldDescriptor field : fields) {
            if (overrides.containsKey(field.name())) {
                values.put(field.name(), overrides.get(field.name()));
                continue;
            }
            if (field.rawType() == java.util.Optional.class) {
                values.put(field.name(), resolveOptional(field));
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
                values.put(field.name(), resolveList(field));
                continue;
            }
            if (field.isSet()) {
                values.put(field.name(), resolveSet(field));
                continue;
            }
            if (field.isMap()) {
                values.put(field.name(), resolveMap(field));
                continue;
            }
            if (field.isArray()) {
                values.put(field.name(), resolveArray(field));
                continue;
            }
            if (field.isQueue()) {
                values.put(field.name(), resolveQueue(field));
                continue;
            }
            List<Object> validList = FieldBuilderFactory.create(field, f, seed != null ? seed : 0L).validList();
            values.put(field.name(), validList.isEmpty() ? null : validList.get(0));
        }
        return values;
    }

    private Object resolveNested(FieldDescriptor field) {
        if (BUILDING.get().add(field.rawType())) {
            try {
                DefaultDataBuilder<?> nested = new DefaultDataBuilder<>(field.rawType()).mode(mode);
                if (seed != null) nested.seed(seed + field.name().hashCode());
                return nested.valid();
            } catch (Exception e) {
                return null;
            } finally {
                BUILDING.get().remove(field.rawType());
            }
        }
        return null;
    }

    private Object resolveOptional(FieldDescriptor field) {
        try {
            Class<?> elemType = field.listElementType();
            if (!elemType.getPackageName().startsWith("java.")) {
                if (BUILDING.get().contains(elemType)) return Optional.empty();
                DefaultDataBuilder<?> ob = new DefaultDataBuilder<>(elemType).mode(mode);
                if (seed != null) ob.seed(seed);
                return Optional.of(ob.valid());
            }
            List<?> elemValid = FieldBuilderFactory.create(elemType, faker(), seed != null ? seed : 0L).validList();
            Object elem = elemValid.isEmpty() ? null : elemValid.get(0);
            return elem != null ? Optional.of(elem) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Object resolveSet(FieldDescriptor field) {
        try {
            Class<?> elemType = field.listElementType();
            Object elem = null;
            if (elemType.isEnum()) {
                Object[] constants = elemType.getEnumConstants();
                elem = constants.length > 0 ? constants[0] : null;
            } else if (!elemType.getPackageName().startsWith("java.")) {
                DefaultDataBuilder<?> lb = new DefaultDataBuilder<>(elemType).mode(mode);
                if (seed != null) lb.seed(seed);
                elem = lb.valid();
            } else {
                List<?> elemValid = FieldBuilderFactory.create(elemType, faker(), seed != null ? seed : 0L).validList();
                elem = elemValid.isEmpty() ? null : elemValid.get(0);
            }
            // Return the correct concrete Set type matching the field declaration
            Class<?> rawType = field.rawType();
            if (elem == null) return new java.util.HashSet<>();
            if (rawType == java.util.TreeSet.class || rawType == java.util.SortedSet.class
                    || rawType == java.util.NavigableSet.class) {
                java.util.TreeSet<Object> s = new java.util.TreeSet<>(); s.add(elem); return s;
            }
            if (rawType == java.util.LinkedHashSet.class) {
                java.util.LinkedHashSet<Object> s = new java.util.LinkedHashSet<>(); s.add(elem); return s;
            }
            java.util.HashSet<Object> s = new java.util.HashSet<>(); s.add(elem); return s;
        } catch (Exception e) {
            return new java.util.HashSet<>();
        }
    }

    private Object resolveMap(FieldDescriptor field) {
        try {
            Class<?> keyType = field.mapKeyType();
            Class<?> valType = field.mapValueType();
            List<?> keys = FieldBuilderFactory.create(keyType, faker(), seed != null ? seed : 0L).validList();
            Object key = keys.isEmpty() ? "key" : keys.get(0);
            Object val;
            if (valType.isEnum()) {
                Object[] constants = valType.getEnumConstants();
                val = constants.length > 0 ? constants[0] : null;
            } else if (!valType.getPackageName().startsWith("java.")) {
                DefaultDataBuilder<?> vb = new DefaultDataBuilder<>(valType).mode(mode);
                if (seed != null) vb.seed(seed);
                val = vb.valid();
            } else {
                List<?> vals = FieldBuilderFactory.create(valType, faker(), seed != null ? seed : 0L).validList();
                val = vals.isEmpty() ? null : vals.get(0);
            }
            if (key == null || val == null) return new java.util.HashMap<>();
            Class<?> rawType = field.rawType();
            if (rawType == java.util.TreeMap.class || rawType == java.util.SortedMap.class
                    || rawType == java.util.NavigableMap.class) {
                java.util.TreeMap<Object,Object> m = new java.util.TreeMap<>(); m.put(key, val); return m;
            }
            if (rawType == java.util.LinkedHashMap.class) {
                java.util.LinkedHashMap<Object,Object> m = new java.util.LinkedHashMap<>(); m.put(key, val); return m;
            }
            java.util.HashMap<Object,Object> m = new java.util.HashMap<>(); m.put(key, val); return m;
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }

    private Object resolveArray(FieldDescriptor field) {
        try {
            Class<?> compType = field.rawType().getComponentType();
            List<?> elemValid = FieldBuilderFactory.create(compType, faker(), seed != null ? seed : 0L).validList();
            Object elem = elemValid.isEmpty() ? null : elemValid.get(0);
            if (elem == null) return java.lang.reflect.Array.newInstance(compType, 0);
            Object arr = java.lang.reflect.Array.newInstance(compType, 1);
            java.lang.reflect.Array.set(arr, 0, elem);
            return arr;
        } catch (Exception e) {
            return java.lang.reflect.Array.newInstance(field.rawType().getComponentType(), 0);
        }
    }

    private Object resolveQueue(FieldDescriptor field) {
        try {
            Class<?> elemType = field.listElementType();
            List<?> elemValid = FieldBuilderFactory.create(elemType, faker(), seed != null ? seed : 0L).validList();
            Object elem = elemValid.isEmpty() ? null : elemValid.get(0);
            Class<?> rawType = field.rawType();
            if (rawType == java.util.LinkedList.class) {
                java.util.LinkedList<Object> q = new java.util.LinkedList<>(); if (elem != null) q.add(elem); return q;
            }
            if (rawType == java.util.PriorityQueue.class) {
                java.util.PriorityQueue<Object> q = new java.util.PriorityQueue<>(); if (elem != null) q.add(elem); return q;
            }
            java.util.ArrayDeque<Object> q = new java.util.ArrayDeque<>();
            if (elem != null) q.add(elem);
            return q;
        } catch (Exception e) {
            return new java.util.ArrayDeque<>();
        }
    }

    private Object resolveList(FieldDescriptor field) {
        try {
            Class<?> elemType = field.listElementType();
            // Enum elements — use EnumTypeShaper directly
            if (elemType.isEnum()) {
                Object[] constants = elemType.getEnumConstants();
                if (constants.length == 0) return new java.util.ArrayList<>();
                java.util.ArrayList<Object> l = new java.util.ArrayList<>(); l.add(constants[0]); return l;
            }
            // Nested POJO elements
            if (!elemType.getPackageName().startsWith("java.")) {
                DefaultDataBuilder<?> lb = new DefaultDataBuilder<>(elemType).mode(mode);
                if (seed != null) lb.seed(seed);
                java.util.ArrayList<Object> ll = new java.util.ArrayList<>(); ll.add(lb.valid()); return ll;
            }
            // Scalar elements
            List<?> elemValid = FieldBuilderFactory.create(elemType, faker(), seed != null ? seed : 0L).validList();
            Object elem = elemValid.isEmpty() ? null : elemValid.get(0);
            if (elem == null) return new java.util.ArrayList<>();
            java.util.ArrayList<Object> lr = new java.util.ArrayList<>(); lr.add(elem); return lr;
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
}