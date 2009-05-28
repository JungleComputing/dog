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
        
        add(Box.createRigidArea(new Dimension(5, 5)));

        JPanel top = new JPanel();
        
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));

        JPanel cameraAndControl = new JPanel();
        cameraAndControl.setBorder(BorderFactory.createTitledBorder("Control"));
        cameraAndControl.setMaximumSize(new Dimension(360, 410));
        cameraAndControl.setPreferredSize(new Dimension(360, 410));
        cameraAndControl.setMinimumSize(new Dimension(360, 410));
        cameraAndControl.setLayout(new BoxLayout(cameraAndControl, BoxLayout.Y_AXIS));
        cameraAndControl.add(cameraPanel);
        cameraAndControl.add(controlPanel);
        
        top.add(cameraAndControl);
        top.add(Box.createRigidArea(new Dimension(5, 5)));
        top.add(serverPanel);
        
        JPanel logosAndMessages = new JPanel();
        logosAndMessages.setLayout(new BoxLayout(logosAndMessages, BoxLayout.Y_AXIS));
        
        JPanel logos = new JPanel();
        logos.setLayout(new BoxLayout(logos, BoxLayout.X_AXIS));
        logos.add(Box.createRigidArea(new Dimension(25, 25)));
        logos.add(new JLabel(new ImageIcon("images/vu.png")));
        logos.add(Box.createRigidArea(new Dimension(25, 25)));
        logos.add(new JLabel(new ImageIcon("images/MultimediaN.png")));
        logos.add(Box.createRigidArea(new Dimension(25, 25)));
        logos.add(new JLabel(new ImageIcon("images/vl-e.png")));
        logos.add(Box.createRigidArea(new Dimension(25, 25)));
        logos.add(new JLabel(new ImageIcon("images/ibis-logo.png")));
        logos.add(Box.createRigidArea(new Dimension(25, 25)));

        
        logosAndMessages.add(messagePanel);
        logosAndMessages.add(Box.createRigidArea(new Dimension(12, 12)));

        logosAndMessages.add(logos);
        logosAndMessages.add(Box.createRigidArea(new Dimension(12, 12)));

        
        top.add(logosAndMessages);

        top.add(Box.createRigidArea(new Dimension(5, 5)));
        
        add(top);

        add(statisticsPanel);
        
        
//        JPanel middle = new JPanel();
//        middle.setPreferredSize(new Dimension(200, 200));
//        middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
//        middle.add(Box.createRigidArea(new Dimension(5, 5)));
//        middle.add(statisticsPanel);
//        middle.add(Box.createRigidArea(new Dimension(5, 5)));
//        add(middle);
//
//        JPanel bottom = new JPanel();
//        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
//        bottom.add(messagePanel);
//        bottom.add(Box.createRigidArea(new Dimension(5, 5)));
//        add(bottom);

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
