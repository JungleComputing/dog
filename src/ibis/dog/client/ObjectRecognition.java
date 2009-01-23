package ibis.dog.client;

import ibis.dog.gui.LearnedObjects.LearnedObject;
import ibis.dog.shared.FeatureVector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import jorus.weibull.CxWeibull;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.database.Cursor;

public class ObjectRecognition {

    private static Logger logger = Logger.getLogger(ObjectRecognition.class);

    // Should be dynamic ?
    public static final int NR_INVARS = CxWeibull.getNrInvars();

    public static final int NR_RFIELDS = CxWeibull.getNrRfields();

    private double weibullDiff(double gam1, double bet1, double gam2,
            double bet2) {
        double gam = 1.0 - (gam1 > gam2 ? gam2 / gam1 : gam1 / gam2);
        double bet = 1.0 - (bet1 > bet2 ? bet2 / bet1 : bet1 / bet2);
        return gam * bet;
    }

    /*
     * Not used ?? private double scoreOnePoint(double[] data1, double[] data2) { //
     * get score for 1 receptive field over all invariants
     * 
     * double score = 0; for (int i = 0; i < NR_INVARS; i++) { score +=
     * weibullDiff(data1[i * 2], data1[i * 2 + 1], data2[i * 2], data2[i * 2 +
     * 1]); } return score / NR_INVARS; }
     */

    double getScore(double[] params, double[] matchparams) {

        int count1 = NR_RFIELDS;
        int count2 = NR_RFIELDS;

        double sErrorTreshold = 1.0;

        double[] dist = new double[count1 * count2];

        for (int i = 0; i < count1; i++) {
            for (int j = 0; j < count2; j++) {
                double score = 0;
                for (int k = 0; k < NR_INVARS; k++) {
                    score += weibullDiff(params[i * 2 * NR_INVARS + k * 2],
                            params[i * 2 * NR_INVARS + k * 2 + 1],
                            matchparams[j * 2 * NR_INVARS + k * 2],
                            matchparams[j * 2 * NR_INVARS + k * 2 + 1]);
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

    /*
     * Not used ?? private double[] getWeibulls() { // TODO: implement return
     * null; }
     */

    public synchronized Cursor recognize(FeatureVector source, Activity activity)
            throws Exception {
        long start = System.currentTimeMillis();
        String result = null;
        double[] weibulls = source.vector;

        if (weibulls == null) {
            throw new Exception("invalid Feature Vector");
        }

        int MAX_OBJ = 100;
        double[] scores = new double[MAX_OBJ];

        int j = 0;
        int idx = 0;

        double minScore = 1000.;

        System.out.println("before recognition.recognize: query");
        Cursor cursor = activity.managedQuery(LearnedObject.CONTENT_URI,
                PROJECTION, null, null, LearnedObject.DEFAULT_SORT_ORDER);

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            int id = cursor.getInt(0); // the id
            try {
                ObjectInputStream in = new ObjectInputStream(activity
                        .getContentResolver().openInputStream(
                                LearnedObject.getFeatureVectorUri(id)));
                Object obj = in.readObject();
                FeatureVector target = (FeatureVector) obj;
                in.close();
                System.out.println("done reading feature vector from file: "
                        + target);
                scores[j] = getScore(weibulls, target.vector);
                System.out.println("score for '"
                        + cursor.getString(cursor
                                .getColumnIndex(LearnedObject.OBJECT_NAME))
                        + "': " + scores[j]);
                if (scores[j] < minScore) {
                    minScore = scores[j];
                    idx = j;
                }
                j += 1;

            } catch (StreamCorruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        cursor.moveToPosition(idx);
        System.out.println("objectrecognition.recognize took: "
                + (System.currentTimeMillis() - start) / 1000.0 + " sec.");
        return cursor;
    }

    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            LearnedObject._ID, // 0
            LearnedObject.OBJECT_NAME, // 1
            LearnedObject.AUTHOR, // 2
    };

}
