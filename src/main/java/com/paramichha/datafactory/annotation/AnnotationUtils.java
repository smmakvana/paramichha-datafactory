package com.paramichha.datafactory.annotation;

import com.paramichha.datafactory.AnnotationParseException;
import com.paramichha.datafactory.builder.DefaultAnnotationParser;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Public utility for validating and parsing Jakarta validation annotation strings.
 *
 * <p>A natural byproduct of DataFactory's annotation engine. Useful for tools
 * that receive annotation definitions from an external source — a YAML schema,
 * a DSL, a config file — and need to validate or parse them before use.
 *
 * <pre>
 * AnnotationUtils.isValid("@Email")          // true
 * AnnotationUtils.isValid("@Emal")           // false — typo
 * AnnotationUtils.isValid("@Min(18)")        // true
 *
 * Annotation a = AnnotationUtils.parse("@Min(18)");
 *
 * List&lt;Annotation&gt; anns = AnnotationUtils.parseAll(
 *         List.of("@NotBlank", "@Email", "@Size(max=100)"));
 * </pre>
 */
public final class AnnotationUtils {

    private AnnotationUtils() {}

    /**
     * Returns {@code true} if the string is a recognised Jakarta validation
     * annotation that DataFactory can parse.
     *
     * @param annotationString the annotation string, e.g. {@code "@NotBlank"}
     * @return {@code true} if recognised, {@code false} otherwise
     */
    public static boolean isValid(String annotationString) {
        if (annotationString == null || !annotationString.startsWith("@")) return false;
        try {
            return DefaultAnnotationParser.INSTANCE.parse(annotationString) != null;
        } catch (AnnotationParseException e) {
            return false;
        }
    }

    /**
     * Parses a single annotation string into an {@link Annotation} instance.
     *
     * @param annotationString the annotation string, e.g. {@code "@Min(18)"}
     * @return the parsed annotation, or {@code null} if not recognised
     * @throws AnnotationParseException if the string is malformed
     */
    public static Annotation parse(String annotationString) {
        return DefaultAnnotationParser.INSTANCE.parse(annotationString);
    }

    /**
     * Parses a list of annotation strings. Unrecognised strings are skipped.
     *
     * @param annotationStrings annotation strings, each starting with {@code @}
     * @return list of parsed annotations, never {@code null}
     * @throws AnnotationParseException if any string is malformed
     */
    public static List<Annotation> parseAll(List<String> annotationStrings) {
        return annotationStrings.stream()
                .map(DefaultAnnotationParser.INSTANCE::parse)
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }
}
