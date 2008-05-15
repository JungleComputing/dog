package gui.application;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ServerInfo extends JPanel implements ActionListener {

    private final JLabel [] labels;
    private final JButton [] buttons;
    private final boolean [] used;
    
    private final int maxServers; 
    
    public ServerInfo(int maxServers) { 

        this.maxServers = maxServers;
        
        setLayout(new GridLayout(maxServers, 2));
        setBorder(BorderFactory.createTitledBorder("Servers"));
             
        labels = new JLabel[maxServers];
        buttons = new JButton[maxServers];
        used = new boolean[maxServers];
        
        for (int i=0;i<maxServers;i++) { 
            labels[i] = new JLabel("- unused -", JLabel.CENTER);
            buttons[i] = new JButton("Connect");
            buttons[i].setEnabled(false);
            buttons[i].addActionListener(this);
            
            used[i] = false;
      
            add(labels[i]);
            add(buttons[i]);
        }
    }

    public void addServer(String name) throws Exception { 
        
        for (int i=0;i<maxServers;i++) { 
            if (!used[i]) { 
                used[i] = true;
                labels[i].setText(name);
                buttons[i].setEnabled(true);
                return;
            }
        }
        
        throw new Exception("No more server slots available!");
    }
    
    public void removeServer(String name) throws Exception { 
        
        for (int i=0;i<maxServers;i++) { 
            if (used[i]) { 
                if (labels[i].getText().equals(name)) { 
                    used[i] = false;
                    labels[i].setText("- unused -");
                    buttons[i].setEnabled(false);
                    return;
                }
            }
        }
        
        throw new Exception("Server " + name + " not found!");
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("Got action " + e);
    }
    
    
}
