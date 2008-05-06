package ibis.mbf.client.gui;

import java.util.EventListener;

public interface ClientWindowListener extends EventListener {
	public void clientWindowFileSelected(ClientWindowEvent e);
}
