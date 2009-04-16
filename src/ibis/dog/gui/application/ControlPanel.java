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

    Speech speech;
    boolean speak = true;

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

        speech = new Speech(true);
        speech.speak("Voice initialized");

        buttonPanel.add(learnButton);
        buttonPanel.add(recognizeButton);
        buttonPanel.add(speechCheckBox);

        add(buttonPanel);
    }

    // Listens to checkbox
    public synchronized void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == speechCheckBox) {
            if (output != null) {
                speak = e.getStateChange() == ItemEvent.SELECTED;
            }
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
                JOptionPane.showMessageDialog(getRootPane(), text);
                speak(text);
                inputField.setText("");
            } else {
                String text = "I failed to learn object called: " + name;
                JOptionPane.showMessageDialog(getRootPane(), text, "Error",
                        JOptionPane.ERROR_MESSAGE);
                speak(text);
            }

        } else if (e.getSource() == recognizeButton) {

            String object = client.recognize();

            if (object != null) {
                String text = "This object is a \"" + object + "\"";
                JOptionPane.showMessageDialog(getRootPane(), text);
                speak(text);
            } else {
                String text = "I do not recognize this object";
                JOptionPane.showMessageDialog(getRootPane(), text, "Warning",
                        JOptionPane.WARNING_MESSAGE);
                speak(text);
            }
        }
    }

    public void exit() {
        speech.done();
    }

}
