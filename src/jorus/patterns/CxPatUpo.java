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
import jorus.operations.CxUpo;
import jorus.parallel.PxSystem;

public class CxPatUpo {
    /*
     * public static CxArray2d dispatch(CxArray2d s1, boolean inplace, CxUpo
     * upo) { CxArray2d dst = s1;
     * 
     * if (!inplace) dst = s1.clone();
     * 
     * if (PxSystem.initialized()) { // run parallel try {
     * 
     * if (s1.getLocalState() != CxArray2d.VALID || s1.getDistType() !=
     * CxArray2d.PARTIAL) { if (PxSystem.myCPU() == 0) System.out.println("UPO
     * SCATTER 1..."); PxSystem.scatterOFT(s1); }
     * 
     * if (dst.getLocalState() != CxArray2d.VALID || dst.getDistType() !=
     * CxArray2d.PARTIAL) { if (PxSystem.myCPU() == 0) System.out.println("UPO
     * SCATTER 2..."); PxSystem.scatterOFT(dst); }
     * 
     * upo.init(s1, true); upo.doIt(dst.getPartialDataReadWrite());
     * dst.setGlobalState(CxArray2d.INVALID);
     *  // if (PxSystem.myCPU() == 0) System.out.println("UPO GATHER..."); //
     * PxSystem.gatherOFT(dst);
     *  } catch (Exception e) { System.err.println("Failed to perform
     * operation!"); e.printStackTrace(System.err); }
     *  } else { // run sequential upo.init(s1, false);
     * upo.doIt(dst.getDataReadWrite()); }
     * 
     * return dst; }
     */

    public static CxArray2d dispatch(CxArray2d s1, boolean inplace, CxUpo upo) {
        CxArray2d dst = s1;

        if (PxSystem.initialized()) { // run parallel

            final PxSystem px = PxSystem.get();
            final int rank = px.myCPU();
            final int size = px.nrCPUs();

            try {

                if (s1.getLocalState() != CxArray2d.VALID
                        || s1.getDistType() != CxArray2d.PARTIAL) {

                    // The data structure has not been distibuted yet, or is no
                    // longer valid

                    if (s1.getGlobalState() != CxArray2d.NONE) {

                        // if (PxSystem.myCPU() == 0) System.out.println("UPO
                        // SCATTER 1...");
                        px.scatter(dst);

                    } else {
                        // Added -- J
                        //
                        // A hack that assumes dst is a target data structure
                        // which we do not need to
                        // scatter. We only initialize the local partitions.

                        final int pHeight = px.getPartHeight(s1.getHeight(),
                                rank);

                        final double[] pData = new double[(s1.getWidth() + s1
                                .getBorderWidth() * 2)
                                * (pHeight + s1.getBorderHeight() * 2)
                                * s1.getExtent()];

                        s1.setPartialData(s1.getWidth(), pHeight, pData,
                                CxArray2d.VALID, CxArray2d.PARTIAL);
                    }
                }

                if (!inplace)
                    dst = s1.clone();

                upo.init(s1, true);
                upo.doIt(dst.getPartialDataReadWrite());
                dst.setGlobalState(CxArray2d.INVALID);

                // if (PxSystem.myCPU() == 0) System.out.println("UPO
                // GATHER...");
                // PxSystem.gatherOFT(dst);

            } catch (Exception e) {
                System.err.println("Failed to perform operation!");
                e.printStackTrace(System.err);
            }

        } else { // run sequential
            if (s1.getGlobalState() == CxArray2d.NONE) {
                final double[] data = new double[(s1.getWidth() + s1
                        .getBorderWidth() * 2)
                        * (s1.getHeight() + s1.getBorderHeight() * 2)
                        * s1.getExtent()];

                s1
                        .setData(s1.getWidth(), s1.getHeight(), data,
                                CxArray2d.VALID);
            }

            upo.init(s1, false);
            upo.doIt(dst.getDataReadWrite());

        }

        return dst;
    }
}
