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
import jorus.operations.CxSvo;
import jorus.parallel.PxSystem;


public class CxPatSvo
{
    public static CxArray2d dispatch(CxArray2d s1, int x, int y,
            boolean inplace, CxSvo svo)
    {
        CxArray2d dst = s1;

        if (PxSystem.initialized()) {				// run parallel
            
            final PxSystem px = PxSystem.get();
            final int rank = px.myCPU();
            
            try {

                if (s1.getLocalState() != CxArray2d.VALID ||
                        s1.getDistType() != CxArray2d.PARTIAL) {

                    // The data structure has not been distibuted yet, or is no 
                    // longer valid

                    if (s1.getGlobalState() != CxArray2d.NONE) { 

                        if (rank == 0) System.out.println("SVO SCATTER 1...");
                        px.scatter(dst);

                    } else { 
                        // Added -- J
                        //
                        // A hack that assumes dst is a target data structure which we do not need to 
                        // scatter. We only initialize the local partitions.

                        final int pHeight = px.getPartHeight(s1.getHeight(),rank);

                        final double[] pData = 
                            new double[(s1.getWidth() + s1.getBorderWidth() * 2)
                                       * (pHeight + s1.getBorderHeight() * 2) 
                                       * s1.getExtent()];

                        s1.setPartialData(s1.getWidth(), pHeight, pData, 
                                CxArray2d.VALID, CxArray2d.PARTIAL);
                    }                    
                }

                if (!inplace) dst = s1.clone();

                svo.init(s1, true);

                int start = px.getLclStartY(s1.getHeight(), rank);

                if ((y >= start) && (y < start+s1.getPartialHeight())) {
                    svo.doIt(dst.getPartialDataWriteOnly(), x, y - start);
                }

                if (dst.getGlobalState() != CxArray2d.NONE) { 
                    dst.setGlobalState(CxArray2d.INVALID);
                }

//              if (PxSystem.myCPU() == 0) System.out.println("SVO GATHER...");
//              PxSystem.gatherOFT(dst);

            } catch (Exception e) {
                System.err.println("Failed to perform operation!");
                e.printStackTrace(System.err);
            }

        } else {									// run sequential

            if (!inplace) dst = s1.clone();

            svo.init(s1, false);
            svo.doIt(dst.getDataWriteOnly(), x, y);
        }

        return dst;
    }
}
