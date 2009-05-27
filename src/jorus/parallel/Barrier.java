package jorus.parallel;

public abstract class Barrier extends Collective {

    protected Barrier(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    public abstract void barrier() throws Exception; 
    
    /*
    public static Barrier create(String type, PxSystem s, Class c) throws Exception { 
        
        // FIXME: This should be done using reflection, but I'm not sure how to mix this 
        // with generics -- J.
        if (type.equals("Flat")) { 
            return new FlatBarrier(s, c);
        } else { 
            throw new Exception("Unknown barrier implementation selected!");
        }
    }*/
    
    
}
