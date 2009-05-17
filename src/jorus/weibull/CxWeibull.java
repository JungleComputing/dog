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


import java.util.Arrays;

import jorus.array.CxArray2dScalarDouble;
import jorus.array.CxArray2dVec3Byte;
import jorus.array.CxArray2dVec3Double;
import jorus.patterns.CxPatSet;
import jorus.patterns.CxPatTask;
import jorus.pixel.CxPixelScalarDouble;
import jorus.pixel.CxPixelVec3Double;


public class CxWeibull
{

    //private static double	M_PI = 3.14159265358979323846;
    private static int		NR_INVAR_IMS = 13;
    private static int		NR_INVARS	 = 6;
    private static int		NR_RFIELDS	 = 37;
    private static int		NR_BINS		 = 1000;

    private static int		EX2EY2	= 0;
    private static int		WX		= 1;
    private static int		WR1		= 2;
    private static int		WY		= 3;
    private static int		WR2		= 4;
    private static int		CLX		= 5;
    private static int		CLR1	= 6;
    private static int		CLY		= 7;
    private static int		CLR2	= 8;
    private static int		CLLX	= 9;
    private static int		CLLR1	= 10;
    private static int		CLLY	= 11; 
    private static int		CLLR2	= 12;

    private	static CxArray2dScalarDouble	Wx, W45, Wy, W135;
    private	static CxArray2dScalarDouble	Clx, Cl45, Cly, Cl135;
    private	static CxArray2dScalarDouble	Cllx, Cll45, Clly, Cll135;
    private	static CxArray2dScalarDouble	Ex2Ey2 = null;
    private	static CxArray2dScalarDouble[]	rfIm = new
    CxArray2dScalarDouble[NR_RFIELDS];

    private static double[][][] histos =
        new double[NR_INVAR_IMS][NR_RFIELDS][NR_BINS];
    private static double[][] betas =
        new double[NR_INVAR_IMS][NR_RFIELDS];
    private static double[][] gammas =
        new double[NR_INVAR_IMS][NR_RFIELDS];
    private static double[][] resbetas =
        new double[NR_INVAR_IMS][NR_RFIELDS];
    private static double[][] resgammas =
        new double[NR_INVAR_IMS][NR_RFIELDS];

    private static boolean initialized = false;



    /*** Public Methods ***********************************************/

    public static int getNrInvars()
    {
        return NR_INVARS;
    }


    public static int getNrRfields()
    {
        return NR_RFIELDS;
    }



    /*** Private Methods***********************************************/

    public static void initialize(int inImW, int inImH)
    {
  //      MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

        // Create central Gaussian at high resolution

     //   MemoryUsage mu = mbean.getHeapMemoryUsage();

    //    System.out.println("Memory used: " + mu.getUsed());

 //       long start = System.currentTimeMillis();

        double	sigma = 30.0 * (double)inImH / 576;
        int     centerx = inImW/2;
        int     centery = inImH/2;

        // NOTE: we now create an empty distibuted structure here! -- J
        CxArray2dScalarDouble pntIm =
            new CxArray2dScalarDouble(inImW, inImH, 0, 0, false);
//            new CxArray2dScalarDouble(inImW, inImH, 0, 0, true);

        CxPixelScalarDouble zero =
            new CxPixelScalarDouble(new double[]{0.});

        CxPixelScalarDouble one =
            new CxPixelScalarDouble(new double[]{1.});

        // We now fill the distibuted structure with zeros -- J
        pntIm.setVal(zero, true);

        // .. and put a 1 in the middle -- J
        pntIm.setSingleValue(one, centerx, centery, true);

        // Calculate the blur (in a new distributed structure). -- J
        rfIm[0] = pntIm.gaussDerivative(sigma, 0, 0, 5.);

        // Reset the original data structure -- J
        pntIm.setSingleValue(zero, centerx, centery, true);

    //    mu = mbean.getHeapMemoryUsage();
   //     System.out.println("Memory used: " + mu.getUsed());

    //    long init = System.currentTimeMillis();

        // Create remaining Gaussians in circular fashion around center

        int idx = 1;
        final int order = 3;		// nr. of circles
        final int dens = 1;		// density
        final int radialdens = 6;	// radial density
        double r = dens*sigma;

        for (int n=1; n<=order; n++) {
            r += dens*sigma;
            for (int phi=0; phi<n*radialdens; phi++) {
                
                final double xp = r*Math.cos(2*Math.PI*phi/(n*radialdens));
                final double yp = -r*Math.sin(2*Math.PI*phi/(n*radialdens));
                
                final int x = (int)(centerx + xp);
                final int y = (int)(centery + yp);
                
                if (x >= sigma && y >= sigma &&
                        (x < inImW - sigma) && (y < inImH - sigma)) {
                    
                    pntIm.setSingleValue(one, x, y, true);
                    rfIm[idx] = pntIm.gaussDerivative(sigma, 0, 0, 5.);
                    pntIm.setSingleValue(zero, x, y, true);
                    idx++;
                }

     //           mbean.gc();
                
     //           mu = mbean.getHeapMemoryUsage();
     //           System.out.println("Memory used after " + n + ", " + phi + ": " + mu.getUsed());
            }

     //       mbean.gc();
            
     //       mu = mbean.getHeapMemoryUsage();
     //       System.out.println("Memory used after " + n + ": " + mu.getUsed());

            r += dens*sigma;
        }

    //    long end = System.currentTimeMillis();

    //    mbean.gc();
        
   /*     mu = mbean.getHeapMemoryUsage();
        System.out.println("Memory used (END): " + mu.getUsed());

        System.out.println("Initialization took " + (end-start));
        System.out.println("     hi-res         " + (init-start));
        System.out.println("     circles        " + (end-init));
*/
        
        initialized = true;
    }


