package com.paramichha.datafactory;

import com.paramichha.datafactory.core.ConstraintCase;
import com.paramichha.datafactory.core.FieldBuilder;
import com.paramichha.datafactory.core.FieldDescriptor;
import com.paramichha.datafactory.core.ObjectAssembler;
import jakarta.validation.Valid;

import java.util.*;

class DefaultDataBuilder<T> implements DataBuilder<T> {

    private final Class<T> type;
    private final Map<String, Object> overrides = new LinkedHashMap<>();

    DefaultDataBuilder(Class<T> type) {
        this.type = type;
    }

    private static boolean isBooleanType(Class<?> type) {
        return type == Boolean.class || type == boolean.class;
    }

    @Override
    public DefaultDataBuilder<T> with(String fieldName, Object value) {
        overrides.put(fieldName, value);
        return this;
    }

    @Override
    public T valid() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            return ObjectAssembler.instantiate(type, fields, resolveAllValid(fields));
        } catch (Exception e) {
            throw new DataFactoryException("Failed to build valid " + type.getSimpleName(), e);
        }
    }

    @Override
    public Map<String, List<T>> validAll() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> semanticValues = resolveAllValid(fields);
            Map<String, List<T>> result = new LinkedHashMap<>();

            for (FieldDescriptor field : fields) {
                if (overrides.containsKey(field.name())) continue;

                List<Object> allValidValues = FieldBuilder.of(field).validValues();
                if (allValidValues.size() <= 1) continue;
                if (isBooleanType(field.rawType())) continue;

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
            throw new DataFactoryException("Failed to build validAll for " + type.getSimpleName(), e);
        }
    }

    @Override
    public T invalidFor(String fieldName) {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> values = resolveAllValid(fields);

            fields.stream()
                    .filter(f -> f.name().equals(fieldName))
                    .findFirst()
                    .ifPresent(f -> {
                        List<ConstraintCase> all = FieldBuilder.of(f).constraintCases();
                        List<ConstraintCase> driven = all.stream()
                                .filter(ConstraintCase::isAnnotationDriven).toList();
                        List<ConstraintCase> cases = driven.isEmpty() ? all : driven;
                        if (!cases.isEmpty()) {
                            values.put(fieldName, cases.get(0).value());
                        }
                    });

            return ObjectAssembler.instantiate(type, fields, values);
        } catch (Exception e) {
            throw new DataFactoryException(
                    "Failed to build invalidFor('" + fieldName + "') on " + type.getSimpleName(), e);
        }
    }

    @Override
    public List<ViolationScenario<T>> violations() {
        try {
            List<FieldDescriptor> fields = FieldDescriptor.extract(type);
            Map<String, Object> validValues = resolveAllValid(fields);
            List<ViolationScenario<T>> scenarios = new ArrayList<>();

            for (FieldDescriptor field : fields) {
                for (ConstraintCase cc : FieldBuilder.of(field).annotationCases()) {
                    Map<String, Object> scenarioValues = new LinkedHashMap<>(validValues);
                    scenarioValues.put(field.name(), cc.value());
                    T obj = ObjectAssembler.instantiate(type, fields, scenarioValues);
                    scenarios.add(new ViolationScenario<>(
                            field.name(), cc.constraint(),
                            cc.testNameSuffix(), cc.value(), obj));
                }
            }

            return List.copyOf(scenarios);
        } catch (Exception e) {
            throw new DataFactoryException("Failed to build violations for " + type.getSimpleName(), e);
        }
    }

    private Map<String, Object> resolveAllValid(List<FieldDescriptor> fields) throws Exception {
        Map<String, Object> values = new LinkedHashMap<>();

        for (FieldDescriptor field : fields) {
            if (overrides.containsKey(field.name())) {
                values.put(field.name(), overrides.get(field.name()));
                continue;
            }
            if (field.isNested() && field.hasAnnotation(Valid.class)) {
                values.put(field.name(), new DefaultDataBuilder<>(field.rawType()).valid());
                continue;
            }
            if (field.isList()) {
                Class<?> elemType = field.listElementType();
                if (!elemType.getPackageName().startsWith("java.")) {
                    values.put(field.name(), List.of(new DefaultDataBuilder<>(elemType).valid()));
                    continue;
                }
            }
            List<Object> validList = FieldBuilder.of(field).validValues();
            values.put(field.name(), validList.isEmpty() ? null : validList.get(0));
        }

        return values;
    }
}
