package jorus.parallel.collectives;

import jorus.parallel.PxSystem;
import jorus.parallel.Scatter;

public class BinomialScatter<T> extends Scatter<T> { 

    public BinomialScatter(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    @Override
    public void scatter(T send, int[] offsets, int[] sizes, 
            T receive, int offset, int length) throws Exception {
/*
        // int xSize = (globW + a.getBorderWidth() * 2) * a.getExtent();
        // int length = xSize * globH;
        // int offset = xSize * a.getBorderHeight();

        int mask = 1 << (logCPUs - 1);

        for (int i = 0; i < logCPUs; i++) {
            int partner = rank ^ mask;

            if ((rank % mask == 0) && (partner < size)) {
                if (rank < partner) {
                    comm.send(partner, send, offsets[], len)




                    WriteMessage w = sps[partner].newMessage();
                    w.writeArray(a.getPartialDataReadOnly(), offset, length);
                    w.finish();

                    //        dataOutBroadcastSBT += length * 8;
                } else {
//                  if (rps[partner] == null) {
//                  rps[partner] = ibis.createReceivePort(portType, COMM_ID
//                  + partner);
//                  rps[partner].enableConnections();
//                  }
                    ReadMessage r = rps[partner].receive();
                    r.readArray(a.getPartialDataWriteOnly(), offset, length);
                    r.finish();

                    //        dataInBroadcastSBT += length * 8; 
                }
            }
            mask >>= 1;
        }*/
        
        // NOTE: this implementation assumes the data is in a single uninterrupted block in 
        // memory, and it is in the right order. In addition machine 0 is assumed to do be 
        // the source of the scatter. 

        /*
        if (offsets != null && sizes != null) { 
            System.out.println("Binomial scatter " + offsets.length + " " + sizes.length);

            for (int i=0;i<offsets.length;i++) { 
                System.out.println(i + " " + offsets[i] + " " + sizes[i]);
            }
        }
        */
        
        /*
        int mask = 1 << logCPUs;        
        
        int end = size;
        
        for (int i = 0; i < logCPUs+1; i++) {
            int partner = rank ^ mask;

            if ((rank % mask == 0) && (partner < size)) {
                if (rank < partner) {
               
                    int count = 0;
                    
                    for (int p=partner;p<=end;p++) { 
                        count += sizes[p];
                    }
                    
              //      comm.send(partner, send, offsets[partner] - offsets[rank], count);
                    System.out.println(rank + " sends [" + partner + "-" + end + "]: " + count + " to " + partner);
                    
                    end = partner-1;
                } else {
        
                    if ((rank % 2) == 1) { 
                        System.out.println(rank + " receives [" + rank + "] from " + partner);
                        return;
                    } else { 

                        int count = 0;

                        for (int p=rank;p<=end;p++) { 
                            count += sizes[p];
                        }

                        System.out.println(rank + " receives [" + rank + "-" + end + "]: " + count + " from " + partner);
                   
                    }
                    //comm.receive(partner, tmp, 0, count);
                }
            } else { 
                end--;
            }
            
            mask >>= 1;
        }
        */
        
        T tmp = null;
        
        int mask = 0x1;
        
        while (mask < size) { 
            
            if ((rank & mask) != 0) { 
            
                final int partner = rank ^ mask;
                
                if ((rank % 2) == 1) {
                    // we should now receive a single slice and return
                    comm.receive(partner, receive, offset, length);
                    
                    //System.out.println(rank + " receives 1 slice from " + partner + " (END)");
                     return;
                } else { 
                    //System.out.println(rank + " receives " + mask + " slices from " + partner);
                
                    // we should now receive multiple slices and start sending
                   
                    int count = 0;
                    
                    for (int d=0;d<mask;d++) { 
                        count += sizes[rank + d];
                    }
             
                    tmp = (T) util.create(count);
                    
                    comm.receive(partner, tmp, 0, count);
                }
                
                break;
            }
            
            mask <<= 1;
        }
        
        // We now own 'mask' blocks, starting with our own. 
        // we should now receive a single slice and return
        
        if (rank == 0) { 
            tmp = send;
        }
        
        mask >>= 1;
       
        while (mask > 0) {
            if (rank + mask < size) {
                int dst = rank + mask;
                
                //if (dst >= size) dst -= size;
            
                int count = 0;
                
                for (int d=0;d<mask;d++) { 
                    count += sizes[dst + d];
                }
                
                comm.send(dst, tmp, offsets[dst] - offsets[rank], count);
                
                //System.out.println(rank + " send " + mask + " slices to " + dst);
            }
            
            mask >>= 1; 
        }
        
        System.arraycopy(tmp, 0, receive, offset, length);
    }
}