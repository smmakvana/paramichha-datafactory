package com.paramichha.datafactory.fixture.partial;
import jakarta.validation.constraints.*;
/**
 * Gap: superclass fields (id, createdAt) not extracted.
 * Snapshot shows only subclass fields. When fixed: move to type/.
 */
public record SuperclassExtractionRecord(@NotBlank String name, @NotNull @Positive Integer value) {}
