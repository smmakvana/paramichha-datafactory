package com.paramichha.datafactory.fixture.type;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class EnumsAllArgs {
     Status status;
     Priority priority;
     Day day;

    public enum Status{ACTIVE,INACTIVE,PENDING}
    public enum Priority{LOW,MEDIUM,HIGH,CRITICAL}
    public enum Day{MON,TUE,WED,THU,FRI,SAT,SUN}
}
