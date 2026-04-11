package com.paramichha.datafactory.snapshot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Produces a human-readable diff between two JSON strings.
 * Shows only what changed — not the entire JSON.
 */
final class SnapshotDiff {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SnapshotDiff() {}

    static String diff(String expected, String actual) {
        try {
            JsonNode exp = MAPPER.readTree(expected);
            JsonNode act = MAPPER.readTree(actual);
            List<String> diffs = new ArrayList<>();
            compare("", exp, act, diffs);
            if (diffs.isEmpty()) return "  (no differences found)";
            return String.join("\n", diffs);
        } catch (Exception e) {
            return "  (could not parse JSON for diff)";
        }
    }

    private static void compare(String path, JsonNode exp, JsonNode act, List<String> diffs) {
        if (exp == null && act == null) return;

        if (exp == null) {
            diffs.add(String.format("  + %-40s  %s", path, act));
            return;
        }
        if (act == null) {
            diffs.add(String.format("  - %-40s  %s", path, exp));
            return;
        }

        if (exp.isObject() && act.isObject()) {
            // check all expected keys
            Iterator<String> fields = exp.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                String childPath = path.isEmpty() ? field : path + "." + field;
                compare(childPath, exp.get(field), act.get(field), diffs);
            }
            // check for new keys in actual
            Iterator<String> actFields = act.fieldNames();
            while (actFields.hasNext()) {
                String field = actFields.next();
                if (!exp.has(field)) {
                    String childPath = path.isEmpty() ? field : path + "." + field;
                    diffs.add(String.format("  + %-40s  %s", childPath, act.get(field)));
                }
            }
        } else if (exp.isArray() && act.isArray()) {
            int max = Math.max(exp.size(), act.size());
            for (int i = 0; i < max; i++) {
                String childPath = path + "[" + i + "]";
                JsonNode e = i < exp.size() ? exp.get(i) : null;
                JsonNode a = i < act.size() ? act.get(i) : null;
                compare(childPath, e, a, diffs);
            }
        } else if (!exp.equals(act)) {
            diffs.add(String.format("  ~ %-40s  was: %-30s  now: %s", path, exp, act));
        }
    }
}
