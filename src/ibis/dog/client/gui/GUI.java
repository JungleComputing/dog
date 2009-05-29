package ibis.dog.client.gui;

import ibis.dog.client.Client;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    private final Client client;

    private final ControlPanel controlPanel;

    public GUI() throws Exception {
        MessagePanel messagePanel = new MessagePanel();
        ServerPanel serverPanel = new ServerPanel();
        StatisticsPanel graphPanel = new StatisticsPanel();
        StatisticsPanel statisticsPanel = new StatisticsPanel();

        client = new Client(messagePanel, serverPanel, graphPanel,
                statisticsPanel);

        controlPanel = new ControlPanel(client);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel top = new JPanel();

        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(controlPanel);
        top.add(Box.createRigidArea(new Dimension(5, 5)));
        top.add(serverPanel);
        top.add(Box.createRigidArea(new Dimension(5, 5)));

        JPanel logos = new JPanel();
        logos.setLayout(new GridLayout(4, 1));
        // logos.setMaximumSize(new Dimension(150, 300));
        logos.setMaximumSize(new Dimension(110, 400));
        logos.add(new JLabel(new ImageIcon("images/ibis-logo.png")));
        logos.add(new JLabel(new ImageIcon("images/MultimediaN.png")));
        logos.add(new JLabel(new ImageIcon("images/vl-e.png")));
        logos.add(new JLabel(new ImageIcon("images/vu.png")));

        top.add(logos);

        top.add(Box.createRigidArea(new Dimension(5, 5)));

        add(top);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(graphPanel);
        bottom.add(Box.createRigidArea(new Dimension(5, 5)));
        bottom.add(messagePanel);

        add(bottom);

        // JPanel middle = new JPanel();
        // middle.setPreferredSize(new Dimension(200, 200));
        // middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
        // middle.add(Box.createRigidArea(new Dimension(5, 5)));
        // middle.add(statisticsPanel);
        // middle.add(Box.createRigidArea(new Dimension(5, 5)));
        // add(middle);
        //
        // JPanel bottom = new JPanel();
        // bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        // bottom.add(messagePanel);
        // bottom.add(Box.createRigidArea(new Dimension(5, 5)));
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
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setMaximumSize(new Dimension(WIDTH, HEIGHT));

        frame.addWindowListener(this);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        this.setOpaque(true); // content panes must be opaque
        frame.setContentPane(this);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private void exit() {
        controlPanel.close();
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
