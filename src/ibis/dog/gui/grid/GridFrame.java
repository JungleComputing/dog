package ibis.dog.gui.grid;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class GridFrame {
    
    public GridFrame(GridRunner runner, Grid grid) { 
        
        final JFrame frame = new JFrame("Grid Management");
        final GridPanel gp = new GridPanel(frame, runner, grid);
   
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.remove(gp);
                frame.dispose();
            }
        });
        frame.add("Center", gp);
        
        Dimension size = gp.getPreferredSize();
        frame.setSize((int)(size.getWidth()+25), (int) (size.getHeight()+25));        
        frame.setVisible(true);
    }
}