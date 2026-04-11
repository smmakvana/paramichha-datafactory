package com.paramichha.datafactory.snapshot;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.fixture.annotation.NullRecord;
import com.paramichha.datafactory.fixture.assembly.UserRegistrationRecord;
import com.paramichha.datafactory.fixture.partial.ListEnumShapingAllArgs;
import com.paramichha.datafactory.fixture.type.PrimitivesRecord;
import com.paramichha.datafactory.fixture.unsupported.SetType;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.fail;

/**
 * DataFactory snapshot contract test.
 *
 * Five packages, auto-discovered via anchor class:
 *   type/        — Java type families, unannotated, 4 strategies each
 *   annotation/  — Jakarta annotation groups, 4 strategies each
 *   assembly/    — real-world shapes, 4 strategies each
 *   partial/     — known gaps, documented in fixture Javadoc
 *   unsupported/ — all fields null, invalidList empty
 *
 * GENERATE: mvn test -Dsnapshot.generate=true -Dtest=DataFactorySnapshotTest
 * COMPARE:  mvn test -Dtest=DataFactorySnapshotTest
 *
 * Adding a fixture: drop a class in the right package — auto-discovered.
 * Moving a fixture: move the file — classification follows the package.
 */
@DisplayName("DataFactory — snapshot contract for all fixtures")
class DataFactorySnapshotTest {

    static final long      SEED      = 42L;
    static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("allFixtures")
    void contract(Class<?> fixture) throws Exception {
        runTest(fixture);
    }

    private void runTest(Class<?> fixture) {
        Object               valid    = DataFactory.of(fixture).seed(SEED).valid();
        Map<String, List<?>> perField = (Map) DataFactory.of(fixture).seed(SEED).validMap();
        List<?>              invalids = safeInvalidList(fixture);

        String actual      = SnapshotWriter.toJson(valid, perField, invalids, VALIDATOR);
        String fixtureName = fixture.getSimpleName();

        if (SnapshotStore.isGenerateMode()) {
            SnapshotStore.write(fixtureName, actual);
            return;
        }

        if (!SnapshotStore.exists(fixtureName)) {
            fail("Snapshot missing: " + fixtureName + ".json\n\n" +
                    "Run:    mvn test -Dsnapshot.generate=true -Dtest=DataFactorySnapshotTest\n" +
                    "Then commit the generated JSON files to git.");
        }

        String expected = SnapshotStore.read(fixtureName);
        if (!actual.equals(expected)) {
            String diff = SnapshotDiff.diff(expected, actual);
            fail("Snapshot mismatch: " + fixtureName + "\n\n" +
                    "What changed:\n" + diff + "\n\n" +
                    "If intentional:\n" +
                    "  1. Delete: src/test/resources/snapshots/" + fixtureName + ".json\n" +
                    "  2. Run:    mvn test -Dsnapshot.generate=true -Dtest=DataFactorySnapshotTest\n" +
                    "  3. Commit the new snapshot file");
        }
    }

    private List<?> safeInvalidList(Class<?> fixture) {
        try {
            return DataFactory.of(fixture).seed(SEED).invalidList();
        } catch (Exception e) {
            return List.of();
        }
    }

    static Stream<Named<Class<?>>> allFixtures() {
        return Stream.of(
                        packageOf(PrimitivesRecord.class),          // type/
                        packageOf(NullRecord.class),                // annotation/
                        packageOf(UserRegistrationRecord.class),    // assembly/
                        packageOf(ListEnumShapingAllArgs.class),    // partial/
                        packageOf(SetType.class)                    // unsupported/
                )
                .flatMap(List::stream)
        .map(c -> Named.named(category(c) + " | " + c.getSimpleName(), c));
    }

    /** Extracts the fixture package category — type, annotation, assembly, partial, unsupported */
    private static String category(Class<?> c) {
        String pkg = c.getPackageName();
        return pkg.substring(pkg.lastIndexOf('.') + 1).toUpperCase();
    }

    /**
     * Loads all classes from the same package as the anchor class.
     * No string package names — the anchor class IS the package reference.
     */
    static List<Class<?>> packageOf(Class<?> anchor) {
        String packageName = anchor.getPackageName();
        String path        = packageName.replace('.', '/');
        List<Class<?>> classes = new ArrayList<>();
        try {
            URL url = anchor.getClassLoader().getResource(path);
            if (url == null) return classes;
            File dir = new File(url.toURI());
            File[] files = dir.listFiles();
            if (files == null) return classes;
            for (File file : files) {
                if (!file.getName().endsWith(".class")) continue;
                if (file.getName().contains("$")) continue;
                String className = packageName + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        } catch (URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load package: " + packageName, e);
        }
        return classes;
    }
}