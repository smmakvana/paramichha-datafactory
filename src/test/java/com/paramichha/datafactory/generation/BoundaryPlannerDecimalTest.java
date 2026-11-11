package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.NumericBounds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Decimal bounds in the boundary planner: equal bounds plan one target; bounds on a collection are ignored. */
@DisplayName("BoundaryPlanner — decimal bounds")
class BoundaryPlannerDecimalTest {

    @Test
    @DisplayName("equal decimal bounds plan a single target")
    void equalBoundsPlanOneTarget() {
        BigDecimal five = new BigDecimal("5");
        FieldConstraints pinned = FieldConstraints.builder("rate", BigDecimal.class)
                .bounds(new NumericBounds.DecimalBounds(five, five)).build();
        assertThat(BoundaryPlanner.plan(pinned)).containsExactly(BoundaryTarget.atDecimalMin(five));
    }

    @Test
    @DisplayName("decimal bounds on a collection are ignored")
    void boundsOnACollectionAreIgnored() {
        BigDecimal five = new BigDecimal("5");
        FieldConstraints list = FieldConstraints.builder("items", List.class)
                .bounds(new NumericBounds.DecimalBounds(five, null)).build();
        assertThat(BoundaryPlanner.plan(list)).containsExactly(BoundaryTarget.semantic());
    }
}
