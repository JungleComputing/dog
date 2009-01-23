package ibis.dog.client;

import ibis.dog.gui.LearnedObjects.LearnedObject;
import ibis.dog.shared.Communication;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.JPEGImage;
import ibis.dog.shared.MachineDescription;
import ibis.dog.shared.Reply;
import ibis.dog.shared.Request;
import ibis.dog.shared.ServerDescription;
import ibis.dog.shared.Upcall;
import ibis.dog.shared.YUV422SPImage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;

public class Client extends Thread implements Upcall {

    private static final Logger logger = Logger.getLogger(Client.class);

    public static final int DEFAULT_TIMEOUT = 5000;

    // Communication object.
    private Communication comm;

    // Local machine description (used as reply address for servers).
    private MachineDescription me;

    // Object responsible for the recognition
    private final ObjectRecognition recognition;

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

    private Map<Integer, PendingRequest> pendingLearnRequests = new HashMap<Integer, PendingRequest>();

    private Map<Integer, PendingRequest> pendingRecognizeRequests = new HashMap<Integer, PendingRequest>();

    private Activity mActivity;

    private Camera mCamera;

    private int mSampleSize;

    private static int id = 0;

    private static synchronized int nextId() {
        return id++;
    }

    public Client(Activity activity, Camera camera) {
        super("CLIENT");
        mActivity = activity;
        mCamera = camera;
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

    public void learn(final String name, final String author, JPEGImage image,
            final ClientActionListener listener) throws Exception {
        if (servers == null) {
            throw new Exception("servers is null");
        }
        final ServerData target = servers.findIdleServer();

        if (target == null) {
            throw new Exception("no idle server");
        }
        target.send(new Request(Request.OPERATION_LABELING, 0L, id, image, me));

        // store everything except the feature vector in a table, when
        // the feature vector arrives, this learning action will be
        // stored in the content provider
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = mSampleSize;
        pendingLearnRequests
                .put(nextId(), new PendingRequest(name, author, System
                        .currentTimeMillis(), BitmapFactory.decodeByteArray(
                        image.cdata, 0, image.cdata.length, options), listener));

    }

    public void recognize(JPEGImage image, final ClientActionListener listener)
            throws Exception {
        if (servers == null) {
            throw new Exception("servers is null");
        }
        final ServerData target = servers.findIdleServer();

        if (target == null) {
            throw new Exception("no idle server");
        }

        System.out.println("Found an idle server: " + target);

        System.out.println("Image: " + image);
        target
                .send(new Request(Request.OPERATION_RECOGNIZE, 0L, id, image,
                        me));
        System.out.println("request sent: " + image.cdata.length);

        // store everything except the feature vector in a table, when
        // the feature vector arrives, this learning action will be
        // stored in the content provider
        pendingRecognizeRequests.put(nextId(), new PendingRequest(null, null,
                System.currentTimeMillis(), null, listener));
        System.out.println("pending request stored");
    }

    public synchronized void registerListener(ClientListener l) {
        this.listener = l;
    }

    private synchronized void forwardServersToListener() {
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
        System.out.println("Got reply from " + r.server);

        ServerData server = servers.findServer(r.server);
        if (server == null) {
            System.err.println("EEP! server not found!!!" + r.server);
            return;
        } else {
            if (r.operation == Request.OPERATION_LABELING) {
                server.hasFrame(false);
                if (pendingLearnRequests.containsKey(r.id)) {
                    // there's a pending request that matches this reply. Store
                    // the request with the feature vector from the reply in the
                    // content provider
                    PendingRequest request = pendingLearnRequests.remove(r.id);
                    ContentValues values = new ContentValues();
                    values.put(LearnedObject.OBJECT_NAME, request.name);
                    values.put(LearnedObject.AUTHOR, request.author);
                    values.put(LearnedObject.CREATED_DATE, request.requestTime);
                    Uri uri = mActivity.getContentResolver().insert(
                            LearnedObject.CONTENT_URI, values);
                    OutputStream thumbOutStream = null;
                    try {
                        thumbOutStream = mActivity.getContentResolver()
                                .openOutputStream(
                                        LearnedObject.getThumbUri(uri));
                        request.thumb.compress(Bitmap.CompressFormat.JPEG, 75,
                                thumbOutStream);
                        thumbOutStream.flush();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            thumbOutStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ObjectOutputStream featureVectorOutStream = null;
                    try {
                        featureVectorOutStream = new ObjectOutputStream(
                                mActivity
                                        .getContentResolver()
                                        .openOutputStream(
                                                LearnedObject
                                                        .getFeatureVectorUri(uri)));
                        featureVectorOutStream
                                .writeObject(((FeatureVector) r.result));
                        featureVectorOutStream.flush();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            featureVectorOutStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // inform the listener that the learning of this object has
                    // been done!
                    Cursor cursor = mActivity.managedQuery(uri, PROJECTION,
                            null, null, LearnedObject.DEFAULT_SORT_ORDER);
                    System.out.println("first==" + cursor.moveToFirst());
                    System.out.println("cursor.count=" + cursor.getCount());

                    request.listener.replyReceived(cursor);
                } else {
                    System.out.println("got spurious reply with id: " + r.id);
                    return;
                }
            } else if (r.operation == Request.OPERATION_RECOGNIZE) {
                System.out.println("recognize reply!");
                server.hasFrame(false);
                if (pendingRecognizeRequests.containsKey(r.id)) {
                    // there's a pending request that matches this reply.
                    // Compare
                    // the reply's feature vector with the existing feature
                    // vectors
                    PendingRequest request = pendingRecognizeRequests
                            .remove(r.id);

                    Cursor cursor = null;
                    try {
                        System.out.println("before recognition.recognize");
                        cursor = recognition.recognize(
                                (FeatureVector) r.result, mActivity);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        System.out.println("recognition gave an exception!");
                        e.printStackTrace();
                    }

                    // inform the listener that the learning of this object has
                    // been done!

                    request.listener.replyReceived(cursor);
                } else {
                    System.out.println("got spurious reply with id: " + r.id);
                    return;
                }

            } else if (r.operation == Request.OPERATION_DUMMY) {
                System.out
                        .println("Dummy reply received " + (Integer) r.result);
            } else {
                System.out.println("Unknown reply received (ignored)");
            }
        }
    }

    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            LearnedObject._ID, // 0
            LearnedObject.OBJECT_NAME, // 1
            LearnedObject.AUTHOR, // 2
    };

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
                forwardServersToListener();
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
            // sendFrameToServer(image);
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
