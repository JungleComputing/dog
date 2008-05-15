/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


import jorus.array.CxArray2d;


// Single Value Operation

public abstract class CxSvo<T>
{
	protected int		w      = 0;
	protected int		e      = 0;
	protected int		off    = 0;
	protected int		stride = 0;


	public void init(CxArray2d s1, boolean parallel)
	{
		int w1  = parallel ? s1.getPartialWidth() : s1.getWidth();
		int bw1 = s1.getBorderWidth();

		e      = s1.getExtent();
		w      = w1 * e;
		off    = ((w1 + 2*bw1) * s1.getBorderHeight() + bw1) * e;
		stride = bw1 * e*2;
	}


	public abstract void doIt(T dst, int x, int y);
}
