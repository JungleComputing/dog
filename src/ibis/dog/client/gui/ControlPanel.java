package ibis.dog.client.gui;

import ibis.dog.client.Client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ControlPanel extends JPanel implements 
        ActionListener {

    // Generated
    private static final long serialVersionUID = 1L;

    private final JTextField inputField;

    private final JButton learnButton;
    private final JButton recognizeButton;

    private final Client client;

    Speech speech;

    public ControlPanel(Client client) {

        this.client = client;
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //setBorder(BorderFactory.createTitledBorder("Control"));
        setPreferredSize(new Dimension(250, 60));


        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(2,1));

        learnButton = new JButton("Learn");
        learnButton.addActionListener(this);
        buttons.add(learnButton);
        
        recognizeButton = new JButton("Recognize");
        recognizeButton.addActionListener(this);
        buttons.add(recognizeButton);
        
        buttons.setAlignmentY(TOP_ALIGNMENT);
        
        add(buttons);

        add(Box.createRigidArea(new Dimension(5, 5)));
        
        inputField = new JTextField("");
        inputField.setAlignmentY(Component.TOP_ALIGNMENT);
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        add(inputField);
        
        speech = new Speech(true);
        speech.speak("Voice initialized");

    }
   
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == learnButton) {

            String name = inputField.getText();

            if (name.equals("")) {
                JOptionPane.showMessageDialog(getRootPane(),
                        "Please enter object name first", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // output.write("I will now learn an object called: " + name);

            boolean success = client.learn(name);

            if (success) {
                String text = "I have just learned a new object called: \""
                        + name + "\"";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text);
                inputField.setText("");
            } else {
                String text = "I failed to learn object called: \"" + name
                        + "\"";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == recognizeButton) {

            String object = client.recognize();

            if (object != null) {
                String text = "This object is a \"" + object + "\"";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text);
            } else {
                String text = "I do not recognize this object";
                speech.speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text, "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void exit() {
        speech.done();
    }

}
