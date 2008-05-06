package ibis.mbf.client.gui;

import ibis.mbf.client.Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.util.LinkedList;

public class ClientWindow implements ActionListener {

    private static final String MEDIA       = "Media";
    private static final String OPEN_FILE   = "Open File";
    private static final String NOISE       = "Noise";
    private static final String OPEN_CAM    = "Open Webcam";    
    private static final String EXIT        = "Exit";
    private static final String HELP        = "Help";
    private static final String ABOUT       = "About MMclient";

    public static final JFrame frame = new JFrame("MMclient Test Case");
    
    private final JFileChooser fc = new JFileChooser();

    private final LinkedList<ClientWindowListener> listeners = 
        new LinkedList<ClientWindowListener>();
    
    private ClientGui gui;
    
    public ClientWindow() {
	
		JMenuBar menuBar   = new JMenuBar();
		JMenu menu         = new JMenu(MEDIA);
        
        JMenuItem menuItem = new JMenuItem(OPEN_CAM);		
        menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem(OPEN_FILE);
        menu.add(menuItem);
        menuItem.addActionListener(this);
        
        menuItem = new JMenuItem(NOISE);
        menu.add(menuItem);
        menuItem.addActionListener(this);
        
        
        menu.addSeparator();
        
        menuItem = new JMenuItem(EXIT);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);

		menu = new JMenu(HELP);
		menuItem = new JMenuItem(ABOUT);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);

        gui = new ClientGui(this);
        
        frame.setJMenuBar(menuBar);
        frame.add(gui);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.pack();
		frame.setVisible(true);
        
        getIbisCommandLine();
    } 
    
    private void getIbisCommandLine() { 
        
        String server = System.getProperty("ibis.server.address");
        String pool = System.getProperty("ibis.pool.name");
        
        IbisConfig ic = new IbisConfig(server, pool);       
        ic.showDialog(frame, null);
        
        server = ic.getAddress();
        pool = ic.getPool();
        
        System.out.println("Server == " + server);
        System.out.println("Pool   == " + pool);
        
        System.setProperty("ibis.server.address", server);
        System.setProperty("ibis.pool.name", pool);
    }  
    

    public void setClient(Client c) { 
        gui.setClient(c);
    }
    
    
	public synchronized void addClientWindowListener(ClientWindowListener l) {
		listeners.add(l);
	}


	public synchronized void removeClientWindowListener(ClientWindowListener l) {
		listeners.remove(l);
	}
    
    private void forwardToListeners(ClientWindowEvent cwe) { 
        
        ClientWindowListener [] tmp;
        
        synchronized (this) {
            tmp = listeners.toArray(new ClientWindowListener[listeners.size()]);
        }
        
        for (ClientWindowListener c : tmp) {
            c.clientWindowFileSelected(cwe);
        }        
    }
    
	public void actionPerformed (ActionEvent e) {

        String cmd = e.getActionCommand();
		
        if (cmd.equals(OPEN_CAM)) {            
            // FIXME: hardcoded to linux!
            forwardToListeners(new ClientWindowEvent(this, "webcam://"));        
        } else if (cmd.equals(OPEN_FILE)) {
			int result = fc.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                forwardToListeners(new ClientWindowEvent(this,
                        fc.getSelectedFile().getPath()));
			}
        } else if (cmd.equals(NOISE)) {
            forwardToListeners(new ClientWindowEvent(this, null));
        } else if (cmd.equals(EXIT)) {
            // TODO: implement
			System.out.println("Action PERFORMED: " + cmd);
		} else if (cmd.equals(ABOUT)) {
			// TODO: implement
            System.out.println("Action PERFORMED: " + cmd);
		}
	}
}
