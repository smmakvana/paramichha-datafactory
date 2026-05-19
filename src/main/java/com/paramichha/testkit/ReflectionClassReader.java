package com.paramichha.testkit;

import com.paramichha.testkit.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Reads class structure via standard Java reflection.
 * No dependency on data factory engine internals.
 */
public final class ReflectionClassReader implements ClassReader {

    public static final ReflectionClassReader INSTANCE = new ReflectionClassReader();

    private ReflectionClassReader() {}

    @Override
    public ClassInfo read(Class<?> type) {
        if (isUtilityClass(type)) {
            List<MethodInfo> methods = extractMethods(type, true);
            return new ReflectionClassInfo(
                    ConstructorStyle.ALL_ARGS, List.of(), methods, true, type);
        }
        List<FieldInfo> fields  = extractFields(type);
        List<MethodInfo> methods = extractMethods(type, false);
        return new ReflectionClassInfo(
                detectStyle(type, fields), fields, methods, false, type);
    }

    // ── Fields ────────────────────────────────────────────────────────────

    private ConstructorStyle detectStyle(Class<?> type, List<FieldInfo> fields) {
        if (type.isRecord()) return ConstructorStyle.RECORD;
        try {
            Method b = type.getDeclaredMethod("builder");
            if (Modifier.isStatic(b.getModifiers())) return ConstructorStyle.LOMBOK_BUILDER;
        } catch (NoSuchMethodException ignored) {}
        for (Constructor<?> c : type.getDeclaredConstructors())
            if (c.getParameterCount() == fields.size()) return ConstructorStyle.ALL_ARGS;
        try {
            type.getDeclaredConstructor();
            return ConstructorStyle.SETTER;
        } catch (NoSuchMethodException ignored) {}
        return ConstructorStyle.ALL_ARGS;
    }

    private List<FieldInfo> extractFields(Class<?> type) {
        return type.isRecord() ? extractRecordFields(type) : extractClassFields(type);
    }

    private List<FieldInfo> extractRecordFields(Class<?> type) {
        List<FieldInfo> result = new ArrayList<>();
        RecordComponent[] components = type.getRecordComponents();
        Parameter[] params = type.getDeclaredConstructors()[0].getParameters();
        for (int i = 0; i < components.length; i++) {
            RecordComponent rc = components[i];
            List<Annotation> all = new ArrayList<>(Arrays.asList(rc.getAnnotations()));
            try { all.addAll(Arrays.asList(type.getDeclaredField(rc.getName()).getAnnotations())); }
            catch (NoSuchFieldException ignored) {}
            try { all.addAll(Arrays.asList(rc.getAccessor().getAnnotations())); }
            catch (Exception ignored) {}
            if (i < params.length) all.addAll(Arrays.asList(params[i].getAnnotations()));
            List<Annotation> validations = filterValidation(all);
            boolean indirect = typeHasConstraints(rc.getType());
            result.add(new ReflectionFieldInfo(rc.getName(), rc.getType(), rc.getGenericType(),
                    validations, isRequired(validations), indirect));
        }
        return result;
    }

