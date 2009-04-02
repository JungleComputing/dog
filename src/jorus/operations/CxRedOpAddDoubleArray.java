/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxRedOpAddDoubleArray extends CxRedOpArray<double[]>
{
	public void doIt(double[] target, double[] src)
	{
		for (int i=0; i<target.length; i++) {
			target[i] += src[i];
		}
	}
	
	@Override
	public void doItRange(double[] target, double[] src, int startIndex,int length) {
		for (int i=startIndex; i<startIndex+length; i++) {
			target[i] += src[i];
		}
	}

}
