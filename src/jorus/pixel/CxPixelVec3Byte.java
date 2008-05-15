/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelVec3Byte extends CxPixel<byte[]>
{
	public CxPixelVec3Byte(byte[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelVec3Byte(int xidx, int yidx,
						   int w, int h, int bw, int bh, byte[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 3, array);
	}


	public byte[] getValue()
	{
		return new byte[]{data[index], data[index+1], data[index+2]};
	}
}
