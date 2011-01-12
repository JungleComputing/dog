package ibis.dog.client.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.dog.client.Client;
import ibis.dog.client.MessageListener;
import ibis.dog.client.ServerHandler;
import ibis.dog.client.StatisticsListener;
import ibis.dog.client.WebCam;
import ibis.imaging4j.Image;
import ibis.imaging4j.io.IO;

public class CLI implements MessageListener, StatisticsListener {

    private static final Logger logger = LoggerFactory.getLogger(CLI.class);

    private static final class ShutDown extends Thread {
        private final Client client;

        ShutDown(Client client) {
            this.client = client;
        }

        public void run() {
            client.end();
        }
    }
    
    @Override
    public void message(String message) {
        logger.info(message);
    }

    @Override
    public void newStatistics(double inputFps, double displayedFps,
            double processedFps) {
        logger.info(String.format(
                "Input %.2f fps. Displayed %2f fps. Processed %2f fps",
                inputFps, displayedFps, processedFps));
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

            CLI cli = new CLI();

            Client client = new Client(cli, null, false, cli);

            // Install a shutdown hook that terminates ibis.
            Runtime.getRuntime().addShutdownHook(new CLI.ShutDown(client));

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
                Thread.sleep(1000);

                for (ServerHandler handler : client.getServers()) {
                    handler.setEnabled(true);
                }
            }

        } catch (Exception e) {
            logger.error("Could not start command line client", e);
        }
    }

   
}
