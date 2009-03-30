package jorus.parallel.collectives;

import jorus.parallel.Broadcast;
import jorus.parallel.PxSystem;

public class BinomialBroadcast<T> extends Broadcast<T> {

    /* binomial tree broadcast, good for small messages */ 
    
    public BinomialBroadcast(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @Override
    public void broadcast(T data) throws Exception {
 
        final int length = util.getLength(data);
        
        // First handle the receive phase
        int mask = 0x1;
        
        while (mask < size) { 

            if ((rank & mask) != 0) {
                comm.receive(rank - mask, data, 0, length);
                break;
            }
            mask <<= 1;
        }
        
        // Next handle the send phase
        mask >>= 1;
        
        while (mask > 0) { 

            if ((rank + mask) < size) { 
                comm.send(rank + mask, data, 0, length);
            }     
            mask >>= 1;
        }
    }
}
