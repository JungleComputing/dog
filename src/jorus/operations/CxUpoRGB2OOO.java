/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxUpoRGB2OOO extends CxUpo<double[]>
{
	public void doIt(double[] a)
	{
		// NOTE: here we assume array 'a' to be in RGB color space

		int		id;
		double	x, y, z;

		for (int j=0; j<h; j++) {
			for (int i=0; i<w; i+=3) {
				id = off+j*(w+stride)+i;
				x  = a[id];
				y  = a[id+1];
				z  = a[id+2];
				a[id]   = x*0.000233846 + y*0.00261968 + z* 0.00127135;
				a[id+1] = x*0.000726333 + y*0.000718106+ z*-0.00121377;
				a[id+2] = x*0.000846833 + y*-0.00173932+ z* 0.000221515;
			}
		}
	}
}
