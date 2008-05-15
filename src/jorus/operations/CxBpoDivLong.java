/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxBpoDivLong extends CxBpo<long[]>
{
	public void doIt(long[] dst, long[] s2)
	{
		for (int j=0; j<h; j++) {
			for (int i=0; i<w; i++) {
				dst[off1+j*(w+stride1)+i] /= s2[off2+j*(w+stride2)+i];
			}
		}
	}
}
