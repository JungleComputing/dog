/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.array;


import jorus.pixel.CxPixel;
import java.lang.reflect.Constructor;


public abstract class CxArray2d<T>
{
	/*** Private Properties *******************************************/

	protected int	width	= 0;	// array width
	protected int	height	= 0;	// array height
	protected int	bwidth	= 0;	// array border width
	protected int	bheight	= 0;	// array border height
	protected int	extent	= 0;	// pixel extent
	protected Class	type	= null;	// element type
	protected T		data    = null;	// array of elements (not pixels)

	protected int	pwidth  = 0;	// partial array width
	protected int	pheight = 0;	// partial array height
	protected T		pdata   = null;	// partial array of elements

	protected int	gstate	= NONE;
	protected int	pstate	= NONE;
	protected int	ptype	= NONE;


	/*** Distribution States ******************************************/

	public static final int	NONE	= 0;		// empty state
	public static final int	CREATED	= 1;		// global created
	public static final int	VALID	= 2;		// global/local valid
	public static final int	INVALID	= 3;		// global/local invalid
	public static final int	PARTIAL	= 4;		// scattered structure
	public static final int	FULL	= 5;		// replicated structure
	public static final int	NOT_RED	= 6;		// not reduced


	/*** Public Methods ***********************************************/

	public CxArray2d(int w, int h, int bw, int bh, int e, T array)
	{
		// NOTE: here we assume array to be of length (w+2*bw)*(h+2*bh)
		// Subclasses should make sure that this is indeed the case!!!!

		setDimensions(w, h, bw, bh, e);
		data = array;
		if (data != null) {
			type = data.getClass().getComponentType();
gstate = VALID;
		}
	}


	protected void setDimensions(int w, int h, int bw, int bh, int e)
	{
		width   = w;
		height  = h;
		bwidth  = bw;
		bheight = bh;
		extent  = e;
	}


	public int getWidth()
	{
		return width;
	}


	public int getHeight()
	{
		return height;
	}


	public int getBorderWidth()
	{
		return bwidth;
	}


	public int getBorderHeight()
	{
		return bheight;
	}


	public int getExtent()
	{
		return extent;
	}


	public Class getElementType()
	{
		return type;
	}


	public T getData()
	{
		return data;
	}


	public int getPartialWidth()
	{
		return pwidth;
	}


	public int getPartialHeight()
	{
		return pheight;
	}


	public T getPartialData()
	{
		return pdata;
	}


	public void setPartialData(int width, int height,
							   T data, int state, int type)
	{
		// This assumes data size (pwidth+2*bwidth)*(pheight+2*bheight)

		pwidth  = width;
		pheight = height;
		pdata   = data;
		pstate  = state;
		ptype   = type;
	}


	public boolean equalExtent(CxPixel p)
	{
		return (extent == p.getExtent());
	}


	public boolean equalSignature(CxArray2d a)
	{
		// NOTE: The array borders do not need to be equal in size!!!

		return (width == a.getWidth() && height == a.getHeight() &&
				extent == a.getExtent() && type == a.getElementType());
	}


	public int getGlobalState()
	{
		return gstate;
	}


	public void setGlobalState(int state)
	{
		gstate = state;
	}


	public int getLocalState()
	{
		return pstate;
	}


	public void setLocalState(int state)
	{
		pstate = state;
	}


	public int getDistType()
	{
		return ptype;
	}


	public void setDistType(int type)
	{
		ptype = type;
	}


	/*** Clone ********************************************************/

	public abstract CxArray2d clone();
	public abstract CxArray2d clone(int newBW, int newBH);


	/*** Creator from byte[] ******************************************/

//	NOTE: THIS SOLUTION REQUIRES ALL SUBCLASSES TO HAVE A CONSTRUCTOR
//	      WITH A byte[] AS ONE OF ITS PARAMETERS...

	public static <U> U makeFromData(int w, int h,
						int bw, int bh, byte[] array, Class<U> clazz)
	{
		try {
			Class[] args = new Class[5];
			args[0] = int.class;
			args[1] = int.class;
			args[2] = int.class;
			args[3] = int.class;
			args[4] = byte[].class;
			Constructor<U> c = clazz.getConstructor(args);
			return c.newInstance(w, h, bw, bh, array);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}


	/*** Single Pixel (Value) Operations ******************************/

	public abstract CxArray2d setSingleValue(CxPixel p, int xidx,
											 int yidx, boolean inpl);


	/*** Unary Pixel Operations ***************************************/

	public abstract CxArray2d setVal(CxPixel p, boolean inpl);
	public abstract CxArray2d mulVal(CxPixel p, boolean inpl);


	/*** Binary Pixel Operations **************************************/

	public abstract CxArray2d add(CxArray2d a, boolean inpl);
	public abstract CxArray2d sub(CxArray2d a, boolean inpl);
	public abstract CxArray2d mul(CxArray2d a, boolean inpl);
	public abstract CxArray2d div(CxArray2d a, boolean inpl);


	/*** Pixel Manipulation (NOT PARALLEL) ****************************/

	public abstract CxPixel<T> getPixel(int xindex, int yindex);
	public abstract void setPixel(CxPixel p, int xindex, int yindex);
}
