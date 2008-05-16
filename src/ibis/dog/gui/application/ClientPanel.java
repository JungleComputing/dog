package ibis.dog.gui.application;

import ibis.dog.client.Client;
import ibis.dog.gui.console.OutputFrame;
import ibis.dog.gui.grid.GridFrame;
import ibis.dog.gui.network.SmartSocketsFrame;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class ClientPanel extends JPanel 
    implements ActionListener, WindowListener {

    // Generated
    private static final long serialVersionUID = 7697445736367043254L;

    private static final String NETWORK_TOPOLOGY = "Network Topology";
    private static final String GRID_MANAGER     = "Grid Manager";
    private static final String CONSOLE_OUTPUT   = "Console Output";
    
    private static final int MAX_SERVERS = 10;
    
    private final Client client;
    
    private SmartSocketsFrame networkTopology;
    private GridFrame grid;    
    private OutputFrame output;
    
    private final JFrame frame;
    
    private JMenuBar menuBar;
    
   // private VideoStream videoStream;
    
    private ApplicationInfo applicationInfo;
    private ControlPanel controlPanel;
    private OutputPanel outputPanel;
    private ServerInfo serverInfo;
    
    public ClientPanel(JFrame frame, Client application) { 
        
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.frame = frame;
        this.client = application;
     
        createMenuBar();
        
        outputPanel = new OutputPanel("Application Output");
        controlPanel = new ControlPanel(outputPanel, client);
        
        applicationInfo = new ApplicationInfo();
        serverInfo = new ServerInfo(MAX_SERVERS, applicationInfo);
        
        JPanel IOPanel = new JPanel();
        IOPanel.setLayout(new BoxLayout(IOPanel, BoxLayout.Y_AXIS));
        IOPanel.add(controlPanel);
        IOPanel.add(outputPanel);
        
        // Combine video and IOPanel into 1 panel
        JPanel videoAndInfo = new JPanel();
        videoAndInfo.setLayout(new GridLayout(1,3));
        videoAndInfo.add(new VideoPanel(applicationInfo, application));
        videoAndInfo.add(IOPanel);
        videoAndInfo.add(serverInfo);
        
        add(videoAndInfo);
        add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Create the info panel
        add(new InfoPanel(applicationInfo));
        add(Box.createRigidArea(new Dimension(0,5)));
        add(new LogoPanel());
        
        outputPanel.write("Voice initialized", true);
        client.registerListener(serverInfo);
    }
    
    private void createMenuBar() { 

        menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("Views");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription(
                "Views");
        menuBar.add(menu);
       
        JMenuItem menuItem = new JMenuItem(GRID_MANAGER);
        menuItem.setMnemonic(KeyEvent.VK_G);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem(NETWORK_TOPOLOGY);
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem(CONSOLE_OUTPUT);
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        
        frame.setJMenuBar(menuBar);
        
    }
    
    public void actionPerformed(ActionEvent e) {
        
        String cmd = e.getActionCommand();
        
        if (cmd.equals(GRID_MANAGER)) { 
            
            if (grid == null) { 
                
                // Dummy object
                //GridRunner gr = new GridRunner();
                //Grid g = Grid.loadGrid("./grids/DAS-3_globus.grid");
                
                grid = new GridFrame(client.getDeployment());
            }
            
        } else if (cmd.equals(NETWORK_TOPOLOGY)) { 
            
            if (networkTopology == null) { 
                networkTopology = 
                    new SmartSocketsFrame(client.getDeployment().getHubAddresses());
            }
  
      } else if (cmd.equals(CONSOLE_OUTPUT)) { 
            
            if (output == null) { 
                output = new OutputFrame();
            }
            
        } else { 
            System.out.println("Unknown action " + cmd);
        } 
    }
    
    private void exit() { 
        
        System.out.println("Exit called!");
        
        if (networkTopology != null) { 
            networkTopology.exit();
        }
        
        if (grid != null) { 
            grid.dispose();
        }

        outputPanel.exit();
        
        client.done();
        
        // Note: output should go last!
        if (output != null) { 
            output.dispose();
        }
        
        System.exit(0);
    }
    
    
    public void windowClosed(WindowEvent arg0) {
        exit();
    }

    public void windowClosing(WindowEvent arg0) {
        exit();
    }

    public void windowDeactivated(WindowEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public void windowDeiconified(WindowEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public void windowIconified(WindowEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public void windowOpened(WindowEvent arg0) {
        // TODO Auto-generated method stub
        
    }

   
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI(Client app) {
        //Create and set up the window.
        JFrame frame = new JFrame("Application Control");
        
        //Create and set up the content pane.
        ClientPanel content = new ClientPanel(frame, app);
        content.setOpaque(true); //content panes must be opaque
        frame.setContentPane(content);

        frame.addWindowListener(content);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    
    
    }

    public static void createGUI(final Client app) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this client's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(app);
            }
        });
    }

    public void windowActivated(WindowEvent arg0) {
        // TODO Auto-generated method stub
        
    }



}
      