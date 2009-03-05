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
import jorus.operations.CxSetBorder;
import jorus.parallel.PxSystem;


public class CxPatSetBorder
{
    public static void dispatch(CxArray2d s1,
            int numX, int numY, CxSetBorder sbo)
    {
        if (PxSystem.initialized()) {				// run parallel
            sbo.init(s1, true);
            sbo.doIt(s1.getPartialDataReadWrite(), numX, numY);
        } else {									// run sequential
            sbo.init(s1, false);
            sbo.doIt(s1.getDataReadWrite(), numX, numY);
        }
    }
}
