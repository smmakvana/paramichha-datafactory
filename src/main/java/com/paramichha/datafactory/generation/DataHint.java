package com.paramichha.datafactory.generation;

import java.util.function.Function;
import net.datafaker.Faker;

/**
 * Internal service — maps variable names to the most appropriate Faker function.
 *
 * <p>DataFactory knows only field names. This class is the bridge between
 * a plain variable name and a realistic Faker provider call.
 *
 * <p>Package-private — never exposed outside generation package.
 * {@link FakerDataMachine} is the only consumer.
 *
 * <p>Three layers:
 * <ol>
 *   <li>Variable name → canonical hint (e.g. "postcode" → "zipcode")
 *   <li>Canonical hint → Faker function
 *   <li>Unknown hint → fallback (lorem word)
 * </ol>
 */
enum DataHint {

    // ── Identity ──────────────────────────────────────────────────────────
    FULL_NAME(f -> f.name().fullName()),
    FIRST_NAME(f -> f.name().firstName()),
    LAST_NAME(f -> f.name().lastName()),
    USERNAME(f -> f.internet().username()),
    PASSWORD(f -> f.internet().password(8, 16, true, true, true)),
    PREFIX(f -> f.name().prefix()),
    SUFFIX(f -> f.name().suffix()),

    // ── Contact ───────────────────────────────────────────────────────────
    EMAIL(f -> f.internet().emailAddress()),
    PHONE(f -> f.phoneNumber().phoneNumber()),
    URL(f -> f.internet().url()),
    IP_ADDRESS(f -> f.internet().ipV4Address()),

    // ── Address ───────────────────────────────────────────────────────────
    STREET(f -> f.address().streetAddress()),
    CITY(f -> f.address().city()),
    COUNTRY(f -> f.address().country()),
    COUNTRY_CODE(f -> f.address().countryCode()),
    ZIP_CODE(f -> f.address().zipCode()),

    // ── Commerce ──────────────────────────────────────────────────────────
    COMPANY(f -> f.company().name()),
    PRODUCT(f -> f.commerce().productName()),
    DEPARTMENT(f -> f.commerce().department()),
    PRICE(f -> f.commerce().price()),
    CURRENCY(f -> Currencies.CODES.get(f.random().nextInt(Currencies.CODES.size()))),
    ORDER_ID(f -> f.numerify("ORD-######")),

    // ── Finance ───────────────────────────────────────────────────────────
    CREDIT_CARD(f -> f.finance().creditCard()),
    IBAN(f -> f.finance().iban()),
    BIC(f -> f.finance().bic()),
    EAN(f -> f.code().ean13()),
    ISBN(f -> f.code().isbn13()),
    ISBN_10(f -> f.code().isbn10()),
    ACCOUNT_CODE(f -> f.numerify("ACC-########")),

    // ── Text ──────────────────────────────────────────────────────────────
    WORD(f -> f.lorem().word()),
    SENTENCE(f -> f.lorem().sentence()),
    PARAGRAPH(f -> f.lorem().paragraph()),
    TAG(f -> f.lorem().word()),
    CODE(f -> f.numerify("###-###")),
    REFERENCE(f -> f.bothify("??###??")),

    // ── Technical ─────────────────────────────────────────────────────────
    COLOR(f -> f.color().name()),
    JOB_TITLE(f -> f.job().title()),
    LANGUAGE(f -> f.nation().language()),
    NATIONALITY(f -> f.nation().nationality());

    private final Function<Faker, String> fakerFn;

    DataHint(Function<Faker, String> fakerFn) {
        this.fakerFn = fakerFn;
    }

    /** Executes the Faker function for this hint. */
    String produce(Faker faker) {
        return fakerFn.apply(faker);
    }

    /**
     * Maps a variable name to the most appropriate DataHint.
     * Case-insensitive. Falls back to {@link #WORD} for unknown names.
     */
    static DataHint fromVariableName(String name) {
        if (name == null || name.isBlank()) {
            return WORD;
        }
        return switch (name.toLowerCase()) {
            case "name", "fullname", "displayname", "realname", "contactname" -> FULL_NAME;
            case "firstname", "givenname", "forename" -> FIRST_NAME;
            case "lastname", "surname", "familyname" -> LAST_NAME;
            case "username", "login", "handle", "screenname", "alias", "nickname" -> USERNAME;
            case "password", "passwd", "pwd", "secret", "passphrase" -> PASSWORD;
            case "prefix", "salutation" -> PREFIX;
            case "suffix" -> SUFFIX;
            case "email", "emailaddress", "mail" -> EMAIL;
            case "phone", "phonenumber", "mobile", "tel", "telephone", "cell" -> PHONE;
            case "url", "website", "homepage", "link", "uri", "href" -> URL;
            case "ip", "ipaddress", "host" -> IP_ADDRESS;
            case "street", "address", "streetaddress", "line1", "addressline1" -> STREET;
            case "city", "town" -> CITY;
            case "country", "nation" -> COUNTRY;
            case "countrycode", "country_code", "iso" -> COUNTRY_CODE;
            case "zip", "zipcode", "postcode", "postalcode", "postal" -> ZIP_CODE;
            case "company", "companyname", "firm", "organisation", "organization" -> COMPANY;
            case "product", "productname", "item" -> PRODUCT;
            case "department", "division", "team" -> DEPARTMENT;
            case "price", "cost", "amount", "fee", "charge", "totalamount" -> PRICE;
            case "currency", "currencycode" -> CURRENCY;
            case "orderid", "ordernumber", "transactionid" -> ORDER_ID;
            case "creditcard", "cardnumber", "card", "pan" -> CREDIT_CARD;
            case "iban" -> IBAN;
            case "bic", "swift" -> BIC;
            case "ean", "barcode" -> EAN;
            case "isbn" -> ISBN;
            case "isbn10" -> ISBN_10;
            case "accountcode", "accountnumber", "sortcode", "bankcode" -> ACCOUNT_CODE;
            case "tag", "label", "keyword", "category", "type", "kind" -> TAG;
            case "code", "cvv", "cvc", "pin", "otp", "token", "key" -> CODE;
            case "reference", "ref", "bookcode", "vouchercode" -> REFERENCE;
            case "word" -> WORD;
            case "sentence", "description", "summary", "subject", "headline" -> SENTENCE;
            case "paragraph", "body", "content", "text", "notes", "comment", "bio", "about", "overview", "details" ->
                PARAGRAPH;
            case "color", "colour" -> COLOR;
            case "job", "jobtitle", "role", "position" -> JOB_TITLE;
            case "language", "lang" -> LANGUAGE;
            case "nationality" -> NATIONALITY;
            default -> WORD;
        };
    }

    /**
     * Common ISO 4217 codes, picked with the seeded random so the same seed always gives the
     * same code. (DataFaker's currency().code() is marked for removal, and money().currencyCode()
     * was not stable across runs inside DataFactory.)
     */
    private static final class Currencies {
        static final java.util.List<String> CODES = java.util.List.of(
                "AED", "AUD", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP", "HKD", "HUF", "IDR", "ILS", "INR",
                "JPY", "KRW", "MXN", "MYR", "NOK", "NZD", "PHP", "PLN", "SAR", "SEK", "SGD", "THB", "TRY", "USD",
                "ZAR");
    }
}
