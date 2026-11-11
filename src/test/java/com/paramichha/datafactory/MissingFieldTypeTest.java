package com.paramichha.datafactory;

import com.paramichha.datafactory.regression.missingtype.HoldsMissingType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** A field whose type is missing at runtime (a classpath mismatch) makes the call fail, not throw. */
@DisplayName("a field type missing at runtime")
class MissingFieldTypeTest {

    @Nested
    @DisplayName("a field type missing at runtime (classpath mismatch)")
    class MissingType {

        /** Loads HoldsMissingType in a class loader that can't find MissingAtRuntime. */
        private Class<?> holderWithoutItsElementType() throws Exception {
            String holder = HoldsMissingType.class.getName();
            String hidden = holder.replace("HoldsMissingType", "MissingAtRuntime");
            URL root = HoldsMissingType.class.getProtectionDomain().getCodeSource().getLocation();
            @SuppressWarnings("resource") // the loader lives as long as the class it loaded
            ClassLoader loader = new URLClassLoader(new URL[]{root}, getClass().getClassLoader()) {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    if (name.equals(hidden)) throw new ClassNotFoundException(name);
                    if (!name.equals(holder)) return super.loadClass(name, resolve);
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> c = findLoadedClass(name);
                        return c != null ? c : findClass(name);
                    }
                }
            };
            return loader.loadClass(holder);
        }

        @Test
        void everyCallReturnsAFailureInsteadOfThrowing() throws Exception {
            DataBuilder<?> b = DataFactory.of(holderWithoutItsElementType());
            assertThat(b.valid().errorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
            assertThat(b.invalidList().failed()).singleElement()
                    .satisfies(r -> assertThat(r.reason()).contains("Failed to build invalidList"));
            assertThat(b.validPerField()).containsOnlyKeys("_error");
            Map<String, ? extends DataFactoryResult<?>> fields = b.fields();
            assertThat(fields).containsOnlyKeys("_error");
            assertThat(fields.get("_error").reason()).contains("MissingAtRuntime");
        }
    }
}
