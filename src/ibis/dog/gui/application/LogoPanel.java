package ibis.dog.gui.application;

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
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createRigidArea(new Dimension(5,50)));
        add(new JLabel(new ImageIcon("images/ibis-logo.png")));
        add(Box.createRigidArea(new Dimension(5,5)));
        add(new JLabel(new ImageIcon("images/JavaGAT.png")));
        add(Box.createRigidArea(new Dimension(5,5)));
        add(new JLabel(new ImageIcon("images/vu.png")));
        add(Box.createRigidArea(new Dimension(5,5)));
        add(new JLabel(new ImageIcon("images/MultimediaN.gif")));        
    }
    
    
}
