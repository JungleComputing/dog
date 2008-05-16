package ibis.dog.gui.grid;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class GridPanel extends JPanel implements ActionListener {

    // Generated
    private static final long serialVersionUID = 1L;

    private static final String ADD    = "Add";
    private static final String REMOVE = "Remove";
    
    private JFrame frame;
    private JMenuBar menuBar;
    private JFileChooser chooser;
    
    private GridMap map;
    private JobInfo jobs;
    
    private JMenuItem add; 
    private JMenuItem remove; 
    
    public GridPanel(JFrame frame, GridRunner runner, Grid grid) {
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        this.frame = frame;        
        
        createMenuBar();
       
        jobs = new JobInfo();
        map = new GridMap(runner, grid);
        
        
        add(map);
        add(Box.createRigidArea(new Dimension(5,0)));
        add(jobs);
        
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
        System.out.println("Got event: " + e);        
        
        if (e.getSource() == add) { 
            System.out.println("Must ADD grid");  
            
            int returnVal = chooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                //This is where a real application would open the file.
                
                System.out.println("Must ADD grid " + file.getName());                      
            } 

            
            
        } else if (e.getSource() == remove) { 
            System.out.println("Must REMOVE grid");        
        }
    }
}
