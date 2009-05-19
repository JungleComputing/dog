package ibis.dog.client.cli;

import ibis.dog.client.Client;
import ibis.imaging4j.Image;
import ibis.util.ThreadPool;

public class FakeVideoProvider implements Runnable {
    
    private Client client;
    private Image image;
    private double fps;

    public FakeVideoProvider(Client client, Image image, double fps) {
        this.client = client;
        this.image = image;
        this.fps = fps;
        
        ThreadPool.createNew(this, "fake video provider @ " + fps + "fps");
    }

    @Override
    public void run() {
        while(true) {
            client.gotImage(image);
            try {
                Thread.sleep((long) (1000 / fps));
            } catch (InterruptedException e) {
                //IGNORE
            }
        }
        
    }

}
