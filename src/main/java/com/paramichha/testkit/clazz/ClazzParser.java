package com.paramichha.testkit.clazz;

import java.util.ArrayList;
import java.util.List;

public interface ClazzParser {

    Class<? extends Object> parse(String primitiveOrNonPrimitve) throws ClazzParserException;

    default List< Class<? extends Object>> parseAll(List<String> primitiveOrNonPrimitve) throws ClazzParserException {
        List< Class<? extends Object>> result = new ArrayList<>();
        for (String s : primitiveOrNonPrimitve) {
            result.add(parse(s));
        }
        return result;
    }
}
