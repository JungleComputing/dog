package ibis.dog.gui.application;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.video4j.VideoConsumer;
import ibis.video4j.VideoDeviceDescription;
import ibis.video4j.VideoDeviceFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class VideoPanel extends JPanel implements ActionListener {

    // Generated
    private static final long serialVersionUID = 1L;

    private static final String NONE = "Off";
    private static final String SCAN = "Scan for devices";
        
    private JComboBox deviceList;
    
    private VideoStream videoStream;
    
    public VideoPanel(FrameRateConsumer f, VideoConsumer v) {
        
        videoStream = new VideoStream(352, 288, f, v);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(videoStream);
        add(Box.createRigidArea(new Dimension(0,5)));
      
        // Create the combo box, select the item at index 0 (Item "none").
        deviceList = new JComboBox();
        updateVideoDevices();
        deviceList.setSelectedIndex(0);
        deviceList.addActionListener(this);
        
        deviceList.setMinimumSize(new Dimension(352, 25));
        deviceList.setMaximumSize(new Dimension(352, 25));
        
        add(deviceList);
    
        setBorder(BorderFactory.createTitledBorder("Camera"));
    }

    private void updateVideoDevices() { 
        
        // Find all video devices
        VideoDeviceDescription [] devices = null;
        
        try { 
            devices = VideoDeviceFactory.availableDevices(); 
        } catch (Exception e) {
            devices = new VideoDeviceDescription[0];
        }
        
        deviceList.removeAllItems();
        deviceList.addItem(NONE);
        
        for (int i=0;i<devices.length;i++) { 
            deviceList.addItem(devices[i]);
        }
            
        deviceList.addItem(SCAN);
    }
    
    public void actionPerformed(ActionEvent e) {
        
        JComboBox cb = (JComboBox) e.getSource();

        Object tmp = cb.getSelectedItem();
        
        if (tmp instanceof VideoDeviceDescription) {
            VideoDeviceDescription d = (VideoDeviceDescription) tmp;
            System.out.println("Selected device: " + d.getSimpleDescription());
            
            try { 
                videoStream.selectDevice(d.deviceNumber);
            } catch (Exception ex) {
                videoStream.setMessage("Failed to select device " + d.deviceNumber);
            
                ex.printStackTrace();
            }
        } else { 
            String s = (String) tmp;
            System.out.println("Selected special option: " + s);
            
            try {
                videoStream.selectDevice(-1);
            } catch (Exception e1) {
                // ignored
            }
            
            if (s.equals(SCAN)) { 
                updateVideoDevices();
            }
        }   
    }
}