    private static void buildInvariantImages(CxArray2dVec3Double input)
    {
        double s = 1.0;										// sigma


        // Convert to opponent color space (and stretch)

        // Note: this operation will also scatter the input image to all 
        // participating machines -- J.
        input.convertRGB2OOO(true);

        input.mulVal(new
                CxPixelVec3Double(new double[]{255., 255., 255.}), true);

        CxArray2dScalarDouble plane = input.getPlane(0);

        CxArray2dScalarDouble E  = plane.gaussDerivative(s, 0, 0, 3.);
        CxArray2dScalarDouble Ex = plane.gaussDerivative(s, 1, 0, 3.);
        CxArray2dScalarDouble Ey = plane.gaussDerivative(s, 0, 1, 3.);

        plane = input.getPlane(1);

        CxArray2dScalarDouble El  = plane.gaussDerivative(s, 0, 0, 3.);
        CxArray2dScalarDouble Elx = plane.gaussDerivative(s, 1, 0, 3.);
        CxArray2dScalarDouble Ely = plane.gaussDerivative(s, 0, 1, 3.);

        plane = input.getPlane(2);

        CxArray2dScalarDouble Ell  = plane.gaussDerivative(s, 0, 0, 3.);
        CxArray2dScalarDouble Ellx = plane.gaussDerivative(s, 1, 0, 3.);
        CxArray2dScalarDouble Elly = plane.gaussDerivative(s, 0, 1, 3.);


        // Intensity contrast

        Wx   = (CxArray2dScalarDouble) Ex.div(E, false);
        Wy   = (CxArray2dScalarDouble) Ey.div(E, false);
        W45  = (CxArray2dScalarDouble) Wx.add(Wy, false);
        W135 = (CxArray2dScalarDouble) Wx.sub(Wy, false);


        // Chromatic C invarient

        CxArray2dScalarDouble E2;

        E2  = (CxArray2dScalarDouble) E.mul(E, false);
        Clx = (CxArray2dScalarDouble) El.mul(Ex, false);
        Elx.mul(E, true);
        Clx = (CxArray2dScalarDouble) Elx.sub(Clx, false);
        Clx.div(E2, true);

        Cly = (CxArray2dScalarDouble) El.mul(Ey, false);
        Ely.mul(E, true);
        Cly = (CxArray2dScalarDouble) Ely.sub(Cly, false);
        Cly.div(E2, true);

        Cllx = (CxArray2dScalarDouble) Ell.mul(Ex, false);
        Ellx.mul(E, true);
        Cllx = (CxArray2dScalarDouble) Ellx.sub(Cllx, false);
        Cllx.div(E2, true);

        Clly = (CxArray2dScalarDouble) Ell.mul(Ey, false);
        Elly.mul(E, true);
        Clly = (CxArray2dScalarDouble) Elly.sub(Clly, false);
        Clly.div(E2, true);

        Cl45   = (CxArray2dScalarDouble) Clx.add(Cly, false);
        Cl135  = (CxArray2dScalarDouble) Clx.sub(Cly, false);
        Cll45  = (CxArray2dScalarDouble) Cllx.add(Clly, false);
        Cll135 = (CxArray2dScalarDouble) Cllx.sub(Clly, false);


        // Squared gradient

        Ex.mul(Ex, true);
        Ey.mul(Ey, true);
        Ex2Ey2 = (CxArray2dScalarDouble) Ey.add(Ex, false);
    }


