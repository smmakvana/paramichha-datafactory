package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
public class PostalAddressPojo {
    @NotBlank String line1;
     String line2;
    @NotBlank String city;
    @NotBlank @Size(min=2,max=10) String postcode;
    @NotBlank @Size(min=2,max=2) String countryCode;
    public PostalAddressPojo() {}
    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }
    public String getLine2() { return line2; }
    public void setLine2(String line2) { this.line2 = line2; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
