package ibis.dog.gui.console;


import ibis.dog.gui.application.OutputPanel;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class OutputFrame {
    
    public OutputFrame() { 
        
        final JFrame frame = new JFrame("Console Output");
        final OutputPanel out = new OutputPanel(null);
   
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.remove(out);
                frame.dispose();
            }
        });
        frame.add("Center", out);
        
        Dimension size = new Dimension(800, 600);
        frame.setSize((int)(size.getWidth()), (int) (size.getHeight()));        
        frame.setVisible(true);
    }
}