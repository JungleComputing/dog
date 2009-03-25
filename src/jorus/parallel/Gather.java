package jorus.parallel;

public abstract class Gather<T> extends Collective {
   
    protected Gather(PxSystem system, Class c) throws Exception {
        super(system, c);
    }
    
    public abstract void gather(T out, int offset, int size, 
            T in, int [] offsets, int [] sizes) throws Exception;   
}
