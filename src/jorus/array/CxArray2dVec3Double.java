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


public class CxArray2dVec3Double extends CxArray2dDoubles
{
    /*** Public Methods ***********************************************/
    
    public CxArray2dVec3Double(CxArray2dVec3Double orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dVec3Double(CxArray2dVec3Double orig) {
        super(orig);
    }

    public CxArray2dVec3Double(int w, int h, int bw, int bh, boolean create) {
        super(w, h, bw, bh, 3, create);
    }

    public CxArray2dVec3Double(int w, int h, int bw, int bh, double[] array, 
            boolean copy) {
        super(w, h, bw, bh, 3, array, copy);
    }


    /*** Unary Pixel Operations ***************************************/

    public CxArray2dVec3Double convertRGB2OOO(boolean inpl)
    {
        return (CxArray2dVec3Double) CxPatUpo.dispatch(this, inpl,
                new CxUpoRGB2OOO());
    }


    /*** Binary Pixel Operations **************************************/

    public CxArray2dScalarDouble getPlane(int idx)
    {
        // Skip this new, since the constructor will do it for us -- J
        // double[] a = new double[(width+2*bwidth)*(height+2*bheight)];

        CxArray2dScalarDouble dst = new CxArray2dScalarDouble(width, height,
                    bwidth, bheight, false);

        dst.setGlobalState(CxArray2d.NONE);
        
        return (CxArray2dScalarDouble) CxPatBpo.dispatch(dst, this,
                true, new CxBpoGetPixEltDouble(idx));
    }


    /*** Pixel Manipulation (NOT PARALLEL) ****************************/

    public CxPixelVec3Double getPixel(int xidx, int yidx)
    {
        return new CxPixelVec3Double(xidx, yidx, width, height,
                bwidth, bheight, data);
    }


    public void setPixel(CxPixel p, int xidx, int yidx)
    {
        double[] values = ((CxPixelVec3Double)p).getValue();

        int off = ((width + 2*bwidth) * bheight + bwidth) * extent;
        int stride = bwidth * extent * 2;
        int pos = off + yidx*(width*extent+stride) + xidx*extent;

        for (int i=0; i<extent; i++) {
            data[pos+i] = values[i];
        }
        return;
    }
    
    @Override
    public CxArray2d clone() {
        return new CxArray2dVec3Double(this);
    }

    @Override
    public CxArray2d clone(int newBorderWidth, int newBorderHeight) {
        return new CxArray2dVec3Double(this, newBorderWidth, newBorderHeight);
    }
}
