package ibis.dog.client.gui;

import ibis.dog.client.Client;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class StatisticsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // The data to plot
    private TimeSeriesCollection dataset;

    private XYPlot plot;

    private final Client client;

    /*
     * private double currentThroughput = 0.0;
     * 
     * private double currentLatency = 0.0;
     */

    private class Update extends Thread {

        Update() {
            setDaemon(true);
        }

        public void run() {

            while (true) {
                try {
                    sleep(5000);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                dataset.getSeries(0).add(new Millisecond(), client.inputFPS());
                dataset.getSeries(1).add(new Millisecond(),
                        client.processedFPS());

                dataset.getSeries(2).add(new Millisecond(),
                        client.displayedFPS());

            }
        }

    }

    public StatisticsPanel(Client client) {
        this.client = client;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        // setMinimumSize(new Dimension(100, 100));
        setBorder(BorderFactory.createTitledBorder("Statistics"));
        setPreferredSize(new Dimension(600, 400));

        dataset = new TimeSeriesCollection();

        TimeSeries series = new TimeSeries("in", Millisecond.class);
        dataset.addSeries(series);

        series = new TimeSeries("processed", Millisecond.class);
        dataset.addSeries(series);

        series = new TimeSeries("displayed", Millisecond.class);
        dataset.addSeries(series);

        NumberAxis yAxis = new NumberAxis("Frames/sec.");
        yAxis.setAutoRangeIncludesZero(true);
        yAxis.setAutoRangeMinimumSize(10.0);
        yAxis.setAutoRange(true);

        ValueAxis xAxis = new DateAxis();
        xAxis.setAutoRange(true);
        xAxis.setFixedAutoRange(60000.0); // 60 seconds

        plot = new XYPlot(dataset, xAxis, yAxis, new StandardXYItemRenderer());

        plot.setForegroundAlpha(0.75f);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.darkGray);
        plot.setRangeGridlinePaint(Color.darkGray);

        JFreeChart chart = new JFreeChart(plot);
        chart.setBorderVisible(false);
        // chart.removeLegend();
        chart.setAntiAlias(true);

        ChartPanel panel = new ChartPanel(chart);

        dataset.getSeries(0).add(new Millisecond(), 0);
        dataset.getSeries(1).add(new Millisecond(), 0);
        dataset.getSeries(2).add(new Millisecond(), 0);

        // panel.setMaximumDrawHeight(290);

        add(panel);

        new Update().start();

        // setPreferredSize(new Dimension(352, 288));
        // setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }
}
