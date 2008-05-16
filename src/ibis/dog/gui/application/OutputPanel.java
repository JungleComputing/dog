package ibis.dog.gui.application;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OutputPanel extends JPanel implements SpeechInterface {

    // Generated
    private static final long serialVersionUID = 1L;
   
    private JTextArea textOutput;
    private Speech speech;
    
    private boolean useSpeech;
    
    public OutputPanel(String text) { 
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        textOutput = new JTextArea();
        textOutput.setLineWrap(true);
        textOutput.setWrapStyleWord(true);
        textOutput.setEditable(false);
        
        JScrollPane textScroll = new JScrollPane(textOutput,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        if (text != null) { 
            setBorder(BorderFactory.createTitledBorder(text));
        } else { 
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));        
        }
        add(textScroll);   

        speech = new Speech(true);
        useSpeech = true;
    }
    
    public void write(String text, boolean speak) {
        if (speak) {
            speech.speak(text);
        }
        textOutput.append(text + "\n");
    }

    public synchronized void setSpeech(boolean on) {
        System.out.println("Set useSpeech to: " + on);
        useSpeech = on;
    }

    public synchronized void write(String text) {
        write(text, useSpeech);
    }    
}
