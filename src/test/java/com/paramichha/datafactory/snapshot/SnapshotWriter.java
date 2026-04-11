package com.paramichha.datafactory.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.Validator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts DataFactory output into a deterministic JSON string.
 *
 * Output format per fixture:
 * {
 *   "valid":       { "object": {...}, "violations": [] },
 *   "validMap":    { "fieldName": [ { "object": {...}, "violations": [] }, ... ] },
 *   "invalidList": [ { "object": {...}, "violations": ["fieldName"] }, ... ]
 * }
 *
 * Contract:
 *   valid.violations         → always []
 *   validMap violations      → always []
 *   invalidList violations   → non-[] for every entry (when list is non-empty)
 *   invalidList empty        → valid contract for unannotated fixtures
 */
public final class SnapshotWriter {

    private static final ObjectMapper MAPPER = buildMapper();

    private SnapshotWriter() {}

    private static ObjectMapper buildMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                .configure(com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    public static String toJson(
            Object valid,
            Map<String, List<?>> validMap,
            List<?> invalidList,
            Validator validator) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("valid",       toEntry(valid, validator));
            root.put("validMap",    toValidMapEntries(validMap, validator));
            root.put("invalidList", toInvalidEntries(invalidList, validator));
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize snapshot", e);
        }
    }

    private static Map<String, Object> toEntry(Object obj, Validator validator) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("object",     obj);
        entry.put("violations", violations(obj, validator));
        return entry;
    }

    private static Map<String, List<?>> toValidMapEntries(
            Map<String, List<?>> validMap, Validator validator) {
        Map<String, List<?>> result = new LinkedHashMap<>();
        validMap.forEach((field, variants) ->
                result.put(field, variants.stream()
                        .map(v -> toEntry(v, validator))
                        .collect(Collectors.toList())));
        return result;
    }

    private static List<Map<String, Object>> toInvalidEntries(
            List<?> invalidList, Validator validator) {
        return invalidList.stream()
                .map(obj -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("object",     obj);
                    entry.put("violations", violations(obj, validator));
                    return entry;
                })
                .collect(Collectors.toList());
    }

    private static List<String> violations(Object obj, Validator validator) {
        if (obj == null) return List.of();
        return validator.validate(obj).stream()
                .map(v -> v.getPropertyPath().toString())
                .sorted()
                .collect(Collectors.toList());
    }
}