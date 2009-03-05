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


public class CxArray2dScalarShort extends CxArray2dShorts
{
    /*** Public Methods ***********************************************/

    public CxArray2dScalarShort(CxArray2dScalarShort orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dScalarShort(CxArray2dScalarShort orig) {
        super(orig);
    }

    public CxArray2dScalarShort(int w, int h, int bw, int bh, boolean create) {
        super(w, h, bw, bh, 1, create);
    }


    public CxArray2dScalarShort(int w, int h, int bw, int bh, short[] array, 
            boolean copy) {
        super(w, h, bw, bh, 1, array, copy);
    }

    /*** Pixel Manipulation (NOT PARALLEL) ****************************/

    public CxPixelScalarShort getPixel(int xidx, int yidx)
    {
        return new CxPixelScalarShort(xidx, yidx, width, height,
                bwidth, bheight, data);
    }


    public void setPixel(CxPixel p, int xidx, int yidx)
    {
        short[] values = ((CxPixelScalarShort)p).getValue();

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
        return new CxArray2dScalarShort(this);
    }

    @Override
    public CxArray2d clone(int newBorderWidth, int newBorderHeight) {
        return new CxArray2dScalarShort(this, newBorderWidth, newBorderHeight);
    }
}
