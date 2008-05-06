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


public abstract class CxArray2dBytes extends CxArray2d<byte[]>
{
	/*** Public Methods ***********************************************/


	public CxArray2dBytes(int w, int h,
						  int bw, int bh, int e, byte[] array)
	{
		// Initialize

		super(w, h, bw, bh, e, array);

		// Create new array and copy values, ignoring border values

		int fullw = w + 2*bw;
		int fullh = h + 2*bh;
		int start = (fullw*bh+bw)*e;

		byte[] newarray = new byte[fullw*fullh*e];

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


	/*** Single Pixel (Value) Operations ******************************/

	public CxArray2d setSingleValue(CxPixel p,
									int xidx, int yidx, boolean inpl)
	{
		if (!equalExtent(p)) return null;
		return CxPatSvo.dispatch(this, xidx, yidx, inpl,
							new CxSvoSetByte((byte[])p.getValue()));
	}


	/*** Unary Pixel Operations ***************************************/

	public CxArray2d setVal(CxPixel p, boolean inpl)
	{
		if (!equalExtent(p)) return null;
		return CxPatUpo.dispatch(this, inpl,
							new CxUpoSetValByte((byte[])p.getValue()));
	}


	public CxArray2d mulVal(CxPixel p, boolean inpl)
	{
		if (!equalExtent(p)) return null;
		return CxPatUpo.dispatch(this, inpl,
							new CxUpoMulValByte((byte[])p.getValue()));
	}


	/*** Binary Pixel Operations **************************************/

	public CxArray2d add(CxArray2d a, boolean inpl)
	{
		if (!equalSignature(a)) return null;
		return CxPatBpo.dispatch(this, a, inpl, new CxBpoAddByte());
	}


	public CxArray2d sub(CxArray2d a, boolean inpl)
	{
		if (!equalSignature(a)) return null;
		return CxPatBpo.dispatch(this, a, inpl, new CxBpoSubByte());
	}


	public CxArray2d mul(CxArray2d a, boolean inpl)
	{
		if (!equalSignature(a)) return null;
		return CxPatBpo.dispatch(this, a, inpl, new CxBpoMulByte());
	}


	public CxArray2d div(CxArray2d a, boolean inpl)
	{
		if (!equalSignature(a)) return null;
		return CxPatBpo.dispatch(this, a, inpl, new CxBpoDivByte());
	}
}
