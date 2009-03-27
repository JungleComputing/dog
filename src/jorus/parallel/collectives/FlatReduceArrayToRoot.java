package jorus.parallel.collectives;

import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;
import jorus.parallel.ReduceArrayToRoot;

public final class FlatReduceArrayToRoot<T> extends ReduceArrayToRoot<T> {

    public FlatReduceArrayToRoot(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T reduceArrayToRoot(T data, CxRedOpArray<T> op) throws Exception {
   
        final int length = util.getLength(data);
        
        if (rank == 0) {
            
            final T tmp = (T) util.create(length);
            
            for (int partner = 1; partner < size; partner++) {
                comm.receive(partner, tmp, 0, length);
                op.doIt(data, tmp);
            }
      
            util.release(tmp);
            
        } else {
            comm.send(0, data, 0, length);
        }
   
        return data;
    }
}
