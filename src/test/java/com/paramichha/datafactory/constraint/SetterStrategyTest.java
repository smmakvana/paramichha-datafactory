package com.paramichha.datafactory.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.instantiation.PojoObjectCreation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Setter strategy: a field that no class in the hierarchy declares is skipped. */
@DisplayName("Setter strategy")
class SetterStrategyTest {

    @Nested
    @DisplayName("SetterStrategy — findField exits when c==null")
    class FieldMissingFromEveryClass {

        @Test
        void fieldMissingFromEveryClassIsSkipped() throws Exception {
            // "ghost" not in ChildBean or ParentBean -> loop exhausts -> c becomes Object -> exits -> null
            var fields = List.of(
                    new FieldDescriptor("childField", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("ghost", String.class, String.class, Collections.emptyList()));
            var values = new LinkedHashMap<String, Object>();
            values.put("childField", "child");
            values.put("ghost", "ignored");
            var r = PojoObjectCreation.INSTANCE.create(ChildBean.class, fields, values);
            assertThat(r.childField).isEqualTo("child");
        }

        static class ParentBean {
            String parentField;
        }

        static class ChildBean extends ParentBean {
            String childField;
        }
    }

    @Nested
    @DisplayName("SetterStrategy — findField null return")
    class UnknownFieldIsSkipped {

        @Test
        void unknownFieldIsSilentlySkipped() throws Exception {
            // "ghost" field does not exist in SimpleBean — findField returns null, skip
            var fields = List.of(
                    new FieldDescriptor("real", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("ghost", String.class, String.class, Collections.emptyList()));
            var values = new LinkedHashMap<String, Object>();
            values.put("real", "ok");
            values.put("ghost", "ignored");
            var result = PojoObjectCreation.INSTANCE.create(SimpleBean.class, fields, values);
            assertThat(result.real).isEqualTo("ok");
        }

        static class SimpleBean {
            String real;
        }
    }

    @Nested
    @DisplayName("SetterStrategy — superclass field and null-not-found")
    class SuperclassAndPrimitiveFields {

        @Test
        void fieldInSuperclassIsSet() throws Exception {
            // 'inherited' is declared in Base, not Child — findField traverses superclass
            var fields = FieldDescriptor.extract(Child.class);
            var values = new LinkedHashMap<String, Object>();
            fields.forEach(f -> values.put(f.name(), f.name().equals("localInt") ? 7 : "parent-value"));

            var result = PojoObjectCreation.INSTANCE.create(Child.class, fields, values);
            assertThat(result.inherited).isEqualTo("parent-value");
            assertThat(result.localInt).isEqualTo(7);
        }

        @Test
        void nullPrimitiveFieldIsSkipped() throws Exception {
            var fields = FieldDescriptor.extract(WithPrimitive.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("count", null); // primitive int — must be skipped
            values.put("name", "test");
            var result = PojoObjectCreation.INSTANCE.create(WithPrimitive.class, fields, values);
            assertThat(result.count).isEqualTo(0);
            assertThat(result.name).isEqualTo("test");
        }

        @Test
        void missingFieldIsSkippedGracefully() throws Exception {
            var fields = List.of(
                    new FieldDescriptor("real", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("ghost", String.class, String.class, Collections.emptyList()));
            var values = Map.of("real", (Object) "ok", "ghost", (Object) "ignored");
            var result = PojoObjectCreation.INSTANCE.create(FieldNotFound.class, fields, values);
            assertThat(result.real).isEqualTo("ok");
        }

        static class Base {
            String inherited;
        }

        static class Child extends Base {
            int localInt;

            public void setLocalInt(int v) {
                this.localInt = v;
            }
        }

        static class WithPrimitive {
            int count;
            String name;

            public void setName(String n) {
                this.name = n;
            }
        }

        static class FieldNotFound {
            // No field matching "ghost" — findField returns null -> silently skipped
            String real;
        }
    }
}
