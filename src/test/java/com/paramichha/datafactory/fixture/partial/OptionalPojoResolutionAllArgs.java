package com.paramichha.datafactory.fixture.partial;
import jakarta.validation.constraints.*;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class OptionalPojoResolutionAllArgs {
     Optional<String> nickname;
     Optional<Integer> age;

    /** Gap: Optional<POJO> returns empty — nested POJO not resolved. When fixed: move to annotation/. */
}
