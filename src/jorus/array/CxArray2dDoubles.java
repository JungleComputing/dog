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


public abstract class CxArray2dDoubles extends CxArray2d<double[]>
{
    /*** Public Methods ***********************************************/

    public CxArray2dDoubles(CxArray2dDoubles orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }


    public CxArray2dDoubles(CxArray2dDoubles orig) {
        super(orig);
    }


    public CxArray2dDoubles(int w, int h, int bw, int bh, int e, boolean create) {
        super(w, h, bw, bh, e, create);
    }


    public CxArray2dDoubles(int w, int h, int bw, int bh, int e, double[] array, 
            boolean copy) {
        super(w, h, bw, bh, e, array, copy);
    }

    /*** Single Pixel (Value) Operations ******************************/

    public CxArray2d setSingleValue(CxPixel p,
            int xidx, int yidx, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatSvo.dispatch(this, xidx, yidx, inpl, 
                new CxSvoSetDouble((double[])p.getValue()));
    }


    /*** Unary Pixel Operations ***************************************/

    public CxArray2d setVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoSetValDouble((double[])p.getValue()));
    }


    public CxArray2d mulVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoMulValDouble((double[])p.getValue()));
    }


    /*** Binary Pixel Operations **************************************/

    public CxArray2d add(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoAddDouble());
    }


    public CxArray2d sub(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoSubDouble());
    }


    public CxArray2d mul(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoMulDouble());
    }


    public CxArray2d div(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoDivDouble());
    }
    
    protected double [] createDataArray(int size) { 
        return new double[size];
    }
    
    protected Class getDataType() { 
        return double.class;
    }
}
