package jorus.patterns;

import jorus.array.CxArray2d;
import jorus.operations.CxBpoToHist;
import jorus.operations.CxRedOpAddDoubleArray;
import jorus.parallel.PxSystem;

public class CxPatBpoArrayToHistArray {

    public static double [][] dispatch(CxArray2d s1, CxArray2d [] a2, 
            int nBins, double minVal, double maxVal, CxBpoToHist bpo) {

        double [][] result = new double[a2.length][nBins];


        if (PxSystem.initialized()) {				// run parallel

            // FIXME: inefficient!
            double [] buffer = new double[a2.length * nBins];
            double [] dst = new double[nBins];
            
            try {
                if (s1.getLocalState() != CxArray2d.VALID ||
                        s1.getDistType() != CxArray2d.PARTIAL) {
                    if (PxSystem.myCPU() == 0) System.out.println("BPO2HIST SCATTER 1...");
                    PxSystem.scatterOFT(s1);
                }

                for (int i=0;i<a2.length;i++) {

                    CxArray2d s2 = a2[i];

                    if (s2.getLocalState() != CxArray2d.VALID ||
                            s2.getDistType() != CxArray2d.PARTIAL) {
                        if (PxSystem.myCPU() == 0) System.out.println("BPO2HIST SCATTER 2...");
                        PxSystem.scatterOFT(s2);
                    }

                    bpo.init(s1, s2, true);

                    bpo.doIt(dst, s1.getPartialDataReadOnly(),
                            s2.getPartialDataReadOnly(), nBins, minVal, maxVal);

                    System.arraycopy(dst, 0, buffer, i*nBins, nBins);
                }

//              if (PxSystem.myCPU() == 0) System.out.println("BPO2HIST ALLREDUCE..");
                PxSystem.reduceArrayToAllOFT(buffer, new CxRedOpAddDoubleArray());

                // FIXME: inefficient
                for (int i=0;i<result.length;i++) {
                    System.arraycopy(buffer, i*nBins, result[i], 0, nBins);
                }

            } catch (Exception e) {
                System.err.println("Failed to perform operation!");
                e.printStackTrace(System.err);
            }

        } else {									// run sequential
            for (int i=0;i<a2.length;i++) {
                CxArray2d s2 = a2[i];

                bpo.init(s1, s2, false);
                bpo.doIt(result[i], s1.getDataReadOnly(),
                        s2.getDataReadOnly(), nBins, minVal, maxVal);
            }
        }

        return result;
    }
}
