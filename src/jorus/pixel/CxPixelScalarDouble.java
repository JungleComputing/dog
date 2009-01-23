/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public class CxPixelScalarDouble extends CxPixel<double[]>
{
	public CxPixelScalarDouble(double[] array)
	{
		this(0, 0, 1, 1, 0, 0, array);
	}


	public CxPixelScalarDouble(int xidx, int yidx, int w, int h,
							   int bw, int bh, double[] array)
	{
		super(xidx, yidx, w, h, bw, bh, 1, array);
	}


	public double[] getValue()
	{
		return new double[]{data[index]};
	}
}
