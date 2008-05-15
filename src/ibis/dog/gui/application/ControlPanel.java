package ibis.dog.gui.application;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ControlPanel extends JPanel {
    
    // Generated
    private static final long serialVersionUID = 1L;
   
    private JTextField inputField;
    private JButton learnButton; 
    private JButton recognizeButton;
    
    public ControlPanel() { 
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Control"));
             
        inputField = new JTextField("");
        inputField.setMinimumSize(new Dimension(0, 25));
        inputField.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        
        add(inputField);
        add(Box.createRigidArea(new Dimension(0,5)));
        
        // Create the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        learnButton = new JButton("Learn");
        recognizeButton = new JButton("Recognize");
        
        buttonPanel.add(learnButton);
        buttonPanel.add(recognizeButton);
        
        add(buttonPanel);
    }
    
}

