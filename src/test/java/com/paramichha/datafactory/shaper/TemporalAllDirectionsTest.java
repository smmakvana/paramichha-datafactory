package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exhaustively covers every TemporalDirection arm for every temporal type.
 * Each shapeXxx() switch has 3 arms: PAST/PAST_OR_NOW, FUTURE/FUTURE_OR_NOW, default(NONE).
 * Targets the 8 remaining missing branches.
 */
@DisplayName("TemporalShaper — all directions for all types")
class TemporalAllDirectionsTest {

    private static Object shape(String type, String annotation) {
        var fc = AnnotationAnalyzer.analyzeFromStrings("f", type, List.of(annotation));
        return TemporalShaper.shape(fc, BoundaryTarget.semantic());
    }

    // ── Instant ────────────────────────────────────────────────────────────

    @Test
    void instant_future() {
        var result = (Instant) shape("Instant", "@Future");
        assertThat(result).isAfter(Instant.now().minusSeconds(1));
    }

    @Test
    void instant_pastOrNow() {
        var result = (Instant) shape("Instant", "@PastOrPresent");
        assertThat(result).isBefore(Instant.now().plusSeconds(1));
    }

    @Test
    void instant_futureOrNow() {
        var result = (Instant) shape("Instant", "@FutureOrPresent");
        assertThat(result).isAfter(Instant.now().minusSeconds(1));
    }

    // ── LocalDate ─────────────────────────────────────────────────────────

    @Test
    void localDate_future() {
        var result = (LocalDate) shape("LocalDate", "@Future");
        assertThat(result).isAfter(LocalDate.now());
    }

    @Test
    void localDate_pastOrNow() {
        var result = (LocalDate) shape("LocalDate", "@PastOrPresent");
        assertThat(result).isBeforeOrEqualTo(LocalDate.now());
    }

    @Test
    void localDate_futureOrNow() {
        var result = (LocalDate) shape("LocalDate", "@FutureOrPresent");
        assertThat(result).isAfterOrEqualTo(LocalDate.now().minusDays(1));
    }

    // ── LocalDateTime ─────────────────────────────────────────────────────

    @Test
    void localDateTime_future() {
        var result = (LocalDateTime) shape("LocalDateTime", "@Future");
        assertThat(result).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void localDateTime_pastOrNow() {
        var result = (LocalDateTime) shape("LocalDateTime", "@PastOrPresent");
        assertThat(result).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void localDateTime_futureOrNow() {
        var result = (LocalDateTime) shape("LocalDateTime", "@FutureOrPresent");
        assertThat(result).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    // ── ZonedDateTime ─────────────────────────────────────────────────────

    @Test
    void zonedDateTime_past() {
        var result = (ZonedDateTime) shape("ZonedDateTime", "@Past");
        assertThat(result).isBefore(ZonedDateTime.now().plusSeconds(1));
    }

    @Test
    void zonedDateTime_pastOrNow() {
        var result = (ZonedDateTime) shape("ZonedDateTime", "@PastOrPresent");
        assertThat(result).isBefore(ZonedDateTime.now().plusSeconds(1));
    }

    @Test
    void zonedDateTime_futureOrNow() {
        var result = (ZonedDateTime) shape("ZonedDateTime", "@FutureOrPresent");
        assertThat(result).isAfter(ZonedDateTime.now().minusSeconds(1));
    }

    // ── OffsetDateTime ────────────────────────────────────────────────────

    @Test
    void offsetDateTime_past() {
        var result = (OffsetDateTime) shape("OffsetDateTime", "@Past");
        assertThat(result).isBefore(OffsetDateTime.now().plusSeconds(1));
    }

    @Test
    void offsetDateTime_pastOrNow() {
        var result = (OffsetDateTime) shape("OffsetDateTime", "@PastOrPresent");
        assertThat(result).isBefore(OffsetDateTime.now().plusSeconds(1));
    }

    @Test
    void offsetDateTime_futureOrNow() {
        var result = (OffsetDateTime) shape("OffsetDateTime", "@FutureOrPresent");
        assertThat(result).isAfter(OffsetDateTime.now().minusSeconds(1));
    }
}
