package com.paramichha.datafactory.fixture.partial;
import jakarta.validation.constraints.*;
import java.util.List;
public record ListEnumShapingRecord(@NotEmpty List<ListEnumShapingRecord.Day> workdays, @NotEmpty List<ListEnumShapingRecord.Priority> priorities, List<ListEnumShapingRecord.Day> optionalDays) {
    public enum Day { MON,TUE,WED,THU,FRI,SAT,SUN }
    public enum Priority { LOW,MEDIUM,HIGH,CRITICAL }
}
