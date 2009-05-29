package ibis.dog.client;

public interface StatisticsListener {
    
    public void newStatistics(double inputFps, double displayedFps, double processedFps);

}
