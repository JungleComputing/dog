package ibis.dog.database;

import ibis.dog.Communication;
import ibis.dog.FeatureVector;
import ibis.dog.Upcall;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisIdentifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database implements Upcall {

    private static final Logger logger = LoggerFactory
            .getLogger(Database.class);

    public static final int SAVE_INTERVAL = 10000;

    public static final int NR_INVARS = 6;

    public static final int NR_RFIELDS = 37;

    public static final File FILE = new File("dog.database");

    public static final File OLD_FILE = new File("dog.database.old");

    private static double weibullDiff(double gam1, double bet1, double gam2,
            double bet2) {
        double gam = 1.0 - (gam1 > gam2 ? gam2 / gam1 : gam1 / gam2);
        double bet = 1.0 - (bet1 > bet2 ? bet2 / bet1 : bet1 / bet2);
        return gam * bet;
    }

    private static double getScore(FeatureVector one, FeatureVector other) {
        int count1 = NR_RFIELDS;
        int count2 = NR_RFIELDS;

        double sErrorTreshold = 1.0;

        double[] dist = new double[count1 * count2];

        for (int i = 0; i < count1; i++) {
            for (int j = 0; j < count2; j++) {
                double score = 0;
                for (int k = 0; k < NR_INVARS; k++) {
                    score += weibullDiff(one.vector[i * 2 * NR_INVARS + k * 2],
                            one.vector[i * 2 * NR_INVARS + k * 2 + 1],
                            other.vector[j * 2 * NR_INVARS + k * 2],
                            other.vector[j * 2 * NR_INVARS + k * 2 + 1]);
                }
                dist[i * count2 + j] = score / NR_INVARS;
            }
        }

        double score = 1.0;
        for (int i = 0; i < count1; i++) {
            double mindist = sErrorTreshold;
            for (int j = 0; j < count2; j++) {
                double d = dist[i * count2 + j];
                if (d < mindist) {
                    mindist = d;
                }
            }
            score *= sErrorTreshold - mindist;
        }

        double revscore = 1.0;
        for (int j = 0; j < count2; j++) {
            double mindist = sErrorTreshold;
            for (int i = 0; i < count1; i++) {
                double d = dist[i * count2 + j];
                if (d < mindist) {
                    mindist = d;
                }
            }
            revscore *= sErrorTreshold - mindist;
        }
        return (score < revscore) ? (1.0 - score) : (1.0 - revscore);
    }

    private final Communication communication;

    private ArrayList<Item> items;

    private boolean ended = false;

    @SuppressWarnings("unchecked")
    private Database() throws IbisCreationFailedException, IOException {

        // Create an Communication object
        communication = new Communication(Communication.DATABASE_ROLE, this);

        if (!FILE.exists()) {
            logger.warn("Database file \"" + FILE + "\" does not exist");
        } else {
            // load database file
            try {
                FileInputStream in = new FileInputStream(FILE);
                ObjectInputStream objectIn = new ObjectInputStream(in);
                items = (ArrayList<Item>) objectIn.readObject();
                objectIn.close();
                logger.info("loaded database from \"" + FILE + "\"");
                return;
            } catch (Exception e) {
                logger.warn("failed to load objects from file: \"" + FILE
                        + "\"", e);
            }
        }

        // try backup file
        if (items == null) {
            if (!OLD_FILE.exists()) {
                logger.warn("Database file \"" + OLD_FILE
                                + "\" does not exist");
            } else {
                try {
                    FileInputStream in = new FileInputStream(OLD_FILE);
                    ObjectInputStream objectIn = new ObjectInputStream(in);
                    items = (ArrayList<Item>) objectIn.readObject();
                    objectIn.close();
                    logger.info("loaded database from \"" + OLD_FILE + "\"");
                    return;
                } catch (Exception e) {
                    logger.warn("failed to load objects from old file: \""
                            + FILE + "\"", e);
                }
            }
        }

        // just create a new database
        if (items == null) {
            items = new ArrayList<Item>();
            logger.info("created new (empty) database, will save in \"" + FILE
                    + "\"");
        }
        
        communication.start();
        logger.info("Database initialized");
    }

    private synchronized int getSize() {
        return items.size();
    }

    private synchronized void save() {
        if (FILE.exists()) {
            FILE.renameTo(OLD_FILE);
        }

        try {
            FileOutputStream out = new FileOutputStream(FILE);
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(items);
            objectOut.close();
            return;
        } catch (Exception e) {
            logger.warn("failed to write objects to file: " + FILE, e);
        }
    }

    private synchronized TreeMap<Double, Item> recognize(FeatureVector vector,
            int nrOfResults) {
        if (nrOfResults == 0) {
            return new TreeMap<Double, Item>();
        }

        TreeMap<Double, Item> results = new TreeMap<Double, Item>();

        // sort database according to score, limit to result size
        for (Item item : items) {
            double score = getScore(vector, item.getVector());

            while (results.get(score) != null) {
                // add a small offset to the score to make it unique
                // just in case two scores are identical.
                score = score + Double.MIN_VALUE;
            }

            results.put(score, item);

            // purge "worst" result if result set is too big
            if (results.size() > nrOfResults) {
                results.remove(results.lastKey());
            }
        }

        // return best results.
        return results;
    }

    private void end() {
        synchronized (this) {
            ended = true;
        }
        logger.info("ending database");
        save();
        communication.end();
    }

    private synchronized boolean hasEnded() {
        return ended;
    }

    private synchronized boolean learn(Item item) {

        double[] weibulls = item.getVector().vector;

        if (weibulls == null) {
            return false;
        }

        items.add(item);

        return true;
    }

    @Override
    public void newServer(IbisIdentifier identifier) {
        // IGNORE
    }

    @Override
    public void serverGone(IbisIdentifier identifier) {
        // IGNORE
    }

    @Override
    public void gotMessage(Object object) {
        if (!(object instanceof DatabaseRequest)) {
            logger.error("Database received an unknown object in message: "
                    + object);
            return;
        }

        DatabaseRequest request = (DatabaseRequest) object;

        switch (request.getFunction()) {

        case LEARN:
            logger.debug("got a Request to add something to database");
            learn(request.getItem());
            break;
        case RECOGNIZE:
            logger.debug("got a Recognize Request");

            SortedMap<Double, Item> results = recognize(request.getVector(),
                    request.getNrOfResults());

            DatabaseReply reply = new DatabaseReply(request.getServer(), request.getSequenceNumber(), results);

            try {
                communication.send(request.getReplyAddress(), reply);
            } catch (IOException e) {
                logger.error("Could not send reply", e);
            }
            break;
        default:
            logger.error("Received unknown request function: "
                    + request.getFunction());
        }
    }

    private static final class ShutDown extends Thread {
        final Database database;

        ShutDown(Database database) {
            this.database = database;
        }

        public void run() {
            database.end();
        }
    }

    public static void main(String[] args) {
        try {
            Database database = new Database();

            // Install a shutdown hook that terminates Ibis.
            Runtime.getRuntime().addShutdownHook(new ShutDown(database));

            while (!database.hasEnded()) {
                Thread.sleep(SAVE_INTERVAL);
                logger.info("Database now contains " + database.getSize()
                        + " items");
                database.save();
            }
        } catch (Throwable e) {
            System.err.println("database died unexpectedly!");
            e.printStackTrace(System.err);
        }
    }

}
