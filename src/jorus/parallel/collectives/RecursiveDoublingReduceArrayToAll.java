package jorus.parallel.collectives;

import jorus.parallel.ReduceArrayToAll;
import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;

public class RecursiveDoublingReduceArrayToAll<T> extends ReduceArrayToAll<T> {

    public RecursiveDoublingReduceArrayToAll(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T reduceArrayToAll(T data, CxRedOpArray<T> op) throws Exception {

        // NOTE: approach used the recursive doubling approach. We assume that 
        // the reduce operation in commutative.

        // First allocate a temporary buffer.
        final int length = util.getLength(data);
        final T tmp = (T) util.create(length);

        // Next, we need to find nearest power-of-two less than or equal to 
        // the number of participating machines.

        int pof2 = 1;
        while (pof2 <= size) pof2 <<= 1;
        pof2 >>=1;

        int rem = size - pof2;
        int newrank;

        /* In the non-power-of-two case, all even-numbered
               processes of rank < 2*rem send their data to
               (rank+1). These even-numbered processes no longer
               participate in the algorithm until the very end. The
               remaining processes form a nice power-of-two. */

        if (rank < 2*rem) {

            //  System.out.println("ALLREDUCE: Adjust processes (PRE)!");

            if (rank % 2 == 0) { /* even */
                comm.send(rank+1, data, 0, length);
                /* temporarily set the rank to -1 so that this
                       process does not pariticipate in recursive
                       doubling */
                newrank = -1;

            } else { /* odd */
                comm.receive(rank-1, tmp, 0, length);
                /* do the reduction on received data. since the
                       ordering is right, it doesn't matter whether
                       the operation is commutative or not. */

                op.doIt(data, tmp);

                /* change the rank */
                newrank = rank / 2;
            }

        } else { /* rank >= 2*rem */
            newrank = rank - rem;
        }

        // We will now perform a reduce using recursive doubling when there is 
        // little data, or recursive halving followed by an allgather when there
        // is enough data. Some machines will be left out here if the number of 
        // machines is not a power of two. 

        if (newrank != -1) {

            /* use recursive doubling */                
            int mask = 0x1;

            while (mask < pof2) {
                final int newdst = newrank ^ mask;

                /* find real rank of dest */
                final int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;

                /* Send the most current data, which is in a. Receive
                           into tmp */

                comm.exchange(dst, data, 0, length, tmp, 0, length);

                /* tmp contains data received in this step.
                           a contains data accumulated so far */

                op.doIt(data, tmp);            

                mask <<= 1;
            }
        }

        /* In the non-power-of-two case, all odd-numbered
               processes of rank < 2*rem send the result to
              (rank-1), the ranks who didn't participate above. */

        if (rank < 2*rem) {
            if (rank % 2 == 1) { /* odd */
                comm.send(rank-1, data, 0, length);
            } else {  
                comm.receive(rank+1, data, 0, length);
            }
        }

        util.release(tmp);

        //   addTime(System.nanoTime() - start);

        return data;
    }
}
