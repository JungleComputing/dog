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


public class CxArray2dVec3Long extends CxArray2dLongs
{
    /*** Public Methods ***********************************************/
    public CxArray2dVec3Long(CxArray2dVec3Long orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dVec3Long(CxArray2dVec3Long orig) {
        super(orig);
    }

    public CxArray2dVec3Long(int w, int h, int bw, int bh, boolean create) {
        super(w, h, bw, bh, 3, create);
    }

    public CxArray2dVec3Long(int w, int h, int bw, int bh, long[] array, 
            boolean copy) {
        super(w, h, bw, bh, 3, array, copy);
    }

    /*** Pixel Manipulation (NOT PARALLEL) ****************************/

    public CxPixelVec3Long getPixel(int xidx, int yidx)
    {
        return new CxPixelVec3Long(xidx, yidx, width, height,
                bwidth, bheight, data);
    }


    public void setPixel(CxPixel p, int xidx, int yidx)
    {
        long[] values = ((CxPixelVec3Long)p).getValue();

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
        return new CxArray2dVec3Long(this);
    }

    @Override
    public CxArray2d clone(int newBorderWidth, int newBorderHeight) {
        return new CxArray2dVec3Long(this, newBorderWidth, newBorderHeight);
    }
}
