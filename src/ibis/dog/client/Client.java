package ibis.dog.client;

import ibis.dog.database.DatabaseReply;
import ibis.dog.database.DatabaseRequest;
import ibis.dog.database.Item;
import ibis.dog.server.ServerReply;
import ibis.dog.shared.Communication;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.Upcall;
import ibis.imaging4j.Image;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisIdentifier;
import ibis.video4j.VideoConsumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client implements Upcall, VideoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static final int DEFAULT_TIMEOUT = 5000;

    // Communication object.
    private final Communication communication;

    private final MessageListener messageListener;

    private final Map<IbisIdentifier, ServerHandler> servers;

    // current image
    private Image input = null;
    private long inputSeqNr = 0;

    private Item currentResult = null;

    private FeatureVector vector = null;
    
    private boolean done = false;

    // statistics for frame rate

    private long inputFrameStart;
    private long inputFrameCount;

    private long displayedFrameStart;
    private long displayedFrameCount;
    private long lastDisplayedFrame;

    private long processedFrameStart;
    private long processedFrameCount;
    private long lastProcessedFrame;

    public Client(MessageListener listener) throws IbisCreationFailedException,
            IOException {
        this.messageListener = listener;
        logger.debug("Initializing client");

        communication = new Communication(Communication.CLIENT_ROLE, this);

        servers = new HashMap<IbisIdentifier, ServerHandler>();

        logger.debug("Done initializing client");

        // initialize fps
        inputFPS();
        displayedFPS();
        processedFPS();
        
        lastDisplayedFrame = -1;
        lastProcessedFrame = -1;
    }

    public void log(String message) {
        if (messageListener != null) {
            messageListener.message(message);
        }
        logger.info(message);
    }
    
    public synchronized double inputFPS() {
        long now = System.currentTimeMillis();

        double result;
        if (inputFrameCount == 0 || inputFrameStart >= now) {
            result = 0;
        } else {
            result = (double) inputFrameCount * 1000.0
                    / (double) (now - inputFrameStart);
        }

        inputFrameCount = 0;
        inputFrameStart = now;

        return result;
    }

    public synchronized double displayedFPS() {
        long now = System.currentTimeMillis();

        double result;
        if (displayedFrameCount == 0 || displayedFrameStart >= now) {
            result = 0;
        } else {
            result = (double) displayedFrameCount * 1000.0
                    / (double) (now - displayedFrameStart);
        }

        displayedFrameCount = 0;
        displayedFrameStart = now;

        return result;
    }

    public synchronized double processedFPS() {
        long now = System.currentTimeMillis();

        double result;
        if (processedFrameCount == 0 || processedFrameStart >= now) {
            result = 0;
        } else {
            result = (double) processedFrameCount * 1000.0
                    / (double) (now - processedFrameStart);
        }

        processedFrameCount = 0;
        processedFrameStart = now;

        return result;
    }

    @Override
    public synchronized void gotImage(Image image) {
        if(logger.isDebugEnabled()) {
            logger.debug("got Image, format = " + image.getFormat() + " width = " + image.getWidth() + ", height = " + image.getHeight() + ", size = " + image.getSize());
        }

        //copy image
        this.input = new Image(image);
        
        inputFrameCount++;
        inputSeqNr++;
        notifyAll();
    }

    public synchronized Image getDisplayImage() {
        while(lastDisplayedFrame == inputSeqNr) {
            if (done) {
                return null;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                //IGNORE
            }
        }
        lastDisplayedFrame = inputSeqNr;
        return input;
    }
    
    public synchronized Image getProcessImage() {
        while(lastProcessedFrame == inputSeqNr) {
            if (done) {
                return null;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                //IGNORE
            }
        }
        lastProcessedFrame = inputSeqNr;
        return input;
    }

    public boolean learn(String name) {
        FeatureVector vector = getFeatureVector();

        if (vector == null) {
            return false;
        }

        // create new database item
        Item item = new Item(vector, name, System.getProperty("user.name"),
                null);

        // send item to broker

        IbisIdentifier database = communication.getDatabase();

        if (database == null) {
            logger.error("database location unknown (not in pool)");
            return false;
        }

        try {
            communication.send(database, item);
        } catch (Exception e) {
            logger.error("Could not send item to database", e);
            return false;
        }

        return true;
    }

    public synchronized String recognize() {
        if (currentResult == null) {
            return null;
        }

        return currentResult.getName();
    }

    private synchronized FeatureVector getFeatureVector() {
        return vector;
    }

    private void processServerReply(ServerReply reply) {
        logger.debug("Got reply from " + reply.getServer());

        ServerHandler handler = null;

        synchronized (this) {
            handler = servers.get(reply.getServer());

            // set vector as current vector
            if (reply.getResult() != null) {
                this.vector = reply.getResult();
                processedFrameCount++;
            }
        }

        // tell handler it can send a new request now
        if (handler != null) {
            handler.replyReceived();
        }

        if (reply.getException() != null) {
            logger.error("Error received from server", reply.getException());
            return;
        }

        // send vector to database for lookup
        DatabaseRequest request = new DatabaseRequest(
                DatabaseRequest.Function.RECOGNIZE, 1, null, 0, reply
                        .getResult(), communication.getIdentifier());

        IbisIdentifier database = communication.getDatabase();

        if (database == null) {
            logger.error("Could not locate database");
        } else {
            try {
                communication.send(database, request);
            } catch (IOException e) {
                logger.error("could not send request to database: " + e);
            }
        }

    }

    private synchronized void processDatabaseReply(DatabaseReply reply) {
        if (reply.getResults().isEmpty()) {
            log(reply.getServer().location().toString()
                    + " does not recognize this object");
            return;
        }
        Double key = reply.getResults().firstKey();
        this.currentResult = reply.getResults().get(key);

        log(reply.getServer().location().toString() + " says this is a "
                + currentResult);

    }

    @Override
    public void gotMessage(Object object) {
        if (object instanceof ServerReply) {
            processServerReply((ServerReply) object);
        } else if (object instanceof DatabaseReply) {
            processDatabaseReply((DatabaseReply) object);
        } else {
            logger.error("unknown message: " + object);
        }
    }

    @Override
    public synchronized void newServer(IbisIdentifier identifier) {
        logger.debug("new server: " + identifier);

        ServerHandler handler = new ServerHandler(identifier, this,
                communication);

        servers.put(identifier, handler);
    }

    @Override
    public synchronized void serverGone(IbisIdentifier identifier) {
        logger.debug("server gone: " + identifier);

        ServerHandler handler = servers.remove(identifier);

        if (handler != null) {
            handler.end();
        }
    }

    public synchronized void end() {
        done = true;
        for (ServerHandler handler : servers.values()) {
            handler.end();
        }
        servers.clear();

        communication.end();
    }

    public synchronized ServerHandler[] getServers() {
        return servers.values().toArray(new ServerHandler[0]);
    }

}
