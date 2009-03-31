package jorus.patterns;

import jorus.array.CxArray2d;
import jorus.operations.CxBpoConvertByteDouble;
import jorus.parallel.PxSystem;

public class CxPatSet {

    public static CxArray2d dispatch(CxArray2d dst, CxArray2d src) {
     
        if (PxSystem.initialized()) {                           // run parallel
       
            final PxSystem px = PxSystem.get();
            final int rank = px.myCPU();
            
            try {
                if (src.getLocalState() != CxArray2d.VALID ||
                        src.getDistType() != CxArray2d.PARTIAL) {

                    if (rank == 0) System.out.println("BPO SCATTER 1...");
                    
                    px.scatter(src);
                }
                
                if (!dst.hasPartialData()) { 
               
                    byte [] tmp = (byte[]) src.getPartialDataReadOnly();
                    
                    dst.setPartialData(src.getPartialWidth(), 
                            src.getPartialHeight(), new double[tmp.length], 
                            CxArray2d.VALID, CxArray2d.PARTIAL);
                }
                
                CxBpoConvertByteDouble tmp = new CxBpoConvertByteDouble();
                tmp.init(dst, src, true);
                tmp.doIt((double [])dst.getPartialDataWriteOnly(), 
                        (byte [])src.getPartialDataReadWrite());

                dst.setGlobalState(CxArray2d.INVALID);
                
//              if (PxSystem.myCPU() == 0) System.out.println("BPO GATHER...");
//              PxSystem.gatherOFT(dst);

            } catch (Exception e) {
                System.err.println("Failed to perform operation!");
                e.printStackTrace(System.err);
            }

        } else {
            if (!dst.hasData()) {

                byte[] tmp = (byte[]) src.getDataReadOnly();

                dst.setData(src.getWidth(), src.getHeight(),
                        new double[tmp.length], CxArray2d.VALID);
            }
            
            CxBpoConvertByteDouble tmp = new CxBpoConvertByteDouble();
            
            tmp.init(dst, src, false);
            tmp.doIt((double [])dst.getDataWriteOnly(), 
                    (byte [])src.getDataReadWrite());

            dst.setGlobalState(CxArray2d.INVALID);
        }

        return dst;
    }

    
}
