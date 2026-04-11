package com.paramichha.datafactory.fixture.unsupported;
import java.util.*;
/** NOT supported. Rare in REST layer. */
public class QueueType {
    private Queue<String> pending; private Deque<String> history; private ArrayDeque<Integer> buffer;
    public QueueType(){}
    public Queue<String> getPending(){return pending;} public Deque<String> getHistory(){return history;} public ArrayDeque<Integer> getBuffer(){return buffer;}
    public void setPending(Queue<String> v){pending=v;} public void setHistory(Deque<String> v){history=v;} public void setBuffer(ArrayDeque<Integer> v){buffer=v;}
}
