/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxRedOpAddShort extends CxRedOp<Short>
{
	public Short doIt(Short val1, Short val2)
	{
		Integer iVal = new Integer(val1.intValue() + val2.intValue());
		return iVal.shortValue();
	}
}
