package ibis.mbf.client;

import ibis.ipl.IbisCreationFailedException;
import ibis.mbf.client.gui.ClientWindow;
import ibis.mbf.media.Media;
import ibis.mbf.media.MediaFactory;
import ibis.mbf.shared.Communication;
import ibis.mbf.shared.FeatureVector;
import ibis.mbf.shared.Image;
import ibis.mbf.shared.MachineDescription;
import ibis.mbf.shared.Reply;
import ibis.mbf.shared.Request;
import ibis.mbf.shared.ServerDescription;
import ibis.mbf.shared.Upcall;

import java.io.IOException;


public class Client implements Upcall
{
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int DEFAULT_WIDTH   = 320;
    public static final int DEFAULT_HEIGHT  = 240;
//public static final int DEFAULT_WIDTH   = 285;
//public static final int DEFAULT_HEIGHT  = 231;
        
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
    private Media media;
    private int width = DEFAULT_WIDTH; 
    private int height = DEFAULT_HEIGHT; 
    
    private Image image;
    private int frameNumber = 0;    
    private FeatureVector vector; 
    
    // Link to the GUI.
    private ClientListener listener;
    private MediaFactory factory; 
    

    public Client(MediaFactory factory)
	throws IbisCreationFailedException, IOException
	{ 
        this.factory = factory;
        comm = new Communication("Client", this);
        me = comm.getMachineDescription();
        recognition = new ObjectRecognition();
        servers = new Servers(comm); 
    }
    

    public boolean learn(String name)
	{
        FeatureVector v = getFeatureVector();
        
        if (v != null) { 
            return recognition.learn(name, v);
        }
        return false;
    }


    public String recognize()
	{
        FeatureVector v = getFeatureVector();
        if (v != null) { 
            return recognition.recognize(v);
        }
        return null;
    }


    public synchronized void registerListener(ClientListener l)
	{
        this.listener = l;
    }
    

    private synchronized
	void forwardFrameToListnener(Image image, int index)
	{
        if (listener != null) { 
            listener.updateFrame(image.pixels, image.width,
								 image.height, frameNumber, index);
        } 
    }
    

    private synchronized void forwardServersToListnener()
	{
        if (listener != null) { 
            listener.updateServers(servers.getServers());
        }
    }
    

    private synchronized void setFeatureVector(FeatureVector vector)
	{ 
        this.vector = vector;
    }
    

    private synchronized FeatureVector getFeatureVector()
	{
        FeatureVector v = vector;        
        vector = null;        
        return v;
    }
    

    public void serverConnected(ServerData server, boolean connected)
	{ 
        server.setConnected(connected);
    }
    

    public synchronized void mediaSelected(String description)
	{
        // TODO: should be more dynamic (size wise ?);
        media = factory.getMedia(description, width, height);        
        image = new Image(width, height);
        notifyAll();
    }
    

    private synchronized boolean waitForMedia(long timeout)
	{ 
        long start = System.currentTimeMillis();
        long timeLeft = timeout;
        
        while ((media == null || !media.hasImages()) && timeLeft > 0) {
            try { 
                wait(timeout);
            } catch (InterruptedException e) {
                // ignored
            }
            
            timeLeft = System.currentTimeMillis() - start;            
        }
        return (media != null && media.hasImages());
    } 
    

    private synchronized boolean getFrame()
	{ 
        if (media == null || !media.hasImages()) {
            System.out.println("Failed to get frame!");
            return false;
        }
        media.nextImage(image.pixels);                
        frameNumber++;
        return true;
    }
    

    private void processReply(Reply r)
	{ 
        //System.err.println("Got reply from " + r.server);

        ServerData data = servers.findServer(r.server);
        if (data == null) { 
            System.err.println("EEP! server not found!!!" + r.server);
            return;        
        } else { 
            data.hasFrame(false);
            if (r.operation == Request.OPERATION_RECOGNISE) { 
                setFeatureVector((FeatureVector) r.result);
            } else if (r.operation == Request.OPERATION_LABELING) { 
                Image image = (Image) r.result;        
                forwardFrameToListnener(image, 1);
            } else if (r.operation == Request.OPERATION_DUMMY) {
				System.out.println("Dummy reply received " +
													(Integer)r.result);
			} else {

				System.out.println("Unknown reply received (ignored)");
			}
        }
    }

    
    public void upcall(byte opcode, Object... objects)
	throws Exception
	{
        try { 
            switch (opcode) { 
            case Communication.CLIENT_REPLY_GETSERVERS: {
                // It a reply to a lookup request.

                ServerDescription [] s = null;
                if (objects != null) { 

                    s = new ServerDescription[objects.length];

                    for (int i=0;i<objects.length;i++) { 
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
    

    public synchronized byte getCurrentOperation()
	{
        return operation;
    }


    public synchronized void setCurrentOperation(byte operation)
	{
        this.operation = operation;
    }
    

    private void sendFrameToServer()
	{ 
        ServerData target = servers.findIdleServer();
        if (target != null) {       
            target.send(new Request(getCurrentOperation(),
									0L, image, me));
        } 
    }
    
    
    private void processFrames()
	{
        while (getFrame()) {
            sendFrameToServer();
            forwardFrameToListnener(image, 0);
        }
    }
    

    public void run()
	{         
        while (true) {            
            if (waitForMedia(DEFAULT_TIMEOUT)) { 
                processFrames();
            } else { 
                System.err.println("No input media selected!");
            }            
        }
    } 
}
