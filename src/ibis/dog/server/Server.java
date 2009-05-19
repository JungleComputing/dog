package ibis.dog.server;

import ibis.dog.shared.Communication;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.Upcall;
import ibis.imaging4j.Conversion;
import ibis.imaging4j.Format;
import ibis.imaging4j.Image;
import ibis.imaging4j.Scaling;
import ibis.imaging4j.conversion.Convertor;
import ibis.imaging4j.effects.Scaler;
import ibis.ipl.IbisIdentifier;

import java.util.Arrays;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jorus.parallel.PxSystem;
import jorus.weibull.CxWeibull;

public class Server implements Upcall {

    public static final int IMAGE_WIDTH = 1024;
    public static final int IMAGE_HEIGHT = 768;

    public static final int DEFAULT_TIMEOUT = 5000;

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final boolean master;

    private final Communication communication;

    private final LinkedList<ServerRequest> requests;

    private final PxSystem px;

    private boolean ended = false;

    private Server(String poolName, String poolSize) throws Exception {
        requests = new LinkedList<ServerRequest>();

        logger.info("Initializing PxSystem at " + IMAGE_WIDTH + "x" +IMAGE_HEIGHT);
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
            communication = new Communication(Communication.SERVER_ROLE, this);
            logger.info("Initializing Weibull at " + IMAGE_WIDTH + "x" +IMAGE_HEIGHT);
        } else {
            communication = null;
        }

        // do initialization now instead of after the first request is received.
        CxWeibull.initialize(IMAGE_WIDTH, IMAGE_HEIGHT);
        logger.info("Rank " + px.myCPU() + " of " + px.nrCPUs() + " Initialization done");
    }

    private synchronized void setEnded() {
        ended = true;
    }

    private synchronized boolean hasEnded() {
        return ended;
    }

    void end() {
        logger.info("Ending server");
        setEnded();
        if (communication != null) {
            communication.end();
        }
        try {
            px.exitParallelSystem();
        } catch (Exception e) {
            System.err.println("error on exiting parallel system");
            e.printStackTrace(System.err);
        }
    }

    private synchronized void queueRequest(ServerRequest request) {
        requests.addLast(request);
        notifyAll();
    }

    private synchronized ServerRequest getRequest(long timeout) {
        long start = System.currentTimeMillis();
        long timeLeft = timeout;

        while (requests.size() == 0 && timeLeft > 0 && !ended) {
            try {
                wait(timeLeft);
            } catch (InterruptedException e) {
                // ignored
            }

            timeLeft = System.currentTimeMillis() - start;
        }

        if (requests.size() > 0) {
            return requests.removeFirst();
        } else {
            return null;
        }
    }

    @Override
    public void gotMessage(Object object) {
        if (!(object instanceof ServerRequest)) {
            logger
                    .error("Server Received an unknown request object: "
                            + object);
            return;
        }

        ServerRequest request = (ServerRequest) object;

        logger.info("Server request received...");

        queueRequest(request);
    }

    @Override
    public void newServer(IbisIdentifier identifier) {
        // IGNORE
    }

    @Override
    public void serverGone(IbisIdentifier identifier) {
        // IGNORE
    }

    private void processRequest(boolean master) {
        ServerRequest request = null;

        long start = System.currentTimeMillis();
        long opReceived = 0;
        long opStart = 0;
        long opEnd = 0;
        long end = 0;

        byte[] pixels;

        ServerReply reply = null;
        try {
            if (master) {
                // The master should dequeue a request, and prepare it for
                // processing

                request = getRequest(DEFAULT_TIMEOUT);
                Format srcFormat = request.getImage().getFormat();

                opReceived = System.currentTimeMillis();

                logger.debug(px.myCPU() + " Got request " + request);

                if (request == null) {
                    return;
                }

                Image rgb24Image;
                if (srcFormat == Format.RGB24) {
                    // no need to convert
                    rgb24Image = request.getImage();
                } else {
                    Convertor torgb24 = Conversion.getConvertor(srcFormat,
                            Format.RGB24);

                    if (torgb24 == null) {
                        throw new Exception("cannot convert from " + srcFormat
                                + " to " + Format.RGB24);
                    }

                    rgb24Image = torgb24.convert(request.getImage(), null);
                }

                Image image;
                if (rgb24Image.getWidth() == IMAGE_WIDTH
                        && rgb24Image.getHeight() == IMAGE_HEIGHT) {
                    // no need to scale
                    image = rgb24Image;
                } else {
                    Scaler scaler = Scaling.getScaler(Format.RGB24);

                    if (scaler == null) {
                        throw new Exception("cannot scale " + rgb24Image.getFormat());
                    }

                    image = scaler.scale(rgb24Image, IMAGE_WIDTH, IMAGE_HEIGHT);
                }
                pixels = image.getData().array();
            } else {
                //allocate space for image
                pixels = new byte[(int) Format.RGB24.bytesRequired(IMAGE_WIDTH,
                        IMAGE_HEIGHT)];
            }

            opStart = System.currentTimeMillis();

            FeatureVector result = new FeatureVector(CxWeibull.getNrInvars(),
                    CxWeibull.getNrRfields());
            CxWeibull.doRecognize(IMAGE_WIDTH, IMAGE_HEIGHT, pixels,
                    result.vector);

            opEnd = System.currentTimeMillis();

            reply = new ServerReply(communication.getIdentifier(), request
                    .getSequenceNumber(), result);
        } catch (Exception exception) {
            logger.error("error while processing request", exception);
            reply = new ServerReply(communication.getIdentifier(), request
                    .getSequenceNumber(), new Exception(
                    "could not process request", exception));
        }

        if (master) {
            logger.info("Sending reply....");
            try {
                communication.send(request.getReplyAddress(), reply);
            } catch (Exception e) {
                logger.error("Failed to return reply to "
                        + request.getReplyAddress(), e);
            }
            logger.info("Reply send....");
        }

        end = System.currentTimeMillis();
        /*
         * System.out.println("Total time   " + (end - start) + " ms.");
         * System.out.println("  request    " + (request - start) + " ms.");
         * System.out.println("  decompress " + (decompress - request) +
         * " ms."); System.out.println("  bcast      " + (commdone - decompress)
         * + " ms."); System.out.println("  op         " + (endop - commdone) +
         * " ms."); System.out.println("  reply      " + (end - endop) +
         * " ms.");
         */

        if (master) {
            px.printStatistics();

            System.out.println("Time = " + (end - start) + " (receive: "
                    + (opReceived - start) + " (convert: "
                    + (opStart - opReceived)

                    + " operation: " + (opEnd - opStart) + " post: "
                    + (end - opEnd) + " )");
        }
    }

    private void run() {
        while (!hasEnded()) {
            processRequest(master);
            System.gc();
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

        System.out.println("Server: " + Arrays.toString(args));

        if (args.length == 0) {
            poolName = System.getProperty("ibis.deploy.job.id", null);
            poolSize = System.getProperty("ibis.deploy.job.size", null);
        } else if (args.length == 2) {
            poolName = args[0];
            poolSize = args[1];
        }

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
