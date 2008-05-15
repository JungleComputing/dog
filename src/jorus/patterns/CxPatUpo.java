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


public class CxPatUpo
{
	public static CxArray2d dispatch(CxArray2d s1,
									 boolean inplace, CxUpo upo)
	{
		CxArray2d dst = s1;
		if (!inplace) dst = s1.clone();

		if (PxSystem.initialized()) {				// run parallel
			try {

				if (s1.getLocalState() != CxArray2d.VALID ||
							s1.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("UPO SCATTER 1...");
					PxSystem.scatterOFT(s1);
				}
				if (dst.getLocalState() != CxArray2d.VALID ||
							dst.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("UPO SCATTER 2...");
					PxSystem.scatterOFT(dst);
				}

				upo.init(s1, true);
				upo.doIt(dst.getPartialData());
				dst.setGlobalState(CxArray2d.INVALID);

//if (PxSystem.myCPU() == 0) System.out.println("UPO GATHER...");
//PxSystem.gatherOFT(dst);

			} catch (Exception e) {
				//
			}

		} else {									// run sequential
			upo.init(s1, false);
			upo.doIt(dst.getData());
		}

		return dst;
	}
}
