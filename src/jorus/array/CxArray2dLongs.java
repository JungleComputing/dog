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


public abstract class CxArray2dLongs extends CxArray2d<long[]>
{
    /*** Public Methods ***********************************************/
    public CxArray2dLongs(CxArray2dLongs orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }

    public CxArray2dLongs(CxArray2dLongs orig) {
        super(orig);
    }

    public CxArray2dLongs(int w, int h, int bw, int bh, int e, boolean create) {
        super(w, h, bw, bh, e, create);
    }

    public CxArray2dLongs(int w, int h, int bw, int bh, int e, long[] array, 
            boolean copy) {
        super(w, h, bw, bh, e, array, copy);
    }


    /*** Single Pixel (Value) Operations ******************************/

    public CxArray2d setSingleValue(CxPixel p,
            int xidx, int yidx, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatSvo.dispatch(this, xidx, yidx, inpl,
                new CxSvoSetLong((long[])p.getValue()));
    }


    /*** Unary Pixel Operations ***************************************/

    public CxArray2d setVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoSetValLong((long[])p.getValue()));
    }


    public CxArray2d mulVal(CxPixel p, boolean inpl)
    {
        if (!equalExtent(p)) return null;
        return CxPatUpo.dispatch(this, inpl,
                new CxUpoMulValLong((long[])p.getValue()));
    }


    /*** Binary Pixel Operations **************************************/

    public CxArray2d add(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoAddLong());
    }


    public CxArray2d sub(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoSubLong());
    }


    public CxArray2d mul(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoMulLong());
    }


    public CxArray2d div(CxArray2d a, boolean inpl)
    {
        if (!equalSignature(a)) return null;
        return CxPatBpo.dispatch(this, a, inpl, new CxBpoDivLong());
    }
    
    protected long [] createDataArray(int size) { 
        return new long[size];
    }
    
    protected Class getDataType() { 
        return long.class;
    }
}
