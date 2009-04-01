package ibis.dog.test;

import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.ImageUtils;
import ibis.dog.shared.RGB24Image;
import ibis.ipl.IbisCreationFailedException;

import java.io.File;
import java.io.IOException;

import jorus.parallel.PxSystem;
import jorus.weibull.CxWeibull;

public class TestServer {

    public static final int DEFAULT_TIMEOUT = 5000;

    private final boolean master;
    private final PxSystem px;
    private final RGB24Image image;
    private final int count;  
    private final int [] params = new int[3];
    
    private final FeatureVector vector;

    private TestServer(PxSystem px, RGB24Image image, int count)
        throws IbisCreationFailedException, IOException {

        this.px = px;
        this.image = image;
        this.count = count;
        this.master = (px.myCPU() == 0);
        this.vector = new FeatureVector(CxWeibull.getNrInvars(), 
                CxWeibull.getNrRfields());
    }

    private void processImage() {
        
        int operation = 0;
        
        RGB24Image img = image;

        long start = System.currentTimeMillis();
        
        if (master) {

            // The master should dequeue a request and broadcast
            // the details.

            params[0] = image.width;
            params[1] = image.height;
            params[2] = operation;
            
            try {
                px.broadcastArray(params);
            } catch (Exception e) {
                // TODO: REACT TO FAILURE PROPERLY
                e.printStackTrace(System.err);
            }

        } else {
            try {
                px.broadcastArray(params);
            } catch (Exception e) {
                // TODO: REACT TO FAILURE PROPERLY
                e.printStackTrace(System.err);
            }

            int width = params[0];
            int height = params[1];
            operation = params[2];
            img = new RGB24Image(width, height);
        }
        
        CxWeibull.doRecognize(img.width, img.height, img.pixels, vector.vector);
        
        long end = System.currentTimeMillis();
        
        if (master) {
       
            double tmp = 0.0;
            
            for (int i=0;i<vector.vector.length;i++) { 
                tmp += vector.vector[i];
            }
            
            System.out.println("Time = " + (end-start) + " CHECK: " + tmp);
            px.printStatistics();
        } 
        
    }

    private void run() {
    
        for (int i=0;i<count;i++) { 
            processImage();
            System.gc();
        }
    }

    public static void main(String[] args) {
        
        if (args.length != 4) { 
            System.err.println("Usage TestServer <poolname> <poolsize> <image.png> <count>");
            System.exit(1);     
        }
        
        try { 
            String poolName = args[0];
            String poolSize = args[1];

            RGB24Image image = ImageUtils.load(new File(args[2])).toRGB24();

            int count = Integer.parseInt(args[3]);

            System.out.println("Initializing Parallel System...");
        
            PxSystem.init(poolName, poolSize);
            
            PxSystem px = PxSystem.get();

            System.out.println("nrCPUs = " + px.nrCPUs());
            System.out.println("myCPU = " + px.myCPU());
            
            if (px.myCPU() != 0) { 
                image = null;
            }

            new TestServer(px, image, count).run();
        
            System.out.println("Exit Parallel System...");
        
            px.exitParallelSystem();
        } catch (Exception e) {
            e.printStackTrace();            
        }
    }
}
