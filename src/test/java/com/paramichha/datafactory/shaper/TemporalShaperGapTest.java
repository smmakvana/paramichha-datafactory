package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers TemporalShaper NONE direction for Date, Year, YearMonth
 * and PAST_OR_NOW / FUTURE_OR_NOW for all types not yet exercised.
 */
@DisplayName("TemporalShaper — missing direction branches")
class TemporalShaperGapTest {

    private static Object shape(String type, String annotation) {
        var fc = AnnotationAnalyzer.analyzeFromStrings("f", type, List.of(annotation));
        return TemporalShaper.shape(fc, BoundaryTarget.semantic());
    }

    private static Object shapeNone(String type) {
        var fc = AnnotationAnalyzer.analyzeFromStrings("f", type, List.of());
        return TemporalShaper.shape(fc, BoundaryTarget.semantic());
    }

    // NONE direction — default arm for Date, Year, YearMonth
    @Test
    void date_none() {
        assertThat(shapeNone("Date")).isInstanceOf(Date.class);
    }

    @Test
    void year_none() {
        assertThat(shapeNone("Year")).isInstanceOf(Year.class);
    }

    @Test
    void yearMonth_none() {
        assertThat(shapeNone("YearMonth")).isInstanceOf(YearMonth.class);
    }

    // PAST_OR_NOW — hits PAST arm in switch (same case label)
    @Test
    void date_pastOrNow() {
        assertThat(shape("Date", "@PastOrPresent")).isInstanceOf(Date.class);
        assertThat(((Date) shape("Date", "@PastOrPresent")).before(new Date(System.currentTimeMillis() + 1000))).isTrue();
    }

    @Test
    void year_pastOrNow() {
        assertThat(shape("Year", "@PastOrPresent")).isInstanceOf(Year.class);
        assertThat((Year) shape("Year", "@PastOrPresent")).isLessThanOrEqualTo(Year.now());
    }

    @Test
    void yearMonth_pastOrNow() {
        assertThat(shape("YearMonth", "@PastOrPresent")).isInstanceOf(YearMonth.class);
        assertThat((YearMonth) shape("YearMonth", "@PastOrPresent")).isLessThanOrEqualTo(YearMonth.now());
    }

    // FUTURE_OR_NOW — hits FUTURE arm in switch
    @Test
    void date_futureOrNow() {
        assertThat(shape("Date", "@FutureOrPresent")).isInstanceOf(Date.class);
        assertThat(((Date) shape("Date", "@FutureOrPresent")).after(new Date(System.currentTimeMillis() - 1000))).isTrue();
    }

    @Test
    void year_futureOrNow() {
        assertThat(shape("Year", "@FutureOrPresent")).isInstanceOf(Year.class);
        assertThat((Year) shape("Year", "@FutureOrPresent")).isGreaterThanOrEqualTo(Year.now());
    }

    @Test
    void yearMonth_futureOrNow() {
        assertThat(shape("YearMonth", "@FutureOrPresent")).isInstanceOf(YearMonth.class);
        assertThat((YearMonth) shape("YearMonth", "@FutureOrPresent")).isGreaterThanOrEqualTo(YearMonth.now());
    }

    // Unknown type fallback — Instant.now()
    @Test
    void unknown_type_fallback() {
        var fc = AnnotationAnalyzer.analyzeFromStrings("f", "String", List.of("@Past"));
        // String field that somehow gets routed to TemporalShaper (edge case)
        // Covered by calling shape() directly with a non-temporal field type
        var result = TemporalShaper.shape(fc, BoundaryTarget.semantic());
        assertThat(result).isInstanceOf(Instant.class);
    }
}
