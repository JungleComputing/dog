package ibis.dog.gui.grid;

import ibis.dog.client.Deployment;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class GridPanel extends JPanel implements ActionListener {

	private static final Logger logger = Logger.getLogger(GridPanel.class);
	
    // Generated
    private static final long serialVersionUID = 1L;

    private static final String ADD    = "Add";
    private static final String REMOVE = "Remove";
    
    private JFrame frame;
    private JMenuBar menuBar;
    private JFileChooser chooser;
    
    private GridMap map;
  //  private JobInfo jobs;
    
    private JMenuItem add; 
    private JMenuItem remove; 
    
    private Deployment deploy;
    
    public GridPanel(JFrame frame, Deployment deploy) {
        
        this.deploy= deploy;
        this.frame = frame;        
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        createMenuBar();
       
    //    jobs = new JobInfo();
        map = new GridMap(deploy);
         
        add(map);
        //FIXME: add jobs info stats and re-anable job info box
  //      add(Box.createRigidArea(new Dimension(5,0)));
    //    add(jobs);
        
        chooser = new JFileChooser(new File("grids"));
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private void createMenuBar() { 

        menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("Grids");
        menu.setMnemonic(KeyEvent.VK_G);
        menu.getAccessibleContext().setAccessibleDescription(
                "Load a Grid description");
        menuBar.add(menu);
       
        add = new JMenuItem(ADD);
        add.setMnemonic(KeyEvent.VK_A);
        add.addActionListener(this);
        menu.add(add);
        
        remove = new JMenuItem(REMOVE);
        remove.setMnemonic(KeyEvent.VK_R);
        remove.addActionListener(this);
        menu.add(remove);
        
        frame.setJMenuBar(menuBar);        
    }

    public void actionPerformed(ActionEvent e) {
        logger.debug("Got event: " + e);        
        
        if (e.getSource() == add) { 
        	logger.debug("Must ADD grid");  
            
            int returnVal = chooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                //This is where a real application would open the file.
                logger.debug("Must ADD grid " + file.getName());                      
                deploy.loadGrid(file.getAbsolutePath());
                
                map.repaint();
            } 
            
        } else if (e.getSource() == remove) { 
        	logger.debug("Must REMOVE grid");        
        }
    }
}
