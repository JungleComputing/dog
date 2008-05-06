package ibis.mbf.client;

import ibis.mbf.shared.Communication;
import ibis.mbf.shared.Request;
import ibis.mbf.shared.ServerDescription;


public class ServerData implements Runnable
{
    private static int nextServerID = 0;
    
    public final ServerDescription server;
    public final int serverID;
    private final Communication comm;

    private boolean connected = false;        
    private boolean hasFrame = false;    
    private boolean done = false;    
    
    private Request request = null;
    

    ServerData(ServerDescription server, Communication comm)
	{ 
        this.server = server;
        this.serverID = getNextServerID();
        this.comm = comm;
    
        new Thread(this).start();
    }
    

    public synchronized void hasFrame(boolean value)
	{
        this.hasFrame = value;
    }
    

    public synchronized boolean hasFrame()
	{
        return hasFrame;
    }
        

    public synchronized void setConnected(boolean value)
	{ 
        connected = value;
    }
    

    public synchronized boolean isConnected()
	{ 
        return connected;
    }
    

    public String getName()
	{ 
        return server.getName();
    }
    

    private static synchronized int getNextServerID()
	{
        return nextServerID++;
    }
  

    private synchronized boolean getDone()
	{ 
        return done;
    }

    
    public synchronized void done()
	{ 
        this.done = true;
        notifyAll();
    }
    
    private synchronized Request getRequest()
	{ 
        while (!done && request == null) { 
            try { 
                wait();
            } catch (InterruptedException e) {
                // ignored
            }
        }
        Request tmp = request;
        request = null;   
        return tmp;
    }
    
    
	public synchronized void send(Request request)
	{ 
        this.request = request;
        this.hasFrame = true;
        notifyAll();
    }
    

    private void sendRequest(Request r)
	{ 
        try {
            comm.send(server, Communication.SERVER_REQUEST, r);
        } catch (Exception e) { 
            // TODO: how do we handle this error ?
            System.err.println("Failed to send request to " + server);
            e.printStackTrace(System.err);
            hasFrame(false);
        }
    }
    

    public void run()
	{ 
        while (!getDone()) { 
            Request r = getRequest();
            if (r != null) { 
                sendRequest(r);
            }
        }
    }
}
