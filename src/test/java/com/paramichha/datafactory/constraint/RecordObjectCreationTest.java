package com.paramichha.datafactory.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.instantiation.RecordObjectCreation;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Record creation: null primitive components take their default values. */
@DisplayName("Record creation")
class RecordObjectCreationTest {

    @Nested
    @DisplayName("RecordStrategy — all defaultPrimitive arms")
    class NullPrimitivesGetDefaults {

        @Test
        void nullPrimitivesUseDefaults() throws Exception {
            var fields = FieldDescriptor.extract(AllPrimRec.class);
            var values = new LinkedHashMap<String, Object>();
            fields.forEach(fd -> values.put(fd.name(), null));
            var r = RecordObjectCreation.INSTANCE.create(AllPrimRec.class, fields, values);
            assertThat(r.b()).isFalse();
            assertThat(r.c()).isEqualTo('\0');
            assertThat(r.by()).isEqualTo((byte) 0);
            assertThat(r.s()).isEqualTo((short) 0);
            assertThat(r.i()).isEqualTo(0);
            assertThat(r.l()).isEqualTo(0L);
            assertThat(r.f()).isEqualTo(0.0f);
            assertThat(r.d()).isEqualTo(0.0d);
        }

        record AllPrimRec(boolean b, char c, byte by, short s, int i, long l, float f, double d) {}
    }

    @Nested
    @DisplayName("RecordStrategy — defaultPrimitive double arm")
    class NullDoubleComponents {

        @Test
        void nullDoubleAndFloatComponentsUseZero() throws Exception {
            var fields = FieldDescriptor.extract(WithDouble.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("d", null);
            values.put("f", null);
            var result = RecordObjectCreation.INSTANCE.create(WithDouble.class, fields, values);
            assertThat(result.d()).isEqualTo(0.0d);
            assertThat(result.f()).isEqualTo(0.0f);
        }

        record WithDouble(double d, float f) {}
    }

    @Nested
    @DisplayName("RecordStrategy — defaultPrimitive double arm")
    class NullDoubleComponent {}
}
