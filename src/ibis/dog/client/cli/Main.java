package ibis.dog.client.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.dog.client.Client;
import ibis.dog.client.ServerHandler;
import ibis.dog.client.WebCam;
import ibis.imaging4j.IO;
import ibis.imaging4j.Image;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // public static RGB32Image load(File file) throws IOException {
    //        
    // BufferedImage image = ImageIO.read(file);
    //        
    // final int width = image.getWidth();
    // final int height = image.getHeight();
    //        
    // int [] argbs = new int[width*height];
    //        
    // image.getRGB(0, 0, width, height, argbs, 0, width);
    //        
    // return new RGB32Image(width,height,argbs);
    // }

    private static final class ShutDown extends Thread {
        private final Client client;

        ShutDown(Client client) {
            this.client = client;
        }

        public void run() {
            client.end();
        }
    }

    public static void main(String[] args) {
        Image image = null;

        try {
            if (args.length > 1) {
                System.err.println("Usage Main.main [image.jpg]");
                System.exit(1);
            }

            if (args.length == 1) {
                image = IO.load(new File(args[0]));
            }

            Client client = new Client(null);

            // Install a shutdown hook that terminates ibis.
            Runtime.getRuntime().addShutdownHook(new Main.ShutDown(client));

            if (image == null) {
                // use first webcam as input
                WebCam cam = new WebCam(client);
                cam.selectDevice(cam.availableDevices()[0]);
            } else {
                // provide fake video @ 30fps
                new FakeVideoProvider(client, image, 30);
            }

            // do nothing
            while (true) {
                Thread.sleep(10000);
                logger.info("Input Frames/Sec = " + client.inputFPS());
                logger.info("Displayed Frames/Sec = " + client.displayedFPS());
                logger.info("Processed Frames/Sec = " + client.processedFPS());

                for (ServerHandler handler : client.getServers()) {
                    handler.setEnabled(true);
                }
            }

        } catch (Exception e) {
            logger.error("Could not start command line client", e);
        }
    }
}
