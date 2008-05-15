package ibis.dog.gui.grid;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JobInfo extends JPanel {

    private static final String totalText     = "Total:";
    private static final String prestageText  = "Prestage:";
    private static final String queuedText    = "Scheduled:";
    private static final String runningText   = "Running:";
    private static final String poststageText = "Poststage:";
    private static final String stoppedText   = "Stopped:";
    
    private final JLabel total     = new JLabel(totalText, JLabel.CENTER);
    private final JLabel prestage  = new JLabel(prestageText, JLabel.CENTER);
    private final JLabel queued    = new JLabel(queuedText, JLabel.CENTER);
    private final JLabel running   = new JLabel(runningText, JLabel.CENTER);
    private final JLabel poststage = new JLabel(poststageText, JLabel.CENTER);
    private final JLabel stopped   = new JLabel(stoppedText, JLabel.CENTER);
    
    private final JLabel totalC     = new JLabel("0", JLabel.CENTER);
    private final JLabel prestageC  = new JLabel("0", JLabel.CENTER);
    private final JLabel queuedC    = new JLabel("0", JLabel.CENTER);
    private final JLabel runningC   = new JLabel("0", JLabel.CENTER);
    private final JLabel poststageC = new JLabel("0", JLabel.CENTER);
    private final JLabel stoppedC   = new JLabel("0", JLabel.CENTER);
    
    private int totalCount;
    private int prestageCount;
    private int queuedCount;
    private int runningCount;
    private int poststageCount;
    private int stoppedCount;
    
    public JobInfo() { 
    
        setLayout(new GridLayout(12,1));
        
        add(total);
        add(totalC);
        
        add(queued);
        add(queuedC);
        
        add(prestage);
        add(prestageC);
        
        add(running);
        add(runningC);
        
        add(poststage);
        add(poststageC);
        
        add(stopped);
        add(stoppedC);
        
        setBorder(BorderFactory.createTitledBorder("Job Status"));
    }

    public synchronized void addTotal() { 
        totalCount++;
        totalC.setText("" + totalCount);
    }
    
    public synchronized void subTotal() { 
        totalCount--;
        totalC.setText("" + totalCount);
    }
  
    public synchronized void addQueued() { 
        queuedCount++;
        queuedC.setText("" + queuedCount);
    }
    
    public synchronized void subQueued() { 
        queuedCount--;
        queuedC.setText("" + queuedCount);
    }
  
    public synchronized void addPrestage() { 
        prestageCount++;
        prestageC.setText("" + prestageCount);
    }
    
    public synchronized void subPrestage() { 
        prestageCount--;
        prestageC.setText("" + prestageCount);
    }
  
    public synchronized void addPostage() { 
        poststageCount++;
        poststageC.setText("" + poststageCount);
    }
    
    public synchronized void subPostage() { 
        poststageCount--;
        poststageC.setText("" + poststageCount);
    }
  
    public synchronized void addRunning() { 
        runningCount++;
        runningC.setText("" + runningCount);
    }
    
    public synchronized void subRunning() { 
        runningCount--;
        runningC.setText("" + runningCount);
    }
  
    public synchronized void addStopped() { 
        stoppedCount++;
        stoppedC.setText("" + stoppedCount);
    }
    
    public synchronized void subStopped() { 
        stoppedCount--;
        stoppedC.setText("" + stoppedCount);
    }
       
}
