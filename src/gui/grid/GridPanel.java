package gui.grid;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
    
    private GridMap map;
    private JobInfo jobs;
    
    public GridPanel(JFrame frame, GridRunner runner, Grid grid) {
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        this.frame = frame;        
        
        createMenuBar();
       
        jobs = new JobInfo();
        map = new GridMap(runner, grid);
        
        
        add(map);
        add(Box.createRigidArea(new Dimension(5,0)));
        add(jobs);
        
       
    }

    private void createMenuBar() { 

        menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("Grids");
        menu.setMnemonic(KeyEvent.VK_G);
        menu.getAccessibleContext().setAccessibleDescription(
                "Load a Grid description");
        menuBar.add(menu);
       
        JMenuItem menuItem = new JMenuItem(ADD);
        menuItem.setMnemonic(KeyEvent.VK_A);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem(REMOVE);
        menuItem.setMnemonic(KeyEvent.VK_R);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        frame.setJMenuBar(menuBar);        
    }

    public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub
        
    }
}
