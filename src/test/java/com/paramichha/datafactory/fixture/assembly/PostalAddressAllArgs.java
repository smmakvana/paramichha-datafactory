package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class PostalAddressAllArgs {
    @NotBlank String line1;
     String line2;
    @NotBlank String city;
    @NotBlank @Size(min=2,max=10) String postcode;
    @NotBlank @Size(min=2,max=2) String countryCode;
}
