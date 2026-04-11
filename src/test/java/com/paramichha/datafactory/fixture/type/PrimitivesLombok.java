package com.paramichha.datafactory.fixture.type;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class PrimitivesLombok {
     byte byteVal;
     short shortVal;
     int intVal;
     long longVal;
     float floatVal;
     double doubleVal;
     char charVal;
     boolean boolVal;
}
