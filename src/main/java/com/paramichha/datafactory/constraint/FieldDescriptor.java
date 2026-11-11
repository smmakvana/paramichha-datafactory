package com.paramichha.datafactory.constraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes a single field — its type, generic type, and validation annotations.
 * Extracted via reflection; works for Lombok {@code @Value @Builder}, Records, plain POJOs,
 * and inherited fields.
 */
public record FieldDescriptor(String name, Class<?> rawType, Type genericType, List<Annotation> validations) {

    private static final java.util.concurrent.ConcurrentHashMap<Class<?>, List<FieldDescriptor>> CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static List<FieldDescriptor> extract(Class<?> type) {
        return CACHE.computeIfAbsent(type, t -> t.isRecord() ? extractRecord(t) : extractClass(t));
    }

    /** #8 — evict a single class (e.g. after hot-reload in dev/test environments). */
    public static void evict(Class<?> type) {
        CACHE.remove(type);
    }

    /** #8 — evict the entire cache (e.g. after classloader reload or between test suites). */
    public static void evictAll() {
        CACHE.clear();
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

        // #15 — find the canonical constructor by matching component types exactly,
        //        not by index 0 which can be wrong when other constructors are declared
        Class<?>[] componentTypes =
                Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
        Parameter[] params = Arrays.stream(type.getDeclaredConstructors())
                .filter(c -> Arrays.equals(c.getParameterTypes(), componentTypes))
                .findFirst()
                .map(Constructor::getParameters)
                .orElse(new Parameter[0]);

        for (int i = 0; i < components.length; i++) {
            RecordComponent rc = components[i];
            List<Annotation> all = new ArrayList<>();

            all.addAll(Arrays.asList(rc.getAnnotations()));
            for (Field f : type.getDeclaredFields()) { // the component's private backing field
                if (f.getName().equals(rc.getName())) {
                    all.addAll(Arrays.asList(f.getAnnotations()));
                }
            }
            all.addAll(Arrays.asList(rc.getAccessor().getAnnotations()));
            if (i < params.length) {
                all.addAll(Arrays.asList(params[i].getAnnotations()));
            }

            result.add(build(rc.getName(), rc.getType(), rc.getGenericType(), all.toArray(new Annotation[0])));
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
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) {
                continue;
            }
            result.add(build(f.getName(), f.getType(), f.getGenericType(), f.getAnnotations()));
        }
        return result;
    }

    private static FieldDescriptor build(String name, Class<?> rawType, Type genericType, Annotation[] annotations) {
        List<Annotation> validations = new ArrayList<>();
        Set<Class<? extends Annotation>> seen = new HashSet<>();
        for (Annotation a : annotations) {
            String pkg = a.annotationType().getPackageName();
            if ((pkg.startsWith("jakarta.validation")
                            || pkg.startsWith("javax.validation")
                            || pkg.startsWith("org.hibernate.validator"))
                    && seen.add(a.annotationType())) {
                validations.add(a);
            }
        }
        return new FieldDescriptor(name, rawType, genericType, Collections.unmodifiableList(validations));
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
        if (genericType instanceof java.lang.reflect.ParameterizedType pt && pt.getActualTypeArguments().length >= 2) {
            java.lang.reflect.Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) {
                return c;
            }
        }
        return String.class;
    }

    public Class<?> mapValueType() {
        if (genericType instanceof java.lang.reflect.ParameterizedType pt && pt.getActualTypeArguments().length >= 2) {
            java.lang.reflect.Type arg = pt.getActualTypeArguments()[1];
            if (arg instanceof Class<?> c) {
                return c;
            }
        }
        return String.class;
    }

    /**
     * True for fields whose value is a container or object graph rather than a
     * single generated scalar: {@code Optional}, collections, maps and nested
     * domain objects. Per-field boundary variation does not apply to these —
     * their element values cannot be assigned to the field directly.
     */
    public boolean isComposite() {
        return rawType == java.util.Optional.class || isList() || isSet() || isMap() || isQueue() || isNested();
    }

    public boolean isNested() {
        if (rawType == java.util.Optional.class) {
            return false;
        }
        String pkg = rawType.getPackageName();
        return !rawType.isPrimitive()
                && !rawType.isArray()
                && !rawType.isEnum()
                && !pkg.startsWith("java.")
                && !pkg.startsWith("javax.")
                && !pkg.startsWith("jakarta.")
                && !pkg.startsWith("org.springframework.")
                && !pkg.startsWith("com.fasterxml.");
    }

    public Class<?> listElementType() {
        if (genericType instanceof ParameterizedType pt) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) {
                return c;
            }
        }
        return String.class;
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A annotation(Class<A> type) {
        for (Annotation a : validations) {
            if (type.isInstance(a)) {
                return (A) a;
            }
        }
        return null;
    }

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return annotation(type) != null;
    }
}
