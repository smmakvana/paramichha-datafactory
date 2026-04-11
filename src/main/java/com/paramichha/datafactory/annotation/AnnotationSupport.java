package com.paramichha.datafactory.annotation;

import com.paramichha.datafactory.AnnotationParseException;
import java.lang.annotation.Annotation;

public interface AnnotationSupport {

    Class<? extends Annotation> getSupportedAnnotation(String name) throws AnnotationParseException;

    AnnotationType getAnnotationType(Class<? extends Annotation> clazz) throws AnnotationParseException;

}
