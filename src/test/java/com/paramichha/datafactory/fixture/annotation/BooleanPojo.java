package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
public class BooleanPojo {
    @AssertTrue boolean activePrimitive;
    @AssertTrue Boolean verifiedWrapper;
    @AssertFalse boolean deletedPrimitive;
    @AssertFalse Boolean archivedWrapper;
    public BooleanPojo() {}
    public boolean getActivePrimitive() { return activePrimitive; }
    public void setActivePrimitive(boolean activePrimitive) { this.activePrimitive = activePrimitive; }
    public Boolean getVerifiedWrapper() { return verifiedWrapper; }
    public void setVerifiedWrapper(Boolean verifiedWrapper) { this.verifiedWrapper = verifiedWrapper; }
    public boolean getDeletedPrimitive() { return deletedPrimitive; }
    public void setDeletedPrimitive(boolean deletedPrimitive) { this.deletedPrimitive = deletedPrimitive; }
    public Boolean getArchivedWrapper() { return archivedWrapper; }
    public void setArchivedWrapper(Boolean archivedWrapper) { this.archivedWrapper = archivedWrapper; }
}
