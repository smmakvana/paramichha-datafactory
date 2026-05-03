package com.paramichha.testkit.fixture.annotation.creditcard;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;

@Testable
public record AnnotationCreditCardString(
        @CreditCardNumber String cardNumber
) {}
