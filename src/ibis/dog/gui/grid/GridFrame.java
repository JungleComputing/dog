package ibis.dog.gui.grid;

import ibis.dog.client.Deployment;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class GridFrame extends JFrame {
    
    public GridFrame(Deployment deploy) { 
        
        super("Grid Management");
        
        final GridPanel gp = new GridPanel(this, deploy);
   
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                remove(gp);
                dispose();
            }
        });
       
        add("Center", gp);
        
        //Dimension size = gp.getPreferredSize();
        //setSize((int)(size.getWidth()+25), (int) (size.getHeight()+25));        
        
        pack();
        setVisible(true);
    }
}