package ibis.dog.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Voter {

	public static final int RESULT_SET_SIZE = 10;

	private long startTime;
	private ArrayList<String> votes;
	private ArrayList<String> sources;

	private String result;
	private double confidence;

	Voter() {
		votes = new ArrayList<String>();
		sources = new ArrayList<String>();
		
		result = null;
		confidence = 0.0;
		
		startTime = 0;
	}

	public synchronized void addResult(String vote, String source, long timestamp) {
		// vote already done
		if (votes.size() >= RESULT_SET_SIZE) {
			return;
		}

		// result too old
		if (timestamp < startTime) {
			return;
		}

		// add item to vote
		votes.add(vote);
		sources.add(source);

		// determine result if we now have enough items
		if (votes.size() >= RESULT_SET_SIZE) {
			determineResult();
		}

		notifyAll();
	}

	private synchronized void determineResult() {
		Map<String, Integer> resultMap = new HashMap<String, Integer>();

		// map of results
		for (String vote: votes) {
			int count = 0;

			if (resultMap.containsKey(vote)) {
				count = resultMap.get(vote);
			}

			count++;

			resultMap.put(vote, count);
		}

		int maxCount = 0;
		String bestResult = null;

		for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				bestResult = entry.getKey();
			}
		}
		
		System.err.println("Result of vote is " + bestResult + " with " + maxCount + " votes");
		
		result = bestResult;
		confidence = (double) maxCount / RESULT_SET_SIZE;
	}

	public String waitForVote(int voteNumber) {
		while (votes.size() <= voteNumber) {
			try {
				wait();
			} catch (Exception e) {
				// IGNORE
			}
		}

		return votes.get(voteNumber);
	}

	public synchronized void startVote() {
		votes.clear();
		sources.clear();
		startTime = System.currentTimeMillis();
	}

	public synchronized String getResult() {
		return result;
	}

	public synchronized double getConfidence() {
		return confidence;
	}

	public synchronized String getSourceOfVote(int i) {
		return sources.get(i);
	}


}
