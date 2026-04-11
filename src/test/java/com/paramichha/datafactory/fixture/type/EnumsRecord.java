package com.paramichha.datafactory.fixture.type;

public record EnumsRecord(Status status, Priority priority, Day day) {
    public enum Status{ACTIVE,INACTIVE,PENDING}
    public enum Priority{LOW,MEDIUM,HIGH,CRITICAL}
    public enum Day{MON,TUE,WED,THU,FRI,SAT,SUN}
}
