package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.analyzer.FormatType;
import com.paramichha.datafactory.planner.BoundaryTarget;
import net.datafaker.Faker;

import java.util.Locale;

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
final class StringShaper {

    private static final Faker FAKER = new Faker(Locale.ENGLISH);

    private StringShaper() {
    }

    static String shape(FieldConstraints field, BoundaryTarget target) {
        String base = baseValue(field);

        if (target.isSemantic() || target.targetQuantity() == null) {
            return fitToBounds(base, field, field.format());
        }

        long targetLength = target.targetQuantity();
        return switch (field.format()) {
            case EMAIL -> fitEmail(base, targetLength);
            case URL -> fitUrl(base, targetLength);
            case PATTERN -> base;
            default -> fitPlain(base, targetLength);
        };
    }

    private static String baseValue(FieldConstraints field) {
        return switch (field.format()) {
            case EMAIL -> FAKER.internet().emailAddress();
            case URL -> FAKER.internet().url();
            case CREDIT_CARD -> FAKER.finance().creditCard();
            case ISBN -> FAKER.code().isbn13();
            case EAN -> FAKER.code().ean13();
            case UUID_STRING -> java.util.UUID.randomUUID().toString();
            case PATTERN -> {
                System.err.println("[TestDataFactory] WARNING: @Pattern on '" + field.fieldName()
                        + "' cannot be synthesized. Returning semantic fallback.");
                yield semanticByName(field.fieldName());
            }
            default -> semanticByName(field.fieldName());
        };
    }

    private static String semanticByName(String name) {
        String n = name.toLowerCase();
        if (n.contains("email")) return FAKER.internet().emailAddress();
        if (n.contains("phone") || n.contains("mobile")) return FAKER.phoneNumber().phoneNumber();
        if (n.contains("firstname") || (n.contains("first") && n.contains("name"))) return FAKER.name().firstName();
        if (n.contains("lastname") || (n.contains("last") && n.contains("name"))) return FAKER.name().lastName();
        if (n.contains("name")) return FAKER.name().fullName();
        if (n.contains("password")) return FAKER.internet().password(10, 20, true, true, true);
        if (n.contains("url") || n.contains("link") || n.contains("website")) return FAKER.internet().url();
        if (n.contains("city")) return FAKER.address().city();
        if (n.contains("country") && n.contains("code")) return FAKER.address().countryCode();
        if (n.contains("country")) return FAKER.address().country();
        if (n.contains("address")) return FAKER.address().streetAddress();
        if (n.contains("postcode") || n.contains("zip") || n.contains("postal")) return FAKER.address().zipCode();
        if (n.contains("currency") || n.equals("ccy")) return FAKER.currency().code();
        if (n.contains("description") || n.contains("note") || n.contains("comment")) return FAKER.lorem().sentence();
        if (n.contains("title")) return FAKER.job().title();
        if (n.contains("company") || n.contains("organisation") || n.contains("organization"))
            return FAKER.company().name();
        if (n.contains("token")) return FAKER.internet().uuid();
        if (n.contains("reference") || n.equals("ref")) return "REF-" + FAKER.number().digits(6);
        if (n.endsWith("id") || n.endsWith("Id")) return FAKER.internet().uuid();
        if (n.contains("code")) return FAKER.code().isbn10();
        if (n.contains("status")) return "ACTIVE";
        if (n.contains("type")) return "DEFAULT";
        if (n.contains("message") || n.contains("reason")) return FAKER.lorem().sentence(5);
        return FAKER.lorem().word();
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

    private static String fitToBounds(String base, FieldConstraints field, FormatType fmt) {
        Long min = field.bounds().min();
        Long max = field.bounds().max();
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
}