    public static void doRecognize(int width, int height,
            byte[] bArray, double[] vector)
    {

        if (!initialized) {
            initialize(width, height);
        }

        // Create CxArray2d from file image data
        CxArray2dVec3Byte data = new CxArray2dVec3Byte(width, height, 0, 0, bArray, false);
        CxArray2dVec3Double input = new CxArray2dVec3Double(width, height, 0, 0, false);
        CxPatSet.dispatch(input, data);
        
    //    CxArray2dVec3Double input = new CxArray2dVec3Double(width,
     //           height, 0, 0, CxConvert.toDoubles(bArray), false);

 //       long createInput = System.currentTimeMillis();

        // Create all invariant images

//      System.out.println("Building invariant images...");
        buildInvariantImages(input);
//      System.out.println("Done.");

  //      long buildInvar = System.currentTimeMillis();

        // Initialize histos, betas, and gammas

//      System.out.println("Initializing histos, betas, and gammas...");

        /*
		 for (int j=0; j<NR_INVAR_IMS; j++) {
			for (int i=0; i<NR_RFIELDS; i++) {
				betas[j][i] = 0.;
				gammas[j][i] = 0.;
				for (int k=0; k<NR_BINS; k++) {
					histos[j][i][k] = 0.;
				}
			}
		}
         */

        for (int j=0; j<NR_INVAR_IMS; j++) {
            Arrays.fill(betas[j], 0.);
            Arrays.fill(gammas[j], 0.);

            // No need to initialize, they are overwritten below! -- J
            for (int i=0; i<NR_RFIELDS; i++) {
                Arrays.fill(histos[j][i], 0.);
            }
        }

 //      long initHistos = System.currentTimeMillis();

//      System.out.println("Done.");


        // Calculate all histograms

//      System.out.println("Calculating all histograms...");

        /*
  		for (int i=0; i<NR_RFIELDS; i++) {

			histos[EX2EY2][i] =
					Ex2Ey2.impreciseHistogram(rfIm[i], NR_BINS, 0., 1.);

			histos[WX][i] =
					Wx.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[WR1][i] =
					W45.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[WY][i] =
					Wy.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[WR2][i] =
					W135.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);

			histos[CLX][i] =
					Clx.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLR1][i] =
					Cl45.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLY][i] =
					Cly.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLR2][i] =
					Cl135.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);

			histos[CLLX][i] =
					Cllx.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLLR1][i] =
					Cll45.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLLY][i] =
					Clly.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLLR2][i] =
					Cll135.impreciseHistogram(rfIm[i], NR_BINS, -1.,1.);
		}
  
*/
        
        histos[EX2EY2] = Ex2Ey2.impreciseHistograms(rfIm, NR_BINS, 0., 1.);
        histos[WX]     = Wx.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[WR1]    = W45.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[WY]     = Wy.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[WR2]    = W135.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLX]    = Clx.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLR1]   = Cl45.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLY]    = Cly.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLR2]   = Cl135.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLLX]   = Cllx.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLLR1]  = Cll45.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLLY]   = Clly.impreciseHistograms(rfIm, NR_BINS, -1., 1.);
        histos[CLLR2]  = Cll135.impreciseHistograms(rfIm, NR_BINS, -1.,1.);

        
      /*  
        for (int i=0;i<NR_INVAR_IMS;i++) {
            
            
            for (int j=0;j<NR_RFIELDS;j++) {
                double sum = 0.0;
            
                for (int b=0;b<NR_BINS;b++) {
                    sum += histos[i][j][b];
                }

                System.out.println("sum hist [" + i + "][" + j + "] = " + sum);
            } 
        }
        */

