package ibis.dog.client.gui;

import ibis.dog.client.ServerHandler;
import ibis.dog.client.ServerListener;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerPanel extends JPanel implements ActionListener,
        ServerListener {

    private static final Logger logger = LoggerFactory
            .getLogger(ServerPanel.class);

    private static final long serialVersionUID = 1L;

    private static final int MAX_SERVERS = 20;

    private static final String CONNECT = "Connect";

    private static final String DISCONNECT = "Disconnect";

    private static final String UNUSED = "- unused -";

    private final JLabel[] labels;

    private final JButton[] buttons;

    private final ServerHandler[] servers;

    public ServerPanel() {
        // divide over 2 columns
        GridLayout layout = new GridLayout(MAX_SERVERS / 2, 4); 
        setLayout(layout);
        setBorder(BorderFactory.createTitledBorder("Servers"));
        //setPreferredSize(new Dimension(500,300));

        labels = new JLabel[MAX_SERVERS];
        buttons = new JButton[MAX_SERVERS];
        servers = new ServerHandler[MAX_SERVERS];

        for (int i = 0; i < MAX_SERVERS; i++) {
            labels[i] = new JLabel(UNUSED, JLabel.CENTER);
            buttons[i] = new JButton(CONNECT);
            buttons[i].setEnabled(false);
            buttons[i].addActionListener(this);

            servers[i] = null;

            add(labels[i]);
            add(buttons[i]);
        }
    }

    private synchronized int getSourceButton(JButton button) {

        for (int i = 0; i < MAX_SERVERS; i++) {
            if (button == buttons[i]) {
                return i;
            }
        }

        return -1;
    }

    public synchronized void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();

        int index = getSourceButton(button);

        if (index < 0) {
            System.out.println("Source button not found!");
            return;
        }

        ServerHandler s = servers[index];

        if (!s.isEnabled()) {
            s.setEnabled(true);
            button.setText(DISCONNECT);

        } else {
            s.setEnabled(false);
            button.setText(CONNECT);

        }
    }

    @Override
    public synchronized void newServer(ServerHandler handler) {
        for(int i = 0; i < MAX_SERVERS; i++) {
            if (servers[i] == null) { //free slot
                servers[i] = handler;

                labels[i].setText(handler.getName());
                
                buttons[i].setEnabled(true);
                buttons[i].setText(CONNECT);

                servers[i].setEnabled(false);
                return;
            }
        }
        logger.warn("Could not find free slot for server " + handler);
    }

    @Override
    public synchronized void serverGone(ServerHandler handler) {
        for(int i = 0; i < MAX_SERVERS; i++) {
            if (handler == servers[i]) { //found it
                servers[i] = null;

                labels[i].setText(UNUSED);
                
                buttons[i].setEnabled(false);
                buttons[i].setText(CONNECT);

                return;
            }
        }
    }

}
