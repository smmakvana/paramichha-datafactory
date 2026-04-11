package com.paramichha.testkit.annotation;

import java.lang.annotation.Annotation;

class NoArgHandler implements AnnotationTypeHandler {

    @Override
    public Annotation create(Class<? extends Annotation> clazz, String rawAttributeString) throws AnnotationParseException{

        if (!rawAttributeString.isEmpty()) {
            throw new AnnotationParseException(clazz.getSimpleName() + " does not accept attributes");
        }

        return AnnotationProxyFactory.create(clazz);
    }
}