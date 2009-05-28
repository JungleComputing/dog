package ibis.dog.client.gui;

import ibis.dog.client.ServerHandler;
import ibis.dog.client.ServerListener;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerPanel extends JPanel implements 
        ServerListener {

    private static final Logger logger = LoggerFactory
            .getLogger(ServerPanel.class);

    private static final long serialVersionUID = 1L;

    private final JPanel serverList;
    private final Map<ServerHandler, ServerListItem> servers;

    // private final JLabel[] labels;
    //
    // private final JButton[] buttons;
    //
    // private final ServerHandler[] servers;

    public ServerPanel() {
        servers = new HashMap<ServerHandler, ServerListItem>();

        setBorder(BorderFactory.createTitledBorder("Servers"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        setPreferredSize(new Dimension(200, 410));
        setMaximumSize(new Dimension(200, 410));

        serverList = new JPanel();
        serverList.setLayout(new BoxLayout(serverList, BoxLayout.Y_AXIS));
        serverList.add(Box.createVerticalGlue());
        // serverList.setOpaque(true);
        // serverList.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(serverList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane);
    }

    @Override
    public synchronized void newServer(ServerHandler handler) {
        ServerListItem item = new ServerListItem(handler);

        serverList.add(item, 0);

        servers.put(handler, item);
        
        serverList.validate();
        validate();
        serverList.repaint();
        repaint();
    }

    @Override
    public synchronized void serverGone(ServerHandler handler) {
        JPanel panel = servers.remove(handler);

        if (panel != null) {
            logger.debug("removing " + handler + " from list");

            serverList.remove(panel);
        }
        
        serverList.validate();
        validate();
        serverList.repaint();
        repaint();
    }

}
