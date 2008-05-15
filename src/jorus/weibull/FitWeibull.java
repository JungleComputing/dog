/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra		(fjseins@cs.vu.nl;    Java implementation)
 *  Jan-Mark Geusebroek	(mark@science.uva.nl; C++  implementation)
 *
 */


package jorus.weibull;


public class FitWeibull
{
	/*** Private Properties *******************************************/

	private	double[]	_hist;
	private	int			_bins;
	private	double		_dx;
	private	double		_norm;
	private	double		_beta;
	private	double		_gamma;
	private	double		_precision;


	/*** Public Methods ***********************************************/

	public double beta()
	{
		return _beta;
	}

	
	public double gamma()
	{
		return _gamma;
	}


	public void doFit(double[] hist, int bins, double range)
	{
		_dx	  = range / bins;
		_hist = hist;	
		_bins = bins;

		calcWeibullParams();
	}


	public void doFitMarginal(double[] hist, int bins,
							  double minrange, double maxrange)
	{
		// Find mu for folded histogram (2 bins together)

		int		i, j;
		double	x = minrange + 0.5 * _dx;
		double	_mu = 0.0;

		_dx = (maxrange - minrange) / bins;

		for (i=0; i<bins; i++) {
			_mu += hist[i] * x;
			x += _dx;
		}
		int bin0 = (int)((_mu - minrange) / _dx);


		// Fill new histogram with mirrored data

		if (bin0 > bins/2) {
			_bins = bin0;
		} else {
			_bins = bins-bin0;
		}

		_hist = new double[_bins];
		for (i=0; i<_bins; i++) {
			_hist[i] = 0;
		}

		for (i=0; i<bins; i++) {
			if (bin0 > i) {
				j = bin0-i-1;
			} else {
				j = i-bin0;
			}
			_hist[j] += hist[i];
		}
		
		calcWeibullParams();
	}

	
	/*** Private Methods **********************************************/

	private double betaest(double gamma, double xgm)
	{
		double	sum = 0.0;
		double	ddx = _dx / xgm;
		double	x = 0.5 * ddx;

		for (int i=0; i<_bins; i++) {
			if (_hist[i] > _precision) {
				sum += Math.pow(x, gamma) * _hist[i];
			}
			x += ddx;
		}
		return xgm * Math.pow(sum/_norm, 1./gamma);
	}


	private double gfunct(double gamma, double xgm)
	{
		double	sum = 0.0;
		double	beta = betaest(gamma, xgm);
		double	ddx = _dx / beta;
		double	x = 0.5 * ddx;

		for (int i=0; i<_bins; i++) {
			if (_hist[i] > _precision) {
				sum += Math.log(x * beta) *
						(Math.pow(x, gamma) - 1.0) * _hist[i];
			}
			x += ddx;
		}
		return sum/_norm-1./gamma;
	}


	private void calcWeibullParams()
	{
		double	sumy = 0.0;
		double	sumysq = 0.0;
		double	gammal = 0.0;
		double	gammah = 0.0;
		double	xgm, gfm, tol;
		double	x = _dx * 0.5;

		_precision = 0.0;
		_norm = 0.0;

		for (int i=0; i<_bins; i++) {
			if (_hist[i] > _precision) {
				double logy = Math.log(x);
				sumy += _hist[i]*logy;
				sumysq += _hist[i]*logy*logy;
				_norm += _hist[i];
			}
			x += _dx;
		}

		sumy /= _norm;
		sumysq /= _norm;
		_gamma = 1.28 / Math.sqrt((sumysq-sumy*sumy)*_bins/(_bins-1.0));
		xgm = Math.exp(sumy);
		tol = 2.0 * 0.001 * _gamma;
		gfm = gfunct(_gamma, xgm);

		int its = 0;
		if (gfm >= 0.0) {
			while (gfm > 0.0) {
				gammah = _gamma;
				_gamma /= 2.0;
				if (_gamma < tol)
					break;
				gfm = gfunct(_gamma, xgm);
				if (its++ > 30)
					break;
			}
			gammal = _gamma;
		}

		its = 0;
		while (gfm < 0.0) {
			gammal = _gamma;
			_gamma *= 2.0;
			gfm = gfunct(_gamma, xgm);
			if (its++ > 30)
				break;
		}
		gammah = _gamma;

		its = 0;
		while ((gammah-gammal) > tol) {
			_gamma = (gammal + gammah) / 2.0;
			gfm = gfunct(_gamma, xgm);
			if (gfm >= 0.0) {
				gammah = _gamma;
			} else {
				gammal = _gamma;
			}
			if (its++ > 30)
				break;
		}

		_gamma = (gammal + gammah) / 2.0;
		_beta = betaest(_gamma, xgm);
	}
}
