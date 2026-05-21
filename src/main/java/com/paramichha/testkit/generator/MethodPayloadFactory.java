package com.paramichha.testkit.generator;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.testkit.ReflectionClassReader;
import com.paramichha.testkit.model.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link MethodPayload} from a {@link TestSourceContext} and one {@link MethodInfo}.
 *
 * This is the ONLY place DataFactory is called for method test generation.
 * Everything it produces is a plain string — no DataFactory below this point.
 */
final class MethodPayloadFactory {

    private MethodPayloadFactory() {}

    static MethodPayload build(TestSourceContext ctx, MethodInfo method) {
        String className  = ctx.classInfo.simpleName();
        String methodName = method.name();
        String fieldName  = ctx.classInfo.utilityClass()
                ? className : ctx.instanceName;

        String nestedClassName = ctx.template.resolveMethod(
                ctx.template.methodClassName, className, methodName);
        String displayName     = ctx.template.resolveMethod(
                ctx.template.methodDisplay, className, methodName);
        String validSrc        = ctx.template.resolveMethod(
                ctx.template.validInputsSource, className, methodName);
        String invalidSrc      = ctx.template.resolveMethod(
                ctx.template.invalidInputsSource, className, methodName);

        ReturnTypeInfo ret            = method.returnType();
        String         returnTypeName = JavaTypeNames.of(ret.type());
        String         returnAssertion = buildReturnAssertion(ret, ctx.template.validatorField);
        String         wrapperRecord  = needsWrapperRecord(ret)
                ? buildWrapperRecord(methodName, ret) : null;

        List<ParameterInfo>           params     = method.parameters();
        List<MethodPayload.ParamPayload> paramPayloads = buildParamPayloads(params);
        String paramSignature = buildSignature(params);
        String paramCall      = buildCall(params);
        boolean anyConstrained = params.stream().anyMatch(ParameterInfo::isConstrained);

        // ── DataFactory calls — all happen here ───────────────────────────
        List<String> validRows        = buildValidRows(methodName, params, ctx.seed);
        List<String> invalidRows      = anyConstrained
                ? buildInvalidRows(params, ctx.seed) : List.of();
        List<String> enforcementHints = buildEnforcementHints(params);
        // ─────────────────────────────────────────────────────────────────

        return new MethodPayload(
                method.kind(), methodName, nestedClassName, displayName, fieldName,
                validSrc, invalidSrc,
                returnTypeName, ret.isVoid(), ret.isPrimitive(),
                returnAssertion, wrapperRecord,
                paramPayloads, paramSignature, paramCall,
                validRows, invalidRows, enforcementHints,
                ctx.hasJakarta, ctx.template.validatorField
        );
    }

    // ── Valid rows ────────────────────────────────────────────────────────

    private static List<String> buildValidRows(String methodName,
                                               List<ParameterInfo> params, long seed) {
        StringBuilder row = new StringBuilder();
        row.append("Arguments.of(\"valid ").append(methodName).append("\"");
        for (ParameterInfo p : params)
            row.append(",\n        ").append(validLiteral(p, seed));
        row.append(")");
        return List.of(row.toString());
    }

    // ── Invalid rows ──────────────────────────────────────────────────────

    private static List<String> buildInvalidRows(List<ParameterInfo> params, long seed) {
        List<String> rows = new ArrayList<>();
        for (int nullIdx = 0; nullIdx < params.size(); nullIdx++) {
            ParameterInfo p = params.get(nullIdx);
            if (!p.isConstrained()) continue;
            for (String inv : invalidLiterals(p)) {
                StringBuilder row = new StringBuilder();
                row.append("Arguments.of(\"invalid ").append(p.name()).append("\"");
                for (int i = 0; i < params.size(); i++) {
                    String val = i == nullIdx ? inv : validLiteral(params.get(i), seed);
                    row.append(",\n        ").append(val);
                }
                row.append(")");
                rows.add(row.toString());
            }
        }
        return List.copyOf(rows);
    }

    // ── Enforcement hints ─────────────────────────────────────────────────

    private static List<String> buildEnforcementHints(List<ParameterInfo> params) {
        List<String> hints = new ArrayList<>();
        for (ParameterInfo p : params)
            for (Annotation a : p.validations()) {
                String call = dataValidationCall(a, p.name());
                if (call != null)
                    hints.add("// Enforce @" + a.annotationType().getSimpleName()
                            + ": DataValidation." + call + ";");
            }
        return List.copyOf(hints);
    }

    // ── Return assertion ──────────────────────────────────────────────────

