/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.array;


import jorus.pixel.*;


public class CxArray2dVec3Short extends CxArray2dShorts
{
    /*** Public Methods ***********************************************/
    public CxArray2dVec3Short(CxArray2dShorts orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dVec3Short(CxArray2dShorts orig) {
        super(orig);
    }

    public CxArray2dVec3Short(int w, int h, int bw, int bh, boolean create) {
        super(w, h, bw, bh, 3, create);
    }

    public CxArray2dVec3Short(int w, int h, int bw, int bh, short[] array, 
            boolean copy) {
        super(w, h, bw, bh, 3, array, copy);
    }

    /*** Pixel Manipulation (NOT PARALLEL) ****************************/

    public CxPixelVec3Short getPixel(int xidx, int yidx)
    {
        return new CxPixelVec3Short(xidx, yidx, width, height,
                bwidth, bheight, data);
    }


    public void setPixel(CxPixel p, int xidx, int yidx)
    {
        short[] values = ((CxPixelVec3Short)p).getValue();

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
        return new CxArray2dVec3Short(this);
    }

    @Override
    public CxArray2d clone(int newBorderWidth, int newBorderHeight) {
        return new CxArray2dVec3Short(this, newBorderWidth, newBorderHeight);
    }
}
