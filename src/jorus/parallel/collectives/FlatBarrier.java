package jorus.parallel.collectives;

import jorus.parallel.Barrier;
import jorus.parallel.PxSystem;

public class FlatBarrier extends Barrier {

    public FlatBarrier(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @Override
    public void barrier() throws Exception {
        
        if (rank == 0) { 
            for (int partner = 1; partner < size; partner++) {
                comm.receive(partner);
            }
     
            for (int partner = 1; partner < size; partner++) {
                comm.send(partner);
            }
        } else { 
            comm.send(0);
            comm.receive(0);
        }
    }

}
