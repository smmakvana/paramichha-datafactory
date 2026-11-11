package com.paramichha.datafactory.constraint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Map key and value types: a raw map, or a value that is not a plain class, falls back to String. */
@DisplayName("Map key and value types")
class MapTypeArgumentsTest {

    record Maps(@SuppressWarnings("rawtypes") Map raw, Map<String, List<String>> nestedValue) {}

    @Test
    @DisplayName("a raw map and a map whose value is a parameterized type both use String")
    void rawAndParameterizedValuesFallBackToString() {
        FieldDescriptor raw = FieldDescriptor.extract(Maps.class).get(0);
        FieldDescriptor nested = FieldDescriptor.extract(Maps.class).get(1);
        assertThat(raw.mapKeyType()).isEqualTo(String.class);
        assertThat(raw.mapValueType()).isEqualTo(String.class);
        assertThat(nested.mapKeyType()).isEqualTo(String.class);
        assertThat(nested.mapValueType()).isEqualTo(String.class); // List<String> is not a Class
    }
}
