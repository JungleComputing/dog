package jorus.parallel.collectives;

import jorus.parallel.PxSystem;
import jorus.parallel.Scatter;

public final class FlatScatter<T> extends Scatter<T> {

    public FlatScatter(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void scatter(T send, int[] offsets, int[] sizes, 
            T receive, int offset, int length) throws Exception {

        if (rank == 0) { 
            for (int partner = 1; partner < this.size; partner++) {
                comm.send(partner, send, offsets[partner], sizes[partner]);
            }
    
            System.arraycopy(send, 0, receive, offset, length);
        } else { 
            comm.receive(0, receive, offset, length);
        }
    }
}
