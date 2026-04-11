package com.paramichha.datafactory.analyzer;

/**
 * Classifies annotation strings from YAML config,
 * e.g. {@code "@NotBlank"}, {@code "@Min(18)"}, {@code "@Size(min=2, max=50)"}.
 */
final class StringAnnotationClassifier implements AnnotationClassifier {

    public static final StringAnnotationClassifier INSTANCE = new StringAnnotationClassifier();

    private StringAnnotationClassifier() {
    }

    private static String s(Object o) {
        return (String) o;
    }

    private static boolean has(String s, String token) {
        return s.contains(token);
    }

    private static Long extractLong(String s, String prefix) {
        int start = s.indexOf(prefix);
        if (start < 0) return null;
        start += prefix.length();
        int end = start;
        if (end < s.length() && s.charAt(end) == '-') end++;
        while (end < s.length() && Character.isDigit(s.charAt(end))) end++;
        try {
            return Long.parseLong(s.substring(start, end));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Long extractNamedLong(String s, String param) {
        int idx = s.indexOf(param);
        if (idx < 0) return null;
        return extractLong(s.substring(idx), param);
    }

    @Override
    public boolean isNotNull(Object a) {
        return has(s(a), "NotNull");
    }

    @Override
    public boolean isNotBlank(Object a) {
        return has(s(a), "NotBlank");
    }

    @Override
    public boolean isNotEmpty(Object a) {
        return has(s(a), "NotEmpty");
    }

    @Override
    public boolean isNull(Object a) {
        return has(s(a), "Null") && !has(s(a), "NotNull");
    }

    @Override
    public boolean isValid(Object a) {
        return has(s(a), "Valid");
    }

    @Override
    public FormatType classifyFormat(Object raw) {
        String s = s(raw);
        if (has(s, "Email")) return FormatType.EMAIL;
        if (has(s, "URL")) return FormatType.URL;
        if (has(s, "Pattern")) return FormatType.PATTERN;
        if (has(s, "CreditCardNumber")) return FormatType.CREDIT_CARD;
        if (has(s, "ISBN")) return FormatType.ISBN;
        if (has(s, "EAN")) return FormatType.EAN;
        return FormatType.NONE;
    }

    @Override
    public String extractPatternRegexp(Object raw) {
        String s = s(raw);
        int idx = s.indexOf("regexp");
        if (idx < 0) return null;
        int q1 = s.indexOf('"', idx);
        if (q1 < 0) return null;
        int q2 = s.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return s.substring(q1 + 1, q2).replace("\\\\", "\\");
    }

    @Override
    public QuantityBounds classifyQuantity(Object raw) {
        String s = s(raw);
        if (has(s, "PositiveOrZero")) return QuantityBounds.atLeast(0L);
        if (has(s, "Positive") && !has(s, "PositiveOrZero")) return QuantityBounds.atLeast(1L);
        if (has(s, "NegativeOrZero")) return QuantityBounds.atMost(0L);
        if (has(s, "Negative") && !has(s, "NegativeOrZero")) return QuantityBounds.atMost(-1L);
        if (has(s, "Min(")) {
            Long v = extractLong(s, "Min(");
            if (v != null) return QuantityBounds.atLeast(v);
        }
        if (has(s, "Max(")) {
            Long v = extractLong(s, "Max(");
            if (v != null) return QuantityBounds.atMost(v);
        }
        if (has(s, "Size") || has(s, "Length")) {
            Long min = extractNamedLong(s, "min=");
            Long max = extractNamedLong(s, "max=");
            if (min != null && min == 0) min = null;
            if (max != null && max == Integer.MAX_VALUE) max = null;
            if (min != null || max != null) return new QuantityBounds(min, max);
        }
        return null;
    }

    @Override
    public int extractIntegerDigits(Object raw) {
        Long v = extractNamedLong(s(raw), "integer=");
        return v != null ? v.intValue() : -1;
    }

    @Override
    public int extractFractionDigits(Object raw) {
        Long v = extractNamedLong(s(raw), "fraction=");
        return v != null ? v.intValue() : -1;
    }

    @Override
    public TemporalDirection classifyTemporal(Object raw) {
        String s = s(raw);
        if (has(s, "PastOrPresent")) return TemporalDirection.PAST_OR_NOW;
        if (has(s, "Past")) return TemporalDirection.PAST;
        if (has(s, "FutureOrPresent")) return TemporalDirection.FUTURE_OR_NOW;
        if (has(s, "Future")) return TemporalDirection.FUTURE;
        return TemporalDirection.NONE;
    }

    @Override
    public boolean isAssertTrue(Object a) {
        return has(s(a), "AssertTrue");
    }

    @Override
    public boolean isAssertFalse(Object a) {
        return has(s(a), "AssertFalse");
    }
}
