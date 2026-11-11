package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/**
 * Produces {@code Character} / {@code char} values.
 *
 * <p>Uses the machine for variety — no longer always {@code 'A'}.
 * Respects {@code @Pattern} if the pattern contains a simple character class.
 */
final class CharacterDataCreator implements DataCreator {

    static final CharacterDataCreator INSTANCE = new CharacterDataCreator();

    private CharacterDataCreator() {}

    @Override
    public boolean supports(Class<?> type) {
        return type == Character.class || type == char.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        if ("null".equals(target.label())) {
            return null;
        }

        // Pattern constraint — pick first char from a simple [abc] class
        if (field.patternRegexp() != null) {
            String pattern = field.patternRegexp();
            if (pattern.startsWith("[") && pattern.endsWith("]") && pattern.length() > 2) {
                String inner = pattern.substring(1, pattern.length() - 1);
                if (!inner.isEmpty() && !inner.contains("-")) {
                    int idx = (int) machine.number(0, inner.length() - 1);
                    return inner.charAt(idx);
                }
            }
        }

        // Semantic — field name hints
        String name = field.fieldName().toLowerCase();
        if (name.contains("gender")) {
            return machine.number(0, 1) == 0 ? 'M' : 'F';
        }
        if (name.contains("grade")) {
            return (char) ('A' + machine.number(0, 4));
        }
        if (name.contains("flag")) {
            return machine.number(0, 1) == 0 ? 'Y' : 'N';
        }

        // Default — random printable ASCII letter
        return (char) ('A' + machine.number(0, 25));
    }
}
