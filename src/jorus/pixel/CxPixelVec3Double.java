/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelVec3Double extends CxPixel<double[]>
{
	public CxPixelVec3Double(double[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelVec3Double(int xidx, int yidx, int w, int h,
							 int bw, int bh, double[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 3, array);
	}


	public double[] getValue()
	{
		return new double[]{data[index], data[index+1], data[index+2]};
	}
}
