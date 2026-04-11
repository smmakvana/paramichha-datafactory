package com.paramichha.testkit.annotation;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class MultipleArgumentHandler implements AnnotationTypeHandler {

    private static final Set<Class<? extends Annotation>> RANGE_ANNOTATIONS = Set.of(
            Size.class,
            Length.class
    );

    private static final Set<Class<? extends Annotation>> DIGITS_ANNOTATIONS = Set.of(
            Digits.class
    );

    @Override
    public Annotation create(Class<? extends Annotation> clazz, String raw) {
        Map<String, Object> attrs = new HashMap<>();

        if (RANGE_ANNOTATIONS.contains(clazz)) {
            parseRangeAttributes(raw, attrs);
        }

        if (DIGITS_ANNOTATIONS.contains(clazz)) {
            parseDigitsAttributes(raw, attrs);
        }
        return AnnotationProxyFactory.create(clazz, attrs);
    }

    private void parseRangeAttributes(String raw, Map<String, Object> attrs) {
        int min = 0;
        int max = Integer.MAX_VALUE;

        for (String part : raw.split(",")) {
            String[] kv = part.split("=");
            String key = kv[0].trim();
            String value = kv[1].trim();
            if ("min".equals(key)) min = Integer.parseInt(value);
            if ("max".equals(key)) max = Integer.parseInt(value);
        }

        attrs.put("min", min);
        attrs.put("max", max);
    }

    private void parseDigitsAttributes(String raw, Map<String, Object> attrs) {
        int integer = 0;
        int fraction = 0;


        for (String part : raw.split(",")) {
            String[] kv = part.split("=");
            String key = kv[0].trim();
            String value = kv[1].trim();
            if ("integer".equals(key)) integer = Integer.parseInt(value);
            if ("fraction".equals(key)) fraction = Integer.parseInt(value);
        }

        attrs.put("integer", integer);
        attrs.put("fraction", fraction);
    }
}
