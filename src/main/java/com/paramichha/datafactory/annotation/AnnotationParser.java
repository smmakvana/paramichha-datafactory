package com.paramichha.datafactory.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public interface AnnotationParser {

    Annotation parse(String annotationString) ;

    default List<Annotation> parseAll(List<String> annotationStrings)  {
        List<Annotation> result = new ArrayList<>();
        for (String s : annotationStrings) {
            result.add(parse(s));
        }
        return result;
    }
}
