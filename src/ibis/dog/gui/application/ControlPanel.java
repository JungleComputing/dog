package ibis.dog.gui.application;

import ibis.dog.client.Client;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ControlPanel extends JPanel implements ItemListener,
        ActionListener {

    // Generated
    private static final long serialVersionUID = 1L;

    private final JTextField inputField;

    private final JButton learnButton;
    private final JButton recognizeButton;

    private final JCheckBox speechCheckBox;

    private final OutputPanel output;

    private final Client client;

    public ControlPanel(OutputPanel output, Client client) {

        this.output = output;
        this.client = client;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Control"));

        inputField = new JTextField("");
        inputField.setMinimumSize(new Dimension(0, 25));
        inputField.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));

        add(inputField);
        add(Box.createRigidArea(new Dimension(5, 5)));

        // Create the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        learnButton = new JButton("Learn");
        learnButton.addActionListener(this);

        recognizeButton = new JButton("Recognize");
        recognizeButton.addActionListener(this);

        speechCheckBox = new JCheckBox("Speech");
        speechCheckBox.setSelected(true);
        speechCheckBox.addItemListener(this);

        buttonPanel.add(learnButton);
        buttonPanel.add(recognizeButton);
        buttonPanel.add(speechCheckBox);

        add(buttonPanel);
    }

    // Listens to checkbox
    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == speechCheckBox) {
            if (output != null) {
                output.setSpeech(e.getStateChange() == ItemEvent.SELECTED);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        
        if (e.getSource() == learnButton) {
            
            String name = inputField.getText();
            
            if (name.equals("")) {
                JOptionPane.showMessageDialog(getRootPane(),
                        "Please enter object name first",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //output.write("I will now learn an object called: " + name);
            
            boolean success = client.learn(name);
            
            if (success) { 
                JOptionPane.showMessageDialog(getRootPane(), "I have just learned a new object called: \"" + name + "\"");
                inputField.setText("");
            } else {
                JOptionPane.showMessageDialog(getRootPane(),
                        "I failed to learn object called: " + name,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            
        } else if (e.getSource() == recognizeButton) {
            
            String object = client.recognize();
            
            if (object != null) {
                JOptionPane.showMessageDialog(getRootPane(), "This object is a \"" + object + "\"");

            } else {
                JOptionPane.showMessageDialog(getRootPane(),
                        "I do not recognize this object",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
