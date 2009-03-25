/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.patterns;


import jorus.array.CxArray2d;
import jorus.operations.CxBpoToHist;
import jorus.operations.CxRedOpAddDoubleArray;
import jorus.parallel.PxSystem;


public class CxPatBpoToHist
{
    private static final CxRedOpAddDoubleArray reduceOp = new CxRedOpAddDoubleArray();
    
    public static double[] dispatch(CxArray2d s1, CxArray2d s2,
            int nBins, double minVal,
            double maxVal, CxBpoToHist bpo)
    {
        double [] dst = new double[nBins];

        if (PxSystem.initialized()) {		
            
            final PxSystem px = PxSystem.get();
            final int rank = px.myCPU();
      
            try {

                if (s1.getLocalState() != CxArray2d.VALID ||
                        s1.getDistType() != CxArray2d.PARTIAL) {
                    if (rank == 0) System.out.println("BPO2HIST SCATTER 1...");
                    px.scatter(s1);
                }
                if (s2.getLocalState() != CxArray2d.VALID ||
                        s2.getDistType() != CxArray2d.PARTIAL) {
                    if (rank == 0) System.out.println("BPO2HIST SCATTER 2...");
                    px.scatter(s2);
                }

                bpo.init(s1, s2, true);

                bpo.doIt(dst, s1.getPartialDataReadOnly(),
                        s2.getPartialDataReadOnly(), nBins, minVal, maxVal);

//              if (PxSystem.myCPU() == 0) System.out.println("BPO2HIST ALLREDUCE..");
                px.reduceArrayToAll(dst, reduceOp);

            } catch (Exception e) {
                System.err.println("Failed to perform operation!");
                e.printStackTrace(System.err);
            }

        } else {									// run sequential
            bpo.init(s1, s2, false);
            bpo.doIt(dst, s1.getDataReadOnly(),
                    s2.getDataReadOnly(), nBins, minVal, maxVal);
        }
        return dst;
    }
}
