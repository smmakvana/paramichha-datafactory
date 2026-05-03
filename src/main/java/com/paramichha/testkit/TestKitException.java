package com.paramichha.testkit;

public class TestKitException extends RuntimeException {
    public TestKitException(String message, Throwable cause) { super(message, cause); }
    public TestKitException(String message) { super(message); }
}
