package app;

import java.io.IOException;

import ibis.dog.gui.application.ClientGUI;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.hub.Hub;
import ibis.smartsockets.util.TypedProperties;

public class Application {

    // NOTE: this is just a stub for the real application!!!!
    
    private Hub hub;
    
    private Application() throws IOException { 
        
        hub = new Hub(new TypedProperties());
        
        ClientGUI.createGUI(this);      
    }

    public DirectSocketAddress getHubAddress() { 
        return hub.getHubAddress();
    }
    
    public void start() {
        
    }
    
    public static void main(String [] args) { 
    
        try {
            new Application().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
