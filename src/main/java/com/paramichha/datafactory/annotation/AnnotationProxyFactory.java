package com.paramichha.datafactory.annotation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

public final class AnnotationProxyFactory {

    private AnnotationProxyFactory() {}

    public static <T extends Annotation> T create(Class<T> type) {
        return create(type, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T create(Class<T> type,
                                                  Map<String, Object> attrs) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("annotationType")) return type;
                    if (name.equals("toString")) return "@" + type.getSimpleName() + attrs;
                    if (name.equals("hashCode")) return attrs.hashCode();
                    if (name.equals("equals")) return proxy == args[0];
                    if (attrs.containsKey(name)) return attrs.get(name);
                    return method.getDefaultValue();
                }
        );
    }

    /** Creates a synthetic {@code @Min(value)} annotation. */
    public static Min createMin(long value) {
        return create(Min.class, java.util.Map.of("value", value));
    }

    /** Creates a synthetic {@code @Max(value)} annotation. */
    public static Max createMax(long value) {
        return create(Max.class, java.util.Map.of("value", value));
    }

    /** Creates a synthetic {@code @DecimalMin(value)} annotation. */
    public static DecimalMin createDecimalMin(String value) {
        return create(DecimalMin.class, java.util.Map.of("value", value, "inclusive", true));
    }

    /** Creates a synthetic {@code @DecimalMax(value)} annotation. */
    public static DecimalMax createDecimalMax(String value) {
        return create(DecimalMax.class, java.util.Map.of("value", value, "inclusive", true));
    }
}