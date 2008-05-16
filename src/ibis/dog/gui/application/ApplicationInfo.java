package ibis.dog.gui.application;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ApplicationInfo extends JPanel implements FrameRateConsumer, ServerCountConsumer {

    private static final long serialVersionUID = 1L;

    // The data to plot
    private TimeSeriesCollection[] datasets;

    private CombinedDomainXYPlot plot;
    
    private double currentFrameRate = 0.0;
    
    private int currentServers = 0;
    
    private int activeServers = 0; 
    
    /*
    private double currentThroughput = 0.0;
    
    private double currentLatency = 0.0;
    */
    
    private class Update extends Thread { 
        
        Update() { 
            setDaemon(true);
        }
        
        public void run() { 
            
            while (true) { 
                try { 
                    sleep(1000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                
                datasets[0].getSeries(0).add(new Millisecond(), getFramerate());
             
                datasets[1].getSeries(0).add(new Millisecond(), getServers());
                datasets[1].getSeries(1).add(new Millisecond(), getActiveServers());
            }
        }
        
    }
    
    
    public ApplicationInfo() { 
    
        plot = new CombinedDomainXYPlot(new DateAxis("Time"));
        datasets = new TimeSeriesCollection[2];
        
        datasets[0] = new TimeSeriesCollection();
        datasets[1] = new TimeSeriesCollection();
        
        //createSeries(0, "Framerate", "Frames/sec", 40);
        //createSeries(1, "Servers", "Servers", 10);
        
        TimeSeries series = new TimeSeries("in", Millisecond.class);        
        datasets[0].addSeries(series);
        
        NumberAxis rangeAxis = new NumberAxis("Frames/sec.");
        
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRange(true);
        rangeAxis.setLowerBound(0.0);
        rangeAxis.setUpperBound(40.0);
        
        XYPlot subplot = new XYPlot(
                datasets[0], null, rangeAxis, new StandardXYItemRenderer()
        );
        
        subplot.setForegroundAlpha(0.75f);
        subplot.setBackgroundPaint(Color.white);
        subplot.setDomainGridlinePaint(Color.darkGray);
        subplot.setRangeGridlinePaint(Color.darkGray);
        plot.add(subplot);
        
        series = new TimeSeries("total", Millisecond.class);        
        datasets[1].addSeries(series);
        
        series = new TimeSeries("active", Millisecond.class);        
        datasets[1].addSeries(series);
        
        rangeAxis = new NumberAxis("Servers");
        
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRange(true);
        rangeAxis.setLowerBound(0.0);
        rangeAxis.setUpperBound(10.0);
        
        subplot = new XYPlot(
                datasets[1], null, rangeAxis, new StandardXYItemRenderer()
        );
        
        subplot.setForegroundAlpha(0.75f);
        subplot.setBackgroundPaint(Color.white);
        subplot.setDomainGridlinePaint(Color.darkGray);
        subplot.setRangeGridlinePaint(Color.darkGray);
        plot.add(subplot);
        
        JFreeChart chart = new JFreeChart(plot);
        chart.setBorderVisible(false);
        chart.removeLegend();
        chart.setAntiAlias(true);
        
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.darkGray);
        plot.setRangeGridlinePaint(Color.darkGray);
        plot.setGap(10.0);
        
        
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  // 60 seconds
       
        
        ChartPanel panel = new ChartPanel(chart);

      //  panel.setMaximumDrawHeight(290);
        
        panel.setPreferredSize(new Dimension(1000, 300));
        
        
        add(panel);
        
        new Update().start();
        
    //    setPreferredSize(new Dimension(352, 288));
       // setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }
    
   
    
    public synchronized void setServers(int servers) {
        currentServers = servers;
    }
    
    public synchronized void setFramerate(double fps) {
        currentFrameRate = fps;
    }
    
    private synchronized int getServers() { 
        return currentServers;
    }
    
    private synchronized int getActiveServers() { 
        return activeServers;
    }
    
    
    private synchronized double getFramerate() { 
        return currentFrameRate;
    }

    public synchronized void addActiveServer() {
        activeServers++;
    }

    public synchronized void addServer() {
        currentServers++;
    }

    public synchronized void removeActiveServer() {
        activeServers--;
    }

    public synchronized void removeServer() {
        currentServers--;
    }
    
    /*
    public synchronized void setThroughput(double throughput) {
        currentThroughput = throughput;
    }
    
    public synchronized void setLatency(double latency) {
        currentLatency = latency;
    }
    
    private synchronized double getThroughput() { 
        return currentThroughput;
    }
    
    private synchronized double getLatency() { 
        return currentLatency;
    }
    */
}
