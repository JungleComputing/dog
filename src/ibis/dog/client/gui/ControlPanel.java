package ibis.dog.client.gui;

import ibis.dog.client.Client;
import ibis.dog.client.WebCam;
import ibis.media.video.VideoDeviceDescription;
import ibis.media.video.devices.VideoSource;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlPanel extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory
            .getLogger(ControlPanel.class);

    // Generated
    private static final long serialVersionUID = 1L;
    
    private static final String NONE = "Camera Off";
    private static final String SCAN = "Scan for devices";

    private JComboBox inputList;
    private JComboBox deviceList;

    private WebCam webCam;

    private VideoSource[] currentCams;

    private final VideoPanel[] videoPanels;

    private Client client;
    
    private Map<Integer, Integer> cameraList;
    
    Speech speech;

    public ControlPanel(Client c) {

    	client = c;
    	
        speech = new Speech(true);
        speech.speak("Voice initialized");

        // set font for dialogs to slightly bigger font
        UIManager.put("Label.font", new Font("Dialog", Font.BOLD, 16));

        setBorder(BorderFactory.createTitledBorder("Control"));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        // setMinimumSize(new Dimension(665, VideoPanel.HEIGHT + 20));
        // setPreferredSize(new Dimension(665, VideoPanel.HEIGHT + 20));
        // setMaximumSize(new Dimension(665, VideoPanel.HEIGHT + 20));

        webCam = new WebCam(client);
        currentCams = new VideoSource[2];

        add(Box.createRigidArea(new Dimension(7, 7)));

        // actually display video
        videoPanels = new VideoPanel[2];
        for(int i=0; i<videoPanels.length; i++){
        	videoPanels[i] = new VideoPanel(client);
        	add(videoPanels[i]);
        }

        add(Box.createRigidArea(new Dimension(7, 7)));

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(8, 1));
        buttons.setMaximumSize(new Dimension(190, VideoPanel.HEIGHT));
        buttons.setMinimumSize(new Dimension(190, VideoPanel.HEIGHT));
        buttons.setPreferredSize(new Dimension(190, VideoPanel.HEIGHT));

        // Create the combo box, select the item at index 0 (Item "Left").
        inputList = new JComboBox();
        inputList.addItem("Left");
        inputList.addItem("Right");
        inputList.addActionListener(this);
        
        // Create the combo box, select the item at index 0 (Item "none").
        deviceList = new JComboBox();
        deviceList.addActionListener(this);

        // deviceList.setMinimumSize(new Dimension(352, 25));
        // deviceList.setMaximumSize(new Dimension(352, 25));
        buttons.add(inputList);
        buttons.add(deviceList);

        buttons.add(Box.createRigidArea(new Dimension(0, 5)));
        buttons.add(Box.createRigidArea(new Dimension(0, 5)));

        Font buttonFont = new Font(null, Font.BOLD, 16);

        // buttons.add(Box.createRigidArea(new Dimension(5, 5)));

        add(buttons);

        add(Box.createRigidArea(new Dimension(5, 5)));

        // turn on webcam
        cameraList = new HashMap<Integer, Integer>();
        updateVideoDevices();
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == deviceList) {

            Object tmp = deviceList.getSelectedItem();

            if (currentCams[0] != null) {
                currentCams[0].close();
                currentCams[0] = null;
                videoPanels[0].setInvalid();
            }

            if (tmp instanceof VideoDeviceDescription) {
                VideoDeviceDescription d = (VideoDeviceDescription) tmp;
                System.out.println("Selected device: "
                        + d.getSimpleDescription());

                try {
                    currentCams[0] = webCam.selectDevice(d);
                    cameraList.put(0, d.deviceNumber);
                    client.setCameras(cameraList);
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
        currentCams[0].close();
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
