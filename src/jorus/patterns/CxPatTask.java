/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.patterns;


import jorus.weibull.CxWeibullFit;
import jorus.parallel.PxSystem;
import jorus.operations.CxRedOpAddDoubleArray;


public class CxPatTask
{
	// This must be generalized to allow any type of task to be executed


	public static void dispatch(CxWeibullFit task, int iMax, int jMax)
	{
		if (PxSystem.initialized()) {				// run parallel
			try {
				int taskNr = 0;
				for (int j=0; j<jMax; j++) {
					for (int i=0; i<iMax; i++) {
						if (taskNr %
								PxSystem.nrCPUs() == PxSystem.myCPU()) {
							task.doIt(i, j);
						}
					}
				}
				for (int j=0; j<jMax; j++) {
					PxSystem.reduceArrayToRootOFT(task.getBetas(j),
			                        	new CxRedOpAddDoubleArray());
					PxSystem.reduceArrayToRootOFT(task.getGammas(j),
				                        new CxRedOpAddDoubleArray());
				}
			} catch (Exception e) {
				//
			}
		} else {									// run sequential
			for (int j=0; j<jMax; j++) {
				for (int i=0; i<iMax; i++) {
					task.doIt(i, j);
				}
			}
		}
	}
}
