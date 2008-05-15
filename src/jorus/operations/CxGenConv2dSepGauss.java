/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxGenConv2dSepGauss extends CxGenConv2dSep<double[]>
{
	// NOTE: As these methods are only used inside our library we can
	// be a little bit less safe. Here we assume 'dst' and 'src' to be
	// of identical signature, such that 'offset' and 'stride' apply
	// to both these structures.

	public void doIt(double[] dst, double[] src,
								   double[] ker, int direction)
	{
		if (direction == 0) {
			doIt_H(dst, src, ker);
		} else {
			doIt_V(dst, src, ker);
		}
	}


	private void doIt_H(double[] dst, double[] src, double[] ker)
	{
		int		kerBW  = kw_H / 2;
		int		srcPtr = 0;
		int		dstPtr = 0;
		double	tmpVal = 0.;

		for (int j=0; j<h; j++) {
			for (int i=0; i<w; i++) {
				dstPtr = off+j*(w+stride)+i;
				srcPtr = dstPtr - kerBW;
				dst[dstPtr] = 0.;		// neutral element of addition
				for (int k=0; k<kw_H; k++) {
					tmpVal = src[srcPtr+k] * ker[k];
					dst[dstPtr] += tmpVal;
				}
			}
		}
	}


	private void doIt_V(double[] dst, double[] src, double[] ker)
	{
		int		kerBW  = kw_V / 2;
		int		srcPtr = 0;
		int		dstPtr = 0;
		double	tmpVal = 0.;

		for (int i=0; i<w; i++) {
			for (int j=0; j<h; j++) {
				dstPtr = off+j*(w+stride)+i;
				srcPtr = dstPtr - kerBW*(w+stride);
				dst[dstPtr] = 0.;		// neutral element of addition
				for (int k=0; k<kw_V; k++) {
					tmpVal = src[srcPtr + k*(w+stride)] * ker[k];
					dst[dstPtr] += tmpVal;
				}
			}
		}
	}
}
