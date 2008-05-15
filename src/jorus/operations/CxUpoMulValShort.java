/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxUpoMulValShort extends CxUpo<short[]>
{
	protected short[] value;


	public CxUpoMulValShort(short[] p)
	{
		value = p;
	}


	public void doIt(short[] dst)
	{
		for (int j=0; j<h; j++) {
			for (int i=0; i<w; i++) {
				dst[off+j*(w+stride)+i] *= value[i%value.length];
			}
		}
	}
}
