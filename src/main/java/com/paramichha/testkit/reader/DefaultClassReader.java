package com.paramichha.testkit.reader;

import com.paramichha.testkit.ClassReader;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.FieldMeta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Reads class structure via standard Java reflection.
 * No dependency on datafactory engine internals.
 */
public final class DefaultClassReader implements ClassReader {

    public static final DefaultClassReader INSTANCE = new DefaultClassReader();

    private DefaultClassReader() {}

    @Override
    public ClassMeta read(Class<?> type) {
        List<FieldMeta> fields = extractFields(type);
        return new ClassMeta(
                type.getSimpleName(),
                type.getPackageName(),
                detectStyle(type, fields),
                fields
        );
    }

    private ClassMeta.ConstructorStyle detectStyle(Class<?> type, List<FieldMeta> fields) {
        if (type.isRecord()) return ClassMeta.ConstructorStyle.RECORD;
        try {
            Method b = type.getDeclaredMethod("builder");
            if (Modifier.isStatic(b.getModifiers())) return ClassMeta.ConstructorStyle.LOMBOK_BUILDER;
        } catch (NoSuchMethodException ignored) {}
        for (Constructor<?> c : type.getDeclaredConstructors())
            if (c.getParameterCount() == fields.size()) return ClassMeta.ConstructorStyle.ALL_ARGS;
        try {
            type.getDeclaredConstructor();
            return ClassMeta.ConstructorStyle.SETTER;
        } catch (NoSuchMethodException ignored) {}
        return ClassMeta.ConstructorStyle.ALL_ARGS;
    }

    private List<FieldMeta> extractFields(Class<?> type) {
        return type.isRecord() ? extractRecordFields(type) : extractClassFields(type);
    }

    private List<FieldMeta> extractRecordFields(Class<?> type) {
        List<FieldMeta> result = new ArrayList<>();
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
            result.add(new FieldMeta(rc.getName(), rc.getType(), rc.getGenericType(),
                    validations, isRequired(validations)));
        }
        return result;
    }

    private List<FieldMeta> extractClassFields(Class<?> type) {
        List<Field> allFields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
            allFields.addAll(0, Arrays.asList(c.getDeclaredFields()));
        List<FieldMeta> result = new ArrayList<>();
        for (Field f : allFields) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
            List<Annotation> validations = filterValidation(Arrays.asList(f.getAnnotations()));
            result.add(new FieldMeta(f.getName(), f.getType(), f.getGenericType(),
                    validations, isRequired(validations)));
        }
        return result;
    }

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

    private boolean isRequired(List<Annotation> v) {
        return v.stream().anyMatch(a -> a instanceof NotNull || a instanceof NotBlank || a instanceof NotEmpty);
    }
}
