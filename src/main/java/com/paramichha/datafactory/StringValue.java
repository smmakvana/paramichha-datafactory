package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code String} values. Obtained via {@link DataFactory#string()}. */
public interface StringValue extends FieldValue<String> {
    StringValue hint(String hint);

    StringValue email();

    StringValue phone();

    StringValue name();

    StringValue firstName();

    StringValue lastName();

    StringValue creditCard();

    StringValue iban();

    StringValue bic();

    StringValue postcode();

    StringValue address();

    StringValue city();

    StringValue country();

    StringValue company();

    StringValue url();

    StringValue uuid();

    StringValue ipAddress();

    StringValue colour();

    StringValue username();

    StringValue description();

    StringValue password();

    StringValue sortCode();

    StringValue accountNumber();

    StringValue product();

    StringValue department();

    StringValue notBlank();

    StringValue length(int min, int max);

    StringValue maxLength(int max);

    StringValue minLength(int min);

    StringValue with(Class<? extends Annotation> annotation);

    StringValue with(String annotation);
}
