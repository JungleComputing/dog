/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelScalarLong extends CxPixel<long[]>
{
	public CxPixelScalarLong(long[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelScalarLong(int xidx, int yidx,
							int w, int h, int bw, int bh, long[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 1, array);
	}


	public long[] getValue()
	{
		return new long[]{data[index]};
	}
}
