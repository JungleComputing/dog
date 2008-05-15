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
	public static double[] dispatch(CxArray2d s1, CxArray2d s2,
									int nBins, double minVal,
									double maxVal, CxBpoToHist bpo)
	{
		double [] dst = new double[nBins];
		for (int i=0; i<nBins; i++) {
			dst[i] = 0.;
		}

		if (PxSystem.initialized()) {				// run parallel
			try {

				if (s1.getLocalState() != CxArray2d.VALID ||
							s1.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("BPO2HIST SCATTER 1...");
					PxSystem.scatterOFT(s1);
				}
				if (s2.getLocalState() != CxArray2d.VALID ||
							s2.getDistType() != CxArray2d.PARTIAL) {
if (PxSystem.myCPU() == 0) System.out.println("BPO2HIST SCATTER 2...");
					PxSystem.scatterOFT(s2);
				}

				bpo.init(s1, s2, true);
				bpo.doIt(dst, s1.getPartialData(),
						 s2.getPartialData(), nBins, minVal, maxVal);

//if (PxSystem.myCPU() == 0) System.out.println("BPO2HIST ALLREDUCE..");
				PxSystem.reduceArrayToAllOFT(dst,
										new CxRedOpAddDoubleArray());

			} catch (Exception e) {
				//
			}

		} else {									// run sequential
			bpo.init(s1, s2, false);
			bpo.doIt(dst, s1.getData(),
					 s2.getData(), nBins, minVal, maxVal);
		}
		return dst;
	}
}
