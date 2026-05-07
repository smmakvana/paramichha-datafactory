package com.paramichha.example.partial;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Value @Builder
/** Gap: superclass fields not extracted. When fixed: move to type/. */
public class SuperclassExtractionLombok {
    @NotBlank String name;
    @NotNull @Positive Integer value;
}
