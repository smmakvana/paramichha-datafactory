package com.paramichha.datafactory.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface AnnotationParser {

    Annotation parse(String annotationString);

    /** Parses each string with {@link #parse(String)}, keeping the order. */
    default List<Annotation> parseAll(List<String> annotationStrings) {
        List<Annotation> result = new ArrayList<>();
        for (String s : annotationStrings) {
            result.add(parse(s));
        }
        return result;
    }
}
