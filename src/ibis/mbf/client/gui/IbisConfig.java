package ibis.mbf.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class IbisConfig extends JComponent implements ActionListener {

    private String dummy = "                                              "; 
    
    private String server;
    private String pool;
      
    private JDialog dialog = null;
   
    private JTextField serverField;
    private JTextField poolField;
      
    private JLabel serverLabel = new JLabel("Ibis Server:  ");
    private JLabel poolLabel = new JLabel("Ibis Pool  :  ");
     
    private JButton okButton = new JButton("Connect");
    
    public IbisConfig(String server, String pool) {
        this.server = server;
        this.pool = pool;
        
        if (server == null || server.length() == 0) { 
            server = dummy;
        }
        
        if (pool == null || pool.length() == 0) { 
            pool = dummy;
        }
        
        serverField = new JTextField(server);
        poolField = new JTextField(pool);
    }

    protected JDialog createDialog(Component parent) throws HeadlessException {
        String title = "aap"; // getUI().getDialogTitle(this);

        putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY, 
                title);

        JDialog dialog;

        Window window = JOptionPane.getRootFrame();

        dialog = new JDialog((Frame)window, title, true);   
        dialog.setComponentOrientation(this.getComponentOrientation());

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new GridBagLayout());
         
        
        GridBagConstraints gBC = new GridBagConstraints();
        gBC.fill = GridBagConstraints.HORIZONTAL;

        gBC.insets = new Insets(10,1,0,0);
        gBC.weightx = 0.5;
        gBC.gridx = 0;
        gBC.gridy = 0;

        contentPane.add(serverLabel, gBC);
        
        gBC.gridx = 2;
        gBC.gridy = 0;
        
        contentPane.add(serverField, gBC);

        gBC.weightx = 0.5;
        gBC.gridx = 0;
        gBC.gridy = 1;
        
        contentPane.add(poolLabel, gBC);
        
        gBC.gridx = 2;
        gBC.gridy = 1;
        
        contentPane.add(poolField, gBC);
        
       // gBC.ipady = 40;     //This component has more breadth compared to other buttons
        gBC.weightx = 0.0;
        gBC.gridwidth = 3;
        gBC.gridx = 0;
        gBC.gridy = 2;
        gBC.insets = new Insets(10,0,0,0);
        
        contentPane.add(okButton, gBC);
        okButton.addActionListener(this);
        
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
                UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        return dialog;
    }

    int returnValue = 0;
    
    public void showDialog(Component parent, String approveButtonText) throws HeadlessException {
        
        dialog = createDialog(parent);
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setAddress("", "");
            } 
        });
   
        dialog.show();
    }
    
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == okButton) { 
            server = serverField.getText().trim();
            pool = poolField.getText().trim();
            dialog.dispose();
        }
    }
    
    private synchronized void setAddress(String address, String pool) {
        this.server = address;
        this.pool = pool;
        notifyAll();
    }
    
    public synchronized String getAddress() { 
        
        while (server == null) { 
            try { 
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        return server;
    }
    
    public synchronized String getPool() { 
        
        while (pool == null) { 
            try { 
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        return pool;
    }
}
