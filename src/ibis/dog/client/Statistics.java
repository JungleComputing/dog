package ibis.dog.client;

import java.rmi.RemoteException;

import ibis.util.ThreadPool;

public class Statistics implements Runnable {

    public static final int HISTORY_SIZE = 10; // 10 seconds
    public static final int HISTORY_INTERVAL = 1000; // 1 second

    private final StatisticsListener[] statisticsListeners;
    private final Client client;

    // statistics for frame rate

    private final int[] inputFrameCountHistory;
    private final int[] displayedFrameCountHistory;
    private final int[] processedFrameCountHistory;
    private int validHistorySize;
    private int currentHistoryIndex;

    Statistics(StatisticsListener[] statisticListeners, Client client) {
        this.statisticsListeners = statisticListeners;
        this.client = client;

        inputFrameCountHistory = new int[HISTORY_SIZE];
        displayedFrameCountHistory = new int[HISTORY_SIZE];
        processedFrameCountHistory = new int[HISTORY_SIZE];
        validHistorySize = 0;
        currentHistoryIndex = 0;

        ThreadPool.createNew(this, "voter");
    }

    public void gotFrame() {
        inputFrameCountHistory[currentHistoryIndex]++;
    }

    public void displayedFrame() {
        displayedFrameCountHistory[currentHistoryIndex]++;
    }

    public void processedFrame() {
        processedFrameCountHistory[currentHistoryIndex]++;
    }

    /**
     * Updates statistics.
     */
    @Override
    public void run() {
        while (!client.isDone()) {
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

            int databaseSize = 0;
            try {
                databaseSize = client.getDatabaseSize();
            } catch (RemoteException e) {
                // IGNORE
            }

            for (StatisticsListener listener : statisticsListeners) {
                listener.newStatistics(inputFps, displayedFps, processedFps,
                        databaseSize);
            }

            try {
                Thread.sleep(HISTORY_INTERVAL);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

}
