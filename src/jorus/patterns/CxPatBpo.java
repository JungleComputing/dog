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
import jorus.operations.CxBpo;
import jorus.parallel.PxSystem;


public class CxPatBpo
{
	public static CxArray2d dispatch(CxArray2d s1, CxArray2d s2,
									 boolean inplace, CxBpo bpo)
	{
		CxArray2d dst = s1;
		if (!inplace) dst = s1.clone();

		if (PxSystem.initialized()) {				// run parallel
			try {

				if (s1.getLocalState() != CxArray2d.VALID ||
							s1.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("BPO SCATTER 1...");
					PxSystem.scatterOFT(s1);
 				}
				if (s2.getLocalState() != CxArray2d.VALID ||
							s2.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("BPO SCATTER 2...");
					PxSystem.scatterOFT(s2);
 				}
				if (dst.getLocalState() != CxArray2d.VALID ||
							dst.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("BPO SCATTER 3...");
					PxSystem.scatterOFT(dst);
 				}

				bpo.init(s1, s2, true);
				bpo.doIt(dst.getPartialData(), s2.getPartialData());
				dst.setGlobalState(CxArray2d.INVALID);

//if (PxSystem.myCPU() == 0) System.out.println("BPO GATHER...");
//PxSystem.gatherOFT(dst);

			} catch (Exception e) {
				//
			}

		} else {									// run sequential
			bpo.init(s1, s2, false);
			bpo.doIt(dst.getData(), s2.getData());
		}

		return dst;
	}
}
