package com.paramichha.datafactory.examples.advanced;

import com.paramichha.datafactory.DataFactory;

/**
 * Field Level Control — typed builders for every Java type.
 *
 * <p>When you need a single value without an annotated class, the typed builders
 * give you a fluent one-line API for every primitive, wrapper, and temporal type.
 */
public class FieldLevelControl {

    enum Status { ACTIVE, INACTIVE, SUSPENDED, PENDING }

    public static void main(String[] args) {
        strings();
        numbers();
        decimals();
        dates();
        booleans();
        enums();
        validAndInvalid();
    }

    static void strings() {
        section("String — semantic hints");
        System.out.println("  email:   " + DataFactory.string().email().valid());
        System.out.println("  iban:    " + DataFactory.string().iban().valid());
        System.out.println("  bic:     " + DataFactory.string().bic().valid());
        System.out.println("  card:    " + DataFactory.string().creditCard().valid());
        System.out.println("  sort:    " + DataFactory.string().sortCode().valid());
        System.out.println("  ip:      " + DataFactory.string().ipAddress().valid());
        System.out.println("  custom:  " + DataFactory.string().hint("jobTitle").valid());

        section("String — constraints");
        System.out.println("  length(5,10): " + DataFactory.string().length(5, 10).valid());
        System.out.println("  maxLength(8): " + DataFactory.string().maxLength(8).valid());
        System.out.println("  notBlank:     " + DataFactory.string().notBlank().valid());
    }

    static void numbers() {
        section("Numbers — primitives and wrappers");
        int    i = DataFactory.integer().range(18, 65).toInt();
        long   l = DataFactory.longVal().positive().toLong();
        double d = DataFactory.doubles().positive().toDouble();
        float  f = DataFactory.floats().positive().toFloat();
        short  s = DataFactory.shorts().range((short) 1, (short) 100).toShort();
        byte   b = DataFactory.bytes().positive().toByte();
        System.out.println("  int:   " + i + "  long: " + l);
        System.out.println("  double:" + d + "  float: " + f);
        System.out.println("  short: " + s + "  byte: " + b);
    }

    static void decimals() {
        section("BigDecimal");
        System.out.println("  positive:      " + DataFactory.decimal().positive().valid());
        System.out.println("  scale(2):      " + DataFactory.decimal().positive().scale(2).valid());
        System.out.println("  range+scale:   " + DataFactory.decimal().range("0.01", "999.99").scale(2).valid());
        System.out.println("  digits(5,2):   " + DataFactory.decimal().digits(5, 2).valid());
    }

    static void dates() {
        section("LocalDate and LocalDateTime");
        System.out.println("  past:          " + DataFactory.date().past().valid());
        System.out.println("  future:        " + DataFactory.date().future().valid());
        System.out.println("  pastOrPresent: " + DataFactory.date().pastOrPresent().valid());
        System.out.println("  past datetime: " + DataFactory.dateTime().past().valid());
    }

    static void booleans() {
        section("Boolean");
        System.out.println("  assertTrue:  " + DataFactory.bool().assertTrue().valid());
        System.out.println("  assertFalse: " + DataFactory.bool().assertFalse().valid());
    }

    static void enums() {
        section("Enums");
        System.out.println("  one:  " + DataFactory.enumOf(Status.class));
        System.out.println("  five: " + DataFactory.enumOf(Status.class, 5));
    }

    static void validAndInvalid() {
        section("Valid, invalid, boundaries, stream");
        System.out.println("  email valid:     " + DataFactory.string().email().valid());
        System.out.println("  email invalid:   " + DataFactory.string().email().invalid());
        System.out.println("  email invalids:  " + DataFactory.string().email().invalidList());
        System.out.println("  age boundaries:  " + DataFactory.integer().range(18, 120).validList());
        System.out.println("  10 emails:       " + DataFactory.string().email().stream(10).size() + " generated");
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }
}
