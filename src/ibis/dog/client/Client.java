package ibis.dog.client;

import ibis.dog.Communication;
import ibis.dog.FeatureVector;
import ibis.dog.Upcall;
import ibis.dog.database.Database;
import ibis.dog.database.DatabaseReply;
import ibis.dog.database.DatabaseRequest;
import ibis.dog.database.Item;
import ibis.dog.server.ServerReply;
import ibis.imaging4j.Image;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisIdentifier;
import ibis.util.ThreadPool;
import ibis.video4j.VideoConsumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client implements Upcall, VideoConsumer, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static final int DEFAULT_TIMEOUT = 5000;

    public static final int HISTORY_SIZE = 10; // 10 seconds
    public static final int HISTORY_INTERVAL = 1000; // 1 second

    // Communication object.
    private final Communication communication;

    private final MessageListener messageListener;

    private final ServerListener serverListener;

    private final StatisticsListener[] statisticsListeners;

    private final Map<IbisIdentifier, ServerHandler> servers;

    // current image
    private Image input = null;
    private long inputSeqNr = 0;
    private long lastDisplayedFrame = 0;
    private long lastProcessedFrame = 0;

    private Item currentResult = null;

    private FeatureVector vector = null;

    private boolean done = false;

    // statistics for frame rate

    private final int[] inputFrameCountHistory;
    private final int[] displayedFrameCountHistory;
    private final int[] processedFrameCountHistory;
    private int validHistorySize;
    private int currentHistoryIndex;
    
    private Database database;

    public Client(MessageListener messageListener,
            ServerListener serverListener, boolean includeDatabase,
            StatisticsListener... statisticListeners)
            throws IbisCreationFailedException, IOException {
        this.messageListener = messageListener;
        this.serverListener = serverListener;
        this.statisticsListeners = statisticListeners;
        logger.debug("Initializing client");
        servers = new HashMap<IbisIdentifier, ServerHandler>();

        communication = new Communication(Communication.CLIENT_ROLE, this);

        inputFrameCountHistory = new int[HISTORY_SIZE];
        displayedFrameCountHistory = new int[HISTORY_SIZE];
        processedFrameCountHistory = new int[HISTORY_SIZE];
        validHistorySize = 0;
        currentHistoryIndex = 0;

        communication.start();

        ThreadPool.createNew(this, "statistics update");

        if(includeDatabase) {
        	database = new Database();
        }
        
        
        logger.info("Done initializing client");
    }

    public void log(String message) {
        if (messageListener != null) {
            messageListener.message(message);
        }
        logger.debug(message);
    }

    @Override
    public void gotImage(Image image) {
        if (logger.isTraceEnabled()) {
            logger.trace("got Image, format = " + image.getFormat()
                    + " width = " + image.getWidth() + ", height = "
                    + image.getHeight() + ", size = " + image.getSize());
        }

        Image copy;
        // copy image
        try {
            copy = Image.copy(image, null);
        } catch (Exception e) {
            logger.error("could not copy image", e);
            return;
        }

        synchronized (this) {
            this.input = copy;
            inputFrameCountHistory[currentHistoryIndex]++;
            inputSeqNr++;
            notifyAll();
        }
    }

    public synchronized Image getDisplayImage() {
        while (lastDisplayedFrame == inputSeqNr || input == null) {
            if (done) {
                return null;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
        lastDisplayedFrame = inputSeqNr;
        displayedFrameCountHistory[currentHistoryIndex]++;

        return input;
    }

    public synchronized Image getProcessImage() {
        while (lastProcessedFrame == inputSeqNr || input == null) {
            if (done) {
                return null;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                // IGNORE
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

        DatabaseRequest request = new DatabaseRequest(
                DatabaseRequest.Function.LEARN, 0, item, 0, null, vector,
                communication.getIdentifier());

        // send item to broker

        IbisIdentifier database = communication.getDatabase();

        if (database == null) {
            logger.error("database location unknown (not in pool)");
            return false;
        }

        try {
            communication.send(database, request);
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
                processedFrameCountHistory[currentHistoryIndex]++;
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
                        .getServer(), reply.getResult(), communication
                        .getIdentifier());

        IbisIdentifier database = communication.getDatabase();

        if (database == null) {
            IbisIdentifier server = reply.getServer();
            String serverName = server.location().getLevel(
                    server.location().numberOfLevels() - 1);
            log("Database not found while processing result from " + serverName);
        } else {
            try {
                communication.send(database, request);
            } catch (IOException e) {
                logger.error("could not send request to database: " + e);
            }
        }

    }

    private void processDatabaseReply(DatabaseReply reply) {
        IbisIdentifier server = reply.getServer();
        String serverName = server.location().getLevel(
                server.location().numberOfLevels() - 1);

        if (reply.getResults().isEmpty()) {
            log(serverName + " does not recognize this object");
            return;
        }
        Double key = reply.getResults().firstKey();
        synchronized (this) {
            this.currentResult = reply.getResults().get(key);
        }

        log(serverName + " says this is a " + currentResult.getName());
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
    public void newServer(IbisIdentifier identifier) {
        ServerHandler handler;
        synchronized (this) {
            logger.debug("new server: " + identifier);

            handler = new ServerHandler(identifier, this, communication);

            servers.put(identifier, handler);
        }
        serverListener.newServer(handler);
    }

    @Override
    public void serverGone(IbisIdentifier identifier) {
        ServerHandler handler;
        synchronized (this) {
            logger.debug("server gone: " + identifier);

            handler = servers.remove(identifier);

            if (handler != null) {
                handler.end();
            }
        }
        if (handler != null) {
            serverListener.serverGone(handler);
        }
    }

    public synchronized void end() {
        done = true;
        for (ServerHandler handler : servers.values()) {
            handler.end();
        }
        servers.clear();

        communication.end();
        
        if (database != null) {
        	database.end();
        }
    }

    private synchronized boolean isDone() {
        return done;
    }

    public synchronized ServerHandler[] getServers() {
        return servers.values().toArray(new ServerHandler[0]);
    }

    /**
     * Updates statistics.
     */
    @Override
    public void run() {
        while (!isDone()) {
            double inputFps;
            double displayedFps;
            double processedFps;

            synchronized (this) {

                double inputTotal = 0;
                double displayedTotal = 0;
                double processedTotal = 0;
                double count = 0;

                for (int i = 0; i < validHistorySize; i++) {
                    inputTotal += inputFrameCountHistory[i];
                    displayedTotal += displayedFrameCountHistory[i];
                    processedTotal += processedFrameCountHistory[i];
                    count++;
                }

                // logger.debug("count = " + count + ", inputTotal = "
                // + inputTotal);

                if (count == 0) {
                    inputFps = 0;
                    displayedFps = 0;
                    processedFps = 0;
                } else {
                    inputFps = (inputTotal / (count * HISTORY_INTERVAL)) * 1000;
                    displayedFps = (displayedTotal / (count * HISTORY_INTERVAL)) * 1000;
                    processedFps = (processedTotal / (count * HISTORY_INTERVAL)) * 1000;
                }

                if (validHistorySize < HISTORY_SIZE) {
                    validHistorySize++;
                }
                currentHistoryIndex = (currentHistoryIndex + 1) % HISTORY_SIZE;
                inputFrameCountHistory[currentHistoryIndex] = 0;
                displayedFrameCountHistory[currentHistoryIndex] = 0;
                processedFrameCountHistory[currentHistoryIndex] = 0;
            }

            for (StatisticsListener listener : statisticsListeners) {
                listener.newStatistics(inputFps, displayedFps, processedFps);
            }

            try {
                Thread.sleep(HISTORY_INTERVAL);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

}
