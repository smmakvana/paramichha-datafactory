package com.paramichha.datafactory.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.builder.StringAnnotationParser;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Annotation strings and annotation proxies. */
class AnnotationHelpersTest {

    @Test
    void utilsParseAndValidate() {
        assertThat(AnnotationUtils.isValid("@Size(min=1, max=3)")).isTrue();
        assertThat(AnnotationUtils.isValid("@NoSuchThing(")).isFalse();
        assertThat(AnnotationUtils.isValid(null)).isFalse();
        assertThat(AnnotationUtils.isValid("NotBlank")).isFalse();
        assertThat(AnnotationUtils.parse("@NotBlank")).isInstanceOf(NotBlank.class);
        assertThat(AnnotationUtils.parseAll(List.of("@NotBlank", "@Size(max=4)")))
                .extracting(Annotation::annotationType)
                .containsExactly(NotBlank.class, Size.class);
    }

    @Test
    void parserParsesAListInOrder() {
        AnnotationParser parser = StringAnnotationParser.INSTANCE;
        List<Annotation> parsed = parser.parseAll(List.of("@Min(1)", "@Max(9)"));
        assertThat(((Min) parsed.get(0)).value()).isEqualTo(1);
        assertThat(((Max) parsed.get(1)).value()).isEqualTo(9);
    }

    @Test
    void proxiesBehaveLikeAnnotations() {
        Min min = AnnotationProxyFactory.createMin(3);
        assertThat(min.value()).isEqualTo(3);
        assertThat(min.annotationType()).isEqualTo(Min.class);
        assertThat(min.message()).isEqualTo("{jakarta.validation.constraints.Min.message}");
        assertThat(min.toString()).startsWith("@Min");
        assertThat(min.hashCode()).isEqualTo(Map.of("value", 3L).hashCode());
        assertThat(AnnotationProxyFactory.createMax(7).value()).isEqualTo(7);
        DecimalMin dmin = AnnotationProxyFactory.createDecimalMin("0.5");
        assertThat(dmin.value()).isEqualTo("0.5");
        assertThat(dmin.inclusive()).isTrue();
        DecimalMax dmax = AnnotationProxyFactory.createDecimalMax("9.5");
        assertThat(dmax.value()).isEqualTo("9.5");
        assertThat(AnnotationProxyFactory.create(NotBlank.class)).isInstanceOf(NotBlank.class);
    }
}
