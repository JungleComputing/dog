package ibis.dog.gui.network;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class SmartSocketsFrame {
    
    public SmartSocketsFrame(List<DirectSocketAddress> hub) { 
        final Frame frame = new Frame("TouchGraph GraphLayout");
        
        final SmartsocketsViz glPanel = new SmartsocketsViz(hub);
   
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.remove(glPanel);
                frame.dispose();
            }
        });
        frame.add("Center", glPanel);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
