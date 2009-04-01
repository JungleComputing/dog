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


    private void doIt_H(double [] dst, double [] src, double [] ker) {
        final int kerBW  = kw_H / 2;
     
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                final int dstPtr = off+j*(w+stride)+i;
                final int srcPtr = dstPtr - kerBW;
                dst[dstPtr] = 0.;		// neutral element of addition
     
                for (int k=0; k<kw_H; k++) {
                    dst[dstPtr] += src[srcPtr+k] * ker[k];
                }
            }
        }
    }


    private void doIt_V(double[] dst, double[] src, double[] ker)
    {
        final int kerBW  = kw_V / 2;
       
        for (int i=0; i<w; i++) {
            for (int j=0; j<h; j++) {
                final int dstPtr = off+j*(w+stride)+i;
                final int srcPtr = dstPtr - kerBW*(w+stride);
                dst[dstPtr] = 0.; // neutral element of addition
                
                for (int k=0; k<kw_V; k++) {
                    dst[dstPtr] += src[srcPtr + k*(w+stride)] * ker[k];
                }
            }
        }
    }
}
