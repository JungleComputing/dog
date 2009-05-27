package ibis.dog.client.gui;

import ibis.dog.client.Client;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUI extends JPanel implements WindowListener {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(GUI.class);

    // private static final int WIDTH = 1024;
    // private static final int HEIGHT = 768;

    private final Client client;

    private final CameraPanel cameraPanel;

    public GUI() throws Exception {
        MessagePanel messagePanel = new MessagePanel();
        ServerPanel serverPanel = new ServerPanel();

        client = new Client(messagePanel, serverPanel);

        ControlPanel controlPanel = new ControlPanel(client);
        cameraPanel = new CameraPanel(client);
        StatisticsPanel statisticsPanel = new StatisticsPanel(client);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));

        JPanel controlServer = new JPanel();
        controlServer.setLayout(new BoxLayout(controlServer, BoxLayout.Y_AXIS));
        controlServer.add(controlPanel);
        controlServer.add(serverPanel);

        top.add(controlServer);
        top.add(cameraPanel);

        add(top);

        JPanel middle = new JPanel();
        middle.setPreferredSize(new Dimension(200, 200));
        middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
        middle.add(new JLabel(new ImageIcon("images/vl-e.png")));
        middle.add(statisticsPanel);
        middle.add(new JLabel(new ImageIcon("images/MultimediaN.png")));
        add(middle);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(new JLabel(new ImageIcon("images/ibis-logo.png")));
        bottom.add(messagePanel);
        bottom.add(new JLabel(new ImageIcon("images/vu.png")));
        add(bottom);


        // JPanel bottom = new JPanel();
        // bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        //
        // JPanel leftLogos = new JPanel();
        // leftLogos.setLayout(new BoxLayout(leftLogos, BoxLayout.Y_AXIS));
        // leftLogos.add(new JLabel(new ImageIcon("images/ibis-logo.png")));
        // leftLogos.add(new JLabel(new ImageIcon("images/JavaGAT.png")));
        // bottom.add(leftLogos);
        //
        // JPanel bottomCenter = new JPanel();
        // bottomCenter.setLayout(new BoxLayout(bottomCenter,
        // BoxLayout.Y_AXIS));
        // bottomCenter.add(statisticsPanel);
        // bottomCenter.add(messagePanel);
        // bottom.add(bottomCenter);
        //
        // JPanel rightLogos = new JPanel();
        // rightLogos.setLayout(new BoxLayout(rightLogos, BoxLayout.Y_AXIS));
        // rightLogos.add(new JLabel(new ImageIcon("images/vu.png")));
        // rightLogos.add(new JLabel(new ImageIcon("images/MultimediaN.gif")));
        // bottom.add(rightLogos);
        //        
        // add(bottom);

        // create GUI in main Swing thread
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Ibis Object Recognition Demo Client");
        // frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.addWindowListener(this);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        this.setOpaque(true); // content panes must be opaque
        this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setContentPane(this);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private void exit() {
        cameraPanel.close();
        client.end();
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // IGNORE
    }

    @Override
    public void windowClosed(WindowEvent e) {
        exit();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // IGNORE
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // IGNORE
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // IGNORE
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // IGNORE
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // IGNORE
    }

    public static void main(String[] args) {
        try {
            GUI gui = new GUI();

        } catch (Exception e) {
            logger.error("Error in client", e);
        }
    }
}
