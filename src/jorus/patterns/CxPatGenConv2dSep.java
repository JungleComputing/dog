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
import jorus.operations.CxGenConv2dSep;
import jorus.operations.CxSetBorder;
import jorus.parallel.PxSystem;


//import array.CxArray2dScalarDouble;



public class CxPatGenConv2dSep
{
    public static CxArray2d dispatch(CxArray2d s1, CxArray2d ker1,
            CxArray2d ker2, CxGenConv2dSep gco, CxSetBorder sbo)
    {
        int numX = ker1.getWidth() / 2;
        int numY = ker2.getWidth() / 2;

        CxArray2d dst = null;

        if (numX > s1.getBorderWidth() || numY > s1.getBorderHeight()) {
            dst = s1.clone(numX, numY);
        } else {
            dst = s1.clone();
        }

        if (PxSystem.initialized()) {		
            
            final PxSystem px = PxSystem.get();
            final int rank = px.myCPU();
            
            // run parallel
            try {
                if (dst.getLocalState() != CxArray2d.VALID ||
                        dst.getDistType() != CxArray2d.PARTIAL) {
                    if (rank == 0) System.out.println("GENCONV SCATTER 1...");
                    px.scatter(dst);
                }

                CxPatSetBorder.dispatch(dst, numX, 0, sbo);
                CxArray2d tmp = dst.clone();
                gco.init(dst, ker1, ker2, true);
                gco.doIt(tmp.getPartialDataWriteOnly(), 
                		dst.getPartialDataReadOnly(),
                        ker1.getDataReadOnly(), 0);
                
                CxPatSetBorder.dispatch(tmp, 0, numY, sbo);
                gco.doIt(dst.getPartialDataWriteOnly(), 
                		tmp.getPartialDataReadOnly(),
                        ker2.getDataReadOnly(), 1);
                
                dst.setGlobalState(CxArray2d.INVALID);

//              if (PxSystem.myCPU() == 0) System.out.println("GENCONV GATHER...");
//              PxSystem.gatherOFT(dst);

            } catch (Exception e) {
                System.err.println("Failed to perform operation!");
                e.printStackTrace(System.err);

            }

        } else { 									// run sequential
            CxPatSetBorder.dispatch(dst, numX, 0, sbo);
            CxArray2d tmp = dst.clone();
            gco.init(dst, ker1, ker2, false);
            gco.doIt(tmp.getDataWriteOnly(), dst.getDataReadOnly(), ker1.getDataReadOnly(), 0);
            CxPatSetBorder.dispatch(tmp, 0, numY, sbo);
            gco.doIt(dst.getDataWriteOnly(), tmp.getDataReadOnly(), ker2.getDataReadOnly(), 1);
        }

        return dst;
    }
}
