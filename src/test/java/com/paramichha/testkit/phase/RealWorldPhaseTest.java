package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.realworld.registration.RealWorldRegistration;
import com.paramichha.testkit.fixture.realworld.registration.RealWorldRegistrationMinimal;
import com.paramichha.testkit.fixture.realworld.registration.RealWorldRegistrationFull;
import com.paramichha.testkit.fixture.realworld.payment.RealWorldPayment;
import com.paramichha.testkit.fixture.realworld.payment.RealWorldPaymentDomestic;
import com.paramichha.testkit.fixture.realworld.payment.RealWorldPaymentInternational;
import com.paramichha.testkit.fixture.realworld.trading.RealWorldTrade;
import com.paramichha.testkit.fixture.realworld.trading.RealWorldTradeWithLegs;

@DisplayName("Phase 12 — Real world")
class RealWorldPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            RealWorldRegistration.class,
            RealWorldRegistrationMinimal.class,
            RealWorldRegistrationFull.class,
            RealWorldPayment.class,
            RealWorldPaymentDomestic.class,
            RealWorldPaymentInternational.class,
            RealWorldTrade.class,
            RealWorldTradeWithLegs.class
        ).writeTo(tmp);
        assertThat(written).hasSize(8);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(RealWorldRegistration.class).generate().asString(), "RealWorldRegistration");
        assertStructure(TestKit.of(RealWorldRegistrationMinimal.class).generate().asString(), "RealWorldRegistrationMinimal");
        assertStructure(TestKit.of(RealWorldRegistrationFull.class).generate().asString(), "RealWorldRegistrationFull");
        assertStructure(TestKit.of(RealWorldPayment.class).generate().asString(), "RealWorldPayment");
        assertStructure(TestKit.of(RealWorldPaymentDomestic.class).generate().asString(), "RealWorldPaymentDomestic");
        assertStructure(TestKit.of(RealWorldPaymentInternational.class).generate().asString(), "RealWorldPaymentInternational");
        assertStructure(TestKit.of(RealWorldTrade.class).generate().asString(), "RealWorldTrade");
        assertStructure(TestKit.of(RealWorldTradeWithLegs.class).generate().asString(), "RealWorldTradeWithLegs");
    }

    private static void assertStructure(String src, String className) {
        // Accept either a full generated test or a placeholder comment file
        boolean isFull = src.contains("class " + className + "Test")
                && src.contains("class SmokeTest");
        boolean isPlaceholder = src.contains("TODO: " + className + "Test");
        assertThat(isFull || isPlaceholder)
                .as("Expected full test or placeholder for %s", className)
                .isTrue();
    }
}
