package ibis.dog.client.gui;

import java.awt.Dimension;

import ibis.dog.client.MessageListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MessagePanel extends JPanel implements MessageListener {

    // Generated
    private static final long serialVersionUID = 1L;

    private JTextArea textOutput;

    public MessagePanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Messages"));
        //setPreferredSize(new Dimension(400, 200));

        textOutput = new JTextArea();
        textOutput.setLineWrap(true);
        textOutput.setWrapStyleWord(true);
        textOutput.setEditable(false);
        textOutput.setColumns(35);
        textOutput.setOpaque(false);
        

        JScrollPane textScroll = new JScrollPane(textOutput,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(textScroll);

    }

    @Override
    public void message(String message) {
        String output = String.format("%tT %s\n", System.currentTimeMillis(), message);
        
        textOutput.insert(output, 0);
    }

}
