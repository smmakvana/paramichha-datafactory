package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class PostalAddressLombok {
    @NotBlank String line1;
     String line2;
    @NotBlank String city;
    @NotBlank @Size(min=2,max=10) String postcode;
    @NotBlank @Size(min=2,max=2) String countryCode;
}
