package com.paramichha.datafactory.annotation;

import com.paramichha.datafactory.AnnotationParseException;
import java.lang.annotation.Annotation;

public interface AnnotationLookup {

    Class<? extends Annotation> getAnnotation(String name) throws AnnotationParseException;

    ArgumentType getArgumentType(Class<? extends Annotation> clazz) throws AnnotationParseException;

}