//      System.out.println("Done.");

  //      long createHistos = System.currentTimeMillis();


        // Calculate all Weibull fits

//      System.out.println("Calculating all Weibull fits...");
        CxWeibullFit fit = new CxWeibullFit();
        fit.init(new FitWeibull(), histos, betas, gammas, NR_BINS);
        CxPatTask.dispatch(fit, NR_RFIELDS, NR_INVAR_IMS);
//      System.out.println("Done.");
        
        // Sanity check 
        /*
        for (int i=0;i<NR_INVAR_IMS;i++) {
            System.out.print(i + ": [");
            for (int j=0;j<NR_RFIELDS;j++) {
                System.out.printf(" %.6f", betas[i][j]);                
            }
            
            System.out.println(" ]");
        }*/
        
    //    System.exit(1);

        // Extract all final betas and gammas (ignore EX2EY2)

        for (int j=1; j<NR_INVAR_IMS; j+=4) {
            for (int i=0; i<NR_RFIELDS; i++) {
                double xxb = 0.; 
                double xyb = 0.; 
                double yyb = 0.;
                double xxg = 0.; 
                double xyg = 0.; 
                double yyg = 0.;

                xxb += betas[j][i];  
                yyb += betas[j+2][i];
                xxg += gammas[j][i]; 
                yyg += gammas[j+2][i];

                xxb += 0.5*betas[j+1][i]*Math.sqrt(2.0);
                xyb += 0.5*betas[j+1][i]*Math.sqrt(2.0);
                yyb += 0.5*betas[j+1][i]*Math.sqrt(2.0);

                xxg += 0.5*gammas[j+1][i]*Math.sqrt(2.0);
                xyg += 0.5*gammas[j+1][i]*Math.sqrt(2.0);
                yyg += 0.5*gammas[j+1][i]*Math.sqrt(2.0);

                xyb -= 0.5*betas[j+3][i]*Math.sqrt(2.0);
                xxb += 0.5*betas[j+3][i]*Math.sqrt(2.0);
                yyb += 0.5*betas[j+3][i]*Math.sqrt(2.0);

                xyg -= 0.5*gammas[j+3][i]*Math.sqrt(2.0);
                xxg += 0.5*gammas[j+3][i]*Math.sqrt(2.0);
                yyg += 0.5*gammas[j+3][i]*Math.sqrt(2.0);

                /* Unused ? -- J
                double angleb, angleg;

                if (xxb==yyb) {
                    angleb = 0.0;
                } else {
                    angleb = 0.5*Math.atan2(2.0*xyb, xxb-yyb);
                }

                if (xxg==yyg) {
                    angleg = 0.0;
                } else {
                    angleg = 0.5*Math.atan2(2.0*xyg, xxg-yyg);
                }
                 */

                double H  = 0.5*(xxb+yyb);
                double D  = H*H-xxb*yyb+xyb*xyb;
                double Ia = H+Math.sqrt(D);
                double Ib = H-Math.sqrt(D);
                resbetas[((j-1)/2)][i] = 2.0*Ia/4.;
                resbetas[((j-1)/2)+1][i] = 2.0*Ib/4.;

                H  = 0.5*(xxg+yyg);
                D  = H*H-xxg*yyg+xyg*xyg;
                Ia = H+Math.sqrt(D);
                Ib = H-Math.sqrt(D);
                resgammas[((j-1)/2)][i] = 2.0*Ia/4.;
                resgammas[((j-1)/2)+1][i] = 2.0*Ib/4.;
            }
        }


        // Make invalid all parameters that are not finite or too large

        boolean[] invalids = new boolean[NR_RFIELDS];

        // Replaced by Arrays.fill - J.
        //
        //for (int i=0; i<NR_RFIELDS; i++) {
        //    invalids[i] = false;
        //}

        int countIvalids = 0;

        Arrays.fill(invalids, false);

        for (int j=0; j<NR_INVARS; j++) {
            for (int i=0; i<NR_RFIELDS; i++) {
                invalids[i] = (Double.isNaN(resbetas[j][i]) ||
                        Double.isInfinite(resbetas[j][i]) ||
                        resbetas[j][i] > 100. ||
                        Double.isNaN(resgammas[j][i]) ||
                        Double.isInfinite(resgammas[j][i]) ||
                        resgammas[j][i] > 100.);

                if (invalids[i]) { 
                    countIvalids++;
                }

            }
        }


        // Finalize the set of Weibull parameters and... done!

        for (int i=0; i<NR_RFIELDS; i++) {
            for (int j=0; j<NR_INVARS; j++) {
                if (invalids[i]) {
                    resgammas[j][i] = 100.;
                    resbetas[j][i] = 100.;
                }
                vector[2*(i*NR_INVARS+j)]   = resgammas[j][i];
                vector[2*(i*NR_INVARS+j)+1] = resbetas[j][i];
//              if (PxSystem.myCPU() == 0) {
//              System.out.println(resgammas[j][i]);
//              System.out.println(resbetas[j][i]);
//              }
            }
        }

 //       long doWeibuls = System.currentTimeMillis();
