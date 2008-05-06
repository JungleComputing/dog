/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxUpoMulValByte extends CxUpo<byte[]>
{
	protected byte[] value;


	public CxUpoMulValByte(byte[] p)
	{
		value = p;
	}


	public void doIt(byte[] dst)
	{
		for (int j=0; j<h; j++) {
			for (int i=0; i<w; i++) {
				dst[off+j*(w+stride)+i] *= value[i%value.length];
			}
		}
	}
}
