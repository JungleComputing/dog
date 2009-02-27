package ibis.dog.client;

import ibis.dog.shared.Communication;
import ibis.dog.shared.MachineDescription;
import ibis.dog.shared.ServerDescription;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Servers implements Runnable {
	
    private static final Logger logger = LoggerFactory.getLogger(Servers.class); 

    public static final int UPDATE_INTERVAL = 5000;

    private HashMap<ServerDescription, ServerData> servers = 
        new HashMap<ServerDescription, ServerData>();

    private final Communication comm;

    private boolean done = false;
    
    public Servers(Communication comm) {
        this.comm = comm;
        new Thread(this).start();
    }

    public synchronized void setServers(ServerDescription[] descriptions) {
        
        HashMap<ServerDescription, ServerData> oldservers = servers;
        servers = new HashMap<ServerDescription, ServerData>();

        for (ServerDescription s : descriptions) {
            ServerData sd = null;
            if (oldservers.containsKey(s)) {
                logger.debug("Keeping server: " + s.getName());
                sd = oldservers.remove(s);
            } else {
                logger.info("Adding server: " + s.getName());
                sd = new ServerData(s, comm);
            }
            servers.put(s, sd);
        }
        for (ServerData s : oldservers.values()) {
            logger.info("Removing server: " + s.getName());
            s.done();
        }
    }

    public synchronized ServerData findIdleServer() {
        
        for (ServerData s : servers.values()) {
            if (s.isConnected() && !s.hasFrame()) {
                return s;
            }
        }
        return null;
    }

    public synchronized ServerData findServer(ServerDescription s) {
        
        ServerData sd = servers.get(s);
        
        if (sd == null) {
            System.err.println("Failed to find server!!!" + sd);
        }
        
        return sd;
    }

    private void requestServers() {
        
        logger.debug("Client requesting servers from broker...");

        // Try and find the broker. 
        MachineDescription broker = comm.findMachine("Broker", "Broker");

        if (broker == null) {
            System.err.println("Failed to find broker!");
            return;
        }
        try {
            comm.send(broker, Communication.BROKER_REQ_GET_SERVERS, comm
                    .getMachineDescription());

        } catch (Exception e) {
            System.err.println("Problem while contacting broker!");
            e.printStackTrace(System.err);
        }
    }

    public synchronized ServerData[] getServers() {
        
        if (servers.size() == 0) {
            return new ServerData[0];
        }
        
        return servers.values().toArray(new ServerData[servers.size()]);
    }

    public synchronized void done() {
        done = true;
        notifyAll();
    }
    
    public synchronized boolean getDone() { 
        return done;
    }
    
    private synchronized void waitFor(long time) { 
        try { 
            wait(time);
        } catch (InterruptedException e) {
            // ignore
        }
    }
    
    public void run() {
        try {
            while (!getDone()) {
                requestServers();
                waitFor(UPDATE_INTERVAL);
            }
        } catch (Throwable e) {
            System.err.println("Broker died unexpectedly!");
            e.printStackTrace(System.err);
        }
        
        System.out.println("Server polling done!");
    }
}
