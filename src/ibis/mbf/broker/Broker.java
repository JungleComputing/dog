package ibis.mbf.broker;

import ibis.ipl.IbisCreationFailedException;
import ibis.mbf.shared.Communication;
import ibis.mbf.shared.MachineDescription;
import ibis.mbf.shared.ServerDescription;
import ibis.mbf.shared.Upcall;

import java.io.IOException;
import java.util.HashSet;

public class Broker implements Upcall {

    public static final int UPDATE_INTERVAL = 5000;
    
    private Communication communication; 
    
    private final HashSet<ServerDescription> servers = 
        new HashSet<ServerDescription>();
    
    private Broker() throws IbisCreationFailedException, IOException { 

        // Create an Communication object
		communication = new Communication("Broker", this);
        
        // Elect this process to be the broker
        communication.elect("Broker");
    }
    
    private void pingServers() {
        
        System.out.println("Ping to all servers to check if they are alive...");
      
        ServerDescription [] servers = getServers();
        
        for (ServerDescription s : servers) { 
            try { 
                communication.send(s, Communication.SERVER_REGISTERED);
            } catch (Exception cre) {
                removeServer(s);
            }
        }
    }
          
    private synchronized boolean addServer(ServerDescription s) {
    
        if (servers.contains(s)) { 
            return false;
        }
        
        servers.add(s);
        return true;
    }
    
    private synchronized boolean removeServer(ServerDescription s) {
        return servers.remove(s);
    }
    
    private synchronized ServerDescription [] getServers() {        
        return servers.toArray(new ServerDescription[servers.size()]);
    }
    
    public void upcall(byte opcode, Object ... objects) throws IOException {
        
        switch (opcode) { 
        case Communication.BROKER_REQ_GET_SERVERS: {
            // It a lookup request from a client.
            communication.send((MachineDescription) objects[0], 
                    Communication.CLIENT_REPLY_GETSERVERS, 
                    (Object []) getServers());
        }
        break;
            
        case Communication.BROKER_REQ_REGISTER: { 
            // It is a registration request from a server.
            ServerDescription s = (ServerDescription) objects[0];
            
            boolean accept = addServer(s);
            
            if (accept) {
                communication.send(s, Communication.SERVER_REGISTERED);
            }
        }
        break;
        
        case Communication.BROKER_REQ_UNREGISTER: { 
            // It is a deregistration request from a server
            ServerDescription s = (ServerDescription) objects[0]; 
            removeServer(s);
        }
        break;
        
        default:
            System.err.println("Received unknown opcode: " + opcode);
        }
    }

    private void run() {
        
        try {
            while (true) {
                Thread.sleep(UPDATE_INTERVAL);
                pingServers();
            }
        } catch (Throwable e) { 
            System.err.println("Broker died unexpectedly!");
            e.printStackTrace(System.err);
        }   
    }
 
    public static void main(String [] args) {
        
        try {      
            new Broker().run();
        } catch (Throwable e) { 
            System.err.println("Broker died unexpectedly!");
            e.printStackTrace(System.err);
        } 
    }
    
}
