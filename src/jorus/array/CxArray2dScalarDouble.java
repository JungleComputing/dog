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


public class CxArray2dScalarDouble extends CxArray2dDoubles
{
	/*** Public Methods ***********************************************/

	public CxArray2dScalarDouble(int w, int h)
	{
		this(w, h, 0, 0, null);
	}


	public CxArray2dScalarDouble(int w, int h, double[] array)
	{
		this(w, h, 0, 0, array);
	}


	public CxArray2dScalarDouble(int w, int h,
										int bw, int bh, double[] array)
	{
		super(w, h, bw, bh, 1, array);
	}


	/*** Clone ********************************************************/

	public CxArray2dScalarDouble clone()
	{
		CxArray2dScalarDouble c =
					new CxArray2dScalarDouble(width+2*bwidth,
										height+2*bheight, data.clone());
		c.setDimensions(width, height, bwidth, bheight, extent);
		c.setGlobalState(gstate);
		if (pdata != null) {
			c.setPartialData(pwidth, pheight,
							 pdata.clone(), pstate, ptype);
		}
		return c;
	}


	public CxArray2dScalarDouble clone(int newBW, int newBH)
	{
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
		CxArray2dScalarDouble c = new CxArray2dScalarDouble(width,
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
		return c;
	}


    /*** Separated Gaussian Filter Operations *************************/

	public CxArray2dScalarDouble gaussDerivative(double sigma,
							int orderX, int orderY, double truncation)
	{
		return gaussDerivative(sigma, sigma,
							   orderX, orderY, truncation);
	}


	public CxArray2dScalarDouble gaussDerivative(double sx, double sy,
							int orderX, int orderY, double truncation)
	{
		CxArray2dScalarDouble gx =
					CxGaussian1d.create(sx, orderX, 3*sx*2+1, width, 0);
		CxArray2dScalarDouble gy =
					CxGaussian1d.create(sy, orderY, 3*sy*2+1, height,0);
		return (CxArray2dScalarDouble) CxPatGenConv2dSep.dispatch(this,
								gx, gy, new CxGenConv2dSepGauss(),
								new CxSetBorderMirrorDouble());
	}


    /*** Histogram Operations *****************************************/

	public double[] impreciseHistogram(CxArray2dScalarDouble a,
								int nBins, double minVal, double maxVal)
	{
		if (!equalSignature(a)) return null;
		return CxPatBpoToHist.dispatch(this, a, nBins, minVal, maxVal,
									   new CxBpoToHistDouble());
	}


	/*** Pixel Manipulation (NOT PARALLEL) ****************************/

	public CxPixelScalarDouble getPixel(int xidx, int yidx)
	{
		return new CxPixelScalarDouble(xidx, yidx, width, height,
												bwidth, bheight, data);
	}


	public void setPixel(CxPixel p, int xidx, int yidx)
	{
		double[] values = ((CxPixelScalarDouble)p).getValue();

		int off = ((width + 2*bwidth) * bheight + bwidth) * extent;
		int stride = bwidth * extent * 2;
		int pos = off + yidx*(width*extent+stride) + xidx*extent;
		
		for (int i=0; i<extent; i++) {
			data[pos+i] = values[i];
		}
		return;
	}
}
