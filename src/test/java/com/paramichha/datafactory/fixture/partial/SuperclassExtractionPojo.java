package com.paramichha.datafactory.fixture.partial;

import jakarta.validation.constraints.*;

/** Gap: id and createdAt from SuperclassBase not extracted. When fixed: move to type/. */
public class SuperclassExtractionPojo extends SuperclassBase {
    @NotBlank  private String  name;
    @NotNull @Positive private Integer value;

    public SuperclassExtractionPojo() {}

    public String  getName()  { return name;  }
    public Integer getValue() { return value; }
    public void setName(String v)   { this.name  = v; }
    public void setValue(Integer v) { this.value = v; }
}
