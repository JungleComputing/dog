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


public abstract class CxBpo<T>
{
	protected int	w       = 0;
	protected int	h       = 0;
	protected int	off1    = 0;
	protected int	off2    = 0;
	protected int	stride1 = 0;
	protected int	stride2 = 0;
	

	public void init(CxArray2d s1, CxArray2d s2, boolean parallel)
	{
		int w1  = parallel ? s1.getPartialWidth() : s1.getWidth();
		int e1  = s1.getExtent();
		int w2  = parallel ? s2.getPartialWidth() : s2.getWidth();
		int e2  = s2.getExtent();
		int bw1 = s1.getBorderWidth();
		int bw2 = s2.getBorderWidth();

		w       = w1 * e1;
		h       = parallel ? s1.getPartialHeight() : s1.getHeight();
		off1    = ((w1 + 2*bw1) * s1.getBorderHeight() + bw1) * e1;
		off2    = ((w2 + 2*bw2) * s2.getBorderHeight() + bw2) * e2;
		stride1 = bw1 * e1*2;
		stride2 = bw2 * e2*2;
	}


	public abstract void doIt(T dst, T src);
}
