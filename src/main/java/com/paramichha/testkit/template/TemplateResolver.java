package com.paramichha.testkit.template;

import com.paramichha.testkit.utils.TestNames;
import com.paramichha.utils.Texts;

/**
 * @deprecated Use {@link TestNames} and {@link Texts} directly.
 * Kept for backward compatibility only.
 */
@Deprecated(forRemoval = true)
public final class TemplateResolver {

    private TemplateResolver() {}

    /** @deprecated Use {@link TestNames#toVariable(String)} */
    @Deprecated(forRemoval = true)
    public static String instanceFieldName(String className) {
        return TestNames.toVariable(className);
    }

    /** @deprecated Use {@link Texts#capitalize(String)} */
    @Deprecated(forRemoval = true)
    public static String capitalize(String s) {
        return Texts.capitalize(s);
    }
}
