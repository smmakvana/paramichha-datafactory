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
                row(com.paramichha.example.logical.LogicalRecord.class),
                row(com.paramichha.example.logical.LogicalPojo.class),
                row(com.paramichha.example.logical.LogicalLombok.class),
                row(com.paramichha.example.logical.LogicalAllArgs.class),

                row(com.paramichha.example.number.NumberRecord.class),
                row(com.paramichha.example.number.NumberPojo.class),
                row(com.paramichha.example.number.NumberLombok.class),
                row(com.paramichha.example.number.NumberAllArgs.class),

                row(com.paramichha.example.decimal.DecimalRecord.class),
                row(com.paramichha.example.decimal.DecimalPojo.class),
                row(com.paramichha.example.decimal.DecimalLombok.class),
                row(com.paramichha.example.decimal.DecimalAllArgs.class),

                row(com.paramichha.example.text.TextRecord.class),
                row(com.paramichha.example.text.TextPojo.class),
                row(com.paramichha.example.text.TextLombok.class),
                row(com.paramichha.example.text.TextAllArgs.class),

                row(com.paramichha.example.identifier.IdentifierRecord.class),
                row(com.paramichha.example.identifier.IdentifierPojo.class),
                row(com.paramichha.example.identifier.IdentifierLombok.class),
                row(com.paramichha.example.identifier.IdentifierAllArgs.class),

                row(com.paramichha.example.enumtype.EnumRecord.class),
                row(com.paramichha.example.enumtype.EnumPojo.class),
                row(com.paramichha.example.enumtype.EnumLombok.class),
                row(com.paramichha.example.enumtype.EnumAllArgs.class),

                row(com.paramichha.example.date.DateRecord.class),
                row(com.paramichha.example.date.DatePojo.class),
                row(com.paramichha.example.date.DateLombok.class),
                row(com.paramichha.example.date.DateAllArgs.class),

                row(com.paramichha.example.datetime.DateTimeRecord.class),
                row(com.paramichha.example.datetime.DateTimePojo.class),
                row(com.paramichha.example.datetime.DateTimeLombok.class),
                row(com.paramichha.example.datetime.DateTimeAllArgs.class),

                row(com.paramichha.example.timepart.TimePartRecord.class),
                row(com.paramichha.example.timepart.TimePartPojo.class),
                row(com.paramichha.example.timepart.TimePartLombok.class),
                row(com.paramichha.example.timepart.TimePartAllArgs.class),

                row(com.paramichha.example.optional.OptionalRecord.class),
                row(com.paramichha.example.optional.OptionalPojo.class),
                row(com.paramichha.example.optional.OptionalLombok.class),
                row(com.paramichha.example.optional.OptionalAllArgs.class),

                row(com.paramichha.example.nested.NestedRecord.class),
                row(com.paramichha.example.nested.NestedPojo.class),
                row(com.paramichha.example.nested.NestedLombok.class),
                row(com.paramichha.example.nested.NestedAllArgs.class),

                // ── annotation packages ───────────────────────────────────
                row(com.paramichha.example.annotation.notnull.NotNullRecord.class),
                row(com.paramichha.example.annotation.notnull.NotNullPojo.class),
                row(com.paramichha.example.annotation.notnull.NotNullLombok.class),
                row(com.paramichha.example.annotation.notnull.NotNullAllArgs.class),

                row(com.paramichha.example.annotation.notblank.NotBlankRecord.class),
                row(com.paramichha.example.annotation.notblank.NotBlankPojo.class),
                row(com.paramichha.example.annotation.notblank.NotBlankLombok.class),
                row(com.paramichha.example.annotation.notblank.NotBlankAllArgs.class),

                row(com.paramichha.example.annotation.notempty.NotEmptyRecord.class),
                row(com.paramichha.example.annotation.notempty.NotEmptyPojo.class),
                row(com.paramichha.example.annotation.notempty.NotEmptyLombok.class),
                row(com.paramichha.example.annotation.notempty.NotEmptyAllArgs.class),

                row(com.paramichha.example.annotation.size.SizeRecord.class),
                row(com.paramichha.example.annotation.size.SizePojo.class),
                row(com.paramichha.example.annotation.size.SizeLombok.class),
                row(com.paramichha.example.annotation.size.SizeAllArgs.class),

                row(com.paramichha.example.annotation.email.EmailRecord.class),
                row(com.paramichha.example.annotation.email.EmailPojo.class),
                row(com.paramichha.example.annotation.email.EmailLombok.class),
                row(com.paramichha.example.annotation.email.EmailAllArgs.class),

                row(com.paramichha.example.annotation.url.UrlRecord.class),
                row(com.paramichha.example.annotation.url.UrlPojo.class),
                row(com.paramichha.example.annotation.url.UrlLombok.class),
                row(com.paramichha.example.annotation.url.UrlAllArgs.class),

                row(com.paramichha.example.annotation.pattern.PatternRecord.class),
                row(com.paramichha.example.annotation.pattern.PatternPojo.class),
                row(com.paramichha.example.annotation.pattern.PatternLombok.class),
                row(com.paramichha.example.annotation.pattern.PatternAllArgs.class),

                row(com.paramichha.example.annotation.numeric.NumericConstraintRecord.class),
                row(com.paramichha.example.annotation.numeric.NumericConstraintPojo.class),
                row(com.paramichha.example.annotation.numeric.NumericConstraintLombok.class),
                row(com.paramichha.example.annotation.numeric.NumericConstraintAllArgs.class),

                row(com.paramichha.example.annotation.decimal.DecimalConstraintRecord.class),
                row(com.paramichha.example.annotation.decimal.DecimalConstraintPojo.class),
                row(com.paramichha.example.annotation.decimal.DecimalConstraintLombok.class),
                row(com.paramichha.example.annotation.decimal.DecimalConstraintAllArgs.class),

                row(com.paramichha.example.annotation.temporal.TemporalConstraintRecord.class),
                row(com.paramichha.example.annotation.temporal.TemporalConstraintPojo.class),
                row(com.paramichha.example.annotation.temporal.TemporalConstraintLombok.class),
                row(com.paramichha.example.annotation.temporal.TemporalConstraintAllArgs.class),

                row(com.paramichha.example.annotation.creditcard.CreditCardRecord.class),
                row(com.paramichha.example.annotation.creditcard.CreditCardPojo.class),
                row(com.paramichha.example.annotation.creditcard.CreditCardLombok.class),
                row(com.paramichha.example.annotation.creditcard.CreditCardAllArgs.class),

                row(com.paramichha.example.annotation.isbn.IsbnRecord.class),
                row(com.paramichha.example.annotation.isbn.IsbnPojo.class),
                row(com.paramichha.example.annotation.isbn.IsbnLombok.class),
                row(com.paramichha.example.annotation.isbn.IsbnAllArgs.class),

                row(com.paramichha.example.annotation.ean.EanRecord.class),
                row(com.paramichha.example.annotation.ean.EanPojo.class),
                row(com.paramichha.example.annotation.ean.EanLombok.class),
                row(com.paramichha.example.annotation.ean.EanAllArgs.class),

                row(com.paramichha.example.annotation.assertbool.AssertBoolRecord.class),
                row(com.paramichha.example.annotation.assertbool.AssertBoolPojo.class),
                row(com.paramichha.example.annotation.assertbool.AssertBoolLombok.class),
                row(com.paramichha.example.annotation.assertbool.AssertBoolAllArgs.class),

                row(com.paramichha.example.annotation.combined.CombinedRecord.class),
                row(com.paramichha.example.annotation.combined.CombinedPojo.class),
                row(com.paramichha.example.annotation.combined.CombinedLombok.class),
                row(com.paramichha.example.annotation.combined.CombinedAllArgs.class),

                // ── collection/list ───────────────────────────────────────
                row(com.paramichha.example.collection.list.ListRecord.class),
                row(com.paramichha.example.collection.list.ListPojo.class),
                row(com.paramichha.example.collection.list.ListLombok.class),
                row(com.paramichha.example.collection.list.ListAllArgs.class),

                // ── collection/set ────────────────────────────────────────
                row(com.paramichha.example.collection.set.SetRecord.class),
                row(com.paramichha.example.collection.set.SetPojo.class),
                row(com.paramichha.example.collection.set.SetLombok.class),
                row(com.paramichha.example.collection.set.SetAllArgs.class),

                // ── collection/map ────────────────────────────────────────
                row(com.paramichha.example.collection.map.MapRecord.class),
                row(com.paramichha.example.collection.map.MapPojo.class),
                row(com.paramichha.example.collection.map.MapLombok.class),
                row(com.paramichha.example.collection.map.MapAllArgs.class),

                // ── collection/array ──────────────────────────────────────
                row(com.paramichha.example.collection.array.ArrayRecord.class),
                row(com.paramichha.example.collection.array.ArrayPojo.class),
                row(com.paramichha.example.collection.array.ArrayLombok.class),
                row(com.paramichha.example.collection.array.ArrayAllArgs.class),

                // ── collection/queue ──────────────────────────────────────
                row(com.paramichha.example.collection.queue.QueueRecord.class),
                row(com.paramichha.example.collection.queue.QueuePojo.class),
                row(com.paramichha.example.collection.queue.QueueLombok.class),
                row(com.paramichha.example.collection.queue.QueueAllArgs.class),

                // ── realworld ─────────────────────────────────────────────
                row(com.paramichha.example.realworld.registration.UserRegistration.class),
                row(com.paramichha.example.realworld.registration.UserRegistrationMinimal.class),
                row(com.paramichha.example.realworld.registration.UserRegistrationFull.class),
                row(com.paramichha.example.realworld.payment.Payment.class),
                row(com.paramichha.example.realworld.payment.PaymentDomestic.class),
                row(com.paramichha.example.realworld.payment.PaymentInternational.class),
                row(com.paramichha.example.realworld.address.PostalAddress.class),
                row(com.paramichha.example.realworld.address.PostalAddressPojo.class),
                row(com.paramichha.example.realworld.trading.Trade.class),
                row(com.paramichha.example.realworld.trading.TradeWithLegs.class)
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static Arguments row(Class<?> type) {
        return arguments(type.getSimpleName(), type);
    }

    @SuppressWarnings("unchecked")
    private String snapshot(Class<?> type) {
        Object valid = DataFactory.of(type).seed(SEED).valid();
        Map<String, List<?>> perField = (Map) DataFactory.of(type).seed(SEED).validPerField();
        List<?> invalids = safeInvalidList(type);
        return SnapshotWriter.toJson(valid, perField, invalids, VALIDATOR);
    }

    private List<?> safeInvalidList(Class<?> type) {
        try {
            return DataFactory.of(type).seed(SEED).invalidList();
        } catch (Exception e) {
            return List.of();
        }
    }
}