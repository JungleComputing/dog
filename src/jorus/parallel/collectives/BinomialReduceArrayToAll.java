package jorus.parallel.collectives;

import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;
import jorus.parallel.ReduceArrayToAll;

public final class BinomialReduceArrayToAll<T> extends ReduceArrayToAll<T> {

    public BinomialReduceArrayToAll(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T reduceArrayToAll(T data, CxRedOpArray<T> op) throws Exception {

    //    long start = System.nanoTime();

        final int length = util.getLength(data);
        final T tmp = (T) util.create(length);

        int mask = 1;

        for (int i=0; i<logCPUs; i++) {
            comm.exchange(rank ^ mask, data, 0, length, tmp, 0, length);
            op.doIt(data, tmp);
            mask <<= 1;
        }

    //    addTime(System.nanoTime() - start);      
        return data;
    }
}
