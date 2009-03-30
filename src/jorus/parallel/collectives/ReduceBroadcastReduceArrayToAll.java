package jorus.parallel.collectives;

import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;
import jorus.parallel.ReduceArrayToAll;

public class ReduceBroadcastReduceArrayToAll<T> extends ReduceArrayToAll<T> {

    public ReduceBroadcastReduceArrayToAll(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T reduceArrayToAll(T data, CxRedOpArray<T> op) throws Exception {
        
        // This is a simple binomial reduce-to-root followed by a binomial 
        // broadcast. 
        
        // Reduce phase
        final int length = util.getLength(data);
        final T tmp = (T) util.create(length);
        
        int mask = 0x1;
        
        while (mask < size) {
            
            /* Receive */
            if ((mask & rank) == 0) {
                final int source = (rank | mask);
                
                if (source < size) {
                    comm.receive(source, tmp, 0, length);
                    op.doIt(data, tmp);            
                } 
            } else {
                /* Done receiveing. Now send my result to parent */                    
                final int dst = ((rank & (~ mask))) % size; 
                comm.send(dst, data, 0, length);
            }
            
            mask <<= 1;
        }
        
        // Broadcast phase
        // First handle the receive phase
        mask = 0x1;
        
        while (mask < size) { 

            if ((rank & mask) != 0) {
                
                final int source = rank - mask; 
                comm.receive(source, data, 0, length);
                break;
            }
            mask <<= 1;
        }
        
        // Next handle the send phase
        mask >>= 1;
        
        while (mask > 0) { 

            if ((rank + mask) < size) {
                
                final int dst = rank + mask;
                comm.send(dst, data, 0, length);
            }     
            mask >>= 1;
        }
        
        util.release(tmp);

        return data;
    }

}
