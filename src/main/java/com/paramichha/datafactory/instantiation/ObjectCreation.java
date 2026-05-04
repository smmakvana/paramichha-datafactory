package com.paramichha.datafactory.instantiation;

import com.paramichha.datafactory.constraint.FieldDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Strategy for instantiating a class from a field-value map.
 *
 * <p>Implementations are tried in order by {@link ObjectAssembler}:
 * <ol>
 *   <li>{@link LombokObjectCreation} — static {@code builder()} method</li>
 *   <li>{@link RecordObjectCreation} — Java Record canonical constructor</li>
 *   <li>{@link ConstructorObjectCreation} — all-args constructor</li>
 *   <li>{@link PojoObjectCreation} — no-args constructor + setters / field access</li>
 * </ol>
 */
interface ObjectCreation {

    /**
     * Returns {@code true} if this strategy can instantiate the given type.
     */
    boolean canCreate(Class<?> type, List<FieldDescriptor> fields);

    /**
     * Instantiates the type using the provided field values.
     */
    <T> T create(Class<T> type, List<FieldDescriptor> fields,
                 Map<String, Object> values) throws Exception;
}
