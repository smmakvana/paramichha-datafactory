package com.paramichha.testkit.annotation;

import java.lang.annotation.Annotation;

interface AnnotationSupport {

    Class<? extends Annotation> getSupportedAnnotation(String name) throws AnnotationParseException;

    AnnotationType getAnnotationType(Class<? extends Annotation> clazz) throws AnnotationParseException;

}
