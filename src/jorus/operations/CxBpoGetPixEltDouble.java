/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxBpoGetPixEltDouble extends CxBpo<double[]>
{
	protected int index;


	public CxBpoGetPixEltDouble(int idx)
	{
		index = idx;
		if (index <  0) {
			index = 1;
		}
		index %= 3;
	}


	public void doIt(double[] dst, double[] src)
	{
		int e2 = src.length / dst.length;
		for (int j=0; j<h; j++) {
			for (int i=0; i<w; i++) {
				dst[off1+j*(w+stride1)+i] =
							src[off2+j*(w*e2+stride2) + i*e2 + index];
			}
		}
	}
}
