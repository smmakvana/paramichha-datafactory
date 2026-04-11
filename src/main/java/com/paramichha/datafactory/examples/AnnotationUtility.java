package com.paramichha.datafactory.examples;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Annotation Utility — for tools that receive annotation strings from external sources.
 *
 * <p>AnnotationUtils lives in the engine package and is a natural byproduct of
 * DataFactory's annotation parsing capability. Useful for code generators reading
 * YAML schemas, DSLs, or config files.
 */
public class AnnotationUtility {

    public static void main(String[] args) {
        validate();
        parseAndUse();
        fieldFromStrings();
    }

    static void validate() {
        section("Validate annotation strings from YAML schema");
        String[] candidates = {
            "@NotBlank", "@Email", "@Min(18)", "@Size(max=100)",
            "@Emal",     "@Min",   "NotBlank", "@Min(abc)"
        };
        System.out.printf("  %-28s %s%n", "annotation", "valid?");
        System.out.println("  " + "-".repeat(38));
        for (String s : candidates) {
            System.out.printf("  %-28s %s%n", s, AnnotationUtils.isValid(s) ? "✓" : "✗");
        }
    }

    static void parseAndUse() {
        section("Parse annotation strings then pass to DataFactory");
        List<String> fromYaml = List.of("@NotBlank", "@Email", "@Size(max=100)");
        System.out.println("  from YAML: " + fromYaml);

        List<Annotation> annotations = AnnotationUtils.parseAll(fromYaml);
        System.out.println("  parsed:    " + annotations.size() + " annotations");
        annotations.forEach(a -> System.out.println("    @" + a.annotationType().getSimpleName()));

        String valid = DataFactory.field("email", String.class, annotations).valid();
        System.out.println("  generated: " + valid);
    }

    static void fieldFromStrings() {
        section("parse then build — parse strings, pass to DataFactory.field()");
        List<Annotation> annotations = AnnotationUtils.parseAll(
                List.of("@NotBlank", "@Email", "@Size(max=100)"));
        var fb = DataFactory.field("email", String.class, annotations);
        System.out.println("  valid:       " + fb.valid());
        System.out.println("  invalid:     " + fb.invalid());
        System.out.println("  invalidList: " + fb.invalidList());
        System.out.println("  stream(5):   " + fb.stream(5));
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }
}
