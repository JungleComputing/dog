package ibis.dog.client;

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
    private byte operation = Request.OPERATION_RECOGNISE;

    // Current server set.
    private Servers servers;

    // Current input media, pixels size, and input frame.
    
    private RGB32Image image;
    private boolean imageValid = false;

    private boolean done = false;
    
    private int frameNumber = 0;

    private FeatureVector vector;
    
    private Deployment deployment;
    
    // Link to the GUI.
    private ClientListener listener;
    
    private boolean initialized = false;
    
    public Client() {
        super("CLIENT");
        recognition = new ObjectRecognition();
    }

    private void init() throws Exception {

        // This may take a while, since it will deploy the server, hub and 
        // broker for us...
        
        Deployment d = new Deployment(null);
        
        synchronized (this) {
            deployment = d;
            notifyAll();
        }
        
        System.out.println("$$$$$$$$$$$$ comm");
        
        comm = new Communication("Client", this);
        
        System.out.println("$$$$$$$$$$$$ me");
        
        me = comm.getMachineDescription();
        
        System.out.println("$$$$$$$$$$$$ server");
        
        servers = new Servers(comm);
   
        System.out.println("$$$$$$$$$$$$ init");
         
        synchronized (this) {
            initialized = true;
            notifyAll();
        }
    }

    public synchronized Deployment getDeployment() {

        while (deployment == null) { 
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        return deployment;
    }

    
    public synchronized int [] getBuffer(int width, int heigth, int index) {
        
        if (image == null || image.width != width || image.height != heigth) {
            image = new RGB32Image(width, heigth);
        } 
        
        imageValid = false;
        return image.pixels;
    }

    public synchronized void gotImage(int [] image, int index) {
        imageValid= true;
        notifyAll();
    }
    
    private synchronized void returnImage(RGB32Image image) {
        if (image == null) { 
            this.image = image;
        }
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
    
    private void processReply(Reply r) {
        //System.err.println("Got reply from " + r.server);

        ServerData data = servers.findServer(r.server);
        if (data == null) {
            System.err.println("EEP! server not found!!!" + r.server);
            return;
        } else {
            data.hasFrame(false);
            if (r.operation == Request.OPERATION_RECOGNISE) {
                setFeatureVector((FeatureVector) r.result);
                System.out.println("Feature Vector received");
                //			} else if (r.operation == Request.OPERATION_LABELING) { 
                //				RGB24Image image = (RGB24Image) r.result;        
                //				forwardFrameToListnener(image, 1);
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
    
    private void sendFrameToServer(RGB32Image image) {
        ServerData target = servers.findIdleServer();
       
        if (target != null) {
            System.out.println("Sending frame to " + target.getName());
            
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
        deployment.done();
        comm.exit();
    }
    
}
