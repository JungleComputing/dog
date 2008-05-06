/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelScalarInt extends CxPixel<int[]>
{
	public CxPixelScalarInt(int[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelScalarInt(int xidx, int yidx,
							int w, int h, int bw, int bh, int[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 1, array);
	}


	public int[] getValue()
	{
		return new int[]{data[index]};
	}
}
