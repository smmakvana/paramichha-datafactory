package com.paramichha.datafactory.constraint;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.List;

/** Helpers shared by the constraint tests: reading a record's annotations and capturing console output. */
final class ConstraintTestSupport {

    private ConstraintTestSupport() {}

    static List<Annotation> annotationsOf(Class<?> record, String field) {
        return FieldDescriptor.extract(record).stream()
                .filter(f -> f.name().equals(field))
                .findFirst()
                .orElseThrow()
                .validations();
    }

    static String stdout(Runnable r) {
        PrintStream original = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true));
        try {
            r.run();
        } finally {
            System.setOut(original);
        }
        return buf.toString();
    }

    static String stderr(Runnable r) {
        PrintStream original = System.err;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buf, true));
        try {
            r.run();
        } finally {
            System.setErr(original);
        }
        return buf.toString();
    }
}
