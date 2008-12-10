/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


import jorus.parallel.PxSystem;


public class CxSetBorderMirrorDouble extends CxSetBorder<double[]>
{
	public void doIt(double[] dst, int numX, int numY)
	{
		if (doParallel) {
			doItParallel(dst, numX, numY);
		} else {
			doItSequential(dst, numX, numY);
		}
	}


	private void doItSequential(double[] dst, int numX, int numY)
	{
		int ptr1 = 0;
		int ptr2 = 0;

		if (numY > 0) {

			// Mirror top part

			for (int j=0; j<numY; j++) {
				ptr1 = off - (1+j) * (w+stride);
				ptr2 = off + j * (w+stride);
				for (int i=0; i<w; i++) {
					dst[ptr1+i] = dst[ptr2+i];
				}
			}


			// Mirror bottom part

			for (int j=0; j<numY; j++) {
				ptr1 = off + (h+j) * (w+stride);
				ptr2 = off + (h-1-j) * (w+stride);
				for (int i=0; i<w; i++) {
					dst[ptr1+i] = dst[ptr2+i];
				}
			}
		}

		if (numX > 0) {

			// Mirror left part including upper and lower "corners"

			int totalH = h + 2*numY;

			for (int j=0; j<totalH; j++) {
				ptr1 = off + (-numY+j) * (w+stride) - 1;
				ptr2 = ptr1 + 1;
				for (int i=0; i<numX; i++) {
					dst[ptr1-i] = dst[ptr2+i];
				}
			}


			// Mirror right part including upper and lower "corners"

			for (int j=0; j<totalH; j++) {
				ptr1 = off + (-numY+j) * (w+stride) + w;
				ptr2 = ptr1 - 1;
				for (int i=0; i<numX; i++) {
					dst[ptr1+i] = dst[ptr2-i];
				}
			}
		}
	}


	private void doItParallel(double[] dst, int numX, int numY)
	{
		int ptr1 = 0;
		int ptr2 = 0;

		if (numX > 0) {

			// Mirror left part NOT including upper and lower "corners"

			for (int j=0; j<h; j++) {
				ptr1 = off + j*(w+stride) - 1;
				ptr2 = ptr1 + 1;
				for (int i=0; i<numX; i++) {
					dst[ptr1-i] = dst[ptr2+i];
				}
			}


			// Mirror right part NOT including upper and lower "corners"

			for (int j=0; j<h; j++) {
				ptr1 = off + j * (w+stride) + w;
				ptr2 = ptr1 - 1;
				for (int i=0; i<numX; i++) {
					dst[ptr1+i] = dst[ptr2-i];
				}
			}
		}

		if (numY > 0) {

			try {
				PxSystem.borderExchange(dst, w, h, off, stride, numY);
			} catch (Exception e) {
				//
			}

			// Mirror top part including left and right "corners"

			if (PxSystem.myCPU() == 0) {
				for (int j=0; j<numY; j++) {
					ptr1 = off - stride/2 - (1+j) * (w+stride);
					ptr2 = off - stride/2 + j * (w+stride);
					for (int i=0; i<w+stride; i++) {
						dst[ptr1+i] = dst[ptr2+i];
					}
				}
			}


			// Mirror bottom part including left and right "corners"

			if (PxSystem.myCPU() == PxSystem.nrCPUs()-1) {
				for (int j=0; j<numY; j++) {
					ptr1 = off - stride/2 + (h+j) * (w+stride);
					ptr2 = off - stride/2 + (h-1-j) * (w+stride);
					for (int i=0; i<w+stride; i++) {
						dst[ptr1+i] = dst[ptr2+i];
					}
				}
			}
		}
	}
}
