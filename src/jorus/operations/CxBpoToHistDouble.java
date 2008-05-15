/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.operations;


public class CxBpoToHistDouble extends CxBpoToHist<double[]>
{
	public void doIt(double[] dst, double[] s1, double[] s2,
					 int nBins, double minVal, double maxVal)
	{
		double	range = maxVal - minVal;
		int		s1Ptr = 0;
		int		s2Ptr = 0;

		for (int j=0; j<h; j++) {
			s1Ptr = off1 + j*(w+stride1);
			s2Ptr = off2 + j*(w+stride2);
			for (int i=0; i<w; i++) {
				int index = (int) (nBins*(s1[s1Ptr+i]-minVal)/range);
				if (index >= 0 && index < nBins) {
					dst[index] += s2[s2Ptr+i];
				}
			}
		}
	}
}
