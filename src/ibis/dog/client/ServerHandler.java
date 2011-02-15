package ibis.dog.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.dog.Communication;
import ibis.dog.FeatureVector;
import ibis.dog.database.DatabaseInterface;
import ibis.dog.database.Item;
import ibis.dog.server.ServerInterface;
import ibis.media.imaging.Image;
import ibis.ipl.Ibis;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.util.rpc.RPC;
import ibis.util.ThreadPool;

public class ServerHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    public final IbisIdentifier address;

    private final Client client;

    private final DatabaseInterface database;
    private final ServerInterface server;

    private boolean enabled = false;
    private boolean done = false;

    // server is active if last calculation was succesful
    private boolean active = false;

    ServerHandler(IbisIdentifier address, Client client, Ibis ibis,
            DatabaseInterface database) {
        this.address = address;
        this.client = client;
        this.database = database;

        logger.debug("new Server handler: " + this);

        // create proxy to connect to server
        server = RPC.createProxy(ServerInterface.class, address,
                Communication.SERVER_ROLE, ibis);

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
        return address.location().getLevel(
                address.location().numberOfLevels() - 1);
    }

    private synchronized boolean isDone() {
        return done;
    }

    public synchronized void end() {
        this.done = true;
        notifyAll();
    }

    public synchronized boolean isActive() {
        return active;
    }

    private void setActive(boolean active) {
        boolean changed;
        synchronized (this) {
            changed = (active != this.active);
            this.active = active;
            notifyAll();
        }
        if (changed) {
            client.serverStateChanged();
        }

    }

    public void run() {
        if (!isDone()) {
            try {
                server.waitUntilInitialized();
                setActive(true);
            } catch (Exception e) {
                // IGNORE
            }
        }
        while (!isDone()) {
            if (isEnabled()) {
                logger.debug("Getting image to send to " + address);
                Image frame = client.getProcessImage();
                long timestamp = System.currentTimeMillis();
                logger.debug("Got image to send to " + address);

                try {
                    // FIXME:compress images if needed (doesn't work in
                    // Imaging4J at the moment)
                    // if (!frame.getFormat().isCompressed()) {
                    // Image converted;
                    // if (frame.getFormat() == Format.RGB24) {
                    // converted = frame;
                    // } else {
                    // converted = Imaging4j.convert(frame, Format.RGB24);
                    // }
                    // frame = Imaging4j.convert(converted, Format.MJPG);
                    // }

                    FeatureVector vector = server.calculateVector(frame);

                    Item item = database.recognize(vector);

                    client.newResult(address, timestamp, vector, item);

                    setActive(true);
                } catch (Exception e) {
                    logger.error("Could not process frame", e);
                    setActive(false);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        // IGNORE
                    }
                }
            } else {
                waitUntilEnabled();
            }
        }
    }

    public String toString() {
        return getName();
    }
}
