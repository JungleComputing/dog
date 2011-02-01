package ibis.dog.client.gui;

import ibis.dog.client.StatisticsListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class StatisticsPanel extends JPanel implements StatisticsListener {

    private static final long serialVersionUID = 1L;

    // The data to plot
    private TimeSeriesCollection dataset;

    private XYPlot plot;

    private final JLabel inputFps;
   // private final JLabel displayedFps;
    private final JLabel processedFps;

    @Override
    public void newStatistics(double inputFps, double displayedFps,
            double processedFps) {

        // dataset.getSeries(0).add(new Millisecond(), inputFps);
        dataset.getSeries(0).add(new Millisecond(), processedFps);
        //
        // dataset.getSeries(2).add(new Millisecond(),
        // displayedFps);

        this.inputFps.setText(String.format("%.1f fps", inputFps));
        //this.displayedFps.setText(String.format("%.1f fps", displayedFps));
        this.processedFps.setText(String.format("%.1f fps", processedFps));
    }

    public StatisticsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        // setMinimumSize(new Dimension(100, 100));
        setBorder(BorderFactory.createTitledBorder("Statistics"));
//        setMinimumSize(new Dimension(650, 350));
//        setPreferredSize(new Dimension(650, 350));
//        setMaximumSize(new Dimension(650, Integer.MAX_VALUE));

        JPanel numbers = new JPanel();
        numbers.setLayout(new GridLayout(3, 2));

        numbers.add(new JLabel("Webcam"));
        inputFps = new JLabel("0");
        numbers.add(inputFps);

//        numbers.add(new JLabel("Displayed"));
//        displayedFps = new JLabel("0");
//        numbers.add(displayedFps);

        numbers.add(new JLabel("Processed"));
        processedFps = new JLabel("0");
        numbers.add(processedFps);

        //extra panel to place numbers table a bit to the right so it
        //is aligned with the graph
        JPanel numbersOffset = new JPanel();
        numbersOffset.setAlignmentX(LEFT_ALIGNMENT);
        numbersOffset.setMaximumSize(new Dimension(200, 20));
        numbersOffset.setLayout(new BoxLayout(numbersOffset, BoxLayout.X_AXIS));
        numbersOffset.add(Box.createRigidArea(new Dimension(40, 40)));
        numbersOffset.add(numbers);
        
        add(numbersOffset);

        add(Box.createRigidArea(new Dimension(5, 5)));

        dataset = new TimeSeriesCollection();

        // TimeSeries series = new TimeSeries("in", Millisecond.class);
        // dataset.addSeries(series);

        TimeSeries series = new TimeSeries("Processed", Millisecond.class);
        dataset.addSeries(series);
        //
        // series = new TimeSeries("displayed", Millisecond.class);
        // dataset.addSeries(series);

        NumberAxis yAxis = new NumberAxis("Frames/sec.");
        yAxis.setAutoRangeIncludesZero(true);
        yAxis.setAutoRangeMinimumSize(1.0);
        yAxis.setRangeType(RangeType.POSITIVE);
        yAxis.setAutoRange(true);

        ValueAxis xAxis = new DateAxis("Time");
        xAxis.setAutoRange(true);
        xAxis.setFixedAutoRange(60000); // 60 seconds

        plot = new XYPlot(dataset, xAxis, yAxis, new StandardXYItemRenderer());

        plot.setForegroundAlpha(0.75f);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.darkGray);
        plot.setRangeGridlinePaint(Color.darkGray);

        JFreeChart chart = new JFreeChart(plot);
        chart.setBorderVisible(false);
        chart.removeLegend();
        chart.setAntiAlias(true);

        ChartPanel panel = new ChartPanel(chart);

        dataset.getSeries(0).add(new Millisecond(), 0);
        // dataset.getSeries(1).add(new Millisecond(), 0);
        // dataset.getSeries(2).add(new Millisecond(), 0);

        // panel.setMaximumDrawHeight(290);

        add(panel);

        // setPreferredSize(new Dimension(352, 288));
        // setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }

}
