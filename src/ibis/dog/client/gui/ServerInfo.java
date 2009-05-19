package ibis.dog.client.gui;

import ibis.dog.client.ServerHandler;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerInfo extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory
            .getLogger(ServerInfo.class);

    // Generated
    private static final long serialVersionUID = 1L;

    private static final String CONNECT = "Connect";

    private static final String DISCONNECT = "Disconnect";

    private static final String UNUSED = "- unused -";

    private final JLabel[] labels;

    private final JButton[] buttons;

    private final boolean[] used;

    private final ServerHandler[] data;

    private final int maxServers;

    private final ServerCountConsumer counter;

    public ServerInfo(int maxServers, ServerCountConsumer counter) {

        this.maxServers = maxServers;
        this.counter = counter;

        setMinimumSize(new Dimension(200, 200));

        setLayout(new GridLayout(maxServers, 2));

        labels = new JLabel[maxServers];
        buttons = new JButton[maxServers];
        used = new boolean[maxServers];
        data = new ServerHandler[maxServers];

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

        ServerHandler s = data[index];

        if (!s.isEnabled()) {
            s.setEnabled(true);
            button.setText(DISCONNECT);

            if (counter != null) {
                counter.addActiveServer();
            }

        } else {
            s.setEnabled(false);
            button.setText(CONNECT);

            if (counter != null) {
                counter.removeActiveServer();
            }
        }
    }

}
