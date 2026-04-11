package com.paramichha.testkit.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

public final class AnnotationProxyFactory {

    private AnnotationProxyFactory() {
    }

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
}