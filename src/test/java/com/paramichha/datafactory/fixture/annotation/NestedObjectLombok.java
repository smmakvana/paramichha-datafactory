package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class NestedObjectLombok {
    @NotNull @Valid NestedObjectRecord.Address address;
    @NotNull @Valid NestedObjectRecord.ContactInfo contact;

    public record Address(@NotBlank String line1,@NotBlank String city,@NotBlank @Size(min=2,max=10) String postcode){}
    public record ContactInfo(@NotBlank @Email String email,@NotBlank String phone){}
}
