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


public class CxArray2dScalarByte extends CxArray2dBytes {
    
    /*** Public Methods ***********************************************/

    public CxArray2dScalarByte(CxArray2dScalarByte orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dScalarByte(CxArray2dScalarByte orig) {
        super(orig);
    }

    public CxArray2dScalarByte(int w, int h, int bw, int bh, boolean create) {
        super(w, h, bw, bh, 1, create);
    }

    public CxArray2dScalarByte(int w, int h, int bw, int bh, byte[] array, 
            boolean copy) {
        super(w, h, bw, bh, 1, array, copy);
    }

    /*** Pixel Manipulation (NOT PARALLEL) ****************************/

    public CxPixelScalarByte getPixel(int xidx, int yidx)
    {
        return new CxPixelScalarByte(xidx, yidx, width, height,
                bwidth, bheight, data);
    }


    public void setPixel(CxPixel p, int xidx, int yidx)
    {
        byte[] values = ((CxPixelScalarByte)p).getValue();

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
        return new CxArray2dScalarByte(this);
    }

    @Override
    public CxArray2d clone(int newBorderWidth, int newBorderHeight) {
        return new CxArray2dScalarByte(this, newBorderWidth, newBorderHeight);
    }
}
