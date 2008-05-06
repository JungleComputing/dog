/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxSvoSetByte extends CxSvo<byte[]>
{
	protected byte[] value;


	public CxSvoSetByte(byte[] p)
	{
		value = p;
	}


	public void doIt(byte[] dst, int x, int y)
	{
		for (int i=0; i<e; i++) {
			dst[off+y*(w+stride)+x*e+i] = value[i];
		}
	}
}
