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


public class CxArray2dVec3Int extends CxArray2dInts
{
    /*** Public Methods ***********************************************/
    public CxArray2dVec3Int(CxArray2dVec3Int orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dVec3Int(CxArray2dVec3Int orig) {
        super(orig);
    }

    public CxArray2dVec3Int(int w, int h, int bw, int bh, boolean create) {
        super(w, h, bw, bh, 3, create);
    }

    public CxArray2dVec3Int(int w, int h, int bw, int bh, int[] array, boolean copy) {
        super(w, h, bw, bh, 3, array, copy);
    }

    /*** Pixel Manipulation (NOT PARALLEL) ****************************/

    public CxPixelVec3Int getPixel(int xidx, int yidx)
    {
        return new CxPixelVec3Int(xidx, yidx, width, height,
                bwidth, bheight, data);
    }


    public void setPixel(CxPixel p, int xidx, int yidx)
    {
        int[] values = ((CxPixelVec3Int)p).getValue();

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
        return new CxArray2dVec3Int(this);
    }

    @Override
    public CxArray2d clone(int newBorderWidth, int newBorderHeight) {
        return new CxArray2dVec3Int(this, newBorderWidth, newBorderHeight);
    }
    
}
