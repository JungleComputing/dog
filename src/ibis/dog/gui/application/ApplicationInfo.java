package ibis.dog.gui.application;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

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

public class ApplicationInfo extends JPanel implements FrameRateConsumer {

    private static final long serialVersionUID = 1L;

    // The data to plot
    private TimeSeriesCollection[] datasets;

    private CombinedDomainXYPlot plot;
    
    private double currentFrameRate = 0.0;
    
    private int currentServers = 0;
    
    private double currentThroughput = 0.0;
    
    private double currentLatency = 0.0;
    
    private class Update extends Thread { 
        
        public void run() { 
            
            while (true) { 
                try { 
                    sleep(1000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                
                datasets[0].getSeries(0).add(new Millisecond(), getFramerate());
                datasets[1].getSeries(0).add(new Millisecond(), getServers());
            }
        }
        
    }
    
    
    public ApplicationInfo() { 
    
        plot = new CombinedDomainXYPlot(new DateAxis("Time"));
        datasets = new TimeSeriesCollection[2];
        
        createSeries(0, "Framerate", "Frames/sec", 30);
        createSeries(1, "Servers", "Servers", 50);
        
        JFreeChart chart = new JFreeChart(plot);
        chart.setBorderVisible(false);
        chart.removeLegend();
        
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
    
    private void createSeries(int index, String name, String unit, double upperBound) { 
        
        TimeSeries fpsSeries = new TimeSeries(name, Millisecond.class);
        datasets[index] = new TimeSeriesCollection(fpsSeries);
        NumberAxis rangeAxis = new NumberAxis(unit);
     
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRange(true);
        rangeAxis.setLowerBound(0.0);
        rangeAxis.setUpperBound(upperBound);
        
        XYPlot subplot = new XYPlot(
                datasets[index], null, rangeAxis, new StandardXYItemRenderer()
        );
        
        subplot.setBackgroundPaint(Color.white);
        subplot.setDomainGridlinePaint(Color.darkGray);
        subplot.setRangeGridlinePaint(Color.darkGray);
        plot.add(subplot);
    }
    
    public synchronized void setServers(int servers) {
        currentServers = servers;
    }
    
    public synchronized void setFramerate(double fps) {
        currentFrameRate = fps;
    }
    
    public synchronized void setThroughput(double throughput) {
        currentThroughput = throughput;
    }
    
    public synchronized void setLatency(double latency) {
        currentLatency = latency;
    }
    
    private synchronized int getServers() { 
        return currentServers;
    }
    
    private synchronized double getFramerate() { 
        return currentFrameRate;
    }
    
    private synchronized double getThroughput() { 
        return currentThroughput;
    }
    
    private synchronized double getLatency() { 
        return currentLatency;
    }
    
    
    
    
    
    /*
    public void paint(Graphics g) {
        
        Dimension d = getSize();
        
        Graphics2D g2 = (Graphics2D) g;
        FontRenderContext frc = g2.getFontRenderContext();
        
        int x = d.width / 2;
        int y = d.height / 2;
        
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, d.width, d.height);
            
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        Font f = getFont().deriveFont(Font.BOLD);
        TextLayout tl = new TextLayout("INFO PANEL", f, frc);
            
        float sw = (float) tl.getBounds().getWidth();
        float sh = (float) tl.getBounds().getHeight();
        Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(
                    x-sw/2, y+sh/2));
        g2.setColor(Color.GRAY);
        g2.draw(sha);
        g2.setColor(Color.BLACK);
        g2.fill(sha);
    }*/
}
