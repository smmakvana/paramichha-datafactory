package com.paramichha.testkit.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public interface AnnotationParser {

    Annotation parse(String annotationString) throws AnnotationParseException;

    default List<Annotation> parseAll(List<String> annotationStrings) throws AnnotationParseException {
        List<Annotation> result = new ArrayList<>();
        for (String s : annotationStrings) {
            result.add(parse(s));
        }
        return result;
    }
}
