/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.weibull;


public class CxWeibullFit
{
	// A bit of a hack; need to generalize this (much) more!


	FitWeibull		fitW;
	double[][][]	histoArray;
	double[][]		betaArray;
	double[][]		gammaArray;
	int				nrBins;


	public void init(FitWeibull fw, double[][][] histos, 
					 double[][] betas, double[][] gammas, int bins)
	{
		fitW	   = fw;
		histoArray = histos;
		betaArray  = betas;
		gammaArray = gammas;
		nrBins     = bins;
	}


	public void doIt(int i, int j)
	{
		if (j == 0) {
			fitW.doFit(histoArray[j][i], nrBins, 1.);
		} else {
			fitW.doFitMarginal(histoArray[j][i], nrBins, -1., 1.);
		}
		betaArray[j][i] = fitW.beta();
		gammaArray[j][i] = fitW.gamma();
	}


	public double[] getBetas(int idx)
	{
		return betaArray[idx];
	}


	public double[] getGammas(int idx)
	{
		return gammaArray[idx];
	}
}
