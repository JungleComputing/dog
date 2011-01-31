package ibis.dog.client;

/**
 * Interface for notifying a user interface of changes to the list of available
 * servers
 * 
 */
public interface ServerListener {

    public void newServer(ServerHandler handler);

    public void serverGone(ServerHandler handler);

    public void serverActive(ServerHandler handler);
}
