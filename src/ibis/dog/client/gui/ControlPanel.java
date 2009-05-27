package ibis.dog.client.gui;

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

    private final Client client;

    Speech speech;
    boolean speak = true;

    public ControlPanel(Client client) {

        this.client = client;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createTitledBorder("Control"));

        inputField = new JTextField("");
        inputField.setMinimumSize(new Dimension(0, 25));
        inputField.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        inputField.setPreferredSize(new Dimension(300, 25));

        add(inputField);
        add(Box.createRigidArea(new Dimension(5, 5)));

        learnButton = new JButton("Learn");
        learnButton.addActionListener(this);
        add(learnButton);
        
        add(Box.createRigidArea(new Dimension(5, 5)));

        recognizeButton = new JButton("Recognize");
        recognizeButton.addActionListener(this);
        add(recognizeButton);
        
        add(Box.createRigidArea(new Dimension(5, 5)));

        speechCheckBox = new JCheckBox("Speech");
        speechCheckBox.setSelected(true);
        speechCheckBox.addItemListener(this);
        add(speechCheckBox);

        speech = new Speech(true);
        speech.speak("Voice initialized");

    }

    // Listens to checkbox
    public synchronized void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == speechCheckBox) {
            speak = e.getStateChange() == ItemEvent.SELECTED;
        }
    }

    private synchronized void speak(String text) {
        if (speak) {
            speech.speak(text);
        }
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
                speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text);
                inputField.setText("");
            } else {
                String text = "I failed to learn object called: \"" + name
                        + "\"";
                speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == recognizeButton) {

            String object = client.recognize();

            if (object != null) {
                String text = "This object is a \"" + object + "\"";
                speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text);
            } else {
                String text = "I do not recognize this object";
                speak(text);
                JOptionPane.showMessageDialog(getRootPane(), text, "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void exit() {
        speech.done();
    }

}
