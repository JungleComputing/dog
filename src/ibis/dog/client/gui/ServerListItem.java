package ibis.dog.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.dog.client.ServerHandler;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ServerListItem extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final ServerHandler handler;
    private final JButton button;
    private final JLabel label;
    
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
    public static JButton createImageButton(String path, String description,
            String buttonText) {
        JButton result = new JButton(buttonText, createImageIcon(path,
                description));
        result.setHorizontalAlignment(SwingConstants.LEFT);
        result.setMargin(new Insets(2, 2, 2, 2));
        result.setVerticalTextPosition(AbstractButton.CENTER);
        result.setHorizontalTextPosition(AbstractButton.TRAILING);
        result.setToolTipText(description);
        return result;
    }

    ServerListItem(ServerHandler handler) {
        this.handler = handler;
        setPreferredSize(new Dimension(200, 30));
        setMinimumSize(new Dimension(200, 30));
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        button = createImageButton(
                "/images/media-playback-start.png", null, null);
        button.addActionListener(this);
        add(button);
        add(Box.createRigidArea(new Dimension(5, 28)));
        label = new JLabel(handler.getName());
        label.setForeground(Color.RED);
        add(label);
        setAlignmentX(LEFT_ALIGNMENT);
        // panel.setOpaque(true);
        // panel.setBackground(Color.WHITE);

    }

    public void actionPerformed(ActionEvent e) {

        if (!handler.isEnabled()) {
            handler.setEnabled(true);
            button.setText("D");
            label.setForeground(Color.decode("#16B400")); //GREENISH
        } else {
            handler.setEnabled(false);
            button.setText("C");
            label.setForeground(Color.RED);
        }
    }

}
