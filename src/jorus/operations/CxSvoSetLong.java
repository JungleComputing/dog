/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxSvoSetLong extends CxSvo<long[]>
{
	protected long[] value;


	public CxSvoSetLong(long[] p)
	{
		value = p;
	}


	public void doIt(long[] dst, int x, int y)
	{
		for (int i=0; i<e; i++) {
			dst[off+y*(w+stride)+x*e+i] = value[i];
		}
	}
}
