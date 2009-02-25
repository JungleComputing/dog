/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxRedOpAddLongArray extends CxRedOpArray<long[]>
{
	public void doIt(long[] src1, long[] src2)
	{
		for (int i=0; i<src1.length; i++) {
			src1[i] += src2[i];
		}
	}
	
	@Override
	public void doItRange(long[] src1, long[] src2, int startIndex, int length) {
		for (int i=0; i<length; i++) {
			src1[startIndex+i] += src2[i];
		}
	}

}
