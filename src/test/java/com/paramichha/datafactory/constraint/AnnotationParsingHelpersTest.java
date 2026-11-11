package com.paramichha.datafactory.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.annotation.AnnotationProxyFactory;
import com.paramichha.datafactory.annotation.AnnotationUtils;
import com.paramichha.datafactory.utils.PrimitiveDefaults;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Annotation parsing and proxy helpers. */
@DisplayName("Annotation parsing and proxy helpers")
class AnnotationParsingHelpersTest {

    @Test
    @DisplayName("a primitive-default lookup for String gives null")
    void stringDefaultIsNull() {
        assertThat(PrimitiveDefaults.of(String.class)).isNull();
    }

    @Test
    @DisplayName("an unquoted numeric argument such as @Min(55) parses as a number")
    void unquotedNumberParses() {
        // "55" starts and ends with the same character but is not quoted
        assertThat(((Min) AnnotationUtils.parse("@Min(55)")).value()).isEqualTo(55L);
    }

    @Test
    @DisplayName("a proxy annotation equals only itself")
    void proxyEqualsOnlyItself() {
        Min min = AnnotationProxyFactory.createMin(3);
        // List.indexOf calls equals; AssertJ checks identity first
        assertThat(List.of(min).indexOf(min)).isZero();
        assertThat(min).isNotEqualTo(AnnotationProxyFactory.createMin(3));
    }
}
