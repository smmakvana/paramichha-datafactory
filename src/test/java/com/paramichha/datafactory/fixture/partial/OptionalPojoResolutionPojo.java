package com.paramichha.datafactory.fixture.partial;
import jakarta.validation.constraints.*;
import java.util.Optional;
public class OptionalPojoResolutionPojo {
     Optional<String> nickname;
     Optional<Integer> age;
    public OptionalPojoResolutionPojo() {}
    public Optional<String> getNickname() { return nickname; }
    public void setNickname(Optional<String> nickname) { this.nickname = nickname; }
    public Optional<Integer> getAge() { return age; }
    public void setAge(Optional<Integer> age) { this.age = age; }

    /** Gap: Optional<POJO> returns empty — nested POJO not resolved. When fixed: move to annotation/. */
}
