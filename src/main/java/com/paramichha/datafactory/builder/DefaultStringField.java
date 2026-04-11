package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code String} values.
 *
 * <p>Obtained via {@link DataFactory#string()} or {@link DataFactory#string(String)}.
 *
 * <p>Methods fall into two categories:
 * <ul>
 *   <li><b>Semantic hints</b> — tell DataFactory what kind of string you want
 *       and it asks DataFaker for a realistic value: {@link #email()}, {@link #iban()},
 *       {@link #creditCard()}, {@link #name()}, etc.</li>
 *   <li><b>Constraint methods</b> — add Jakarta validation constraints that the
 *       generated value must satisfy: {@link #notBlank()}, {@link #length(int, int)}, etc.</li>
 * </ul>
 *
 * <pre>
 * String email  = DataFactory.string().email().valid();
 * String iban   = DataFactory.string().iban().valid();
 * String name   = DataFactory.string().name().length(2, 50).valid();
 *
 * List&lt;String&gt; emails  = DataFactory.string().email().stream(100);
 * List&lt;String&gt; invalid = DataFactory.string().email().invalidList();
 * </pre>
 */
public final class DefaultStringField implements StringField {

    private FieldBuilder<String> delegate;

    public DefaultStringField(FieldBuilder<String> delegate) {
        this.delegate = delegate;
    }

    /**
     * Sets a custom semantic hint that drives value generation via DataFaker.
     * Use this for any domain-specific string type not covered by the named methods.
     *
     * <pre>
     * DataFactory.string().hint("jobTitle").valid()
     * DataFactory.string().hint("sortCode").valid()
     * DataFactory.string().hint("vehicleRegistration").valid()
     * </pre>
     *
     * @param hint a DataFaker-compatible field name hint
     */
    public DefaultStringField hint(String hint) {
        this.delegate = FieldBuilderFactory.create(String.class, hint);
        return this;
    }

    /** Generates a realistic email address. Example: {@code alice.johnson@example.com} */
    public DefaultStringField email()         { return hint("email"); }

    /** Generates a realistic phone number. Example: {@code +44 7700 900123} */
    public DefaultStringField phone()         { return hint("phone"); }

    /** Generates a realistic full name. Example: {@code Alice Johnson} */
    public DefaultStringField name()          { return hint("name"); }

    /** Generates a realistic first name. Example: {@code Alice} */
    public DefaultStringField firstName()     { return hint("firstName"); }

    /** Generates a realistic last name. Example: {@code Johnson} */
    public DefaultStringField lastName()      { return hint("lastName"); }

    /** Generates a realistic credit card number. */
    public DefaultStringField creditCard()    { return hint("creditCard"); }

    /** Generates a realistic IBAN. Example: {@code GB29NWBK60161331926819} */
    public DefaultStringField iban()          { return hint("iban"); }

    /** Generates a realistic BIC/SWIFT code. Example: {@code NWBKGB2L} */
    public DefaultStringField bic()           { return hint("bic"); }

    /** Generates a realistic postcode. Example: {@code SW1A 1AA} */
    public DefaultStringField postcode()      { return hint("postcode"); }

    /** Generates a realistic street address. */
    public DefaultStringField address()       { return hint("address"); }

    /** Generates a realistic city name. Example: {@code London} */
    public DefaultStringField city()          { return hint("city"); }

    /** Generates a realistic country name. Example: {@code United Kingdom} */
    public DefaultStringField country()       { return hint("country"); }

    /** Generates a realistic company name. Example: {@code Acme Corp} */
    public DefaultStringField company()       { return hint("company"); }

    /** Generates a realistic URL. Example: {@code https://example.com} */
    public DefaultStringField url()           { return hint("url"); }

    /** Generates a random UUID string. */
    public DefaultStringField uuid()          { return hint("uuid"); }

    /** Generates a realistic IP address. Example: {@code 192.168.1.1} */
    public DefaultStringField ipAddress()     { return hint("ipAddress"); }

    /** Generates a realistic colour name. Example: {@code Cerulean} */
    public DefaultStringField colour()        { return hint("colour"); }

    /** Generates a realistic username. Example: {@code alice_j42} */
    public DefaultStringField username()      { return hint("username"); }

    /** Generates a realistic sentence or description. */
    public DefaultStringField description()   { return hint("description"); }

    /** Generates a realistic password string. */
    public DefaultStringField password()      { return hint("password"); }

    /** Generates a realistic sort code. Example: {@code 60-16-13} */
    public DefaultStringField sortCode()      { return hint("sortCode"); }

    /** Generates a realistic bank account number. */
    public DefaultStringField accountNumber() { return hint("accountNumber"); }

    /** Generates a realistic product name. */
    public DefaultStringField product()       { return hint("product"); }

    /** Generates a realistic department name. Example: {@code Engineering} */
    public DefaultStringField department()    { return hint("department"); }

    /**
     * The generated value must not be blank.
     * Equivalent to adding {@code @NotBlank}.
     */
    public DefaultStringField notBlank() {
        delegate = delegate.with(NotBlank.class);
        return this;
    }

    /**
     * Constrains the length to the given inclusive range.
     * Equivalent to adding {@code @Size(min=min, max=max)}.
     *
     * @param min minimum length, inclusive
     * @param max maximum length, inclusive
     */
    public DefaultStringField length(int min, int max) {
        return with("@Size(min=" + min + ", max=" + max + ")");
    }

    /**
     * Constrains the maximum length.
     * Equivalent to adding {@code @Size(max=max)}.
     *
     * @param max maximum length, inclusive
     */
    public DefaultStringField maxLength(int max) {
        return with("@Size(max=" + max + ")");
    }

    /**
     * Constrains the minimum length.
     * Equivalent to adding {@code @Size(min=min)}.
     *
     * @param min minimum length, inclusive
     */
    public DefaultStringField minLength(int min) {
        return with("@Size(min=" + min + ")");
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public DefaultStringField with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Size(max=50)"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public DefaultStringField with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid {@code String}.
     *
     * @return a valid string satisfying all constraints
     * @throws DataFactoryException if no valid value can be generated
     */
    public String valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid strings.
     *
     * @return list of valid boundary strings
     */
    public List<String> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code String}.
     *
     * @return a string that violates at least one constraint
     */
    public String invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid strings, one per constraint.
     *
     * @return list of invalid strings
     */
    public List<String> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid strings.
     *
     * @param count the number of values to generate
     * @return list of valid strings
     */
    public List<String> stream(int count) {
        return delegate.stream(count);
    }
}
