/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelVec3Short extends CxPixel<short[]>
{
	public CxPixelVec3Short(short[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelVec3Short(int xidx, int yidx,
							int w, int h, int bw, int bh, short[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 3, array);
	}


	public short[] getValue()
	{
		return new short[]{data[index], data[index+1], data[index+2]};
	}
}
