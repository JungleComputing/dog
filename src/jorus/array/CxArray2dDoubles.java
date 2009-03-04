/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.array;


import jorus.operations.*;
import jorus.patterns.*;
import jorus.pixel.*;


public abstract class CxArray2dDoubles extends CxArray2d<double[]>
{
    /*** Public Methods ***********************************************/

/* Replaced by version below (keep this version until it is sure to be correct)-- J
 
    public CxArray2dDoubles(int w, int h,
            int bw, int bh, int e, double[] array)
    {
        // Initialize

        super(w, h, bw, bh, e, array);

        // Create new array and copy values, ignoring border values

        int fullw = w + 2*bw;
        int fullh = h + 2*bh;
        int start = (fullw*bh+bw)*e;

        double[] newarray = new double[fullw*fullh*e];

        if (data != null) {
            for (int j=0; j<h; j++) {
                for (int i=0; i<w*e; i++) {
                    newarray[start + j*fullw*e + i] = data[j*w*e+i];
                }
            }
        }
        data = newarray;
        type = data.getClass().getComponentType();
        gstate = VALID;
    }
*/
    
    public CxArray2dDoubles(int w, int h, int bw, int bh, int e, 
            double[] array) {
       
        // Initialize
        super(w, h, bw, bh, e, null);

        // Create new array and copy values, ignoring border values
        final int fullw = w + 2*bw;
        final int fullh = h + 2*bh;
        final int start = (fullw*bh+bw)*e;

        final double [] newarray = new double[fullw*fullh*e];

        if (array != null) {
            for (int j=0; j<h; j++) {
                System.arraycopy(array, j*w*e, newarray, start+j*fullw*e, w*e);
            }
        }
        
        data = newarray;
        type = data.getClass().getComponentType();
        gstate = VALID;
    }
    
    // Copy constructor which copies an existing array, but changes the 
    // dimension of the borders -- J
    public CxArray2dDoubles(CxArray2dDoubles orig, int newBW, int newBH) {

        super(orig.width, orig.height, newBW, newBH, orig.extent, null);

        final int fullw = width + 2*bwidth;
        final int fullh = height + 2*bheight;
        
        data = new double[fullw * fullh * extent];

        final int off = ((orig.width + 2 * orig.bwidth) * orig.bheight + orig.bwidth) * extent;
        final int stride = orig.bwidth * extent * 2;
        
        for (int j = 0; j < orig.height; j++) {
            
            final int srcPtr = off + j * (orig.width * extent + stride);
            final int dstPtr = j * (width * extent);
            
            System.arraycopy(data, srcPtr, orig.data, dstPtr, width * extent);
        }
        
        setGlobalState(orig.gstate);

        if (orig.pdata != null) {
            final double [] newpdata = 
                new double[(orig.pwidth + 2*newBW) * (orig.pheight + 2*newBH) * extent];

            final int srcOff = ((orig.pwidth + 2 * orig.bwidth) * orig.bheight + orig.bwidth) * extent;
            final int dstOff = ((orig.pwidth + 2 * newBW) * newBH + newBW) * extent;

            for (int j = 0; j < orig.pheight; j++) {
                final int srcPtr = srcOff + j * (orig.pwidth + 2 * orig.bwidth) * extent;
                final int dstPtr = dstOff + j * (orig.pwidth + 2 * newBW) * extent;
                
                System.arraycopy(orig.pdata, srcPtr, newpdata, dstPtr, orig.pwidth * extent);
            }
              
            setPartialData(orig.pwidth, orig.pheight, newpdata, orig.pstate, orig.ptype);
        }

        type = data.getClass().getComponentType();
        gstate = VALID;
    }
    
    /*** Single Pixel (Value) Operations ******************************/

    public CxArray2d setSingleValue(CxPixel p,
            int xidx, int yidx, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatSvo.dispatch(this, xidx, yidx, inpl, 
                new CxSvoSetDouble((double[])p.getValue()));
    }


    /*** Unary Pixel Operations ***************************************/

    public CxArray2d setVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoSetValDouble((double[])p.getValue()));
    }


    public CxArray2d mulVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoMulValDouble((double[])p.getValue()));
    }


    /*** Binary Pixel Operations **************************************/

    public CxArray2d add(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoAddDouble());
    }


    public CxArray2d sub(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoSubDouble());
    }


    public CxArray2d mul(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoMulDouble());
    }


    public CxArray2d div(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoDivDouble());
    }
}
