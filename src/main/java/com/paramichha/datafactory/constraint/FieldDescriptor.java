package com.paramichha.datafactory.constraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Describes a single field — its type, generic type, and validation annotations.
 * Extracted via reflection; works for Lombok {@code @Value @Builder}, Records, plain POJOs,
 * and inherited fields.
 */
public record FieldDescriptor(
        String name,
        Class<?> rawType,
        Type genericType,
        List<Annotation> validations
) {

    private static final java.util.concurrent.ConcurrentHashMap<Class<?>, List<FieldDescriptor>>
            CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static List<FieldDescriptor> extract(Class<?> type) {
        return CACHE.computeIfAbsent(type,
                t -> t.isRecord() ? extractRecord(t) : extractClass(t));
    }

    /**
     * Extracts a single FieldDescriptor from a {@link java.lang.reflect.Field}.
     * This is the bridge for the public DataFactory.field(Field) entry point.
     */
    static List<FieldDescriptor> extractField(java.lang.reflect.Field field) {
        return List.of(build(field.getName(), field.getType(),
                field.getGenericType(), field.getAnnotations()));
    }

    /**
     * Collects annotations from all four locations where Jakarta annotations can appear
     * on a record component. The Java compiler distributes them differently depending on
     * the annotation's {@code @Target}: the component declaration, the backing field,
     * the accessor method, and the canonical constructor parameter.
     */
    private static List<FieldDescriptor> extractRecord(Class<?> type) {
        List<FieldDescriptor> result = new ArrayList<>();
        RecordComponent[] components = type.getRecordComponents();
        Parameter[] params = type.getDeclaredConstructors()[0].getParameters();

        for (int i = 0; i < components.length; i++) {
            RecordComponent rc = components[i];
            List<Annotation> all = new ArrayList<>();

            all.addAll(Arrays.asList(rc.getAnnotations()));
            try {
                all.addAll(Arrays.asList(type.getDeclaredField(rc.getName()).getAnnotations()));
            } catch (NoSuchFieldException ignored) {
            }
            try {
                all.addAll(Arrays.asList(rc.getAccessor().getAnnotations()));
            } catch (Exception ignored) {
            }
            if (i < params.length) {
                all.addAll(Arrays.asList(params[i].getAnnotations()));
            }

            result.add(build(rc.getName(), rc.getType(), rc.getGenericType(),
                    all.toArray(new Annotation[0])));
        }
        return result;
    }

    private static List<FieldDescriptor> extractClass(Class<?> type) {
        List<Field> allFields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            allFields.addAll(0, Arrays.asList(c.getDeclaredFields()));
        }
        List<FieldDescriptor> result = new ArrayList<>();
        for (Field f : allFields) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
            result.add(build(f.getName(), f.getType(), f.getGenericType(), f.getAnnotations()));
        }
        return result;
    }

    private static FieldDescriptor build(String name, Class<?> rawType, Type genericType,
                                         Annotation[] annotations) {
        List<Annotation> validations = new ArrayList<>();
        Set<Class<? extends Annotation>> seen = new HashSet<>();
        for (Annotation a : annotations) {
            String pkg = a.annotationType().getPackageName();
            if ((pkg.startsWith("jakarta.validation") || pkg.startsWith("javax.validation")
                    || pkg.startsWith("org.hibernate.validator"))
                    && seen.add(a.annotationType())) {
                validations.add(a);
            }
        }
        return new FieldDescriptor(name, rawType, genericType, Collections.unmodifiableList(validations));
    }

    public boolean hasValidations() {
        return !validations.isEmpty();
    }

    public boolean isEnum() {
        return rawType.isEnum();
    }

    public boolean isList() {
        return rawType == List.class || rawType == Collection.class;
    }

    public boolean isSet() {
        return rawType == java.util.Set.class
            || rawType == java.util.LinkedHashSet.class
            || rawType == java.util.TreeSet.class
            || rawType == java.util.HashSet.class
            || rawType == java.util.SortedSet.class
            || rawType == java.util.NavigableSet.class;
    }

    public boolean isMap() {
        return rawType == java.util.Map.class
            || rawType == java.util.LinkedHashMap.class
            || rawType == java.util.HashMap.class
            || rawType == java.util.TreeMap.class
            || rawType == java.util.SortedMap.class
            || rawType == java.util.NavigableMap.class;
    }

    public boolean isArray() {
        return rawType.isArray();
    }

    public boolean isQueue() {
        return rawType == java.util.Queue.class
            || rawType == java.util.Deque.class
            || rawType == java.util.ArrayDeque.class
            || rawType == java.util.LinkedList.class
            || rawType == java.util.PriorityQueue.class;
    }

    public Class<?> mapKeyType() {
        if (genericType instanceof java.lang.reflect.ParameterizedType pt
                && pt.getActualTypeArguments().length >= 2) {
            java.lang.reflect.Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) return c;
        }
        return String.class;
    }

    public Class<?> mapValueType() {
        if (genericType instanceof java.lang.reflect.ParameterizedType pt
                && pt.getActualTypeArguments().length >= 2) {
            java.lang.reflect.Type arg = pt.getActualTypeArguments()[1];
            if (arg instanceof Class<?> c) return c;
        }
        return String.class;
    }

    public boolean isNested() {
        if (rawType == java.util.Optional.class) return false;
        String pkg = rawType.getPackageName();
        return !rawType.isPrimitive() && !rawType.isArray() && !rawType.isEnum()
                && !pkg.startsWith("java.") && !pkg.startsWith("javax.")
                && !pkg.startsWith("jakarta.") && !pkg.startsWith("org.springframework.")
                && !pkg.startsWith("com.fasterxml.");
    }

    public Class<?> listElementType() {
        if (genericType instanceof ParameterizedType pt) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) return c;
        }
        return String.class;
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A annotation(Class<A> type) {
        for (Annotation a : validations) {
            if (type.isInstance(a)) return (A) a;
        }
        return null;
    }

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return annotation(type) != null;
    }
}