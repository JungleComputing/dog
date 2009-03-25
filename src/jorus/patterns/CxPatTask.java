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

    private static final CxRedOpAddDoubleArray add = new CxRedOpAddDoubleArray();
    
    public static void dispatch(CxWeibullFit task, int iMax, int jMax)
    {
        if (PxSystem.initialized()) {				// run parallel
            
            final PxSystem px = PxSystem.get();
            final int rank = px.myCPU();
            final int size = px.nrCPUs();
            
            
            try {
                int taskNr = 0;
                for (int j=0; j<jMax; j++) {
                    for (int i=0; i<iMax; i++) {
                        if (taskNr % size == rank) {
                            task.doIt(i, j);
                        }

                        // Added -- Jason
                        taskNr++;						
                    }
                }
                for (int j=0; j<jMax; j++) {
                    px.reduceArrayToRoot(task.getBetas(j), add);
                    px.reduceArrayToRoot(task.getGammas(j), add);
                }
            } catch (Exception e) {
                System.err.println("Failed to perform operation!");
                e.printStackTrace(System.err);
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
