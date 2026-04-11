package com.paramichha.datafactory.fixture.partial;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
/** Gap: superclass fields not extracted. When fixed: move to type/. */
public class SuperclassExtractionAllArgs {
    @NotBlank String name;
    @NotNull @Positive Integer value;
}
