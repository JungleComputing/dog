package jorus.parallel.collectives;

import jorus.parallel.Gather;
import jorus.parallel.PxSystem;

public final class FlatGather<T> extends Gather<T> {

    public FlatGather(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void gather(T out, int offset, int size, T in, int[] offsets, int[] sizes) throws Exception {
  
        if (rank == 0) { 
            for (int partner = 1; partner < size; partner++) {
                comm.receive(partner, out, offsets[partner], sizes[partner]);
            }
    
            System.arraycopy(out, offset, in, offsets[0], size);
        } else { 
            comm.send(0, in, offset, size);
        }
    }

}
