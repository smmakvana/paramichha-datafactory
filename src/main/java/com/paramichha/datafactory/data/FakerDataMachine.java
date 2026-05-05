package com.paramichha.datafactory.data;

import net.datafaker.Faker;

import java.util.Random;
import java.util.UUID;

/**
 * {@link DataMachine} backed by DataFaker.
 *
 * <p>Stateful — each call advances the internal Faker random state.
 * One instance per builder means:
 * <ul>
 *   <li>Seeded: same seed → same sequence of values across all fields and objects
 *   <li>Random: different values every call
 * </ul>
 *
 * <p>Obtain via {@link DataMachines} — never construct directly.
 */
final class FakerDataMachine implements DataMachine {

    private final Faker  faker;
    private final Random random; // same Random passed to Faker — used for UUID determinism

    FakerDataMachine(Faker faker, Random random) {
        this.faker  = faker;
        this.random = random;
    }

    @Override
    public String text(String hint) {
        return switch (hint == null ? "" : hint.toLowerCase()) {
            case "email"                     -> faker.internet().emailAddress();
            case "name", "fullname"          -> faker.name().fullName();
            case "firstname"                 -> faker.name().firstName();
            case "lastname"                  -> faker.name().lastName();
            case "prefix"                    -> faker.name().prefix();
            case "suffix"                    -> faker.name().suffix();
            case "username"                  -> faker.internet().username();
            case "phone", "phonenumber"      -> faker.phoneNumber().phoneNumber();
            case "url", "website"            -> faker.internet().url();
            case "ip", "ipaddress"           -> faker.internet().ipV4Address();
            case "street", "address",
                 "streetaddress"             -> faker.address().streetAddress();
            case "city"                      -> faker.address().city();
            case "country"                   -> faker.address().country();
            case "countrycode"               -> faker.address().countryCode();
            case "zip", "zipcode",
                 "postcode", "postal"        -> faker.address().zipCode();
            case "company", "companyname"    -> faker.company().name();
            case "product", "productname"    -> faker.commerce().productName();
            case "department"                -> faker.commerce().department();
            case "price"                     -> faker.commerce().price();
            case "currency"                  -> faker.currency().code();
            case "creditcard", "cardnumber"  -> faker.finance().creditCard();
            case "iban"                      -> faker.finance().iban();
            case "bic"                       -> faker.finance().bic();
            case "ean", "barcode"            -> faker.code().ean13();
            case "isbn"                      -> faker.code().isbn13();
            case "isbn10"                    -> faker.code().isbn10();
            case "uuid"                      -> uuid();
            case "color", "colour"           -> faker.color().name();
            case "job", "title", "jobtitle"  -> faker.job().title();
            case "language"                  -> faker.nation().language();
            case "nationality"               -> faker.nation().nationality();
            case "sentence"                  -> faker.lorem().sentence();
            case "paragraph"                 -> faker.lorem().paragraph();
            default                          -> faker.lorem().word();
        };
    }

    @Override
    public long number(long min, long max) {
        return faker.number().numberBetween(min, max);
    }

    @Override
    public double decimal(int scale, long min, long max) {
        return faker.number().randomDouble(scale, min, max);
    }

    @Override
    public int digit() {
        return faker.number().randomDigit();
    }

    @Override
    public String digits(int count) {
        return faker.number().digits(count);
    }

    // Uses the same Random instance as Faker for true determinism.
    // faker.internet().uuid() does not reliably honour the seed.
    private String uuid() {
        return random != null
                ? new UUID(random.nextLong(), random.nextLong()).toString()
                : UUID.randomUUID().toString();
    }
}
