package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class NestedObjectAllArgs {
    @NotNull @Valid NestedObjectRecord.Address address;
    @NotNull @Valid NestedObjectRecord.ContactInfo contact;

    public record Address(@NotBlank String line1,@NotBlank String city,@NotBlank @Size(min=2,max=10) String postcode){}
    public record ContactInfo(@NotBlank @Email String email,@NotBlank String phone){}
}
