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


public abstract class CxArray2dShorts extends CxArray2d<short[]>
{
    /*** Public Methods ***********************************************/
    public CxArray2dShorts(CxArray2dShorts orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dShorts(CxArray2dShorts orig) {
        super(orig);
    }

    public CxArray2dShorts(int w, int h, int bw, int bh, int e, boolean create) {
        super(w, h, bw, bh, e, create);
    }

    public CxArray2dShorts(int w, int h, int bw, int bh, int e, short[] array, 
            boolean copy) {
        super(w, h, bw, bh, e, array, copy);
    }

    /*** Single Pixel (Value) Operations ******************************/

    public CxArray2d setSingleValue(CxPixel p,
            int xidx, int yidx, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatSvo.dispatch(this, xidx, yidx, inpl,
                new CxSvoSetShort((short[])p.getValue()));
    }


    /*** Unary Pixel Operations ***************************************/

    public CxArray2d setVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoSetValShort((short[])p.getValue()));
    }


    public CxArray2d mulVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoMulValShort((short[])p.getValue()));
    }


    /*** Binary Pixel Operations **************************************/

    public CxArray2d add(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoAddShort());
    }


    public CxArray2d sub(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoSubShort());
    }


    public CxArray2d mul(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoMulShort());
    }


    public CxArray2d div(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoDivShort());
    }
    
    protected short [] createDataArray(int size) { 
        return new short[size];
    }
    
    protected Class getDataType() { 
        return short.class;
    }
}
