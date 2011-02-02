package ibis.dog.client;

import ibis.dog.database.Item;

public class Voter {
	
	private Item latestResult;
	private long latestTimestamp;
	
	Voter() {
		latestResult = null;
		latestTimestamp = 0;
	}

    public synchronized void addResult(Item result, long timestamp) {
    	if (timestamp > latestTimestamp) {
    		latestResult = result;
    	}
    }

    public synchronized void startVote() {
        // TODO Auto-generated method stub
        
    }

	public Item getLatestResult() {
		return latestResult;
	}

}
