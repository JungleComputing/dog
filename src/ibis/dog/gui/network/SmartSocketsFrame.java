package ibis.dog.gui.network;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;

public class SmartSocketsFrame extends JFrame {
    
    // Generated
    private static final long serialVersionUID = 1L;

    private final SmartsocketsViz glPanel;
    
    public SmartSocketsFrame(List<DirectSocketAddress> hub) { 
        
        super("SmartSockets Network Topology");
        
        glPanel = new SmartsocketsViz(hub);
   
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                
                System.out.println("Removing SS frame");
                
                remove(glPanel);
                dispose();
            }
        });

        add("Center", glPanel);
        setSize(800, 600);
        setVisible(true);
    }
    
    public void exit() { 
        System.out.println("SmartSocketsFrame.exit called!");
        
        glPanel.done();
        remove(glPanel);
        dispose();
    }
}
