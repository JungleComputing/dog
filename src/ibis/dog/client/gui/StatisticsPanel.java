package ibis.dog.client.gui;

import ibis.dog.client.StatisticsListener;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatisticsPanel extends JPanel implements StatisticsListener {

    // Generated
    private static final long serialVersionUID = 1L;
    
    private final JLabel inputFps;
    private final JLabel displayedFps;
    private final JLabel processedFps;

    public StatisticsPanel() {

        setLayout(new GridLayout(3,3));
        setBorder(BorderFactory.createTitledBorder("Statistics"));
        setPreferredSize(new Dimension(400, 100));
        
        add(new JLabel("Webcam"));
        inputFps = new JLabel("0");
        add(inputFps);
        add(Box.createRigidArea(new Dimension(1,1)));

        add(new JLabel("Displayed"));
        displayedFps = new JLabel("0");
        add(displayedFps);
        add(Box.createRigidArea(new Dimension(1,1)));

        add(new JLabel("Processed"));
        processedFps = new JLabel("0");
        add(processedFps);
        add(Box.createRigidArea(new Dimension(1,1)));


    }


    @Override
    public void newStatistics(double inputFps, double displayedFps,
            double processedFps) {
        this.inputFps.setText(String.format("%.2f fps", inputFps));
        this.displayedFps.setText(String.format("%.2f fps", displayedFps));
        this.processedFps.setText(String.format("%.2f fps", processedFps));
    }
   
}
