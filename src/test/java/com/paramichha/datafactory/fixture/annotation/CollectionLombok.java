package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class CollectionLombok {
    @NotEmpty List<String> roles;
    @Size(min=1,max=5) List<String> tags;
    @Size(min=1,max=3) List<Integer> scores;
     List<String> notes;
     Collection<String> labels;
}
