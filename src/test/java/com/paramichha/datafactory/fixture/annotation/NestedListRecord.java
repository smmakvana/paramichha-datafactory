package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
public record NestedListRecord(@NotBlank String name, @NotNull @Size(min=1) List<NestedListRecord.Address> addresses, List<NestedListRecord.Tag> tags) {
    public record Address(@NotBlank String line1,@NotBlank String city){}
    public record Tag(@NotBlank String value){}
}
