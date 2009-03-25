package jorus.parallel;

public abstract class Scatter<T> extends Collective {
   
    protected Scatter(PxSystem system, Class c) throws Exception {
        super(system, c);
    }
    
    public abstract void scatter(T in, int [] offsets, int [] sizes, T out, 
            int offset, int size) throws Exception;
}
