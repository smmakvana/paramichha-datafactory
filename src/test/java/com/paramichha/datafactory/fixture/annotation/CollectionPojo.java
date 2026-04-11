package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import java.util.*;
public class CollectionPojo {
    @NotEmpty List<String> roles;
    @Size(min=1,max=5) List<String> tags;
    @Size(min=1,max=3) List<Integer> scores;
     List<String> notes;
     Collection<String> labels;
    public CollectionPojo() {}
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<Integer> getScores() { return scores; }
    public void setScores(List<Integer> scores) { this.scores = scores; }
    public List<String> getNotes() { return notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }
    public Collection<String> getLabels() { return labels; }
    public void setLabels(Collection<String> labels) { this.labels = labels; }
}
