package com.paramichha.datafactory.snapshot;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Snapshot contract test for DataFactory.
 * Each class is run through DataFactory, serialized to JSON and either:
 * GENERATE mode  (-Dsnapshot.generate=true)  — writes file, always passes
 * COMPARE  mode  (default)                   — compares, fails on mismatch
 * To add a new class: add it to allFixtures() below.
 * To regenerate all: mvn test -Dsnapshot.generate=true -Dtest=DataFactorySnapshotTest
 */
@DisplayName("DataFactory — snapshot contract")
class DataFactorySnapshotTest {

    static final long SEED = 42L;
    static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    // ── Single class test ─────────────────────────────────────────────────

    @ParameterizedTest(name = "{0}")
    @MethodSource("allFixtures")
    void contract(String label, Class<?> type) {
        String actual = snapshot(type);

        if (SnapshotStore.isGenerateMode()) {
            SnapshotStore.write(type.getSimpleName(), actual);
            return;
        }

        if (!SnapshotStore.exists(type.getSimpleName())) {
            fail("Snapshot missing: " + type.getSimpleName() + ".json\n\n"
                    + "Run: mvn test -Dsnapshot.generate=true -Dtest=DataFactorySnapshotTest");
        }

        String expected = SnapshotStore.read(type.getSimpleName());
        if (!actual.equals(expected)) {
            fail("Snapshot mismatch: " + type.getSimpleName() + "\n\n"
                    + SnapshotDiff.diff(expected, actual) + "\n\n"
                    + "If intentional: delete the .json and regenerate.");
        }
    }

    // ── Fixture list ──────────────────────────────────────────────────────

