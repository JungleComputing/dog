package ibis.mbf.client;

import ibis.mbf.shared.Communication;
import ibis.mbf.shared.MachineDescription;
import ibis.mbf.shared.ServerDescription;

import java.util.HashMap;


public class Servers implements Runnable
{
    public static final int UPDATE_INTERVAL = 5000;
    
    private HashMap<ServerDescription, ServerData> servers = 
        new HashMap<ServerDescription, ServerData>();
    
    private final Communication comm; 


    public Servers(Communication comm)
	{ 
        this.comm = comm;        
        new Thread(this).start();
    }
    

    public synchronized
	void setServers(ServerDescription [] descriptions)
	{
        HashMap<ServerDescription, ServerData> oldservers = servers;
        servers = new HashMap<ServerDescription, ServerData>();
        
        for (ServerDescription s : descriptions) { 
            ServerData sd = null;
            if (oldservers.containsKey(s)) { 
                System.err.println("Keeping server: " + s.getName());
                sd = oldservers.remove(s);
            } else {                 
                System.err.println("Adding server: " + s.getName());
                sd = new ServerData(s, comm);
            }
            servers.put(s, sd);
        }
        for (ServerData s : oldservers.values()) { 
            System.err.println("Removing server: " + s.getName());
            s.done();
        }
    }


    public synchronized ServerData findIdleServer()
	{
        for (ServerData s : servers.values()) {                
            if (s.isConnected() && !s.hasFrame()) {
                return s;
            }            
        }
        return null;
    }
    

    public synchronized ServerData findServer(ServerDescription s)
	{
        ServerData sd = servers.get(s);
        if (sd == null) { 
            System.err.println("Failed to find server!!!" + sd);
        }
        return sd;
    }


    private void requestServers()
	{ 
        System.out.println("Client requesting servers from broker...");

        // Try and find the broker. 
        MachineDescription broker = comm.findMachine("Broker","Broker");
        
        if (broker == null) { 
            System.err.println("Failed to find broker!");
            return;            
        }
        try {
            comm.send(broker, Communication.BROKER_REQ_GET_SERVERS, 
                    comm.getMachineDescription());
            
        } catch (Exception e) {
            System.err.println("Problem while contacting broker!");
            e.printStackTrace(System.err);
        }    
    }


    public synchronized ServerData[] getServers()
	{
        if (servers.size() == 0) { 
            return new ServerData[0];
        }
        return servers.values().toArray(new ServerData[servers.size()]);
    }


    public void run() { 
        try {
            while (true) {
                Thread.sleep(UPDATE_INTERVAL);
                requestServers();
            }
        } catch (Throwable e) { 
            System.err.println("Broker died unexpectedly!");
            e.printStackTrace(System.err);
        }   
    }
}
