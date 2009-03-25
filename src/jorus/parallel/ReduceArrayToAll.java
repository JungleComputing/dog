package jorus.parallel;

import jorus.operations.CxRedOpArray;

public abstract class ReduceArrayToAll<T> extends Collective {
   
    protected ReduceArrayToAll(PxSystem system, Class c) throws Exception {
        super(system, c);
    }
    
    public abstract T reduceArrayToAll(T data, CxRedOpArray<T> op) throws Exception;  
}
