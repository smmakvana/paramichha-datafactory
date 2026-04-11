package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code String} values. Obtained via {@link DataFactory#string()}. */
public interface StringField extends FieldBuilder<String> {
    StringField hint(String hint);
    StringField email();
    StringField phone();
    StringField name();
    StringField firstName();
    StringField lastName();
    StringField creditCard();
    StringField iban();
    StringField bic();
    StringField postcode();
    StringField address();
    StringField city();
    StringField country();
    StringField company();
    StringField url();
    StringField uuid();
    StringField ipAddress();
    StringField colour();
    StringField username();
    StringField description();
    StringField password();
    StringField sortCode();
    StringField accountNumber();
    StringField product();
    StringField department();
    StringField notBlank();
    StringField length(int min, int max);
    StringField maxLength(int max);
    StringField minLength(int min);
    StringField with(Class<? extends Annotation> annotation);
    StringField with(String annotation);
}
