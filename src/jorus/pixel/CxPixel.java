/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.pixel;


public abstract class CxPixel<T>
{
	/*** Private Properties *******************************************/

	protected int	index;
	protected int	extent;
	protected T		data;


	/*** Public Methods ***********************************************/


	public CxPixel(int xidx, int yidx,
				   int w, int h, int bw, int bh, int ext, T array)
	{
		index  = ((w+2*bw)*(bh+yidx) + bw+xidx) * ext;
		extent = ext;
		data   = array;
	}


	public int getExtent()
	{
		return extent;
	}


	public abstract T getValue();
}
