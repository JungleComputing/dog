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

    public CxArray2dBytes(CxArray2dBytes orig, int newBW, int newBH) {
        super(orig, newBW, newBH);
    }


    public CxArray2dBytes(CxArray2dBytes orig) {
        super(orig);
    }


    public CxArray2dBytes(int w, int h, int bw, int bh, int e, boolean create) {
        super(w, h, bw, bh, e, create);
    }


    public CxArray2dBytes(int w, int h, int bw, int bh, int e, byte[] array, 
            boolean copy) {
        super(w, h, bw, bh, e, array, copy);
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
    
    protected byte [] createDataArray(int size) { 
        return new byte[size];
    }
    
    protected Class getDataType() { 
        return byte.class;
    }
}
