package ibis.dog.client.gui;

import ibis.dog.client.Client;
import ibis.dog.client.WebCam;
import ibis.video4j.VideoDeviceDescription;
import ibis.video4j.devices.VideoSource;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlPanel extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory
            .getLogger(ControlPanel.class);

    // Generated
    private static final long serialVersionUID = 1L;

    private final JTextField inputField;

    private final JButton learnButton;
    private final JButton recognizeButton;

    private final Client client;

    private static final String NONE = "Camera Off";
    private static final String SCAN = "Scan for devices";

    private JComboBox deviceList;

    private WebCam webCam;

    private VideoSource currentCam;

    private final VideoPanel videoPanel;

    Speech speech;

    public ControlPanel(Client client) {
        this.client = client;

        //set font for dialogs to slightly bigger font
        UIManager.put("Label.font", new Font("Dialog", Font.BOLD, 16));
        
        setBorder(BorderFactory.createTitledBorder("Control"));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMinimumSize(new Dimension(665, VideoPanel.HEIGHT + 20));
        setPreferredSize(new Dimension(665, VideoPanel.HEIGHT + 20));
        setMaximumSize(new Dimension(665, VideoPanel.HEIGHT + 20));

        webCam = new WebCam(client);

        add(Box.createRigidArea(new Dimension(7, 7)));
        
        // actually display video
        videoPanel = new VideoPanel(client);
        add(videoPanel);

        add(Box.createRigidArea(new Dimension(7, 7)));

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(8, 1));
        buttons.setMaximumSize(new Dimension(200, VideoPanel.HEIGHT));
        
        // Create the combo box, select the item at index 0 (Item "none").
        deviceList = new JComboBox();
        deviceList.addActionListener(this);
        
        // deviceList.setMinimumSize(new Dimension(352, 25));
        // deviceList.setMaximumSize(new Dimension(352, 25));
        buttons.add(deviceList);
        
        buttons.add(Box.createRigidArea(new Dimension(0, 5)));
        buttons.add(Box.createRigidArea(new Dimension(0, 5)));

        Font inputFont = new Font(null, Font.PLAIN, 16);
        
        inputField = new JTextField("");
        inputField.setFont(inputFont);
        // inputField.setAlignmentY(Component.TOP_ALIGNMENT);
        // inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        buttons.add(inputField);

        Font buttonFont = new Font(null, Font.BOLD, 16);

        learnButton = new JButton("Teach");
        
        learnButton.setFont(buttonFont);
        learnButton.addActionListener(this);
        buttons.add(learnButton);
        
        buttons.add(Box.createRigidArea(new Dimension(0, 5)));
        
        buttons.add(Box.createRigidArea(new Dimension(0, 5)));

        recognizeButton = new JButton("Recognize");
        recognizeButton.setFont(buttonFont);
        recognizeButton.addActionListener(this);
        buttons.add(recognizeButton);

        // buttons.add(Box.createRigidArea(new Dimension(5, 5)));

        add(buttons);
        
        add(Box.createRigidArea(new Dimension(7, 7)));


        speech = new Speech(true);
        speech.speak("Voice initialized");

        //turn on webcam
        updateVideoDevices();
        if (deviceList.getItemCount() > 2) {
            deviceList.setSelectedIndex(1);
        } else {
            deviceList.setSelectedIndex(0);
        }
    }

    public void actionPerformed(ActionEvent e) {
        
        

        if (e.getSource() == learnButton) {

            String name = inputField.getText();

            if (name.equals("")) {
                JOptionPane.showMessageDialog(getRootPane(),
                        "Please enter object name first", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // output.write("I will now learn an object called: " + name);

            boolean success = client.learn(name);

            if (success) {
                String text = "I have just learned a new object called: \""
                        + name + "\"";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text);
                inputField.setText("");
            } else {
                String text = "I failed to learn object called: \"" + name
                        + "\"";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == recognizeButton) {

            String object = client.recognize();

            if (object != null) {
                String text = "This object is a \"" + object + "\"";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text);
            } else {
                String text = "I do not recognize this object";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text, "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else if (e.getSource() == deviceList) {

            Object tmp = deviceList.getSelectedItem();

            if (currentCam != null) {
                currentCam.close();
                currentCam = null;
            }

            if (tmp instanceof VideoDeviceDescription) {
                VideoDeviceDescription d = (VideoDeviceDescription) tmp;
                System.out.println("Selected device: "
                        + d.getSimpleDescription());

                try {
                    currentCam = webCam.selectDevice(d);
                } catch (Exception ex) {
                    logger.error("Failed to select device " + d);

                    ex.printStackTrace();
                }
            } else {
                String s = (String) tmp;
                System.out.println("Selected special option: " + s);

                if (s.equals(SCAN)) {
                    updateVideoDevices();
                } else {
                    // set to "none"
                }
            }
        }
    }

    public void close() {
        speech.done();
        currentCam.close();
    }

    private void updateVideoDevices() {

        // Find all video devices
        VideoDeviceDescription[] devices = null;

        try {
            devices = webCam.availableDevices();
        } catch (Exception e) {
            logger.error("Could not get device list", e);
            devices = new VideoDeviceDescription[0];
        }

        deviceList.removeAllItems();
        deviceList.addItem(NONE);

        for (int i = 0; i < devices.length; i++) {
            deviceList.addItem(devices[i]);
        }

        deviceList.addItem(SCAN);
    }

}