    private static String buildReturnAssertion(ReturnTypeInfo ret, String validatorField) {
        if (ret.isVoid()) return "";
        if (ret.isConstrained()) {
            StringBuilder sb = new StringBuilder();
            for (Annotation a : ret.annotations()) {
                if (a.annotationType().getSimpleName().equals("Valid")) {
                    sb.append("assertThat(").append(validatorField)
                            .append(".validate(result)).isEmpty();");
                } else {
                    String call = dataValidationCall(a, "result");
                    if (call != null) sb.append("DataValidation.").append(call).append(";");
                }
            }
            return sb.toString();
        }
        if (typeHasConstraints(ret.type()))
            return "assertThat(" + validatorField + ".validate(result)).isEmpty();";
        if (ret.isPrimitive()) return "";
        return "assertThat(result).isNotNull();";
    }

    // ── Wrapper record ────────────────────────────────────────────────────

    private static boolean needsWrapperRecord(ReturnTypeInfo ret) {
        if (ret.isVoid() || !ret.isConstrained()) return false;
        Class<?> t = ret.type();
        return t.isPrimitive() || t == String.class || t == Character.class
                || t.getPackageName().startsWith("java.")
                || t.getPackageName().startsWith("javax.");
    }

    private static String buildWrapperRecord(String methodName, ReturnTypeInfo ret) {
        String annotations = buildAnnotationSource(ret.annotations());
        String boxedType   = JavaTypeNames.boxed(ret.type());
        String name        = Character.toUpperCase(methodName.charAt(0))
                + methodName.substring(1) + "Result";
        return "private record " + name + "(" + annotations + boxedType + " value) {}";
    }

    // ── Param helpers ─────────────────────────────────────────────────────

    private static List<MethodPayload.ParamPayload> buildParamPayloads(List<ParameterInfo> params) {
        List<MethodPayload.ParamPayload> result = new ArrayList<>();
        for (ParameterInfo p : params)
            result.add(new MethodPayload.ParamPayload(
                    p.name(), JavaTypeNames.of(p.type()), p.isConstrained()));
        return List.copyOf(result);
    }

