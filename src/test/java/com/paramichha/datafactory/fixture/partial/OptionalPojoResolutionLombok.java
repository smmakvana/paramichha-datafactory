package com.paramichha.example.partial;
import jakarta.validation.constraints.*;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class OptionalPojoResolutionLombok {
     Optional<String> nickname;
     Optional<Integer> age;

    /** Gap: Optional<POJO> returns empty — nested POJO not resolved. When fixed: move to annotation/. */
}
