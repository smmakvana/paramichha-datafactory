package com.paramichha.datafactory.generation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Fitting an email to a target length: the local part grows first, then the domain. */
@DisplayName("StringDataCreator — fitting an email to a length")
class StringDataCreatorEmailFitTest {

    @Test
    @DisplayName("a full 64-character local part is padded by extending the domain")
    void fullLocalPartExtendsTheDomain() {
        String longLocal = "a".repeat(64);
        String email = longLocal + "@example.com";
        String result = StringDataCreator.fitEmail(email, email.length() + 5);
        assertThat(result).hasSize(email.length() + 5);
        assertThat(result).contains("@");
    }

    @Test
    @DisplayName("the local part absorbs the extra length when it has room")
    void localPartAbsorbsTheExtraLength() {
        String email = "ab@example.com";
        String result = StringDataCreator.fitEmail(email, email.length() + 2);
        assertThat(result).hasSize(email.length() + 2);
        assertThat(result).endsWith("@example.com");
    }
}
