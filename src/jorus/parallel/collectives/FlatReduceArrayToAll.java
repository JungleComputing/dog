package jorus.parallel.collectives;

import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;
import jorus.parallel.ReduceArrayToAll;

public final class FlatReduceArrayToAll<T> extends ReduceArrayToAll<T> {

    public FlatReduceArrayToAll(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T reduceArrayToAll(T data, CxRedOpArray<T> op) throws Exception {
        
   //     long start = System.nanoTime();

        final int length = util.getLength(data);
        
        if (rank == 0) {
            
            final T tmp = (T) util.create(length);
            
            for (int partner = 1; partner < size; partner++) {
                comm.receive(partner, tmp, 0, length);
                op.doIt(data, tmp);
            }

            for (int partner = 1; partner < size; partner++) {
                comm.send(partner, data, 0, length);
            }
    
            util.release(tmp);
      
        } else {
            comm.send(0, data, 0, length);
            comm.receive(0, data, 0, length);
        }

    //    addTime(System.nanoTime() - start);
   
        return data;
    }

}