    private static String buildSignature(List<ParameterInfo> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(JavaTypeNames.of(params.get(i).type()))
                    .append(" ").append(params.get(i).name());
        }
        return sb.toString();
    }

    private static String buildCall(List<ParameterInfo> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).name());
        }
        return sb.toString();
    }

    // ── DataFactory — value generation ────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static String validLiteral(ParameterInfo param, long seed) {
        Class<?> type = param.type();
        String   pkg  = type.isPrimitive() ? "" : type.getPackageName();
        boolean isDomain = !type.isPrimitive() && !type.isEnum()
                && !pkg.startsWith("java.") && !pkg.startsWith("jakarta.")
                && !pkg.startsWith("javax.");

        if (isDomain) {
            try {
                ClassInfo paramMeta = ReflectionClassReader.INSTANCE.read(type);
                InstantiationWriter iw = new InstantiationWriter(paramMeta, seed);
                List<Object> parts = new ArrayList<>();
                for (FieldInfo f : paramMeta.fields()) {
                    List<?> vals = iw.validValuesFor(type, f);
                    parts.add(vals.isEmpty() ? null : vals.get(0));
                }
                String call = iw.constructorCall(parts);
                if (call != null && !call.startsWith("/* TODO")) return call;
            } catch (Exception ignored) {}
            return JavaTypeNames.nullCast(type);
        }

        long paramSeed = com.paramichha.utils.Seeds.stableHash(param.name()) ^ seed;
        try {
            Object val = param.validations().isEmpty()
                    ? DataFactory.of(type).seed(paramSeed).valid()
                    : DataFactory.field(param.name(), (Class<Object>) type,
                    param.validations()).seed(paramSeed).valid();
            if (val == null) return JavaTypeNames.nullCast(type);
            String lit = LiteralConverter.toLiteralOrNull(val);
            if (lit != null && !lit.startsWith("/* TODO")) return lit;
        } catch (Exception ignored) {}
        return JavaTypeNames.nullCast(type);
    }

    @SuppressWarnings("unchecked")
    private static List<String> invalidLiterals(ParameterInfo param) {
        try {
            List<?> invalids = DataFactory.field(
                    param.name(), (Class<Object>) param.type(),
                    param.validations()).invalidList();
            List<String> result = new ArrayList<>();
            for (Object inv : invalids) {
                String lit = LiteralConverter.toLiteralOrNull(inv);
                if (lit != null) result.add(lit);
            }
            if (!result.isEmpty()) return result;
        } catch (Exception ignored) {}
        if (!param.type().isPrimitive())
            return List.of(JavaTypeNames.nullCast(param.type()));
        return List.of();
    }

    // ── Annotation helpers ────────────────────────────────────────────────

    private static String dataValidationCall(Annotation a, String expr) {
        String name = a.annotationType().getSimpleName();
        try {
            return switch (name) {
                case "NotNull"        -> "notNull(" + expr + ")";
                case "NotBlank"       -> "notBlank(" + expr + ")";
                case "NotEmpty"       -> "notEmpty(" + expr + ")";
                case "Positive"       -> "positive(" + expr + ")";
                case "PositiveOrZero" -> "positiveOrZero(" + expr + ")";
                case "Negative"       -> "negative(" + expr + ")";
                case "NegativeOrZero" -> "negativeOrZero(" + expr + ")";
                case "Min"            -> "min(" + longAttr(a, "value") + ", " + expr + ")";
                case "Max"            -> "max(" + longAttr(a, "value") + ", " + expr + ")";
                case "DecimalMin"     -> "decimalMin(\"" + stringAttr(a, "value") + "\", " + expr + ")";
                case "DecimalMax"     -> "decimalMax(\"" + stringAttr(a, "value") + "\", " + expr + ")";
                case "Size"           -> "size(" + intAttr(a, "min") + ", " + intAttr(a, "max") + ", " + expr + ")";
                case "Pattern"        -> "pattern(\"" + stringAttr(a, "regexp") + "\", " + expr + ")";
                default               -> null;
            };
        } catch (Exception ignored) { return null; }
    }

    private static String buildAnnotationSource(List<Annotation> annotations) {
        if (annotations.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Annotation a : annotations) sb.append(annotationToSource(a)).append(" ");
        return sb.toString();
    }

    private static String annotationToSource(Annotation a) {
        String name    = a.annotationType().getSimpleName();
        Method[] attrs = a.annotationType().getDeclaredMethods();
        if (attrs.length == 0) return "@" + name;
        if (attrs.length == 1 && attrs[0].getName().equals("value")) {
            try {
                Object val = attrs[0].invoke(a);
                String src = attrToSource(val);
                if (src == null) return "@" + name;
                if (src.endsWith("L")) src = src.substring(0, src.length() - 1);
                return "@" + name + "(" + src + ")";
            } catch (Exception ignored) {}
        }
        StringBuilder sb = new StringBuilder("@").append(name).append("(");
        boolean first = true;
        for (Method m : attrs) {
            try {
                Object val = m.invoke(a); Object def = m.getDefaultValue();
                if (def != null && def.equals(val)) continue;
                String src = attrToSource(val);
                if (src == null) continue;
                if (!first) sb.append(", ");
                sb.append(m.getName()).append("=").append(src);
                first = false;
            } catch (Exception ignored) {}
        }
        sb.append(")");
        return sb.toString().equals("@" + name + "()") ? "@" + name : sb.toString();
    }

    private static String attrToSource(Object val) {
        if (val instanceof String s)   return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        if (val instanceof Long l)     return l + "L";
        if (val instanceof Class<?> c) return c.getSimpleName() + ".class";
        if (val instanceof Annotation ann) return annotationToSource(ann);
        if (val != null && val.getClass().isArray()) return null;
        return val == null ? null : String.valueOf(val);
    }

    private static boolean typeHasConstraints(Class<?> type) {
        if (type == null || type.isPrimitive() || type.getPackageName().startsWith("java.")
                || type.isEnum()) return false;
        for (java.lang.reflect.Field f : type.getDeclaredFields())
            for (Annotation a : f.getAnnotations())
                if (isJakartaAnnotation(a)) return true;
        if (type.isRecord())
            for (java.lang.reflect.RecordComponent rc : type.getRecordComponents())
                for (Annotation a : rc.getAnnotations())
                    if (isJakartaAnnotation(a)) return true;
        return false;
    }

    private static boolean isJakartaAnnotation(Annotation a) {
        String pkg = a.annotationType().getPackageName();
        return pkg.startsWith("jakarta.validation") || pkg.startsWith("javax.validation")
                || pkg.startsWith("org.hibernate.validator");
    }

    private static long   longAttr  (Annotation a, String attr) throws Exception { return (long)   a.annotationType().getMethod(attr).invoke(a); }
    private static String stringAttr(Annotation a, String attr) throws Exception { return (String) a.annotationType().getMethod(attr).invoke(a); }
    private static int    intAttr   (Annotation a, String attr) throws Exception { return (int)    a.annotationType().getMethod(attr).invoke(a); }
}
