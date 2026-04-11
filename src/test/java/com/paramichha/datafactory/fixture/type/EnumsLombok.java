package com.paramichha.datafactory.fixture.type;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class EnumsLombok {
     Status status;
     Priority priority;
     Day day;

    public enum Status{ACTIVE,INACTIVE,PENDING}
    public enum Priority{LOW,MEDIUM,HIGH,CRITICAL}
    public enum Day{MON,TUE,WED,THU,FRI,SAT,SUN}
}
