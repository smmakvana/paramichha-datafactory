package com.paramichha.datafactory.generation;


import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.FormatType;
import com.paramichha.datafactory.constraint.QuantityBounds;

/**
 * Produces realistic strings shaped to hit a {@link BoundaryTarget}.
 *
 * <p>Format-aware fitting:
 * <ul>
 *   <li>EMAIL  — pads/trims the local part; extends subdomain if local would exceed RFC 5321's 64-char limit
 *   <li>URL    — pads/trims the path component
 *   <li>NONE   — pads/trims at the end
 * </ul>
 */
public final class StringShaper {

    private StringShaper() {
    }

    /** Convenience overload — uses a random Faker. Used by tests and internal callers. */
    static String shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    static String shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        String base = baseValue(field, faker);

        if (!(target instanceof BoundaryTarget.Fixed)) {
            return fitToBounds(base, field, field.format(), faker);
        }

        long targetLength = ((BoundaryTarget.Fixed) target).value();
        return switch (field.format()) {
            case EMAIL -> fitEmail(base, targetLength);
            case URL -> fitUrl(base, targetLength);
            case PATTERN -> base;
            default -> fitPlain(base, targetLength);
        };
    }

    private static String baseValue(FieldConstraints field, net.datafaker.Faker faker) {
        // @Digits on String — generate a purely numeric string of the right length
        if (field.maxIntegerDigits() < Integer.MAX_VALUE) {
            int intPart = Math.min(field.maxIntegerDigits(), 6);
            int fracPart = field.maxFractionDigits() < Integer.MAX_VALUE ? field.maxFractionDigits() : 0;
            String digits = faker.number().digits(Math.max(1, intPart));
            return fracPart > 0 ? digits + "." + faker.number().digits(fracPart) : digits;
        }
        // @DecimalMin/@DecimalMax on String — generate a conforming decimal string
        java.math.BigDecimal decMin = field.bounds() instanceof com.paramichha.datafactory.constraint.QuantityBounds.DecimalBounds db
                ? db.decMin() : null;
        java.math.BigDecimal decMax = field.bounds() instanceof com.paramichha.datafactory.constraint.QuantityBounds.DecimalBounds db2
                ? db2.decMax() : null;
        if (decMin != null || decMax != null) {
            java.math.BigDecimal val = decMin != null ? decMin.add(java.math.BigDecimal.ONE)
                    : decMax.subtract(java.math.BigDecimal.ONE);
            return val.toPlainString();
        }
        return switch (field.format()) {
            case EMAIL -> faker.internet().emailAddress();
            case URL -> faker.internet().url();
            case CREDIT_CARD -> luhnValidCard(faker);
            case ISBN -> faker.code().isbn13();
            case EAN -> faker.code().ean13();
            case UUID_STRING -> faker.internet().uuid();
            case PATTERN -> generateFromPattern(field.patternRegexp());
            default -> semanticByName(field.fieldName(), faker);
        };
    }

    /**
     * Generates a string that satisfies a common @Pattern regexp.
     * Handles: anchors ^$, character classes [A-Z] [a-z] [0-9] \d \w,
     * quantifiers {n} {n,m} * + ?, literals, and alternation.
     * Falls back to semantic name matching for complex patterns.
     */
    static String generateFromPattern(String regexp) {
        if (regexp == null || regexp.isEmpty()) return "A";
        try {
            // strip anchors
            String r = regexp;
            if (r.startsWith("^")) r = r.substring(1);
            if (r.endsWith("$"))  r = r.substring(0, r.length() - 1);

            // handle simple alternation: (AB|CD) or just AB|CD
            if (r.contains("|")) {
                String[] parts = r.replace("(","").replace(")","").split("[|]");
                r = parts[0].trim();
            }

            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < r.length()) {
                char ch = r.charAt(i);

                // character class [...]
                if (ch == '[') {
                    int end = r.indexOf(']', i);
                    if (end < 0) { sb.append('A'); i++; continue; }
                    String cls = r.substring(i + 1, end);
                    boolean negate = cls.startsWith("^");
                    if (negate) cls = cls.substring(1);
                    char picked = pickFromCharClass(cls, negate);
                    i = end + 1;
                    // quantifier?
                    int[] qr = readQuantifier(r, i);
                    int count = qr[0];
                    i = qr[1];
                    for (int k = 0; k < count; k++) sb.append(picked);
                    continue;
                }

                // escape sequences
                if (ch == '\\' && i + 1 < r.length()) {
                    char next = r.charAt(i + 1);
                    char base = switch (next) {
                        case 'd' -> '5';
                        case 'D' -> 'A';
                        case 'w' -> 'a';
                        case 'W' -> '!';
                        case 's' -> ' ';
                        default  -> next;
                    };
                    i += 2;
                    int[] qr = readQuantifier(r, i);
                    int count = qr[0];
                    i = qr[1];
                    for (int k = 0; k < count; k++) sb.append(base);
                    continue;
                }

                // skip grouping parens
                if (ch == '(' || ch == ')') { i++; continue; }

                // literal character
                if (ch == '.' ) {
                    i++;
                    int[] qr = readQuantifier(r, i);
                    int count = qr[0];
                    i = qr[1];
                    for (int k = 0; k < count; k++) sb.append('a');
                    continue;
                }

                i++;
                int[] qr = readQuantifier(r, i);
                int count = qr[0];
                i = qr[1];
                for (int k = 0; k < count; k++) sb.append(ch);
            }
            String result = sb.toString();
            return result.isEmpty() ? "A" : result;
        } catch (Exception e) {
            return "A";
        }
    }

    /** Returns {count, newIndex} from a quantifier starting at position i in s. */
    private static int[] readQuantifier(String s, int i) {
        if (i >= s.length()) return new int[]{1, i};
        char q = s.charAt(i);
        if (q == '*') return new int[]{2, i + 1};
        if (q == '+') return new int[]{2, i + 1};
        if (q == '?') return new int[]{1, i + 1};
        if (q == '{') {
            int close = s.indexOf('}', i);
            if (close < 0) return new int[]{1, i};
            String inner = s.substring(i + 1, close);
            String[] parts = inner.split(",");
            int count;
            try {
                count = parts.length == 1
                        ? Integer.parseInt(parts[0].trim())
                        : Integer.parseInt(parts[0].trim()); // use min
            } catch (NumberFormatException e) {
                count = 1;
            }
            return new int[]{count, close + 1};
        }
        return new int[]{1, i};
    }

    /** Picks a character matching the class expression like A-Z, a-z, 0-9, digits. */
    private static char pickFromCharClass(String cls, boolean negate) {
        if (!negate) {
            // simple ranges
            if (cls.contains("A-Z")) return 'A';
            if (cls.contains("a-z")) return 'a';
            if (cls.contains("0-9") || cls.equals("\\d")) return '5';
            if (!cls.isEmpty()) {
                // explicit chars like [AB]
                return cls.charAt(0);
            }
        }
        return 'X';
    }

    /**
     * Generates a credit card number that passes the Luhn check.
     * Uses the Visa prefix (4) and generates the check digit arithmetically.
     */
    static String luhnValidCard(net.datafaker.Faker faker) {
        // 15 random digits with Visa prefix 4 — seeded faker for determinism
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 14; i++) {
            sb.append(faker.number().randomDigit());
        }
        // compute Luhn check digit
        String partial = sb.toString();
        int sum = 0;
        for (int i = partial.length() - 1; i >= 0; i--) {
            int n = partial.charAt(i) - '0';
            if ((partial.length() - i) % 2 == 1) { n *= 2; if (n > 9) n -= 9; }
            sum += n;
        }
        int check = (10 - (sum % 10)) % 10;
        return partial + check;
    }

    private static String semanticByName(String name, net.datafaker.Faker faker) {
        String n = name.toLowerCase();
        if (n.contains("email")) return faker.internet().emailAddress();
        if (n.contains("phone") || n.contains("mobile")) return faker.phoneNumber().phoneNumber();
        if (n.contains("firstname") || (n.contains("first") && n.contains("name"))) return faker.name().firstName();
        if (n.contains("lastname") || (n.contains("last") && n.contains("name"))) return faker.name().lastName();
        if (n.contains("name")) return faker.name().fullName();
        if (n.contains("password")) return faker.internet().password(10, 20, true, true, true);
        if (n.contains("url") || n.contains("link") || n.contains("website")) return faker.internet().url();
        if (n.contains("city")) return faker.address().city();
        if (n.contains("country") && n.contains("code")) return faker.address().countryCode();
        if (n.contains("country")) return faker.address().country();
        if (n.contains("address")) return faker.address().streetAddress();
        if (n.contains("postcode") || n.contains("zip") || n.contains("postal")) return faker.address().zipCode();
        if (n.contains("currency") || n.equals("ccy")) return faker.currency().toString();
        if (n.contains("description") || n.contains("note") || n.contains("comment")) return faker.lorem().sentence();
        if (n.contains("title")) return faker.job().title();
        if (n.contains("company") || n.contains("organisation") || n.contains("organization"))
            return faker.company().name();
        if (n.contains("token")) return faker.internet().uuid();
        if (n.contains("reference") || n.equals("ref")) return "REF-" + faker.number().digits(6);
        if (n.endsWith("id") || n.endsWith("Id")) return faker.internet().uuid();
        if (n.contains("code")) return faker.code().isbn10();
        if (n.contains("status")) return "ACTIVE";
        if (n.contains("type")) return "DEFAULT";
        if (n.contains("message") || n.contains("reason")) return faker.lorem().sentence(5);
        if (n.contains("iban")) return faker.finance().iban();
        if (n.contains("creditcard") || n.contains("credit_card") || n.contains("cardnumber"))
            return faker.finance().creditCard();
        if (n.contains("bic") || n.contains("swift")) return faker.finance().bic();
        if (n.contains("ip") && !n.contains("zip")) return faker.internet().ipV4Address();
        if (n.contains("mac") && n.contains("address")) return faker.internet().macAddress();
        if (n.contains("colour") || n.contains("color")) return faker.color().name();
        if (n.contains("job") || n.contains("role") || n.contains("position")) return faker.job().title();
        if (n.contains("department")) return faker.commerce().department();
        if (n.contains("product")) return faker.commerce().productName();
        if (n.contains("price") || n.contains("amount") || n.contains("cost"))
            return faker.commerce().price();
        if (n.contains("username") || n.contains("user_name")) return faker.internet().username();
        if (n.contains("firstname") || (n.contains("first") && n.contains("name")))
            return faker.name().firstName();
        if (n.contains("prefix") || n.contains("salutation")) return faker.name().prefix();
        if (n.contains("suffix")) return faker.name().suffix();
        if (n.contains("nationality")) return faker.nation().nationality();
        if (n.contains("language")) return faker.nation().language();
        if (n.contains("paragraph") || n.contains("body") || n.contains("content"))
            return faker.lorem().paragraph();
        if (n.contains("uuid") || n.contains("guid")) return faker.internet().uuid();
        if (n.contains("sortcode") || n.contains("sort_code")) return faker.number().digits(6);
        if (n.contains("accountnumber") || n.contains("account_number"))
            return faker.number().digits(8);
        return faker.lorem().word();
    }

    static String fitPlain(String value, long targetLength) {
        int target = (int) Math.min(targetLength, Integer.MAX_VALUE);
        if (value.length() == target) return value;
        if (value.length() > target) return value.substring(0, target);
        return value + "x".repeat(target - value.length());
    }

    /**
     * Fits an email to exactly {@code targetLength} characters.
     * Pads the local part up to the RFC 5321 64-char limit, then extends the domain via a subdomain prefix.
     */
    static String fitEmail(String email, long targetLength) {
        int target = (int) Math.min(targetLength, Integer.MAX_VALUE);
        int at = email.indexOf('@');
        if (at < 0) return fitPlain(email, targetLength);

        String local = email.substring(0, at);
        String domain = email.substring(at);

        if (email.length() == target) return email;

        if (email.length() > target) {
            int newLocalLen = Math.max(1, Math.min(64, target - domain.length()));
            return local.substring(0, Math.min(newLocalLen, local.length())) + domain;
        }

        int needed = target - email.length();
        int localRoom = Math.max(0, 64 - local.length());
        int localGrowth = Math.min(needed, localRoom);
        local = local + "x".repeat(localGrowth);
        needed -= localGrowth;

        if (needed <= 0) return local + domain;

        String domainBody = domain.substring(1);
        return local + "@" + "x".repeat(needed - 1) + "." + domainBody;
    }

    static String fitUrl(String url, long targetLength) {
        int target = (int) Math.min(targetLength, Integer.MAX_VALUE);
        if (url.length() == target) return url;
        if (url.length() > target) return url.substring(0, target);
        return url + "/x".repeat((target - url.length() + 1) / 2)
                .substring(0, target - url.length());
    }

    private static String fitToBounds(String base, FieldConstraints field, FormatType fmt, net.datafaker.Faker faker) {
        Long min = intMin(field.bounds());
        Long max = intMax(field.bounds());
        String result = base;

        if (max != null && result.length() > max) {
            result = switch (fmt) {
                case EMAIL -> fitEmail(result, max);
                case URL -> fitUrl(result, max);
                default -> fitPlain(result, max);
            };
        }
        if (min != null && result.length() < min) {
            result = switch (fmt) {
                case EMAIL -> fitEmail(result, min);
                case URL -> fitUrl(result, min);
                default -> fitPlain(result, min);
            };
        }
        return result;
    }

    private static Long intMin(QuantityBounds b) {
        return switch (b) {
            case QuantityBounds.IntegerBounds i -> i.min();
            case QuantityBounds.DecimalBounds d -> d.decMin() != null ? d.decMin().longValue() : null;
            case QuantityBounds.Unbounded ignored -> null;
        };
    }

    private static Long intMax(QuantityBounds b) {
        return switch (b) {
            case QuantityBounds.IntegerBounds i -> i.max();
            case QuantityBounds.DecimalBounds d -> d.decMax() != null ? d.decMax().longValue() : null;
            case QuantityBounds.Unbounded ignored -> null;
        };
    }
}