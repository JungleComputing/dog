package ibis.dog.test;

import ibis.dog.shared.Communication;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.MachineDescription;
import ibis.dog.shared.RGB24Image;
import ibis.dog.shared.Reply;
import ibis.dog.shared.Request;
import ibis.dog.shared.ServerDescription;
import ibis.dog.shared.Upcall;
import ibis.ipl.IbisIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client extends Thread implements Upcall {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static final int DEFAULT_TIMEOUT = 5000;

    public static final int DEFAULT_WIDTH = 320;

    public static final int DEFAULT_HEIGHT = 240;

    // Communication object.
    private Communication comm;

    // Local machine description (used as reply address for servers).
    private MachineDescription me;

    // Object resposible for the recognition
    private final ObjectRecognition recognition;

    // Current operation
    private byte operation = Request.OPERATION_RECOGNIZE;

    // Current server set.
    private Servers servers;

    // Current input media, pixels size, and input frame.
    private final RGB24Image image;

    private FeatureVector vector;

    // Link to the GUI.
    private ClientListener listener;

    private final int count;

    private int replies;

    private int sends;

    public Client(RGB24Image image, int count) {
        super("CLIENT");
        this.image = image;
        this.count = count;
        recognition = new ObjectRecognition();
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

    public boolean learn(String name) {

        FeatureVector v = getFeatureVector();

        if (v != null) {
            return recognition.learn(name, v);
        }

        return false;
    }

    public String recognize() {
        FeatureVector v = getFeatureVector();

        if (v != null) {
            return recognition.recognize(v);
        }

        return null;
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

    // public synchronized void done() {
    // System.out.println("Client done!");
    // done = true;
    // notifyAll();
    // }

    private RGB24Image getFrame() {
        return image;
    }

    private void processReply(Reply r) {
        // System.err.println("Got reply from " + r.server);

        ServerData data = servers.findServer(r.server);
        if (data == null) {
            System.err.println("EEP! server not found!!!" + r.server);
            return;
        } else {
            data.hasFrame(false);
            if (r.operation == Request.OPERATION_RECOGNIZE) {
                setFeatureVector((FeatureVector) r.result);
                String server = r.server.getName();
                String result = recognition.recognize((FeatureVector) r.result);
                if (result == null) {
                    logger.info(server + " doesn't recognize this object");
                    log(server + " doesn't recognize this object");
                } else {
                    logger.info(server + " says this is a " + result);
                    log(server + " says this is a " + result);
                }
                // } else if (r.operation == Request.OPERATION_LABELING) {
                // RGB24Image image = (RGB24Image) r.result;
                // forwardFrameToListnener(image, 1);

                synchronized (this) {
                    replies++;
                    notifyAll();
                }
            } else if (r.operation == Request.OPERATION_DUMMY) {
                System.out
                        .println("Dummy reply received " + (Integer) r.result);
            } else {

                System.out.println("Unknown reply received (ignored)");
            }
        }
    }

    private synchronized void waitForReplies() {

        while (replies < count) {
            try {
                wait();
            } catch (Exception e) {
                // ignored
            }
        }
    }

    public void upcall(byte opcode, Object... objects) throws Exception {
        try {
            switch (opcode) {
            case Communication.CLIENT_REPLY_GETSERVERS: {
                // It a reply to a lookup request.

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
                break;
            }

            case Communication.CLIENT_REPLY_REQUEST: {
                // It is a reply to a server request.
                processReply((Reply) objects[0]);
                break;
            }

            default:
                System.err.println("Received unknown opcode: " + opcode);
            }
        } catch (Throwable e) {
            System.err.println("Upcall failed!!!");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void sendFrameToServer(RGB24Image image) {
        ServerData target = servers.findIdleServer();

        if (target != null) {
            logger.debug("Sending frame to " + target.getName());

            target.send(new Request(getCurrentOperation(), 0L, image, me));

            sends++;
        } else {
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                // ignored
            }
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

        RGB24Image image = getFrame();

        while (sends < count && image != null) {
            sendFrameToServer(image);
            image = getFrame();
        }

        waitForReplies();

        // We are done, so kill the servers polling, deployment and
        // communication.
        servers.done();
        comm.exit();
    }

    private synchronized void log(String text) {
        System.out.println(text);
    }

    @Override
    public void gone(IbisIdentifier ibis) {
        //IGNORE
    }

}
