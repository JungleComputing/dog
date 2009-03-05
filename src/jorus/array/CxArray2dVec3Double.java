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

    public CxArray2dVec3Double(int w, int h, boolean create)
    {
        super(w, h, 0, 0, 3, create);
    }


    public CxArray2dVec3Double(int w, int h, double[] array, boolean copy)
    {
        this(w, h, 0, 0, array, copy);
    }


    public CxArray2dVec3Double(int w, int h,
            int bw, int bh, double[] array, boolean copy)
    {
        super(w, h, bw, bh, 3, array, copy);
    }

    public CxArray2dVec3Double(CxArray2dVec3Double old, int newBW, int newBH) { 
        super(old, newBW, newBH);
    }
    
    /*** Clone ********************************************************/

    public CxArray2dVec3Double clone()
    {
    	/* Replaced with version below -- J
        CxArray2dVec3Double c = new CxArray2dVec3Double(width+2*bwidth,
                height+2*bheight, data.clone());
     
        c.setDimensions(width, height, bwidth, bheight, extent);

        */
    
    	CxArray2dVec3Double c = new CxArray2dVec3Double(width, height, bwidth, 
    			bheight, data, true);
    	
        c.setGlobalState(gstate);

        if (pdata != null) {
            c.setPartialData(pwidth, pheight,
                    pdata.clone(), pstate, ptype);
        }
        
        return c;
    }


    public CxArray2dVec3Double clone(int newBW, int newBH)
    {
        return new CxArray2dVec3Double(this, newBW, newBH);
        

    	/* Replace by copy constructor -- J
    	
    	double[] newdata = new double[width*height*extent];

        int off    = ((width+2*bwidth) * bheight + bwidth) * extent;
        int stride = bwidth * extent * 2;
        int srcPtr = 0;
        int dstPtr = 0;

        for (int j=0; j<height; j++) {
            srcPtr = off + j*(width*extent+stride);
            dstPtr = j*(width*extent);
            for (int i=0; i<width*extent; i++) {
                newdata[dstPtr + i] = data[srcPtr + i];
            }
        }
        CxArray2dVec3Double c = new CxArray2dVec3Double(width,
                height, newBW, newBH, newdata);
        c.setGlobalState(gstate);
        if (pdata != null) {
            double[] newpdata = new
            double[(pwidth+2*newBW)*(pheight+2*newBH)*extent];

            int srcOff = ((pwidth+2*bwidth)*bheight+bwidth)*extent;
            int dstOff = ((pwidth+2*newBW)*newBH+newBW)*extent;

            for (int j=0; j<pheight; j++) {
                srcPtr = srcOff + j*(pwidth+2*bwidth)*extent;
                dstPtr = dstOff + j*(pwidth+2*newBW)*extent;
                for (int i=0; i<pwidth*extent; i++) {
                    newpdata[dstPtr + i] = pdata[srcPtr + i];
                }
            }
            c.setPartialData(pwidth, pheight, newpdata, pstate, ptype);
        }
        return c;*/
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
}
