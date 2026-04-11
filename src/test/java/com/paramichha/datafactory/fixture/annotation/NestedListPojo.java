package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
public class NestedListPojo {
    @NotBlank String name;
    @NotNull @Size(min=1) List<NestedListRecord.Address> addresses;
     List<NestedListRecord.Tag> tags;
    public NestedListPojo() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<NestedListRecord.Address> getAddresses() { return addresses; }
    public void setAddresses(List<NestedListRecord.Address> addresses) { this.addresses = addresses; }
    public List<NestedListRecord.Tag> getTags() { return tags; }
    public void setTags(List<NestedListRecord.Tag> tags) { this.tags = tags; }

    public record Address(@NotBlank String line1,@NotBlank String city){}
    public record Tag(@NotBlank String value){}
}
