package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
public class NullPojo {
    @NotNull String notNullField;
    @NotBlank String notBlankField;
    @NotEmpty String notEmptyString;
    @NotEmpty java.util.List<String> notEmptyList;
    @Null String nullField;
    public NullPojo() {}
    public String getNotNullField() { return notNullField; }
    public void setNotNullField(String notNullField) { this.notNullField = notNullField; }
    public String getNotBlankField() { return notBlankField; }
    public void setNotBlankField(String notBlankField) { this.notBlankField = notBlankField; }
    public String getNotEmptyString() { return notEmptyString; }
    public void setNotEmptyString(String notEmptyString) { this.notEmptyString = notEmptyString; }
    public java.util.List<String> getNotEmptyList() { return notEmptyList; }
    public void setNotEmptyList(java.util.List<String> notEmptyList) { this.notEmptyList = notEmptyList; }
    public String getNullField() { return nullField; }
    public void setNullField(String nullField) { this.nullField = nullField; }
}
