package gui.application;

import gui.grid.Grid;
import gui.grid.GridFrame;
import gui.grid.GridRunner;
import gui.network.SmartSocketsFrame;
import gui.output.OutputFrame;
import ibis.smartsockets.direct.DirectSocketAddress;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import app.Application;

public class ClientGUI extends JPanel implements ActionListener {

    // Generated
    private static final long serialVersionUID = 7697445736367043254L;

    private static final String NETWORK_TOPOLOGY = "Network Topology";
    private static final String GRID_MANAGER     = "Grid Manager";
    private static final String CONSOLE_OUTPUT   = "Console Output";
    
    private static final int MAX_SERVERS = 10;
    
    private final Application application;
    
    private SmartSocketsFrame networkTopology;
    private GridFrame grid;    
    private OutputFrame output;
    
    private final JFrame frame;
    
    private JMenuBar menuBar;
    
    private VideoStream videoStream;
    
    private ApplicationInfo applicationInfo;
    private ControlPanel controlPanel;
    private OutputPanel outputPanel;
    private ServerInfo serverInfo;
    
    public ClientGUI(JFrame frame, Application application) { 
        
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.frame = frame;
        this.application = application;
     
        createMenuBar();
        
        controlPanel = new ControlPanel();
        outputPanel = new OutputPanel("Application Output");
        applicationInfo = new ApplicationInfo();
        serverInfo = new ServerInfo(MAX_SERVERS);
        
        JPanel IOPanel = new JPanel();
        IOPanel.setLayout(new BoxLayout(IOPanel, BoxLayout.Y_AXIS));
        IOPanel.add(controlPanel);
        IOPanel.add(outputPanel);
        
        // Combine video and IOPanel into 1 panel
        JPanel videoAndInfo = new JPanel();
        videoAndInfo.setLayout(new GridLayout(1,3));
        videoAndInfo.add(new VideoPanel(applicationInfo));
        videoAndInfo.add(IOPanel);
        videoAndInfo.add(serverInfo);
        
        add(videoAndInfo);
        add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Create the info panel
        add(new InfoPanel(applicationInfo));
        add(Box.createRigidArea(new Dimension(0,5)));
        add(new LogoPanel());
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
                GridRunner gr = new GridRunner();
                Grid g = Grid.loadGrid("./grids/DAS-3_globus.grid");
                
                grid = new GridFrame(gr, g);
            }
            
        } else if (cmd.equals(NETWORK_TOPOLOGY)) { 
            
            if (networkTopology == null) { 
                
                List<DirectSocketAddress> tmp = new LinkedList<DirectSocketAddress>();
                tmp.add(application.getHubAddress());
                
                networkTopology = new SmartSocketsFrame(tmp);
            }
  
      } else if (cmd.equals(CONSOLE_OUTPUT)) { 
            
            if (output == null) { 
                output = new OutputFrame();
            }
            
        } else { 
            System.out.println("Unknown action " + cmd);
        } 
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI(Application app) {
        //Create and set up the window.
        JFrame frame = new JFrame("Application Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Create and set up the content pane.
        JComponent content = new ClientGUI(frame, app);
        content.setOpaque(true); //content panes must be opaque
        frame.setContentPane(content);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void createGUI(final Application app) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(app);
            }
        });
    }
}
      