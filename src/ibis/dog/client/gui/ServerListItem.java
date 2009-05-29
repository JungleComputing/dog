package ibis.dog.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.dog.client.ServerHandler;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class ServerListItem extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final ServerHandler handler;
    private final JButton button;
    private final JLabel label;
    
    private static final ImageIcon playIcon;
    private static final ImageIcon stopIcon;
    
    static {
        playIcon = createImageIcon("/images/media-playback-start.png", "");
        stopIcon = createImageIcon("/images/media-playback-stop.png", "");
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = ServerListItem.class.getResource(path);
        // URL imgURL = null;
        // try {
        // imgURL = new URL("file:" + path);
        // } catch (MalformedURLException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            return null;
        }
    }
    
    /**
     * Returns an JButton, or null if the path was invalid.
     * 
     * @param buttonText
     */
    public static JButton createImageButton(ImageIcon icon,
            String buttonText) {
        JButton result = new JButton(buttonText, icon);
        result.setHorizontalAlignment(SwingConstants.LEFT);
        result.setMargin(new Insets(2, 2, 2, 2));
        result.setVerticalTextPosition(AbstractButton.CENTER);
        result.setHorizontalTextPosition(AbstractButton.TRAILING);
        return result;
    }

    ServerListItem(ServerHandler handler) {
        this.handler = handler;
        setPreferredSize(new Dimension(200, 30));
        setMinimumSize(new Dimension(200, 30));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel item = new JPanel();
        item.setOpaque(true);
        item.setBackground(Color.WHITE);

        item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
        
        button = createImageButton(
                playIcon, null);
        button.addActionListener(this);
        item.add(button);
        item.add(Box.createRigidArea(new Dimension(5, 28)));
        label = new JLabel(handler.getName());
        label.setForeground(Color.BLACK);
        item.add(label);
        item.setAlignmentX(LEFT_ALIGNMENT);
        
        add(item);
        
        add(new JSeparator(SwingConstants.HORIZONTAL));

        setAlignmentX(LEFT_ALIGNMENT);
        
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    public void actionPerformed(ActionEvent e) {

        if (!handler.isEnabled()) {
            handler.setEnabled(true);
            button.setIcon(stopIcon);
            label.setForeground(Color.decode("#16B400")); //GREENISH
        } else {
            handler.setEnabled(false);
            button.setIcon(playIcon);
            label.setForeground(Color.BLACK);
        }
    }

}
