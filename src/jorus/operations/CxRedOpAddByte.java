/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;

import java.lang.Integer;


public class CxRedOpAddByte extends CxRedOp<Byte>
{
	public Byte doIt(Byte val1, Byte val2)
	{
		Integer iVal = new Integer(val1.intValue() + val2.intValue());
		return iVal.byteValue();
	}
}
