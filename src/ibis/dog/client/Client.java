package ibis.dog.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.dog.broker.Item;
import ibis.dog.gui.application.FramerateConsumer;
import ibis.dog.gui.application.OutputPanel;
import ibis.dog.shared.Communication;
import ibis.dog.shared.CompressedImage;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.MachineDescription;
import ibis.dog.shared.RGB32Image;
import ibis.dog.shared.Reply;
import ibis.dog.shared.Request;
import ibis.dog.shared.ServerDescription;
import ibis.dog.shared.Upcall;
import ibis.video4j.VideoConsumer;

public class Client extends Thread implements Upcall, VideoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static final int DEFAULT_TIMEOUT = 5000;

    public static final int DEFAULT_WIDTH = 320;

    public static final int DEFAULT_HEIGHT = 240;

    // Communication object.
    private Communication comm;

    // Local machine description (used as reply address for servers).
    private MachineDescription me;

    // Current operation
    private byte operation = Request.OPERATION_RECOGNISE;

    // Current server set.
    private Servers servers;

    // Current input media, pixels size, and input frame.

    private RGB32Image image;

    private boolean imageValid = false;

    private boolean done = false;

    private int frameNumber = 0;

    private Item[] currentResults = null;

    private FeatureVector vector;

    // statistics for vector framerate

    private long start;

    private int vectorCount = 0;

    private FramerateConsumer framerateConsumer;

    // Link to the GUI.
    private ClientListener listener;

    private OutputPanel outputPanel = null;;

    public Client() {
        super("CLIENT");
    }

    private void init() throws Exception {

        // This may take a while, since it will deploy the server, hub and
        // broker for us...

        logger.debug("$$$$$$$$$$$$ comm");

        comm = new Communication("Client", this);

        // Install a shutdown hook that terminates ibis.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                comm.end();
            }
        });

        logger.debug("$$$$$$$$$$$$ me");

        me = comm.getMachineDescription();

        logger.debug("$$$$$$$$$$$$ server");

        servers = new Servers(comm);

        logger.debug("$$$$$$$$$$$$ init");

    }

    public synchronized int[] getBuffer(int width, int heigth, int index) {

        if (image == null || image.width != width || image.height != heigth) {
            image = new RGB32Image(width, heigth);
        }

        imageValid = false;
        return image.pixels;
    }

    public synchronized void gotImage(int[] image, int index) {
        imageValid = true;
        notifyAll();
    }

    private synchronized void returnImage(RGB32Image image) {
        if (image == null) {
            this.image = image;
        }
    }

    public boolean learn(String name) {
        FeatureVector vector = getFeatureVector();

        // create new database item
        Item item = new Item(vector, name, System.getProperty("user.name"),
                null);

        // send item to broker

        MachineDescription broker = comm.findMachine("Broker", "Broker");

        if (broker == null) {
            return false;
        }

        try {
            comm.send(broker, Communication.BROKER_REQ_LEARN, item);
        } catch (Exception e) {
            logger.error("Could not send item to database@broker", e);
            return false;
        }

        return true;
    }

    public synchronized String recognize() {
        if (currentResults == null || currentResults.length == 0) {
            return null;
        }

        return currentResults[0].getName();

    }

    public synchronized void registerListener(ClientListener l) {
        this.listener = l;
    }

    private synchronized void forwardServersToListnener() {
        if (listener != null) {
            listener.updateServers(servers.getServers());
        }
    }

    private synchronized void setFeatureVector(FeatureVector vector) {
        this.vector = vector;
        
        long now = System.currentTimeMillis();

        if (vectorCount == 0) {
            start = System.currentTimeMillis();
        } else if (now >= start + 2500) {
            if (framerateConsumer != null) {
                framerateConsumer
                        .setProcessedFramerate(vectorCount * 1000.0 / (now - start));
            }
            start = now;
            vectorCount = 0;
        }
        vectorCount++;
    }

    private synchronized FeatureVector getFeatureVector() {
        FeatureVector v = vector;
        // vector = null;
        return v;
    }

    public synchronized byte getCurrentOperation() {
        return operation;
    }

    public synchronized void setCurrentOperation(byte operation) {
        this.operation = operation;
    }

    public synchronized void done() {
        System.out.println("Client done!");
        done = true;
        notifyAll();
    }

    private RGB32Image getFrame() {

        synchronized (this) {
            while (!done && !imageValid) {

                try {
                    wait(DEFAULT_TIMEOUT);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            if (done || !imageValid) {
                return null;
            }

            RGB32Image result = image;
            image = null;
            imageValid = false;
            frameNumber++;
            return result;
        }
    }

    public void serverConnected(ServerData server, boolean connected) {
        server.setConnected(connected);
    }

    private void processServerReply(Reply r) {
        // System.err.println("Got reply from " + r.server);

        ServerData data = servers.findServer(r.server);
        if (data == null) {
            System.err.println("EEP! server not found!!!" + r.server);
            return;
        } else {
            data.hasFrame(false);
            if (r.operation == Request.OPERATION_RECOGNISE) {
                // feature vector received from server, set "last known vector"
                // and send a database lookup request to the broker.

                setFeatureVector((FeatureVector) r.result);

                String serverName = r.server.getName();
                FeatureVector vector = (FeatureVector) r.result;

                MachineDescription broker = comm
                        .findMachine("Broker", "Broker");

                if (broker == null) {
                    return;
                }

                try {
                    comm.send(broker, Communication.BROKER_REQ_RECOGNIZE, comm
                            .getMachineDescription(), vector, 3, serverName);
                } catch (Exception e) {
                    logger.error(
                        "Could not send recognize request to database@broker",
                        e);
                }

            } else if (r.operation == Request.OPERATION_DUMMY) {
                System.out
                        .println("Dummy reply received " + (Integer) r.result);
            } else {

                System.out.println("Unknown reply received (ignored)");
            }
        }
    }

    private synchronized void processDatabaseLookup(Item[] items,
            String serverName) {
        this.currentResults = items;

        if (items == null || items.length == 0) {
            logger.info(serverName + " doesn't recognize this object");
            log(serverName + " doesn't recognize this object");
        } else {
            logger.info(serverName + " says this is a " + items[0].getName());
            log(serverName + " says this is a " + items[0].getName());
        }

    }

    public void upcall(byte opcode, Object... objects) throws Exception {
        try {
            switch (opcode) {
            case Communication.CLIENT_REPLY_GETSERVERS: {
                // It a reply to a server lookup request.

                ServerDescription[] s = null;
                if (objects != null) {

                    s = new ServerDescription[objects.length];

                    for (int i = 0; i < objects.length; i++) {
                        s[i] = (ServerDescription) objects[i];
                    }
                } else {
                    s = new ServerDescription[0];
                }
                servers.setServers(s);
                forwardServersToListnener();
            }
                break;

            case Communication.CLIENT_REPLY_REQUEST:
                // It is a reply to a server request.
                processServerReply((Reply) objects[0]);
                break;
            case Communication.CLIENT_REPLY_RECOGNIZE:
                processDatabaseLookup((Item[]) objects[0], (String) objects[1]);
                break;
            default:
                System.err.println("Received unknown opcode: " + opcode);
            }
        } catch (Throwable e) {
            System.err.println("Upcall failed!!!");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void sendFrameToServer(RGB32Image image) {
        ServerData target = servers.findIdleServer();

        if (target != null) {
            logger.debug("Sending frame to " + target.getName());

            target.send(new Request(getCurrentOperation(), 0L,
                    new CompressedImage(image), me));
        } else {
            // System.out.println("Dropping frame");
        }
    }

    public void run() {

        try {
            init();
        } catch (Exception e) {
            System.out.println("Failed to init client!");
            e.printStackTrace();
            return;
        }

        RGB32Image image = getFrame();

        while (image != null) {
            sendFrameToServer(image);
            returnImage(image);
            image = getFrame();
        }

        // We are done, so kill the servers polling, deployment and
        // communication.
        servers.done();
        comm.exit();
    }

    public synchronized void setOutputPanel(OutputPanel outputPanel) {
        this.outputPanel = outputPanel;
    }

    private synchronized void log(String text) {
        if (outputPanel == null) {
            return;
        }
        outputPanel.write(text, false);
    }

    public void setFrameRateConsumer(FramerateConsumer frameRateConsumer) {
        framerateConsumer = frameRateConsumer;
    }

}
