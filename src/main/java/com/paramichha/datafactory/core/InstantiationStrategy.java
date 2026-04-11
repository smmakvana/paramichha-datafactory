package com.paramichha.datafactory.core;

import java.util.List;
import java.util.Map;

/**
 * Strategy for instantiating a class from a field-value map.
 *
 * <p>Implementations are tried in order by {@link ObjectAssembler}:
 * <ol>
 *   <li>{@link LombokBuilderStrategy} — static {@code builder()} method</li>
 *   <li>{@link RecordStrategy} — Java Record canonical constructor</li>
 *   <li>{@link AllArgsConstructorStrategy} — all-args constructor</li>
 *   <li>{@link SetterStrategy} — no-args constructor + setters / field access</li>
 * </ol>
 */
interface InstantiationStrategy {

    /**
     * Returns {@code true} if this strategy can instantiate the given type.
     */
    boolean canHandle(Class<?> type, List<FieldDescriptor> fields);

    /**
     * Instantiates the type using the provided field values.
     */
    <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                      Map<String, Object> values) throws Exception;
}
