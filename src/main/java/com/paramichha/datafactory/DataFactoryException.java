package com.paramichha.datafactory;

public class DataFactoryException extends RuntimeException {

    public DataFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataFactoryException(String message) {
        super(message);
    }
}
