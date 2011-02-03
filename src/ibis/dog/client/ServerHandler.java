package ibis.dog.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.dog.Communication;
import ibis.dog.server.ServerRequest;
import ibis.imaging4j.Image;
import ibis.ipl.IbisIdentifier;
import ibis.util.ThreadPool;

public class ServerHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    // maximum time we wait for an answer before we send a new request
    private static final int TIMEOUT = 120000;

    public final IbisIdentifier address;

    private final Client client;
    private final Communication communication;

    private boolean enabled = false;
    private boolean done = false;

    private boolean waitingForReply = false;
    
    //server is active if a message has ever been received from it
    private boolean active = false;

    ServerHandler(IbisIdentifier address, Client client,
            Communication communication) {
        this.address = address;
        this.client = client;
        this.communication = communication;
        
        logger.debug("new Server handler: " + this);

        ThreadPool.createNew(this, "Server Handler for " + this);
    }

    public synchronized void setEnabled(boolean value) {
        enabled = value;
        notifyAll();
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void waitUntilEnabled() {
        while (!isEnabled() && !isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

    public String getName() {
        return address.location().getLevel(address.location().numberOfLevels() - 1);
    }
    
    private synchronized boolean isDone() {
        return done;
    }

    public synchronized void end() {
        this.done = true;
        notifyAll();
    }

    private boolean sendRequest(Image image) {
        ServerRequest request = new ServerRequest(System.currentTimeMillis(), image, communication
                .getIdentifier());

        try {
            communication.send(address, request);
        } catch (Exception e) {
            // TODO: how do we handle this error ?
            logger.error("Failed to send request to " + this, e);
            return false;
        }
        waitingForReply = true;
        return true;
    }

    private synchronized void waitForReply() {
        long deadline = System.currentTimeMillis() + TIMEOUT;

        long now = System.currentTimeMillis();
        while (waitingForReply && now < deadline) {
            try {
                wait(deadline - now);
            } catch (InterruptedException e) {
                // IGNORE
            }
            now = System.currentTimeMillis();
        }
    }

    synchronized void replyReceived() {
        logger.debug("reply received from " + this);
        waitingForReply = false;
        active = true;
        notifyAll();
    }
    
    public synchronized boolean isActive() {
    	return active;
    }

    public void run() {
    	if (!isDone()) {
        	//send fake request, to trigger a reply
    		sendRequest(null);
    	}
        while (!isDone()) {
            if (isEnabled()) {
                logger.debug("Getting image to send to " + address);
                Image frame = client.getProcessImage();
                logger.debug("Got image to send to " + address);
                boolean success = true;
                
                if (frame == null) {
                    success = false;
                } else {
                    logger.debug("Sending request to " + address);
                    success = sendRequest(frame);
                }
                if (success) {
                    logger.debug("Wating for reply from " + address);
                    waitForReply();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // IGNORE
                    }
                    
                }
                logger.debug("Request/Reply cycle to " + address + " done");
            } else {
                waitUntilEnabled();
            }
        }
    }

    public String toString() {
        return getName();
    }
}
