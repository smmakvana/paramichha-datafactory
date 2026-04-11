package com.paramichha.datafactory.fixture.type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class ValuesLombok {
     Byte byteVal;
     Short shortVal;
     Integer intVal;
     Long longVal;
     Float floatVal;
     Double doubleVal;
     Character charVal;
     Boolean boolVal;
     BigDecimal bigDecimalVal;
     BigInteger bigIntegerVal;
     UUID uuidVal;
     String stringVal;
}
