/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxRedOpAddDouble extends CxRedOp<Double>
{
	public Double doIt(Double val1, Double val2)
	{
		return val1 + val2;
	}
}
