/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelVec3Long extends CxPixel<long[]>
{
	public CxPixelVec3Long(long[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelVec3Long(int xidx, int yidx,
						   int w, int h, int bw, int bh, long[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 3, array);
	}


	public long[] getValue()
	{
		return new long[]{data[index], data[index+1], data[index+2]};
	}
}
