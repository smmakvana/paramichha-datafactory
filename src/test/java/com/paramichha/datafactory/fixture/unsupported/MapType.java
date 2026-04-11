package com.paramichha.datafactory.fixture.unsupported;
import java.util.*;
/** NOT supported. When MapTypeShaper added: move to type/. */
public class MapType {
    private Map<String,String> properties; private Map<String,Integer> counts; private LinkedHashMap<String,String> orderedProps;
    public MapType(){}
    public Map<String,String> getProperties(){return properties;} public Map<String,Integer> getCounts(){return counts;} public LinkedHashMap<String,String> getOrderedProps(){return orderedProps;}
    public void setProperties(Map<String,String> v){properties=v;} public void setCounts(Map<String,Integer> v){counts=v;} public void setOrderedProps(LinkedHashMap<String,String> v){orderedProps=v;}
}
