package ibis.dog.client.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.dog.client.Client;
import ibis.dog.client.WebCam;
import ibis.video4j.VideoDeviceDescription;
import ibis.video4j.devices.VideoSource;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraPanel extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory
            .getLogger(CameraPanel.class);

    // Generated
    private static final long serialVersionUID = 1L;

    private static final String NONE = "Camera Off";
    private static final String SCAN = "Scan for devices";

    private JComboBox deviceList;

    private WebCam webCam;

    private VideoSource currentCam;
    
    private final VideoPanel videoPanel;

    public CameraPanel(Client client) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //setBorder(BorderFactory.createTitledBorder("Camera"));
        //setPreferredSize(new Dimension(300, 400));
        setAlignmentY(Component.TOP_ALIGNMENT);
        
        webCam = new WebCam(client);
        
        // Create the combo box, select the item at index 0 (Item "none").
        deviceList = new JComboBox();
        updateVideoDevices();
        deviceList.setSelectedIndex(0);
        deviceList.addActionListener(this);
        
        if (deviceList.getItemCount() > 2) {
            deviceList.setSelectedIndex(1);
        }

        deviceList.setMinimumSize(new Dimension(352, 25));
        deviceList.setMaximumSize(new Dimension(352, 25));

        add(deviceList);

        add(Box.createRigidArea(new Dimension(0, 5)));

        // actually display video
        videoPanel = new VideoPanel(client);
        add(videoPanel);

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

    public void actionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();

        Object tmp = cb.getSelectedItem();

        if (currentCam != null) {
            currentCam.close();
            currentCam = null;
        }

        if (tmp instanceof VideoDeviceDescription) {
            VideoDeviceDescription d = (VideoDeviceDescription) tmp;
            System.out.println("Selected device: " + d.getSimpleDescription());

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
                //set to "none"
            }
        }
    }

    void close() {

    }
}
