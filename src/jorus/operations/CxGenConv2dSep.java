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


public abstract class CxGenConv2dSep<T>
{
	protected int w      = 0;
	protected int h      = 0;
	protected int off    = 0;
	protected int stride = 0;
	protected int kw_H   = 0;	// kernel size for horizontal filtering
	protected int kw_V   = 0;	// kernel size for vertical filtering


	public void init(CxArray2d s1,
					 CxArray2d ker1, CxArray2d ker2, boolean parallel)
	{
		int w1  = parallel ? s1.getPartialWidth() : s1.getWidth();
		int e1  = s1.getExtent();
		int bw1 = s1.getBorderWidth();

		w      = w1 * e1;
		h      = parallel ? s1.getPartialHeight() : s1.getHeight();
		off    = ((w1 + 2*bw1) * s1.getBorderHeight() + bw1) * e1;
		stride = bw1 * e1*2;

		kw_H   = ker1.getWidth() * ker1.getExtent();
		kw_V   = ker2.getWidth() * ker2.getExtent();
	}


	public abstract void doIt(T dst, T src, T ker1, int direction);
}
