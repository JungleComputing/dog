package ibis.dog.client;

import ibis.dog.database.Item;
import ibis.ipl.IbisIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Voter {
    
    public static class RecognizeResult {
        private final Item item;
        private final double confidence;
        
        RecognizeResult(Item item, double confidence) {
            this.item = item;
            this.confidence = confidence;
        }
        
        public Item getItem() {
            return item;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
    }
    
    // number of frames to use for voting in recognize
    public static final int RESULT_SET_SIZE = 10;

    private long startTime;
    private ArrayList<Item> votes;
    private ArrayList<String> sources;
    
    Voter() {
        votes = new ArrayList<Item>();
        sources = new ArrayList<String>();

        startTime = 0;
    }

    private synchronized RecognizeResult determineResult() {
        Map<Item, Integer> resultMap = new HashMap<Item, Integer>();

        // map of results
        for (Item vote : votes) {
            int count = 0;
         
            if (resultMap.containsKey(vote)) {
                count = resultMap.get(vote);
            }

            count++;

            resultMap.put(vote, count);
        }

        int maxCount = 0;
        Item bestResult = null;

        for (Map.Entry<Item, Integer> entry : resultMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestResult = entry.getKey();
            }
        }

        System.err.println("Result of vote is " + bestResult + " with "
                + maxCount + " votes");

        return new RecognizeResult(bestResult, (double) maxCount / RESULT_SET_SIZE);
    }

    public synchronized void newResult(IbisIdentifier server, long timestamp, Item item) {
        if (item == null) {
            return;
        }
      
        // vote already done
        if (votes.size() >= RESULT_SET_SIZE) {
            return;
        }

        // result too old
        if (timestamp < startTime) {
            return;
        }
        
        String serverName = server.location().getLevel(
                server.location().numberOfLevels() - 1);
 
        // add item to vote
        votes.add(item);
        sources.add(serverName);

        notifyAll();
    }
    
    public synchronized RecognizeResult recognize(ProgressListener listener, Client client) throws Exception {
        //clear votes, start new vote
        votes.clear();
        sources.clear();
        startTime = System.currentTimeMillis();
        int reported = 0;
       
        //wait for results to come in
        
        while(votes.size() < RESULT_SET_SIZE) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                //IGNORE
            }
            if (!listener.progress((double) votes.size() / RESULT_SET_SIZE)) {
                //listener reports it is not interested in result any longer
                return null;
            }
            //WARNING: potential deadlock
            if (client.processingServerCount() == 0) {
                throw new Exception("No active servers to process frames");
            }
            while(reported < votes.size()) {
                String source = sources.get(reported);
                Item vote = votes.get(reported);
                if (vote != null) {
                    listener.message(source + " says this is a " + vote.getName());
                } else {
                    listener.message(source + " does not know what this is");
                }
                reported++;
            }
        }
        
        return determineResult();
    }
}
