package jorus.parallel;

import jorus.operations.CxRedOpArray;

public abstract class ReduceArrayToRoot<T> extends Collective {
   
    protected ReduceArrayToRoot(PxSystem system, Class c) throws Exception {
        super(system, c);
    }
    
    public abstract T reduceArrayToRoot(T data, CxRedOpArray<T> op) throws Exception;
}