    static Stream<Arguments> allFixtures() {
        return Stream.of(

                // ── type packages ─────────────────────────────────────────
                row(com.paramichha.datafactory.catalog.logical.LogicalRecord.class),
                row(com.paramichha.datafactory.catalog.logical.LogicalPojo.class),
                row(com.paramichha.datafactory.catalog.logical.LogicalLombok.class),
                row(com.paramichha.datafactory.catalog.logical.LogicalAllArgs.class),

                row(com.paramichha.datafactory.catalog.number.NumberRecord.class),
                row(com.paramichha.datafactory.catalog.number.NumberPojo.class),
                row(com.paramichha.datafactory.catalog.number.NumberLombok.class),
                row(com.paramichha.datafactory.catalog.number.NumberAllArgs.class),

                row(com.paramichha.datafactory.catalog.decimal.DecimalRecord.class),
                row(com.paramichha.datafactory.catalog.decimal.DecimalPojo.class),
                row(com.paramichha.datafactory.catalog.decimal.DecimalLombok.class),
                row(com.paramichha.datafactory.catalog.decimal.DecimalAllArgs.class),

                row(com.paramichha.datafactory.catalog.text.TextRecord.class),
                row(com.paramichha.datafactory.catalog.text.TextPojo.class),
                row(com.paramichha.datafactory.catalog.text.TextLombok.class),
                row(com.paramichha.datafactory.catalog.text.TextAllArgs.class),

                row(com.paramichha.datafactory.catalog.identifier.IdentifierRecord.class),
                row(com.paramichha.datafactory.catalog.identifier.IdentifierPojo.class),
                row(com.paramichha.datafactory.catalog.identifier.IdentifierLombok.class),
                row(com.paramichha.datafactory.catalog.identifier.IdentifierAllArgs.class),

                row(com.paramichha.datafactory.catalog.enumtype.EnumRecord.class),
                row(com.paramichha.datafactory.catalog.enumtype.EnumPojo.class),
                row(com.paramichha.datafactory.catalog.enumtype.EnumLombok.class),
                row(com.paramichha.datafactory.catalog.enumtype.EnumAllArgs.class),

                row(com.paramichha.datafactory.catalog.date.DateRecord.class),
                row(com.paramichha.datafactory.catalog.date.DatePojo.class),
                row(com.paramichha.datafactory.catalog.date.DateLombok.class),
                row(com.paramichha.datafactory.catalog.date.DateAllArgs.class),

                row(com.paramichha.datafactory.catalog.datetime.DateTimeRecord.class),
                row(com.paramichha.datafactory.catalog.datetime.DateTimePojo.class),
                row(com.paramichha.datafactory.catalog.datetime.DateTimeLombok.class),
                row(com.paramichha.datafactory.catalog.datetime.DateTimeAllArgs.class),

                row(com.paramichha.datafactory.catalog.timepart.TimePartRecord.class),
                row(com.paramichha.datafactory.catalog.timepart.TimePartPojo.class),
                row(com.paramichha.datafactory.catalog.timepart.TimePartLombok.class),
                row(com.paramichha.datafactory.catalog.timepart.TimePartAllArgs.class),

                row(com.paramichha.datafactory.catalog.optional.OptionalRecord.class),
                row(com.paramichha.datafactory.catalog.optional.OptionalPojo.class),
                row(com.paramichha.datafactory.catalog.optional.OptionalLombok.class),
                row(com.paramichha.datafactory.catalog.optional.OptionalAllArgs.class),

                row(com.paramichha.datafactory.catalog.nested.NestedRecord.class),
                row(com.paramichha.datafactory.catalog.nested.NestedPojo.class),
                row(com.paramichha.datafactory.catalog.nested.NestedLombok.class),
                row(com.paramichha.datafactory.catalog.nested.NestedAllArgs.class),

                // ── annotation packages ───────────────────────────────────
                row(com.paramichha.datafactory.catalog.annotation.notnull.NotNullRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.notnull.NotNullPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.notnull.NotNullLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.notnull.NotNullAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.notblank.NotBlankRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.notblank.NotBlankPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.notblank.NotBlankLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.notblank.NotBlankAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.notempty.NotEmptyRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.notempty.NotEmptyPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.notempty.NotEmptyLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.notempty.NotEmptyAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.size.SizeRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.size.SizePojo.class),
                row(com.paramichha.datafactory.catalog.annotation.size.SizeLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.size.SizeAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.email.EmailRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.email.EmailPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.email.EmailLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.email.EmailAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.url.UrlRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.url.UrlPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.url.UrlLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.url.UrlAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.pattern.PatternRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.pattern.PatternPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.pattern.PatternLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.pattern.PatternAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.numeric.NumericConstraintRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.numeric.NumericConstraintPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.numeric.NumericConstraintLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.numeric.NumericConstraintAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.decimal.DecimalConstraintRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.decimal.DecimalConstraintPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.decimal.DecimalConstraintLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.decimal.DecimalConstraintAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.temporal.TemporalConstraintRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.temporal.TemporalConstraintPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.temporal.TemporalConstraintLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.temporal.TemporalConstraintAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.creditcard.CreditCardRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.creditcard.CreditCardPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.creditcard.CreditCardLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.creditcard.CreditCardAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.isbn.IsbnRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.isbn.IsbnPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.isbn.IsbnLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.isbn.IsbnAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.ean.EanRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.ean.EanPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.ean.EanLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.ean.EanAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.assertbool.AssertBoolRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.assertbool.AssertBoolPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.assertbool.AssertBoolLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.assertbool.AssertBoolAllArgs.class),

                row(com.paramichha.datafactory.catalog.annotation.combined.CombinedRecord.class),
                row(com.paramichha.datafactory.catalog.annotation.combined.CombinedPojo.class),
                row(com.paramichha.datafactory.catalog.annotation.combined.CombinedLombok.class),
                row(com.paramichha.datafactory.catalog.annotation.combined.CombinedAllArgs.class),

                // ── collection/list ───────────────────────────────────────
                row(com.paramichha.datafactory.catalog.collection.list.ListRecord.class),
                row(com.paramichha.datafactory.catalog.collection.list.ListPojo.class),
                row(com.paramichha.datafactory.catalog.collection.list.ListLombok.class),
                row(com.paramichha.datafactory.catalog.collection.list.ListAllArgs.class),

                // ── collection/set ────────────────────────────────────────
                row(com.paramichha.datafactory.catalog.collection.set.SetRecord.class),
                row(com.paramichha.datafactory.catalog.collection.set.SetPojo.class),
                row(com.paramichha.datafactory.catalog.collection.set.SetLombok.class),
                row(com.paramichha.datafactory.catalog.collection.set.SetAllArgs.class),

                // ── collection/map ────────────────────────────────────────
                row(com.paramichha.datafactory.catalog.collection.map.MapRecord.class),
                row(com.paramichha.datafactory.catalog.collection.map.MapPojo.class),
                row(com.paramichha.datafactory.catalog.collection.map.MapLombok.class),
                row(com.paramichha.datafactory.catalog.collection.map.MapAllArgs.class),

                // ── collection/array ──────────────────────────────────────
                row(com.paramichha.datafactory.catalog.collection.array.ArrayRecord.class),
                row(com.paramichha.datafactory.catalog.collection.array.ArrayPojo.class),
                row(com.paramichha.datafactory.catalog.collection.array.ArrayLombok.class),
                row(com.paramichha.datafactory.catalog.collection.array.ArrayAllArgs.class),

                // ── collection/queue ──────────────────────────────────────
                row(com.paramichha.datafactory.catalog.collection.queue.QueueRecord.class),
                row(com.paramichha.datafactory.catalog.collection.queue.QueuePojo.class),
                row(com.paramichha.datafactory.catalog.collection.queue.QueueLombok.class),
                row(com.paramichha.datafactory.catalog.collection.queue.QueueAllArgs.class),

                // ── realworld ─────────────────────────────────────────────
                row(com.paramichha.datafactory.catalog.realworld.registration.UserRegistration.class),
                row(com.paramichha.datafactory.catalog.realworld.registration.UserRegistrationMinimal.class),
                row(com.paramichha.datafactory.catalog.realworld.registration.UserRegistrationFull.class),
                row(com.paramichha.datafactory.catalog.realworld.payment.Payment.class),
                row(com.paramichha.datafactory.catalog.realworld.payment.PaymentDomestic.class),
                row(com.paramichha.datafactory.catalog.realworld.payment.PaymentInternational.class),
                row(com.paramichha.datafactory.catalog.realworld.address.PostalAddress.class),
                row(com.paramichha.datafactory.catalog.realworld.address.PostalAddressPojo.class),
                row(com.paramichha.datafactory.catalog.realworld.trading.Trade.class),
                row(com.paramichha.datafactory.catalog.realworld.trading.TradeWithLegs.class)
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static Arguments row(Class<?> type) {
        return arguments(type.getSimpleName(), type);
    }

    @SuppressWarnings("unchecked")
    private String snapshot(Class<?> type) {
        Object valid = DataFactory.of(type).seed(SEED).valid().get();
        Map<String, List<?>> perField = new java.util.LinkedHashMap<>();
        DataFactory.of(type).seed(SEED).validPerField()
                .forEach((field, variants) -> perField.put(field, variants.getAll()));
        List<?> invalids = safeInvalidList(type);
        return SnapshotWriter.toJson(valid, perField, invalids, VALIDATOR);
    }

    private List<?> safeInvalidList(Class<?> type) {
        try {
            return DataFactory.of(type).seed(SEED).invalidList().getAll();
        } catch (Exception e) {
            return List.of();
        }
    }
}