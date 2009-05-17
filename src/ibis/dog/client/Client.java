package ibis.dog.client;

import ibis.dog.database.DatabaseReply;
import ibis.dog.database.DatabaseRequest;
import ibis.dog.database.Item;
import ibis.dog.gui.FramerateConsumer;
import ibis.dog.gui.OutputPanel;
import ibis.dog.server.ServerReply;
import ibis.dog.server.ServerRequest;
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
import java.util.SortedMap;

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
    private Image image = null;

    private boolean done = false;

    private Item currentResult = null;

    private FeatureVector vector = null;

    // statistics for framerate

    private long processedFrameStart;
    private long processedFrameCount;

    public Client(MessageListener listener) throws IbisCreationFailedException,
            IOException {
        this.messageListener = listener;
        logger.debug("Initializing client");

        communication = new Communication("Client", this);

        servers = new HashMap<IbisIdentifier, ServerHandler>();

        logger.debug("Done initializing client");

        processedFrameStart = System.currentTimeMillis();
        processedFrameCount = 0;
    }

    @Override
    public synchronized void gotImage(Image image) {
        this.image = new Image(image.getFormat(), image.getWidth(), image.getHeight());
        try {
            Image.copy(image, this.image);
        } catch (Exception e) {
            logger.error("could not copy image", e);
            this.image = null;
        }
    }
    
    public synchronized Image getLastImage() {
        return this.image;
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
            this.vector = reply.getResult();
            processedFrameCount++;
        }

        // tell handler it can send a new request now
        if (handler != null) {
            handler.replyReceived();
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
            if (messageListener != null) {
                messageListener.message(reply.getServer().location().toString()
                        + " does not recognize this object");
            }
            return;
        }
        Double key = reply.getResults().firstKey();
        this.currentResult = reply.getResults().get(key);

        if (messageListener != null) {
            messageListener.message(reply.getServer().location().toString()
                    + " says this is a " + currentResult);
        }
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
        ServerHandler handler = new ServerHandler(identifier, this, communication);

        servers.put(identifier, handler);
    }

    @Override
    public synchronized void serverGone(IbisIdentifier identifier) {
        ServerHandler handler = servers.remove(identifier);
        
        if (handler != null) {
            handler.end();
        }
    }

    public synchronized void end() {
        for(ServerHandler handler: servers.values()) {
            handler.end();
        }
        servers.clear();
        
        communication.end();
    }

}
