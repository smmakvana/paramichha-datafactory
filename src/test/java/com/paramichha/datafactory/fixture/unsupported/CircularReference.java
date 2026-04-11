package com.paramichha.datafactory.fixture.unsupported;
/** Circular reference — cycle detection. b always null. Always stays here. */
public class CircularReference {
    private String name; private Inner b;
    public CircularReference(){}
    public String getName(){return name;} public Inner getB(){return b;}
    public void setName(String v){name=v;} public void setB(Inner v){b=v;}
    public static class Inner {
        private String label; private CircularReference a;
        public Inner(){}
        public String getLabel(){return label;} public CircularReference getA(){return a;}
        public void setLabel(String v){label=v;} public void setA(CircularReference v){a=v;}
    }
}
