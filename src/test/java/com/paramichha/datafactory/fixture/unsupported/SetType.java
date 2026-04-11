package com.paramichha.datafactory.fixture.unsupported;
import java.util.*;
/** NOT supported. When SetTypeShaper added: move to type/. */
public class SetType {
    private Set<String> tags; private Set<Integer> ids;
    private LinkedHashSet<String> orderedTags; private TreeSet<Integer> sortedIds;
    public SetType(){}
    public Set<String> getTags(){return tags;} public Set<Integer> getIds(){return ids;}
    public LinkedHashSet<String> getOrderedTags(){return orderedTags;} public TreeSet<Integer> getSortedIds(){return sortedIds;}
    public void setTags(Set<String> v){tags=v;} public void setIds(Set<Integer> v){ids=v;}
    public void setOrderedTags(LinkedHashSet<String> v){orderedTags=v;} public void setSortedIds(TreeSet<Integer> v){sortedIds=v;}
}
