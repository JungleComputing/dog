package ibis.dog.database;

import ibis.dog.Communication;
import ibis.dog.FeatureVector;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.util.rpc.RPC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database implements DatabaseInterface {

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

        double[] oneVector = one.getVector();
        double[] otherVector = other.getVector();

        for (int i = 0; i < count1; i++) {
            for (int j = 0; j < count2; j++) {
                double score = 0;
                for (int k = 0; k < NR_INVARS; k++) {
                    score += weibullDiff(oneVector[i * 2 * NR_INVARS + k * 2],
                            oneVector[i * 2 * NR_INVARS + k * 2 + 1],
                            otherVector[j * 2 * NR_INVARS + k * 2],
                            otherVector[j * 2 * NR_INVARS + k * 2 + 1]);
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

    private final Ibis ibis;

    private ArrayList<Item> items;

    private boolean ended = false;

    @SuppressWarnings("unchecked")
    public Database(boolean distributed) throws IbisCreationFailedException,
            IOException {

        if (distributed) {
            // Create an Communication object

            ibis = Communication.createIbis(Communication.DATABASE_ROLE, null);

            RPC.exportObject(DatabaseInterface.class, this,
                    Communication.DATABASE_ROLE, ibis);
            
            //try to win database election
            IbisIdentifier database = ibis.registry().elect("database");
            
            if (!database.equals(ibis.identifier())) {
                throw new IOException("database already present in pool!");
            }
        } else {
            ibis = null;
        }

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
                logger.warn("Database file \"" + OLD_FILE + "\" does not exist");
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

        logger.info("Database initialized");
    }
    
    @Override
    public synchronized int size() throws RemoteException {
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
    
    @Override
    public synchronized Item recognize(FeatureVector vector) {
        TreeMap<Double, Item> result = recognize(vector,1);
        
        if (result.size() == 0) {
            return null;
        }
        
        Double key = result.firstKey();

        return result.get(key);
    }

    @Override
    public synchronized TreeMap<Double, Item> recognize(FeatureVector vector,
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
                score = score + 0.0001;
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

    public void end() {
        synchronized (this) {
            ended = true;
        }
        logger.info("ending database");
        save();
        if (ibis != null) {
            try {
                ibis.end();
            } catch (Exception e) {
                logger.error("Exception while ending ibis", e);
            }
        }
    }

    private synchronized boolean hasEnded() {
        return ended;
    }

    public synchronized void learn(Item item) {
        //remove existing items with this name
        for(int i = 0; i < items.size();i++) {
            if (items.get(i).getName().equals(item.getName())) {
                items.remove(i);
                //go back one, this index wil contain a different item now
                i--;
            }
        }
        items.add(item);
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
            Database database = new Database(true);

            // Install a shutdown hook that terminates Ibis.
            Runtime.getRuntime().addShutdownHook(new ShutDown(database));

            while (!database.hasEnded()) {
                Thread.sleep(SAVE_INTERVAL);
                logger.info("Database now contains " + database.size()
                        + " items");
                database.save();
            }
        } catch (Throwable e) {
            System.err.println("database died unexpectedly!");
            e.printStackTrace(System.err);
        }
    }

}
