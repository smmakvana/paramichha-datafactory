package com.paramichha.testkit.data;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.TestDataProvider;
import com.paramichha.testkit.model.TestData;

import com.paramichha.utils.Seeds;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches test data from DataFactory at generation time.
 * Never throws — if DataFactory fails for any reason, returns empty data
 * and the generator writes a commented placeholder file.
 *
 * validObjects for the smoke section is built as:
 *   1. Start with the base valid object (always at least one entry)
 *   2. Add all per-field boundary variants from validAllPerField
 * This guarantees validObjects is never empty when a valid object exists.
 */
public final class DataFactoryTestDataProvider implements TestDataProvider {

    public static final DataFactoryTestDataProvider INSTANCE = new DataFactoryTestDataProvider();

    public DataFactoryTestDataProvider() {}

    @Override
    public TestData provide(Class<?> type) {
        // Seed from class name — deterministic output so generated files
        // are identical on every run. Same class = same test data = no diff.
        long classSeed = Seeds.stableHash(type);
        return provide(type, GenerationMode.PRODUCTION, classSeed);
    }

/**
     * Builds the smoke valid objects list.
     * Contains only the base valid object — one canonical valid instance
     * is sufficient for smoke testing construction and validation.
     * Per-field boundary variants live in validAllPerField and are used
     * separately by FieldTestWriter for parameterized field tests.
     */
    @SuppressWarnings("unchecked")
    public TestData provide(Class<?> type, GenerationMode mode, Long seed) {
        // Always seed from class name when no explicit seed given — deterministic output.
        long effectiveSeed = seed != null ? seed : Seeds.stableHash(type);
        Object base = null;
        String baseError = null;
        List<?> violations = List.of();
        String violationsError = null;
        Map<String, List<?>> validAllPerField = Map.of();

        try {
            base = DataFactory.of(type).mode(mode).seed(effectiveSeed).valid();
        } catch (Exception e) {
            baseError = e.getMessage();
        }

        try {
            violations = DataFactory.of(type).mode(mode).seed(effectiveSeed).invalidList();
        } catch (Exception e) {
            violationsError = e.getMessage();
        }

        try {
            validAllPerField = (Map<String, List<?>>) (Map)
                    DataFactory.of(type).mode(mode).seed(effectiveSeed).validPerField();
        } catch (Exception ignored) {}

        List<Object> validObjects = buildValidObjects(base, validAllPerField);

        String error = baseError != null ? "valid() failed: " + baseError
                     : violationsError != null ? "invalidList() failed: " + violationsError
                     : null;

        return new TestData(type, base, mode, violations, validAllPerField, validObjects, error);
    }

    public static List<Object> buildValidObjects(Object base,
                                                   Map<String, List<?>> validAllPerField) {
        List<Object> result = new ArrayList<>();
        if (base != null) result.add(base);
        return result;
    }
}
