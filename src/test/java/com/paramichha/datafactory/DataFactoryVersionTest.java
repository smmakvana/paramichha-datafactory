package com.paramichha.datafactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The version reported by DataFactory when its version resource is unreadable. */
@DisplayName("DataFactory version")
class DataFactoryVersionTest {

    @Test
    void anUnreadableVersionResourceIsUnknown() {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("disk error");
            }
        };
        assertThat(DataFactory.readVersion(broken)).isEqualTo("unknown");
    }

    @Test
    void aMissingVersionResourceIsUnknown() {
        assertThat(DataFactory.readVersion(null)).isEqualTo("unknown");
    }
}
