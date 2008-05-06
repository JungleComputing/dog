/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelScalarShort extends CxPixel<short[]>
{
	public CxPixelScalarShort(short[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelScalarShort(int xidx, int yidx, int w, int h,
							  int bw, int bh, short[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 1, array);
	}


	public short[] getValue()
	{
		return new short[]{data[index]};
	}
}
