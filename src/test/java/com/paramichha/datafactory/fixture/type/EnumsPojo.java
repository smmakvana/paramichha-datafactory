package com.paramichha.datafactory.fixture.type;

public class EnumsPojo {
     Status status;
     Priority priority;
     Day day;
    public EnumsPojo() {}
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Day getDay() { return day; }
    public void setDay(Day day) { this.day = day; }

    public enum Status{ACTIVE,INACTIVE,PENDING}
    public enum Priority{LOW,MEDIUM,HIGH,CRITICAL}
    public enum Day{MON,TUE,WED,THU,FRI,SAT,SUN}
}
