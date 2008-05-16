package ibis.dog.gui.console;


import ibis.dog.gui.application.OutputPanel;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class OutputFrame extends JFrame {
    
    private final OutputPanel out;
    
    public OutputFrame() { 
        
        super("Console Output");
        out = new OutputPanel(null);
   
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                remove(out);
                dispose();
            }
        });
        
        add("Center", out);
        
        //Dimension size = new Dimension(800, 600);
        //frame.setSize((int)(size.getWidth()), (int) (size.getHeight()));        
        
        pack();
        setVisible(true);
    }
}