    private List<FieldInfo> extractClassFields(Class<?> type) {
        List<Field> allFields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
            allFields.addAll(0, Arrays.asList(c.getDeclaredFields()));
        List<FieldInfo> result = new ArrayList<>();
        for (Field f : allFields) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
            List<Annotation> validations = filterValidation(Arrays.asList(f.getAnnotations()));
            boolean indirect = typeHasConstraints(f.getType());
            result.add(new ReflectionFieldInfo(f.getName(), f.getType(), f.getGenericType(),
                    validations, isRequired(validations), indirect));
        }
        return result;
    }

    // ── Methods ───────────────────────────────────────────────────────────

    private List<MethodInfo> extractMethods(Class<?> type, boolean utility) {
        if (!utility && (type.isRecord() || type.isEnum() || type.isInterface())) return List.of();

        Set<String> objectMethods = objectMethodNames();
        List<MethodInfo> result = new ArrayList<>();

        for (Method m : type.getMethods()) {
            boolean isStatic = Modifier.isStatic(m.getModifiers());
            if (utility  && !isStatic) continue;
            if (!utility && isStatic)  continue;
            if (objectMethods.contains(m.getName())) continue;
            if (isGetterOrSetter(m)) continue;
            if (m.isSynthetic() || m.isBridge()) continue;

            List<ParameterInfo> params = extractParameters(m);
            // Return annotations come from two places:
            // 1. Method-level: public @NotNull BigDecimal method() → m.getAnnotations()
            // 2. Type-use:     public BigDecimal @NotNull method() → m.getAnnotatedReturnType()
            List<Annotation> returnAnnsRaw = new ArrayList<>();
            returnAnnsRaw.addAll(Arrays.asList(m.getAnnotations()));
            returnAnnsRaw.addAll(Arrays.asList(m.getAnnotatedReturnType().getAnnotations()));
            List<Annotation> returnAnns = filterValidation(returnAnnsRaw);
            MethodKind kind = classify(m.getReturnType(), params);
            result.add(new ReflectionMethodInfo(m.getName(), m.getReturnType(),
                    returnAnns, params, kind));
        }

        return Collections.unmodifiableList(result);
    }

    private List<ParameterInfo> extractParameters(Method m) {
        List<ParameterInfo> result = new ArrayList<>();
        for (Parameter p : m.getParameters()) {
            List<Annotation> validations = filterValidation(Arrays.asList(p.getAnnotations()));
            boolean indirect = typeHasConstraints(p.getType());
            result.add(new ReflectionParameterInfo(p.getName(), p.getType(), validations, indirect));
        }
        return Collections.unmodifiableList(result);
    }

    private MethodKind classify(Class<?> returnType, List<ParameterInfo> params) {
        boolean isVoid = returnType == void.class || returnType == Void.class;
        int paramCount = params.size();
        if (isVoid && paramCount == 0) return MethodKind.COMMAND;
        if (isVoid && paramCount == 1) return MethodKind.MUTATOR;
        if (isVoid)                    return MethodKind.MUTATOR;   // void + multi-param = mutator
        if (paramCount == 0)           return MethodKind.QUERY;
        if (paramCount == 1)           return MethodKind.TRANSFORMER;
        return MethodKind.OPERATION;
    }

    private boolean isGetterOrSetter(Method m) {
        String name = m.getName();
        int params = m.getParameterCount();
        if (params == 0 && (name.startsWith("get") || name.startsWith("is"))
                && name.length() > 2) return true;
        if (params == 1 && name.startsWith("set") && name.length() > 3) return true;
        return false;
    }

    private static Set<String> objectMethodNames() {
        Set<String> names = new HashSet<>();
        for (Method m : Object.class.getMethods()) names.add(m.getName());
        return names;
    }

    // ── Shared ────────────────────────────────────────────────────────────

    private List<Annotation> filterValidation(List<Annotation> annotations) {
        List<Annotation> result = new ArrayList<>();
        Set<Class<? extends Annotation>> seen = new HashSet<>();
        for (Annotation a : annotations) {
            String pkg = a.annotationType().getPackageName();
            if ((pkg.startsWith("jakarta.validation") || pkg.startsWith("javax.validation")
                    || pkg.startsWith("org.hibernate.validator"))
                    && seen.add(a.annotationType()))
                result.add(a);
        }
        return Collections.unmodifiableList(result);
    }

    private boolean typeHasConstraints(Class<?> type) {
        if (type == null || type.isPrimitive()) return false;
        if (type.getPackageName().startsWith("java.")) return false;
        if (type.isEnum()) return false;
        for (Field f : type.getDeclaredFields()) {
            for (Annotation a : f.getAnnotations()) {
                String pkg = a.annotationType().getPackageName();
                if (pkg.startsWith("jakarta.validation") || pkg.startsWith("javax.validation")
                        || pkg.startsWith("org.hibernate.validator")) return true;
            }
        }
        if (type.isRecord()) {
            for (RecordComponent rc : type.getRecordComponents()) {
                for (Annotation a : rc.getAnnotations()) {
                    String pkg = a.annotationType().getPackageName();
                    if (pkg.startsWith("jakarta.validation") || pkg.startsWith("javax.validation")
                            || pkg.startsWith("org.hibernate.validator")) return true;
                }
            }
        }
        return false;
    }

    private static boolean isUtilityClass(Class<?> type) {
        if (type.isEnum() || type.isInterface() || type.isRecord() || type.isAnonymousClass()) return false;
        boolean allPrivate = Arrays.stream(type.getDeclaredConstructors())
                .allMatch(c -> Modifier.isPrivate(c.getModifiers()));
        if (!allPrivate) return false;
        return Arrays.stream(type.getDeclaredMethods())
                .noneMatch(m -> Modifier.isStatic(m.getModifiers())
                        && Modifier.isPublic(m.getModifiers())
                        && m.getReturnType() == type);
    }

    private boolean isRequired(List<Annotation> v) {
        return v.stream().anyMatch(a -> a instanceof NotNull || a instanceof NotBlank
                || a instanceof NotEmpty);
    }
}