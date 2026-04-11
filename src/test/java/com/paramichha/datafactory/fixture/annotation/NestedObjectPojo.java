package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
public class NestedObjectPojo {
    @NotNull @Valid NestedObjectRecord.Address address;
    @NotNull @Valid NestedObjectRecord.ContactInfo contact;
    public NestedObjectPojo() {}
    public NestedObjectRecord.Address getAddress() { return address; }
    public void setAddress(NestedObjectRecord.Address address) { this.address = address; }
    public NestedObjectRecord.ContactInfo getContact() { return contact; }
    public void setContact(NestedObjectRecord.ContactInfo contact) { this.contact = contact; }

    public record Address(@NotBlank String line1,@NotBlank String city,@NotBlank @Size(min=2,max=10) String postcode){}
    public record ContactInfo(@NotBlank @Email String email,@NotBlank String phone){}
}
