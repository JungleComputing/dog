package gui.application;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LogoPanel extends JPanel {

    // Generated
    private static final long serialVersionUID = 1L;

    public LogoPanel() { 
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
   
        add(new JLabel(new ImageIcon("Images/ibis-logo.png")));
        add(Box.createRigidArea(new Dimension(5,0)));
        add(new JLabel(new ImageIcon("Images/JavaGAT.png")));
        add(Box.createRigidArea(new Dimension(5,0)));
        add(new JLabel(new ImageIcon("Images/vu.png")));
        add(Box.createRigidArea(new Dimension(5,0)));
        add(new JLabel(new ImageIcon("Images/MultimediaN.gif")));        
    }
    
    
}
