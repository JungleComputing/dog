package ibis.dog.test;

import ibis.dog.FeatureVector;
import ibis.dog.server.Server;
import ibis.imaging4j.Format;
import ibis.imaging4j.Image;
import ibis.imaging4j.conversion.Conversion;
import ibis.imaging4j.conversion.Convertor;
import ibis.imaging4j.scaling.Scaler;
import ibis.imaging4j.scaling.Scaling;
import ibis.ipl.IbisCreationFailedException;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jorus.parallel.PxSystem;
import jorus.weibull.CxWeibull;

public class TestServer {

    public static final int IMAGE_WIDTH = 1024;
    public static final int IMAGE_HEIGHT = 768;

    private static final Logger logger = LoggerFactory
            .getLogger(TestServer.class);

    public static final int DEFAULT_TIMEOUT = 5000;

    private final boolean master;
    private final PxSystem px;
    private final Image image;
    private final int count;

    private final FeatureVector vector;

    private TestServer(PxSystem px, Image image, int count)
            throws IbisCreationFailedException, IOException {

        this.px = px;
        this.image = image;
        this.count = count;
        this.master = (px.myCPU() == 0);
        this.vector = new FeatureVector(CxWeibull.getNrInvars(), CxWeibull
                .getNrRfields());
    }

    private void processImage() {
        long start = System.currentTimeMillis();

        int requiredBytes = (int) Format.RGB24.bytesRequired(IMAGE_WIDTH,
                IMAGE_HEIGHT);

        // allocate empty image
        byte[] pixels = new byte[requiredBytes];

        if (master) {

            // FIXME: this could be more efficient, we convert twice now!
            try {
                Convertor toargb32 = Conversion.getConvertor(image.getFormat(),
                        Format.ARGB32);

                Image argb32Image = toargb32.convert(image, null);

                Scaler scaler = Scaling.getScaler(Format.ARGB32);

                Image scaledArgb32Image = scaler.scale(argb32Image,
                        IMAGE_WIDTH, IMAGE_HEIGHT);

                Convertor torgb24 = Conversion.getConvertor(Format.ARGB32,
                        Format.RGB24);

                // create image from existing (empty) pixel byte array
                Image rgb24Image = new Image(Format.RGB24, IMAGE_WIDTH,
                        IMAGE_HEIGHT, pixels);

                // fill pixel array
                torgb24.convert(scaledArgb32Image, rgb24Image);

            } catch (Exception e) {
                logger.error("Could not convert image", e);
                return;
            }

        }

        CxWeibull.doRecognize(IMAGE_WIDTH, IMAGE_HEIGHT, pixels, vector.vector);

        long end = System.currentTimeMillis();

        if (master) {

            double tmp = 0.0;

            for (int i = 0; i < vector.vector.length; i++) {
                tmp += vector.vector[i];
            }

            System.out.println("Time = " + (end - start) + " CHECK: " + tmp);
            px.printStatistics();
        }

    }

    private void run() {

        for (int i = 0; i < count; i++) {
            processImage();
            System.gc();
        }
    }

    public static void main(String[] args) {

        if (args.length != 4) {
            System.err
                    .println("Usage TestServer <poolname> <poolsize> <image.png> <count>");
            System.exit(1);
        }

        try {
            String poolName = args[0];
            String poolSize = args[1];

            //FIXME: actually load a picture here!
            Image image = new Image(Format.RGB24, Server.IMAGE_WIDTH, Server.IMAGE_HEIGHT);

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
