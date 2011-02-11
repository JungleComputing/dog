package ibis.dog.test;

import ibis.dog.FeatureVector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectRecognition {
	
	private static Logger logger = LoggerFactory.getLogger(ObjectRecognition.class);
    
    // Should be dynamic ?
    public static final int NR_INVARS = 6;
    public static final int NR_RFIELDS = 37;

    public static final String OBJFILE = "ObjRec.out";

    private double weibullDiff(double gam1, double bet1, double gam2,
            double bet2) {
        double gam = 1.0 - (gam1 > gam2 ? gam2 / gam1 : gam1 / gam2);
        double bet = 1.0 - (bet1 > bet2 ? bet2 / bet1 : bet1 / bet2);
        return gam * bet;
    }

    /* Not used ??
    private double scoreOnePoint(double[] data1, double[] data2) {
        //      get score for 1 receptive field over all invariants

        double score = 0;
        for (int i = 0; i < NR_INVARS; i++) {
            score += weibullDiff(data1[i * 2], data1[i * 2 + 1], data2[i * 2],
                    data2[i * 2 + 1]);
        }
        return score / NR_INVARS;
    }*/

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

    /* Not used ??
    private double[] getWeibulls() {
        //      TODO: implement
        return null;
    } */

    public synchronized String recognize(FeatureVector vector) {
        String result = null;
        double[] weibulls = vector.getVector();

        if (weibulls == null) {
            return null;
        }
        
        try {
            // FIXME: we open a file on every recognize! FIXME!
            FileInputStream inFile = new FileInputStream(OBJFILE);
            InputStreamReader in = new InputStreamReader(inFile);
            BufferedReader buf = new BufferedReader(in);

            int MAX_OBJ = 100;
            String[] objNames = new String[MAX_OBJ];
            double[] scores = new double[MAX_OBJ];

            int j = 0; 
            int idx = 0;
            
            double minScore = 1000.;

            while ((objNames[j] = buf.readLine()) != null) {
                double[] oldWeibs = new double[2 * NR_INVARS * NR_RFIELDS];

                for (int i = 0; i < oldWeibs.length; i++) {
                    oldWeibs[i] = (Double.valueOf(buf.readLine()).doubleValue());
                }
                scores[j] = getScore(weibulls, oldWeibs);

                if (scores[j] < minScore) {
                    minScore = scores[j];
                    idx = j;
                }
                j += 1;
            }

            result = objNames[idx];

            buf.close();
            in.close();
            inFile.close();

        } catch (IOException ioe) {
            logger.debug("unable to recognize object", ioe);
        }
        return result;
    }

    public synchronized boolean learn(String name, FeatureVector vector) {
       
        double[] weibulls = vector.getVector();

        if (weibulls == null) {
            return false;
        }

        // Write learned object parameters to file...

        try {
            FileOutputStream outFile = new FileOutputStream(OBJFILE, true);
            OutputStreamWriter out = new OutputStreamWriter(outFile, "US-ASCII");
            out.write(name + "\n");

            for (int i = 0; i < weibulls.length; i++) {
                out.write(String.valueOf(weibulls[i]) + "\n");
            }
            out.close();
            outFile.close();
        } catch (IOException ioe) {
            System.err.println("IOException thrown");
            return false;
        }
        return true;
    }
}
