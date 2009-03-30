package jorus.parallel.collectives;

import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;
import jorus.parallel.ReduceArrayToRoot;

public class BinomialReduceArrayToRoot<T> extends ReduceArrayToRoot<T> {

    public BinomialReduceArrayToRoot(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T reduceArrayToRoot(T data, CxRedOpArray<T> op) throws Exception {
   
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
        
        util.release(tmp);
   
        return data;
    }
}
