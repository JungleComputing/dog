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
		if (!inplace) dst = s1.clone();

		if (PxSystem.initialized()) {				// run parallel
			try {

				if (s1.getLocalState() != CxArray2d.VALID ||
							s1.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("SVO SCATTER 1...");
					PxSystem.scatterOFT(s1);
				}
				if (dst.getLocalState() != CxArray2d.VALID ||
							dst.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("SVO SCATTER 2...");
					PxSystem.scatterOFT(dst);
				}

				svo.init(s1, true);
				int start = PxSystem.getLclStartY(s1.getHeight(),
												  PxSystem.myCPU());
				if ((y >= start) && (y < start+s1.getPartialHeight())) {
					svo.doIt(dst.getPartialData(), x, y - start);
				}
				dst.setGlobalState(CxArray2d.INVALID);

//if (PxSystem.myCPU() == 0) System.out.println("SVO GATHER...");
//PxSystem.gatherOFT(dst);

			} catch (Exception e) {
				//
			}

		} else {									// run sequential
			svo.init(s1, false);
			svo.doIt(dst.getData(), x, y);
		}

		return dst;
	}
}
