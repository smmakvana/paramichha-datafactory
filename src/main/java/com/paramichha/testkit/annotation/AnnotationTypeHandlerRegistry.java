package com.paramichha.testkit.annotation;

import java.util.EnumMap;
import java.util.Map;

final class AnnotationTypeHandlerRegistry {

    private final Map<AnnotationType, AnnotationTypeHandler> handlers = new EnumMap<>(AnnotationType.class);

    AnnotationTypeHandlerRegistry() {
        handlers.put(AnnotationType.NO_ARG,    new NoArgHandler());
        handlers.put(AnnotationType.ONE_ARG,   new SingleArgumentHandler());
        handlers.put(AnnotationType.MULTI_ARG, new MultipleArgumentHandler());
    }

    AnnotationTypeHandler get(AnnotationType type) {
        return handlers.get(type);
    }
}
