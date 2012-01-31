package ibis.dog.server;

import gpu.*;
import stereo.*;

import ibis.dog.Communication;
import ibis.media.imaging.Format;
import ibis.media.imaging.Image;
import ibis.media.imaging.Imaging;
import ibis.ipl.Ibis;
import ibis.ipl.util.rpc.RPC;
import ibis.ipl.util.rpc.RemoteObject;

import java.rmi.RemoteException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.awt.image.ImagingLib;

public class Server implements ServerInterface {

    // public static final int IMAGE_WIDTH = 352;
    // public static final int IMAGE_HEIGHT = 768;

    // public static final int IMAGE_WIDTH = 1024;
    // public static final int IMAGE_HEIGHT = 768;

    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 480;

    public static final Format IMAGE_FORMAT = Format.GREY;

    public static final int DEFAULT_TIMEOUT = 5000;

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final Ibis ibis;

    private final RemoteObject<ServerInterface> remoteObject;

    private GPURuntime runtime;
    private RectifierGPU rectifierLeft;
    private RectifierGPU rectifierRight;
    private StereoPipelineGPU stereo;
    
    private boolean ended = false;
    private boolean initialized = false;

    private Server() throws Exception {
        // Server needs to provide an Ibis to contact the outside world.
        logger.info("Initializing Global Communication");
        ibis = Communication.createIbis(Communication.SERVER_ROLE, null);
        remoteObject = RPC.exportObject(ServerInterface.class, this,
                "server", ibis);

        logger.info("Initializing Stereo at " + IMAGE_WIDTH + "x"
                + IMAGE_HEIGHT);

        // do initialization now instead of after the first request is received.
        runtime = new GPURuntime();
        rectifierLeft = new RectifierGPU(runtime, IMAGE_WIDTH, IMAGE_HEIGHT);
        rectifierRight = new RectifierGPU(runtime, IMAGE_WIDTH, IMAGE_HEIGHT);
        stereo = new StereoPipelineGPU(runtime, IMAGE_WIDTH, IMAGE_HEIGHT, 16);

        logger.info("Initialization done");

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

       try {
            remoteObject.unexport();
            ibis.end();
        } catch (Exception e) {
            logger.error("Error on stopping communication", e);
        }
    }

    public Image calculateDisparity(Image[] images) throws RemoteException,
            Exception {
        Image result;

        long converting = System.currentTimeMillis();
        long scaling = 0;
        long computing = 0;
        long end = 0;

        try {
            // The server should dequeue a request, and prepare it for
            // processing

            logger.debug("Got request");

            if (images == null) {
                logger.debug("fake request received, sending fake reply");
                return null;
            }

            Image[] convertedImages = new Image[images.length];
            for(int k=0; k<images.length; k++){
            	if (images[k].getFormat() == IMAGE_FORMAT) {
            		// no need to convert
            		convertedImages[k] = images[k];
            	} else {
            		convertedImages[k] = Imaging.convert(images[k], IMAGE_FORMAT);
            	}
            }

            scaling = System.currentTimeMillis();

            Image[] scaledImages = new Image[images.length];
            for(int k=0; k<images.length; k++){
            	if (convertedImages[k].getWidth() == IMAGE_WIDTH
                    && convertedImages[k].getHeight() == IMAGE_HEIGHT) {
            		// no need to scale
            		scaledImages[k] = convertedImages[k];
            	} else {
            		scaledImages[k] = Imaging.scale(convertedImages[k], IMAGE_WIDTH,
                        IMAGE_HEIGHT);
            	}
            }

            // Imaging4j.save(scaledImage, new File("image.rgb"));

            GPUArray rectifiedLeft = rectifierLeft.rectify(scaledImages[0].getData().array());
            GPUArray rectifiedRight = rectifierRight.rectify(scaledImages[1].getData().array());

            logger.debug("Starting computation");
            computing = System.currentTimeMillis();
            
            result = new Image(IMAGE_FORMAT, IMAGE_WIDTH, IMAGE_HEIGHT);
            
            int[] resultMatrix;
            resultMatrix = stereo.executeStereo(rectifiedLeft, rectifiedRight);
            
            for(int i=0; i<IMAGE_HEIGHT; i++)
            	for(int j=0; j<IMAGE_WIDTH; j++)
            		result.getData().array()[i*IMAGE_WIDTH+j] = (byte)resultMatrix[i*IMAGE_WIDTH+j];

            logger.debug("Computation done");
        } catch (Exception exception) {
            logger.error("Error while processing request", exception);
            throw exception;
        }

        end = System.currentTimeMillis();

        logger.info("Processing took: " + (end - converting) + " convert: "
                    + (scaling - converting) + " scale: "
                    + (computing - scaling) + " parallel computation: " + ")");

        // wake up run thread to do GC
        nudge();

        return result;
    }

    private void run() {
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

        logger.info("Image processing server starting");

        Server server = null;
        try {
            server = new Server();
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
