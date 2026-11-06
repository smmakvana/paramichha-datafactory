package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.FormatType;
import com.paramichha.datafactory.constraint.NumericBounds;

/** Produces {@code String} values. */
final class StringDataCreator implements DataCreator {
    static final StringDataCreator INSTANCE = new StringDataCreator();
    private StringDataCreator() {}

    static String getString(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        String base = baseValue(field, machine);

        if (!(target instanceof BoundaryTarget.Fixed)) {
            return fitToBounds(base, field, field.format(), machine);
        }

        long targetLength = ((BoundaryTarget.Fixed) target).value();
        return switch (field.format()) {
            case EMAIL -> fitEmail(base, targetLength);
            case URL -> fitUrl(base, targetLength);
            case PATTERN -> base;
            default -> fitPlain(base, targetLength);
        };
    }

    private static String baseValue(FieldConstraints field, DataMachine machine) {
        // @Digits on String — generate a purely numeric string of the right length
        if (field.maxIntegerDigits() < Integer.MAX_VALUE) {
            int intPart = Math.min(field.maxIntegerDigits(), 6);
            int fracPart = field.maxFractionDigits() < Integer.MAX_VALUE ? field.maxFractionDigits() : 0;
            String digits = machine.digits(Math.max(1, intPart));
            return fracPart > 0 ? digits + "." + machine.digits(fracPart) : digits;
        }
        // @DecimalMin/@DecimalMax on String — generate a conforming decimal string
        java.math.BigDecimal decMin = field.bounds() instanceof NumericBounds.DecimalBounds db
                ? db.decMin() : null;
        java.math.BigDecimal decMax = field.bounds() instanceof NumericBounds.DecimalBounds db2
                ? db2.decMax() : null;
        if (decMin != null || decMax != null) {
            java.math.BigDecimal val = decMin != null ? decMin.add(java.math.BigDecimal.ONE)
                    : decMax.subtract(java.math.BigDecimal.ONE);
            return val.toPlainString();
        }
        return switch (field.format()) {
            case EMAIL -> machine.text("email");
            case URL -> machine.text("url");
            case CREDIT_CARD -> luhnValidCard(machine);
            case ISBN -> machine.text("isbn");
            case EAN -> machine.text("ean");
            case UUID_STRING -> machine.text("uuid");
            case PATTERN -> generateFromPattern(field.patternRegexp());
            default -> semanticByName(field.fieldName(), machine);
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
            // #6 — don't swallow silently; log so callers know the pattern wasn't honoured
            System.err.println("DataFactory [WARN] generateFromPattern failed for regexp='"
                    + regexp + "': " + e.getMessage() + " — falling back to 'A'");
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
    static String luhnValidCard(DataMachine machine) {
        // 15 random digits with Visa prefix 4 — seeded faker for determinism
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 14; i++) {
            sb.append(machine.digit());
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

    private static String semanticByName(String name, DataMachine machine) {
        String n = name.toLowerCase();
        if (n.contains("email")) return machine.text("email");
        if (n.contains("phone") || n.contains("mobile")) return machine.text("phone");
        if (n.contains("firstname") || (n.contains("first") && n.contains("name"))) return machine.text("firstname");
        if (n.contains("lastname") || (n.contains("last") && n.contains("name"))) return machine.text("lastname");
        if (n.contains("name")) return machine.text("name");
        if (n.contains("password")) return machine.text("password");
        if (n.contains("url") || n.contains("link") || n.contains("website")) return machine.text("url");
        if (n.contains("city")) return machine.text("city");
        if (n.contains("country") && n.contains("code")) return machine.text("countrycode");
        if (n.contains("country")) return machine.text("country");
        // Before "address": ipAddress / macAddress are network addresses, not street addresses.
        // (Not a bare contains("ip"): "shippingAddress" is a street address.)
        if (n.contains("ipaddress") || n.contains("ipv4") || n.contains("ipv6") || n.equals("ip")
                || n.contains("macaddress") || (n.contains("mac") && n.contains("address")))
            return machine.text("ip");
        if (n.contains("address")) return machine.text("street");
        if (n.contains("postcode") || n.contains("zip") || n.contains("postal")) return machine.text("zipcode");
        if (n.contains("currency") || n.equals("ccy")) return machine.text("currency");
        if (n.contains("description") || n.contains("note") || n.contains("comment")) return machine.text("sentence");
        if (n.contains("title")) return machine.text("jobtitle");
        if (n.contains("company") || n.contains("organisation") || n.contains("organization"))
            return machine.text("company");
        if (n.contains("token")) return machine.text("uuid");
        if (n.contains("reference") || n.equals("ref")) return "REF-" + machine.digits(6);
        if (n.endsWith("id") || n.endsWith("Id")) return machine.text("uuid");
        if (n.contains("code")) return machine.text("isbn10");
        if (n.contains("status")) return "ACTIVE";
        if (n.contains("type")) return "DEFAULT";
        if (n.contains("message") || n.contains("reason")) return machine.text("sentence");
        if (n.contains("iban")) return machine.text("iban");
        if (n.contains("creditcard") || n.contains("credit_card") || n.contains("cardnumber"))
            return machine.text("creditcard");
        if (n.contains("bic") || n.contains("swift")) return machine.text("bic");
        if (n.contains("ip") && !n.contains("zip")) return machine.text("ip");
        if (n.contains("colour") || n.contains("color")) return machine.text("color");
        if (n.contains("job") || n.contains("role") || n.contains("position")) return machine.text("jobtitle");
        if (n.contains("department")) return machine.text("department");
        if (n.contains("product")) return machine.text("product");
        if (n.contains("price") || n.contains("amount") || n.contains("cost"))
            return machine.text("price");
        if (n.contains("username") || n.contains("user_name")) return machine.text("username");
        if (n.contains("prefix") || n.contains("salutation")) return machine.text("prefix");
        if (n.contains("suffix")) return machine.text("suffix");
        if (n.contains("nationality")) return machine.text("nationality");
        if (n.contains("language")) return machine.text("language");
        if (n.contains("paragraph") || n.contains("body") || n.contains("content"))
            return machine.text("paragraph");
        if (n.contains("uuid") || n.contains("guid")) return machine.text("uuid");
        if (n.contains("sortcode") || n.contains("sort_code")) return machine.digits(6);
        if (n.contains("accountnumber") || n.contains("account_number"))
            return machine.digits(8);
        return machine.text("word");
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

    private static String fitToBounds(String base, FieldConstraints field, FormatType fmt, DataMachine machine) {
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

    private static Long intMin(NumericBounds b) {
        return switch (b) {
            case NumericBounds.IntegerBounds i -> i.min();
            case NumericBounds.DecimalBounds d -> d.decMin() != null ? d.decMin().longValue() : null;
            case NumericBounds.Unbounded ignored -> null;
        };
    }

    private static Long intMax(NumericBounds b) {
        return switch (b) {
            case NumericBounds.IntegerBounds i -> i.max();
            case NumericBounds.DecimalBounds d -> d.decMax() != null ? d.decMax().longValue() : null;
            case NumericBounds.Unbounded ignored -> null;
        };
    }

    @Override public boolean supports(Class<?> type) { return type == String.class; }

    @Override public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        return getString(field, target, machine);
    }
}
