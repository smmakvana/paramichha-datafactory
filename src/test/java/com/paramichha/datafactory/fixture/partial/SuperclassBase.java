package com.paramichha.datafactory.fixture.partial;

import jakarta.validation.constraints.*;

/**
 * Base class for SuperclassExtraction* fixtures.
 * Gap: these fields are NOT extracted by DataFactory today.
 * When inheritance support added: all four strategies move to type/.
 */
public class SuperclassBase {
    @NotBlank private String            id;
    @NotNull  private java.time.Instant createdAt;

    public SuperclassBase() {}

    public String            getId()        { return id;        }
    public java.time.Instant getCreatedAt() { return createdAt; }
    public void setId(String v)                    { this.id        = v; }
    public void setCreatedAt(java.time.Instant v)  { this.createdAt = v; }
}
