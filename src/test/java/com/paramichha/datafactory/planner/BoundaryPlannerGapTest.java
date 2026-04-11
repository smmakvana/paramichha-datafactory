package com.paramichha.datafactory.planner;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Targets the 1 remaining missing branch in BoundaryPlanner:
 * planByBounds max-only path label check (justBelowMax == atMax dedup).
 */
@DisplayName("BoundaryPlanner — coverage gap")
class BoundaryPlannerGapTest {

    @Test
    void maxOnly_adjacent_deduplication() {
        // max=1: justBelowMax(0) and atMax(1) are distinct — 3 targets
        var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Integer", List.of("@Max(1)"));
        var targets = BoundaryPlanner.plan(fc);
        assertThat(targets.stream().map(BoundaryTarget::label).toList())
                .containsExactly("semantic", "justBelowMax", "atMax");
    }

    @Test
    void maxOnly_zero_justBelowMax_negative() {
        // max=0: justBelowMax(-1) and atMax(0) — both distinct
        var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Integer", List.of("@Max(0)"));
        var targets = BoundaryPlanner.plan(fc);
        var labels = targets.stream().map(BoundaryTarget::label).toList();
        assertThat(labels).contains("justBelowMax", "atMax");
    }
}
