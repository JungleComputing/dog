package ibis.dog.broker;

import ibis.dog.shared.FeatureVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database used to store objects/thumbnail/etc at the broker.
 * 
 * @author ndrost
 * 
 */
public class Database {

    private static Logger logger = LoggerFactory.getLogger(Database.class);

    // Should be dynamic ?
    public static final int NR_INVARS = 6;

    public static final int NR_RFIELDS = 37;

    public static final File FILE = new File("dog.database");

    public static final File OLD_FILE = new File("dog.database.old");

    private ArrayList<Item> items;

    private static double weibullDiff(double gam1, double bet1, double gam2,
            double bet2) {
        double gam = 1.0 - (gam1 > gam2 ? gam2 / gam1 : gam1 / gam2);
        double bet = 1.0 - (bet1 > bet2 ? bet2 / bet1 : bet1 / bet2);
        return gam * bet;
    }

    /*
     * Not used ?? private double scoreOnePoint(double[] data1, double[] data2)
     * { // get score for 1 receptive field over all invariants
     * 
     * double score = 0; for (int i = 0; i < NR_INVARS; i++) { score +=
     * weibullDiff(data1[i 2], data1[i 2 + 1], data2[i 2], data2[i 2 + 1]); }
     * return score / NR_INVARS; }
     */

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
                        other.vector[j * 2 * NR_INVARS + k * 2], other.vector[j
                                * 2 * NR_INVARS + k * 2 + 1]);
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

    @SuppressWarnings("unchecked")
    Database() {
        try {
            FileInputStream in = new FileInputStream(FILE);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            items = (ArrayList<Item>) objectIn.readObject();
            objectIn.close();
            return;
        } catch (Exception e) {
            logger.warn("failed to load objects from file: " + FILE
                    + " trying old file...", e);
        }

        try {
            FileInputStream in = new FileInputStream(OLD_FILE);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            items = (ArrayList<Item>) objectIn.readObject();
            objectIn.close();
            return;
        } catch (Exception e) {
            logger.warn("failed to load objects from file: " + OLD_FILE
                    + " creating new database", e);
        }
        items = new ArrayList<Item>();
    }

    synchronized void save() {
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

    public synchronized Item[] recognize(FeatureVector vector, int nrOfResults) {
        if (nrOfResults == 0) {
            return new Item[0];
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
        return results.values().toArray(new Item[0]);
    }

    public synchronized boolean learn(Item item) {

        double[] weibulls = item.getVector().vector;

        if (weibulls == null) {
            return false;
        }

        items.add(item);

        return true;
    }
}
