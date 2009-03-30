package jorus.parallel.collectives;

import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;
import jorus.parallel.ReduceArrayToAll;

public final class MPICHReduceArrayToAll<T> extends ReduceArrayToAll<T> {

    private static final int ALLREDUCE_SHORT_MSG = 2048;

    private final int [] counts; 
    private final int [] displacements; 
    
    private final int pof2;
    private final int rem;
    private final int newrank;
    
    public MPICHReduceArrayToAll(PxSystem system, Class c) throws Exception {
        super(system, c);

        // We need to find nearest power-of-two less than or equal to 
        // the number of participating machines.
        
        int tmp = 1;
        while (tmp <= size) tmp <<= 1;
        tmp >>=1;

        pof2 = tmp;
        rem = size - pof2;
        
        counts = new int[pof2];
        displacements = new int[pof2];
        
        if (rank < 2*rem) {
            if (rank % 2 == 0) { /* even */
                newrank = -1;
            } else { /* odd */
                newrank = rank / 2;
            }
        } else { /* rank >= 2*rem */
            newrank = rank - rem;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T reduceArrayToAll(T data, CxRedOpArray<T> op) throws Exception {

        // NOTE: this is a port of the MPICH allreduce algorithm, which uses 
        // a recursive halving approach. We assume that the reduce operation 
        // in commutative.

        // Added -- J
        // start = System.nanoTime();

        // First allocate a temporary buffer.
        final int length = util.getLength(data);
        final T tmp = (T) util.create(length);

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
            } else { /* odd */
                comm.receive(rank-1, tmp, 0, length);
                /* do the reduction on received data. since the
                   ordering is right, it doesn't matter whether
                   the operation is commutative or not. */

                op.doIt(data, tmp);
            }
        }
        
        // We will now perform a reduce using recursive doubling when there is 
        // little data, or recursive halving followed by an allgather when there
        // is enough data. Some machines will be left out here if the number of 
        // machines is not a power of two. 

        if (newrank != -1) {

            if (((length * util.typeSize()) <= ALLREDUCE_SHORT_MSG) || (length < pof2)) { 
                /* use recursive doubling */                
                int mask = 0x1;

                while (mask < pof2) {
                    int newdst = newrank ^ mask;

                    /* find real rank of dest */
                    int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;

                    /* Send the most current data, which is in a. Receive
                       into tmp */

                    comm.exchange(dst, data, 0, length, tmp, 0, length);

                    /* tmp contains data received in this step.
                       a contains data accumulated so far */

                    op.doIt(data, tmp);            

                    mask <<= 1;
                }
            } else {
                /* do a reduce-scatter followed by allgather */

                /* for the reduce-scatter, calculate the count that
                   each process receives and the displacement within
                   the buffer */

                final int [] cnts = counts;
                final int [] disps = displacements;

                for (int i=0; i<(pof2-1); i++) {
                    cnts[i] = length/pof2;
                }

                cnts[pof2-1] = length - (length/pof2)*(pof2-1);      

                disps[0] = 0;

                for (int i=1; i<pof2; i++) {
                    disps[i] = disps[i-1] + cnts[i-1];
                }

                int mask = 0x1;
                int send_idx = 0; 
                int recv_idx = 0;
                int last_idx = pof2;

                while (mask < pof2) {

                    final int newdst = newrank ^ mask;
                    /* find real rank of dest */
                    final int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;

                    int send_cnt = 0;
                    int recv_cnt = 0;

                    if (newrank < newdst) {
                        send_idx = recv_idx + pof2/(mask*2);

                        for (int i=send_idx; i<last_idx; i++) { 
                            send_cnt += cnts[i];
                        } 

                        for (int i=recv_idx; i<send_idx; i++) { 
                            recv_cnt += cnts[i];
                        }
                    } else {
                        recv_idx = send_idx + pof2/(mask*2);

                        for (int i=send_idx; i<recv_idx; i++) { 
                            send_cnt += cnts[i];
                        }
                        for (int i=recv_idx; i<last_idx; i++) { 
                            recv_cnt += cnts[i];
                        }
                    }

                    /* Send data from a. Receive into tmp */
                    comm.exchange(dst, data, disps[send_idx], send_cnt, 
                            tmp, disps[recv_idx], recv_cnt);

                    /* tmp contains data received in this step.
                       a contains data accumulated so far */
                    op.doItRange(data, tmp, disps[recv_idx], recv_cnt);            

                    /* update send_idx for next iteration */
                    send_idx = recv_idx;
                    mask <<= 1;

                    /* update last_idx, but not in last iteration
                       because the value is needed in the allgather
                       step below. */

                    if (mask < pof2) { 
                        last_idx = recv_idx + pof2/mask;
                    } 
                }

                /* now do the allgather */
                mask >>= 1;

                while (mask > 0) {
                    int newdst = newrank ^ mask;

                    /* find real rank of dest */
                    int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;

                    int send_cnt = 0; 
                    int recv_cnt = 0;

                    if (newrank < newdst) {
                        /* update last_idx except on first iteration */
                        if (mask != pof2/2) {
                            last_idx = last_idx + pof2/(mask*2);
                        }

                        recv_idx = send_idx + pof2/(mask*2);

                        for (int i=send_idx; i<recv_idx; i++) { 
                            send_cnt += cnts[i];
                        }

                        for (int i=recv_idx; i<last_idx; i++) { 
                            recv_cnt += cnts[i];
                        } 
                    } else {
                        recv_idx = send_idx - pof2/(mask*2);

                        for (int i=send_idx; i<last_idx; i++) { 
                            send_cnt += cnts[i];
                        }

                        for (int i=recv_idx; i<send_idx; i++) { 
                            recv_cnt += cnts[i];
                        }
                    }

                    /* Send data from a. Receive into a */
                    comm.exchange(dst, data, disps[send_idx], send_cnt, 
                            data, disps[recv_idx], recv_cnt);

                    if (newrank > newdst) { 
                        send_idx = recv_idx;
                    }

                    mask >>= 1;
                }
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
