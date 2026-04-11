package com.paramichha.testkit.annotation;

public class AnnotationParseException extends Exception {

    AnnotationParseException(String message) {
        super(message);
    }

    AnnotationParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
