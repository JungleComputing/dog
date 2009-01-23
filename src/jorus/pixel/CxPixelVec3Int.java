/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelVec3Int extends CxPixel<int[]>
{
	public CxPixelVec3Int(int[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelVec3Int(int xidx, int yidx,
						  int w, int h, int bw, int bh, int[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 3, array);
	}


	public int[] getValue()
	{
		return new int[]{data[index], data[index+1], data[index+2]};
	}
}
