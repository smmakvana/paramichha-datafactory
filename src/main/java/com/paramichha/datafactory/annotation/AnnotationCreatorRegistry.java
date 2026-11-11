package com.paramichha.datafactory.annotation;

import java.util.EnumMap;
import java.util.Map;

public final class AnnotationCreatorRegistry {

    private final Map<ArgumentType, AnnotationCreator> handlers = new EnumMap<>(ArgumentType.class);

    public AnnotationCreatorRegistry() {
        handlers.put(ArgumentType.NO_ARG, new NoArgHandler());
        handlers.put(ArgumentType.ONE_ARG, new SingleArgumentHandler());
        handlers.put(ArgumentType.MULTI_ARG, new MultipleArgumentHandler());
    }

    public AnnotationCreator get(ArgumentType type) {
        return handlers.get(type);
    }
}
