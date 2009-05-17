package ibis.dog.gui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class InfoPanel extends JPanel {

    // Generated
    private static final long serialVersionUID = 1L;

    public InfoPanel(ApplicationInfo info) { 
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(info);
        setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        setMinimumSize(new Dimension(100, 100));
        setBorder(BorderFactory.createTitledBorder("Statistics"));
    }
    
    
}
