package ibis.dog.client;

import ibis.dog.shared.Communication;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.MachineDescription;
import ibis.dog.shared.Reply;
import ibis.dog.shared.Request;
import ibis.dog.shared.ServerDescription;
import ibis.dog.shared.Upcall;
import ibis.dog.shared.YUV422SPImage;

import org.apache.log4j.Logger;

public class Client extends Thread implements Upcall {

    private static final Logger logger = Logger.getLogger(Client.class);

    public static final int DEFAULT_TIMEOUT = 5000;

    public static final int DEFAULT_WIDTH = 320;

    public static final int DEFAULT_HEIGHT = 240;

    // Communication object.
    private Communication comm;

    // Local machine description (used as reply address for servers).
    private MachineDescription me;

    // Object responsible for the recognition
    private final ObjectRecognition recognition;

    // Current operation
    private byte operation = Request.OPERATION_RECOGNISE;

    // Current server set.
    private Servers servers;

    // Current input media, pixels size, and input frame.

    private YUV422SPImage image;

    private boolean imageValid = false;

    private boolean done = false;

    private int frameNumber = 0;

    private FeatureVector vector;

    // Link to the GUI.
    private ClientListener listener;

    public Client() {
        super("CLIENT");
        recognition = new ObjectRecognition();
    }

    private void init() throws Exception {

        // This may take a while, since it will deploy the server, hub and
        // broker for us...

        System.out.println("$$$$$$$$$$$$ comm");

        comm = new Communication("Client", this);

        System.out.println("$$$$$$$$$$$$ me");

        me = comm.getMachineDescription();

        System.out.println("$$$$$$$$$$$$ server");

        servers = new Servers(comm);

        System.out.println("$$$$$$$$$$$$ init");

    }

    public synchronized byte[] getBuffer(int width, int heigth, int index) {

        if (image == null || image.width != width || image.height != heigth) {
            image = new YUV422SPImage(width, heigth);
        }

        imageValid = false;
        return image.pixels;
    }

    public synchronized void gotImage(byte[] image, int index) {
        imageValid = true;
        notifyAll();
    }

    // for camera's that already allocate a buffer them selves.
    public synchronized void gotImage(byte[] data, int width, int height) {
        this.image = new YUV422SPImage(width, height, data);
        imageValid = true;
        notifyAll();
    }

    private synchronized void returnImage(YUV422SPImage image) {
        if (image == null) {
            this.image = image;
        }
    }

    public boolean learn(String name) {

        FeatureVector v = getFeatureVector();

        if (v != null) {
            System.out.println("Feature Vector not null: " + v);
            return recognition.learn(name, v);
        }

        return false;
    }

    public String recognize() {
        FeatureVector v = getFeatureVector();
        System.out.println("Feature Vector: " + v);

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
        vector = null;
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

    private YUV422SPImage getFrame() {

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

            YUV422SPImage result = image;
            image = null;
            imageValid = false;
            frameNumber++;
            return result;
        }
    }

    public void serverConnected(ServerData server, boolean connected) {
        server.setConnected(connected);
    }

    private void processReply(Reply r) {
        // System.err.println("Got reply from " + r.server);

        ServerData data = servers.findServer(r.server);
        if (data == null) {
            System.err.println("EEP! server not found!!!" + r.server);
            return;
        } else {
            data.hasFrame(false);
            if (r.operation == Request.OPERATION_RECOGNISE) {
                setFeatureVector((FeatureVector) r.result);
                String server = r.server.getName();
                String result = recognition.recognize((FeatureVector) r.result);
                if (result == null) {
                    logger.info(server + " doesn't recognize this object");
                } else {
                    logger.info(server + " says this is a " + result);
                }
                // } else if (r.operation == Request.OPERATION_LABELING) {
                // RGB24Image image = (RGB24Image) r.result;
                // forwardFrameToListnener(image, 1);
            } else if (r.operation == Request.OPERATION_DUMMY) {
                System.out
                        .println("Dummy reply received " + (Integer) r.result);
            } else {

                System.out.println("Unknown reply received (ignored)");
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
            }
                break;

            case Communication.CLIENT_REPLY_REQUEST: {
                // It is a reply to a server request.
                processReply((Reply) objects[0]);
            }
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

    private void sendFrameToServer(YUV422SPImage image) {
        ServerData target = servers.findIdleServer();

        if (target != null) {
            System.out.println("Sending frame to " + target.getName());

            // target.send(new Request(getCurrentOperation(), 0L,
            // new CompressedImage(image), me));
            target.send(new Request(getCurrentOperation(), 0L, image, me));

        } else {
            //System.out.println("Dropping frame (" + servers.getServers().length
            //        + ")");
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

        YUV422SPImage image = getFrame();

        while (image != null) {
            sendFrameToServer(image);
            returnImage(image);
            image = getFrame();
        }
        System.out.println("done!");
        // We are done, so kill the servers polling, deployment and
        // communication.
        servers.done();
        comm.exit();
    }

}
