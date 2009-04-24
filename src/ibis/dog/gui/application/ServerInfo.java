package ibis.dog.gui.application;

import ibis.dog.client.ClientListener;
import ibis.dog.client.ServerData;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerInfo extends JPanel implements ActionListener,
        ClientListener {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfo.class);

    // Generated
    private static final long serialVersionUID = 1L;

    private static final String CONNECT = "Connect";

    private static final String DISCONNECT = "Disconnect";

    private static final String UNUSED = "- unused -";

    private final JLabel[] labels;

    private final JButton[] buttons;

    private final boolean[] used;

    private final ServerData[] data;

    private final int maxServers;

    private final ServerCountConsumer counter;

    public ServerInfo(int maxServers, ServerCountConsumer counter) {

        this.maxServers = maxServers;
        this.counter = counter;
        
        setMinimumSize(new Dimension(100, 200));
        
        setLayout(new GridLayout(maxServers, 2));

        labels = new JLabel[maxServers];
        buttons = new JButton[maxServers];
        used = new boolean[maxServers];
        data = new ServerData[maxServers];

        for (int i = 0; i < maxServers; i++) {
            labels[i] = new JLabel(UNUSED, JLabel.CENTER);
            buttons[i] = new JButton(CONNECT);
            buttons[i].setEnabled(false);
            buttons[i].addActionListener(this);

            used[i] = false;

            add(labels[i]);
            add(buttons[i]);
        }
    }

    public void addServer(ServerData s) {

        for (int i = 0; i < maxServers; i++) {
            if (!used[i]) {
                used[i] = true;
                data[i] = s;
                labels[i].setText(s.getName());
                buttons[i].setEnabled(true);

                if (counter != null) {
                    counter.addServer();
                }

                return;
            }
        }

        System.out.println("No more server slots available!");
    }

    public void removeServer(ServerData s) {

        for (int i = 0; i < maxServers; i++) {
            if (used[i]) {
                if (data[i].serverID == s.serverID) {
                    used[i] = false;
                    labels[i].setText(UNUSED);
                    buttons[i].setEnabled(false);
                    data[i] = null;

                    if (counter != null) {
                        counter.removeServer();
                    }

                    return;
                }
            }
        }

        System.out.println("Server " + s.getName() + " not found!");
    }

    public void updateServers(ServerData[] servers) {
        if (servers == null) {
            servers = new ServerData[0];
        }
        
        logger.info("Updating server list, now " + servers.length);


        LinkedList<ServerData> add = new LinkedList<ServerData>();
        LinkedList<ServerData> remove = new LinkedList<ServerData>();

        for (ServerData s : servers) {

            boolean found = false;

            for (int i = 0; i < maxServers; i++) {
                if (used[i] && data[i].serverID == s.serverID) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                add.add(s);
            }
        }

        for (ServerData s : add) {
            addServer(s);
        }

        for (int i = 0; i < maxServers; i++) {
            if (used[i]) {
                boolean found = false;

                // see if server is still around
                for (ServerData s : servers) {
                    if (data[i].serverID == s.serverID) {
                        found = true;
                    }
                }

                if (!found) {
                    logger
                            .warn("server disappeared from list, removing from GUI");
                    remove.add(data[i]);
                }
            }
        }

        for (ServerData s : remove) {
            logger.warn("removing" + s);
            removeServer(s);
        }

    }

    private int getSourceButton(JButton button) {

        for (int i = 0; i < maxServers; i++) {
            if (button == buttons[i]) {
                return i;
            }
        }

        return -1;
    }

    public void actionPerformed(ActionEvent e) {

        JButton button = (JButton) e.getSource();

        int index = getSourceButton(button);

        if (index < 0) {
            System.out.println("Source button not found!");
            return;
        }

        ServerData s = data[index];

        if (!s.isConnected()) {
            s.setConnected(true);
            button.setText(DISCONNECT);

            if (counter != null) {
                counter.addActiveServer();
            }

        } else {
            s.setConnected(false);
            button.setText(CONNECT);

            if (counter != null) {
                counter.removeActiveServer();
            }
        }
    }

}
