package jorus.parallel.collectives;

import jorus.parallel.Broadcast;
import jorus.parallel.PxSystem;

public final class FlatBroadcast<T> extends Broadcast<T> {

    public FlatBroadcast(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @Override
    public void broadcast(T data) throws Exception {
 
        final int length = util.getLength(data);
        
        if (rank == 0) { 
            for (int partner = 1; partner < size; partner++) {
                comm.send(partner, data, 0, length);
            }
        } else { 
            comm.receive(0, data, 0, length);
        }
    }

}
