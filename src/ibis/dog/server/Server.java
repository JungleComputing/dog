package ibis.dog.server;

import ibis.dog.Communication;
import ibis.dog.FeatureVector;
import ibis.media.imaging.Format;
import ibis.media.imaging.Image;
import ibis.media.imaging.Imaging;
import ibis.ipl.Ibis;
import ibis.ipl.util.rpc.RPC;
import ibis.ipl.util.rpc.RemoteObject;

import ibis.ipl.util.rpc.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jorus.parallel.PxSystem;
import jorus.weibull.CxWeibull;

public class Server implements ServerInterface {

    // public static final int IMAGE_WIDTH = 352;
    // public static final int IMAGE_HEIGHT = 768;

    // public static final int IMAGE_WIDTH = 1024;
    // public static final int IMAGE_HEIGHT = 768;

    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 480;

    public static final Format IMAGE_FORMAT = Format.RGB24;

    public static final int DEFAULT_TIMEOUT = 5000;

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final PxSystem px;

    private final boolean master;

    // master-only fields

    private final Ibis ibis;

    private final RemoteObject<ServerInterface> remoteObject;

    private boolean ended = false;
    private boolean initialized = false;

    private Server(String poolName, String poolSize) throws Exception {
        logger.info("Initializing PxSystem at " + IMAGE_WIDTH + "x"
                + IMAGE_HEIGHT);
        try {
            px = PxSystem.init(poolName, poolSize);
        } catch (Exception e) {
            logger.error("Could not initialize Parallel system", e);
            throw e;
        }

        logger.debug("nrCPUs = " + px.nrCPUs());
        logger.debug("myCPU = " + px.myCPU());

        master = (px.myCPU() == 0);

        // Node 0 needs to provide an Ibis to contact the outside world.
        if (master) {
            logger.info("Local PxSystem initalized. Initializing Global Communication");
            ibis = Communication.createIbis(Communication.SERVER_ROLE, null);
            remoteObject = RPC.exportObject(ServerInterface.class, this,
                    "server", ibis);

            logger.info("Initializing Weibull at " + IMAGE_WIDTH + "x"
                    + IMAGE_HEIGHT);
        } else {
            ibis = null;
            remoteObject = null;
        }

        // do initialization now instead of after the first request is received.
        CxWeibull.initialize(IMAGE_WIDTH, IMAGE_HEIGHT);

        logger.info("Rank " + px.myCPU() + " of " + px.nrCPUs()
                + " Initialization done");

        setInitialized();

    }

    private synchronized void setInitialized() {
        initialized = true;
        notifyAll();
    }

    @Override
    public synchronized void waitUntilInitialized() throws RemoteException {
        while (!initialized && !ended) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

    private synchronized void setEnded() {
        ended = true;
    }

    private synchronized boolean hasEnded() {
        return ended;
    }

    private synchronized void nudge() {
        notifyAll();
    }

    void end() {
        if (hasEnded()) {
            return;
        }
        logger.info("Ending server");
        setEnded();

        if (master) {
            try {
                remoteObject.unexport();
                ibis.end();
            } catch (Exception e) {
                logger.error("Error on stopping communication", e);
            }
        }
        try {
            px.exitParallelSystem();
        } catch (Exception e) {
            logger.error("error on exiting parallel system", e);
        }
    }

    public FeatureVector calculateVector(Image image) throws RemoteException,
            Exception {
        FeatureVector result;

        long converting = System.currentTimeMillis();
        long scaling = 0;
        long computing = 0;
        long end = 0;

        byte[] pixels;

        try {
            if (master) {
                // The master should dequeue a request, and prepare it for
                // processing

                logger.debug("Got request");

                if (image == null) {
                    logger.debug("fake request received, sending fake reply");
                    return null;
                }

                Image convertedImage;
                if (image.getFormat() == IMAGE_FORMAT) {
                    // no need to convert
                    convertedImage = image;
                } else {
                    convertedImage = Imaging.convert(image, IMAGE_FORMAT);
                }

                scaling = System.currentTimeMillis();

                Image scaledImage;
                if (convertedImage.getWidth() == IMAGE_WIDTH
                        && convertedImage.getHeight() == IMAGE_HEIGHT) {
                    // no need to scale
                    scaledImage = convertedImage;
                } else {
                    scaledImage = Imaging.scale(convertedImage, IMAGE_WIDTH,
                            IMAGE_HEIGHT);
                }

                // Imaging4j.save(scaledImage, new File("image.rgb"));

                pixels = scaledImage.getData().array();

                logger.debug("Starting computation");
                computing = System.currentTimeMillis();
            } else {
                // allocate space for image
                pixels = new byte[(int) Format.RGB24.bytesRequired(IMAGE_WIDTH,
                        IMAGE_HEIGHT)];
            }

            result = new FeatureVector(CxWeibull.getNrInvars(),
                    CxWeibull.getNrRfields());
            CxWeibull.doRecognize(IMAGE_WIDTH, IMAGE_HEIGHT, pixels,
                    result.getVector());

            if (master) {
                logger.debug("Computation done");
            }
        } catch (Exception exception) {
            logger.error("error while processing request", exception);
            throw exception;
        }

        end = System.currentTimeMillis();

        if (master) {
            logger.info("Processing took: " + (end - converting) + " convert: "
                    + (scaling - converting) + " scale: "
                    + (computing - scaling) + " parallel computation: " + ")");
            px.printStatistics();

            // wake up run thread to do GC
            nudge();
        }

        return result;
    }

    private void run() {
        if (master) {
            synchronized (this) {
                // we will be woken up after each vector by RPC thread
                while (!hasEnded()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // IGNORE
                    }
                    System.gc();
                }
            }
        } else {
            // Continually calculate vectors
            while (!hasEnded()) {
                try {
                    calculateVector(null);
                } catch (Exception e) {
                    logger.error("Error on caclulating vector", e);
                }
                System.gc();
            }

        }
    }

    static class ShutDown extends Thread {
        final Server server;

        ShutDown(Server server) {
            this.server = server;
        }

        public void run() {
            server.end();
        }
    };

    public static void main(String[] args) {
        String poolName = null;
        String poolSize = null;

        if (args.length == 0) {
            poolName = System.getProperty("ibis.deploy.job.id", null);
            poolSize = System.getProperty("ibis.deploy.job.size", null);
        } else if (args.length == 2) {
            poolName = args[0];
            poolSize = args[1];
        }

        logger.info("Image processing server starting in pool \"" + poolName
                + "\" of size " + poolSize);

        if (poolName == null || poolSize == null) {
            System.err
                    .println("USAGE: Server poolname poolsize OR set ibis.deploy.job.id and ibis.deploy.job.size properties");
            System.exit(1);
        }

        Server server = null;
        try {
            server = new Server(poolName, poolSize);
            // Install a shutdown hook that terminates Ibis.
            Runtime.getRuntime().addShutdownHook(new ShutDown(server));

            server.run();
        } catch (Throwable e) {
            System.err.println("Server died unexpectedly!");
            e.printStackTrace(System.err);
        }

        try {
            if (server != null) {
                server.end();
            }
        } catch (Exception e) {
            // Nothing we can do now...
        }
    }

}
