package com.paramichha.datafactory.fixture.partial;
import jakarta.validation.constraints.*;
import java.util.List;
public class ListEnumShapingPojo {
    @NotEmpty List<ListEnumShapingRecord.Day> workdays;
    @NotEmpty List<ListEnumShapingRecord.Priority> priorities;
     List<ListEnumShapingRecord.Day> optionalDays;
    public ListEnumShapingPojo() {}
    public List<ListEnumShapingRecord.Day> getWorkdays() { return workdays; }
    public void setWorkdays(List<ListEnumShapingRecord.Day> workdays) { this.workdays = workdays; }
    public List<ListEnumShapingRecord.Priority> getPriorities() { return priorities; }
    public void setPriorities(List<ListEnumShapingRecord.Priority> priorities) { this.priorities = priorities; }
    public List<ListEnumShapingRecord.Day> getOptionalDays() { return optionalDays; }
    public void setOptionalDays(List<ListEnumShapingRecord.Day> optionalDays) { this.optionalDays = optionalDays; }

    public enum Day { MON,TUE,WED,THU,FRI,SAT,SUN }
    public enum Priority { LOW,MEDIUM,HIGH,CRITICAL }
}
