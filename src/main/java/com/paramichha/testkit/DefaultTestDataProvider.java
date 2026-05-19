package com.paramichha.testkit;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.model.TestFixture;

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
public final class DefaultTestDataProvider implements TestDataProvider {

    public static final DefaultTestDataProvider INSTANCE = new DefaultTestDataProvider();

    public DefaultTestDataProvider() {}

    @Override
    public TestFixture provide(Class<?> type) {
        return provide(type, GenerationMode.PRODUCTION, Seeds.stableHash(type));
    }

/**
     * Builds the smoke valid objects list.
     * Contains only the base valid object — one canonical valid instance
     * is sufficient for smoke testing construction and validation.
     * Per-field boundary variants live in validAllPerField and are used
     * separately by FieldTestWriter for parameterized field tests.
     */
    @SuppressWarnings("unchecked")
    @Override
    public TestFixture provide(Class<?> type, GenerationMode mode, Long seed) {
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

        // Only fail generation if valid() failed — we cannot build even one valid instance.
        // invalidList() failure is non-fatal — member reject tests are skipped but
        // creation tests and method tests are still generated.
        String error = baseError != null ? "valid() failed: " + baseError : null;

        return new TestFixture(type, base, mode, violations, validAllPerField, validObjects, error);
    }

    public static List<Object> buildValidObjects(Object base,
                                                   Map<String, List<?>> validAllPerField) {
        List<Object> result = new ArrayList<>();
        if (base != null) result.add(base);
        return result;
    }
}
