/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxRedOpAddInt extends CxRedOp<Integer>
{
	public Integer doIt(Integer val1, Integer val2)
	{
		return val1 + val2;
	}
}
