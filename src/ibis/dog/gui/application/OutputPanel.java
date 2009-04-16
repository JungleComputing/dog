package ibis.dog.gui.application;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OutputPanel extends JPanel {

    // Generated
    private static final long serialVersionUID = 1L;
   
    private JTextArea textOutput;
    public OutputPanel(String text) { 
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        textOutput = new JTextArea();
        textOutput.setLineWrap(true);
        textOutput.setWrapStyleWord(true);
        textOutput.setEditable(false);
        textOutput.setColumns(40);
        
        JScrollPane textScroll = new JScrollPane(textOutput,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        if (text != null) { 
            setBorder(BorderFactory.createTitledBorder(text));
        } else { 
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));        
        }
        add(textScroll);   

        
    }
    
    public void write(String text) {
        textOutput.insert(text + "\n", 0);
    }

    public void writeChar(int c) {
        textOutput.append(String.valueOf((char) c));
    }
    
    
}
