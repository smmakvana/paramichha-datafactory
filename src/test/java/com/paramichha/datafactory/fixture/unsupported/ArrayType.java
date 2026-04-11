package com.paramichha.datafactory.fixture.unsupported;
/** NOT supported. When ArrayTypeShaper added: move to type/. */
public class ArrayType {
    private String[] namess; private int[] scores; private byte[] data; private long[] ids;
    public ArrayType(){}
    public String[] getNames(){return namess;} public int[] getScores(){return scores;} public byte[] getData(){return data;} public long[] getIds(){return ids;}
    public void setNames(String[] v){namess=v;} public void setScores(int[] v){scores=v;} public void setData(byte[] v){data=v;} public void setIds(long[] v){ids=v;}
}