/*
        System.out.println("     Total weibul time " + (doWeibuls-start));
        System.out.println("            init       " + (init-start));
        System.out.println("            input      " + (createInput-init));
        System.out.println("            invar      " + (buildInvar-createInput));
        System.out.println("            inithistos " + (initHistos-buildInvar));
        System.out.println("            histos     " + (createHistos-initHistos));
        System.out.println("            weibul     " + (doWeibuls-createHistos));
        System.out.println("              invalids " + countIvalids);
*/
    }


    /*** Main Method **************************************************/

    /*
	public static void main(String[] args)
	{
		// Initializing Parallel system

//		System.out.println("Initializing Parallel System...");
		try {
			PxSystem.initParallelSystem();
		} catch (Exception e) {
			System.out.println("Could not initialize Parallel System");
			System.exit(0);
		}


		// Reading image data from file

//		System.out.println("Getting Weibulls from " + INFILE);

		JxImage jimg = new JxImage();
		jimg.readFile(INFILE);

		byte[]	bArray = jimg.getData();
		int		width  = jimg.getWidth();
		int		height = jimg.getHeight();

		if (bArray == null) {
			System.out.println("Reading image failed");
			System.exit(0);
		}

		if (!initialized) {
//			System.out.println("Creating Gaussian images...");
			initialize(width, height);
//			System.out.println("Done.");
		}


		// Create CxArray2d from file image data

		CxArray2dVec3Double input = new CxArray2dVec3Double(width,
								height, CxConvert.toDoubles(bArray));


		// Create all invariant images

//		System.out.println("Building invariant images...");
		buildInvariantImages(input);
//		System.out.println("Done.");


		// Initialize histos, betas, and gammas

//		System.out.println("Initializing histos, betas, and gammas...");
		for (int j=0; j<NR_INVAR_IMS; j++) {
			for (int i=0; i<NR_RFIELDS; i++) {
				betas[j][i] = 0.;
				gammas[j][i] = 0.;
				for (int k=0; k<NR_BINS; k++) {
					histos[j][i][k] = 0.;
				}
			}
		}
//		System.out.println("Done.");


		// Calculate all histograms

//		System.out.println("Calculating all histograms...");
		for (int i=0; i<NR_RFIELDS; i++) {

			histos[EX2EY2][i] =
					Ex2Ey2.impreciseHistogram(rfIm[i], NR_BINS, 0., 1.);

			histos[WX][i] =
					Wx.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[WR1][i] =
					W45.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[WY][i] =
					Wy.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[WR2][i] =
					W135.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);

			histos[CLX][i] =
					Clx.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLR1][i] =
					Cl45.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLY][i] =
					Cly.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLR2][i] =
					Cl135.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);

			histos[CLLX][i] =
					Cllx.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLLR1][i] =
					Cll45.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLLY][i] =
					Clly.impreciseHistogram(rfIm[i], NR_BINS, -1., 1.);
			histos[CLLR2][i] =
					Cll135.impreciseHistogram(rfIm[i], NR_BINS, -1.,1.);
		}
//		System.out.println("Done.");


		// Calculate all Weibull fits

//		System.out.println("Calculating all Weibull fits...");
		CxWeibullFit fit = new CxWeibullFit();
		fit.init(new FitWeibull(), histos, betas, gammas, NR_BINS);
		CxPatTask.dispatch(fit, NR_RFIELDS, NR_INVAR_IMS);
//		System.out.println("Done.");


		// Extract all final betas and gammas (ignore EX2EY2)

		double	xxb, xyb, yyb;
		double	xxg, xyg, yyg;

		for (int j=1; j<NR_INVAR_IMS; j+=4) {
			for (int i=0; i<NR_RFIELDS; i++) {
				xxb = 0.; xyb = 0.; yyb = 0.;
				xxg = 0.; xyg = 0.; yyg = 0.;

				xxb += betas[j][i];  yyb += betas[j+2][i];
				xxg += gammas[j][i]; yyg += gammas[j+2][i];

				xxb += 0.5*betas[j+1][i]*Math.sqrt(2.0);
				xyb += 0.5*betas[j+1][i]*Math.sqrt(2.0);
				yyb += 0.5*betas[j+1][i]*Math.sqrt(2.0);

				xxg += 0.5*gammas[j+1][i]*Math.sqrt(2.0);
				xyg += 0.5*gammas[j+1][i]*Math.sqrt(2.0);
				yyg += 0.5*gammas[j+1][i]*Math.sqrt(2.0);

				xyb -= 0.5*betas[j+3][i]*Math.sqrt(2.0);
				xxb += 0.5*betas[j+3][i]*Math.sqrt(2.0);
				yyb += 0.5*betas[j+3][i]*Math.sqrt(2.0);

				xyg -= 0.5*gammas[j+3][i]*Math.sqrt(2.0);
				xxg += 0.5*gammas[j+3][i]*Math.sqrt(2.0);
				yyg += 0.5*gammas[j+3][i]*Math.sqrt(2.0);

				double angleb, angleg;

				if (xxb==yyb) {
					angleb = 0.0;
				} else {
					angleb = 0.5*Math.atan2(2.0*xyb, xxb-yyb);
				}

				if (xxg==yyg) {
					angleg = 0.0;
				} else {
					angleg = 0.5*Math.atan2(2.0*xyg, xxg-yyg);
				}

				double H  = 0.5*(xxb+yyb);
				double D  = H*H-xxb*yyb+xyb*xyb;
				double Ia = H+Math.sqrt(D);
				double Ib = H-Math.sqrt(D);
				resbetas[((j-1)/2)][i] = 2.0*Ia/4.;
				resbetas[((j-1)/2)+1][i] = 2.0*Ib/4.;

				H  = 0.5*(xxg+yyg);
				D  = H*H-xxg*yyg+xyg*xyg;
				Ia = H+Math.sqrt(D);
				Ib = H-Math.sqrt(D);
				resgammas[((j-1)/2)][i] = 2.0*Ia/4.;
				resgammas[((j-1)/2)+1][i] = 2.0*Ib/4.;
			}
		}


		// Make invalid all parameters that are not finite or too large

		boolean[] invalids = new boolean[NR_RFIELDS];
		for (int i=0; i<NR_RFIELDS; i++) {
			invalids[i] = false;
		}
		for (int j=0; j<NR_INVARS; j++) {
			for (int i=0; i<NR_RFIELDS; i++) {
				if (Double.isNaN(resbetas[j][i]) ||
					Double.isInfinite(resbetas[j][i]) ||
					resbetas[j][i] > 100. ||
					Double.isNaN(resgammas[j][i]) ||
					Double.isInfinite(resgammas[j][i]) ||
					resgammas[j][i] > 100.) {
						invalids[i] = true;
				}
			}
		}


		// Finalize the set of Weibull parameters and... done!

		for (int i=0; i<NR_RFIELDS; i++) {
			for (int j=0; j<NR_INVARS; j++) {
				if (invalids[i]) {
					resgammas[j][i] = 100.;
					resbetas[j][i] = 100.;
				}
//if (PxSystem.myCPU() == 0) {
//System.out.println(resgammas[j][i]);
//System.out.println(resbetas[j][i]);
//}
			}
		}

		try {
			PxSystem.exitParallelSystem();
		} catch (Exception e) {
			// Nothing we can do now...
		}
		System.exit(0);
	}
     */
}
