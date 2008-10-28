package ibis.dog.gui.application;

import ibis.dog.client.ClientListener;
import ibis.dog.client.ServerData;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ServerInfo extends JPanel implements ActionListener, ClientListener {

    // Generated 
    private static final long serialVersionUID = 1L;
  
    private static final String CONNECT    = "Connect";
    private static final String DISCONNECT = "Disconnect";
    
    private static final String UNUSED     = "- unused -";
    
    private final JLabel [] labels;
    private final JButton [] buttons;
    private final boolean [] used;
    private final ServerData [] data;
    
    private final int maxServers; 
    
    private final ServerCountConsumer counter; 
    
    public ServerInfo(int maxServers, ServerCountConsumer counter) { 

        this.maxServers = maxServers;
        this.counter = counter;
        
        setLayout(new GridLayout(maxServers, 2));
        setBorder(BorderFactory.createTitledBorder("Servers"));
             
        labels = new JLabel[maxServers];
        buttons = new JButton[maxServers];
        used = new boolean[maxServers];
        data = new ServerData[maxServers];
        
        for (int i=0;i<maxServers;i++) { 
            labels[i] = new JLabel(UNUSED, JLabel.CENTER);
            buttons[i] = new JButton(CONNECT);
            buttons[i].setEnabled(false);
            buttons[i].addActionListener(this);
            
            used[i] = false;
      
            add(labels[i]);
            add(buttons[i]);
        }
    }

    public void addServer(ServerData s) { 
        
        for (int i=0;i<maxServers;i++) { 
            if (!used[i]) { 
                used[i] = true;
                data[i] = s;
                labels[i].setText(s.getName());
                buttons[i].setEnabled(true);
                
                if (counter != null) { 
                    counter.addServer();
                }
                
                return;
            }
        }
        
        System.out.println("No more server slots available!");
    }
    
    public void removeServer(ServerData s) { 
        
        for (int i=0;i<maxServers;i++) { 
            if (used[i]) { 
                if (data[i].serverID == s.serverID) {
                    used[i] = false;
                    labels[i].setText(UNUSED);
                    buttons[i].setEnabled(false);
                    data[i] = null;
                    
                    if (counter != null) { 
                        counter.removeServer();
                    }

                    return;
                }
            }
        }
        
        System.out.println("Server " + s.getName() + " not found!");
    }

    public void updateServers(ServerData [] servers) {

        if (servers == null || servers.length == 0) { 
            return;
        }

        LinkedList<ServerData> add = new LinkedList<ServerData>();
        LinkedList<ServerData> remove = new LinkedList<ServerData>();
        
        for (ServerData s : servers) { 

            boolean found = false;
            
            for (int i=0;i<maxServers;i++) { 
                if (used[i] && data[i].serverID == s.serverID) {
                    found = true;
                    break;
                }
            }
            
            if (!found) { 
                add.add(s);
            }
        }
        
        // TODO: also remove servers!!
        
        for (ServerData s : add) { 
            addServer(s);
        }
    }
    
    private int getSourceButton(JButton button) { 
        
        for (int i=0;i<maxServers;i++) { 
            if (button == buttons[i]) { 
                return i;
            }
        }
        
        return -1;
    }
    
    public void actionPerformed(ActionEvent e) {

        JButton button = (JButton) e.getSource();
        
        int index = getSourceButton(button);
    
        if (index < 0) { 
            System.out.println("Source button not found!");
            return;
        }
    
        ServerData s = data[index];
        
        if (!s.isConnected()) { 
            s.setConnected(true);
            button.setText(DISCONNECT);
            
            if (counter != null) { 
                counter.addActiveServer();
            }
            
        } else { 
            s.setConnected(false);
            button.setText(CONNECT);
            
            if (counter != null) { 
                counter.removeActiveServer();
            }
        }
    }

    
}