package ibis.dog.gui.application;

import java.util.LinkedList;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Speech implements Runnable {
    
    private Voice myVoice;

    private boolean done = false;
    
    private LinkedList<String> lines = new LinkedList<String>();
    
    public Speech(boolean start) {
        try { 
            System.out.println("Initializing speech synthesizer...");

            String voiceName = "kevin16";
            VoiceManager voiceManager = VoiceManager.getInstance();
            myVoice = voiceManager.getVoice(voiceName);

            if (myVoice == null) {
                System.err.println("Can not find voice: " + voiceName);
                return;
            }

            myVoice.allocate();

        } catch (Throwable e) { 
            System.err.println("Failed to init voice!");
            e.printStackTrace(System.err);
        }
        
        if (start) { 
            new Thread(this).start();
        }
    }

    public synchronized void speak(String text) { 
        lines.addLast(text);
        notifyAll();
    }
    
    public synchronized String nextLine() { 

        while (!done && lines.size() == 0) { 
            try { 
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    
        if (lines.size() > 0) { 
            return lines.removeFirst();
        } else { 
            return null;
        }

    }
    
    public synchronized void done() { 
        done = true;
        notifyAll();
    }
    
    public synchronized boolean getDone() { 
        return done;
    }
    
    public void run() { 
        
        while (!getDone()) { 
            String line = nextLine();
            
            if (line != null && myVoice != null) { 
                    myVoice.speak(line);
            }
        }
        
        System.out.println("Speech done!");
    }
}
