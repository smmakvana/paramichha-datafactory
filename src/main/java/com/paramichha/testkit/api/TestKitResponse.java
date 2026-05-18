package com.paramichha.testkit.api;

import java.nio.file.Path;

/**
 * Response from a test generation request.
 * File is on disk — no content returned.
 */
public final class TestKitResponse {

    private final boolean success;
    private final Path    file;
    private final String  error;

    private TestKitResponse(boolean success, Path file, String error) {
        this.success = success;
        this.file    = file;
        this.error   = error;
    }

    public static TestKitResponse ok(Path file) {
        return new TestKitResponse(true, file, null);
    }

    public static TestKitResponse failed(String error) {
        return new TestKitResponse(false, null, error);
    }

    public boolean success() { return success; }
    public Path    file()    { return file; }
    public String  error()   { return error; }
}
