package com.paramichha.testkit.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

class SingleArgumentHandler implements AnnotationTypeHandler {

    @Override
    public Annotation create(Class<? extends Annotation> clazz, String raw) throws AnnotationParseException {
        try {
            if (!raw.isEmpty()) {
                String key;
                String extracted;
                if (raw.contains("=")) {
                    // named form: "value = 18"  or  "regexp = \"^[A-Z]+$\""
                    int eq = raw.indexOf('=');
                    key = raw.substring(0, eq).trim();
                    extracted = stripQuotes(raw.substring(eq + 1).trim());
                } else {
                    // positional form: "18"  or  "'10.5'"
                    // all positional ONE_ARG annotations have a method named "value"
                    key = "value";
                    extracted = stripQuotes(raw.trim());
                }
                if (!extracted.isBlank()) {
                    Method method = findMethod(clazz, key);
                    Object coerced = coerce(extracted, method.getReturnType());
                    return AnnotationProxyFactory.create(clazz, Map.of(key, coerced));
                }
            }
        } catch (Exception e) {
            throw new AnnotationParseException(e.getMessage());
        }
        throw new AnnotationParseException(clazz.getSimpleName() + " could not parse");
    }

    private Method findMethod(Class<? extends Annotation> clazz, String name) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(name);
    }

    private Object coerce(String raw, Class<?> targetType) {
        // Only two real types used by ONE_ARG annotations:
        //   long   → @Min(value), @Max(value)
        //   String → @DecimalMin(value), @DecimalMax(value), @Pattern(regexp)
        if (targetType == long.class) {
            return Long.parseLong(raw);
        }
        return raw;
    }

    private String stripQuotes(String s) {
        if (s.length() < 2) return s;
        char first = s.charAt(0);
        if (first != s.charAt(s.length() - 1)) return s;
        if (first == '"')  return s.substring(1, s.length() - 1);
        if (first == '\'') return s.substring(1, s.length() - 1);
        return s;
    }
}
