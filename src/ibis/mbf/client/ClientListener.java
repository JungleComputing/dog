package ibis.mbf.client;

public interface ClientListener {

    void updateServers(ServerData [] servers);
    void updateFrame(byte [] pixels, int width, int height, int frame, int id);
    
}
