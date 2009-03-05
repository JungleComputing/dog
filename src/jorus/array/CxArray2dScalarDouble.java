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

public class CxArray2dScalarDouble extends CxArray2dDoubles {
    /** * Public Methods ********************************************** */

       
    public CxArray2dScalarDouble(CxArray2dScalarDouble orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dScalarDouble(CxArray2dScalarDouble orig) {
        super(orig);
    }

    public CxArray2dScalarDouble(int w, int h, int bw, int bh, boolean create) {
        super(w, h, bw, bh, 1, create);
    }

    public CxArray2dScalarDouble(int w, int h, int bw, int bh, double[] array, 
            boolean copy) {
        super(w, h, bw, bh, 1, array, copy);
    }
    
    /** * Separated Gaussian Filter Operations ************************ */

    public CxArray2dScalarDouble gaussDerivative(double sigma, int orderX,
            int orderY, double truncation) {
        return gaussDerivative(sigma, sigma, orderX, orderY, truncation);
    }

    public CxArray2dScalarDouble gaussDerivative(double sx, double sy,
            int orderX, int orderY, double truncation) {
        
        CxArray2dScalarDouble gx = CxGaussian1d.create(sx, orderX,
                3 * sx * 2 + 1, width, 0);
        CxArray2dScalarDouble gy = CxGaussian1d.create(sy, orderY,
                3 * sy * 2 + 1, height, 0);
        
        return (CxArray2dScalarDouble) CxPatGenConv2dSep.dispatch(this, gx, gy,
                new CxGenConv2dSepGauss(), new CxSetBorderMirrorDouble());
    }

    /** * Histogram Operations **************************************** */

    public double[] impreciseHistogram(CxArray2dScalarDouble a, int nBins,
            double minVal, double maxVal) {
        if (!equalSignature(a))
            return null;
        return CxPatBpoToHist.dispatch(this, a, nBins, minVal, maxVal,
                new CxBpoToHistDouble());
    }

    public double[][] impreciseHistograms(CxArray2dScalarDouble[] a, int nBins,
            double minVal, double maxVal) {
        
        if (a == null || a.length == 0 || !equalSignature(a[0])) {
            return null;
        }
        
        return CxPatBpoArrayToHistArray.dispatch(this, a, nBins, minVal,
                maxVal, new CxBpoToHistDouble());
    }

    /** * Pixel Manipulation (NOT PARALLEL) *************************** */

    public CxPixelScalarDouble getPixel(int xidx, int yidx) {
        return new CxPixelScalarDouble(xidx, yidx, width, height, bwidth,
                bheight, data);
    }

    public void setPixel(CxPixel p, int xidx, int yidx) {
        double[] values = ((CxPixelScalarDouble) p).getValue();

        int off = ((width + 2 * bwidth) * bheight + bwidth) * extent;
        int stride = bwidth * extent * 2;
        int pos = off + yidx * (width * extent + stride) + xidx * extent;

        for (int i = 0; i < extent; i++) {
            data[pos + i] = values[i];
        }
        return;
    }
    
    @Override
    public CxArray2d clone() {
        return new CxArray2dScalarDouble(this);
    }

    @Override
    public CxArray2d clone(int newBorderWidth, int newBorderHeight) {
        return new CxArray2dScalarDouble(this, newBorderWidth, newBorderHeight);
    }
}
