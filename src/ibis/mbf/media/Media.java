package ibis.mbf.media;

public abstract class Media extends Thread {
    
    protected final MediaConsumer consumer;
    protected final int width;
    protected final int height;
    protected final int delay;
    
    private final byte [] pixels;
    
    private boolean initialized = false;
    private boolean hasImages = false;
    
    private boolean succes = false;
    
    protected Media(MediaConsumer consumer, int width, int height, int delay) {
        this.consumer = consumer;
        this.width = width;
        this.height = height;
        this.delay = delay;
        
        this.pixels = new byte[3 * width * height];
    
        start();
    }
    
    protected Media(int width, int height) {
        this.width = width;
        this.height = height;

        this.pixels = new byte[3 * width * height];

        this.delay = -1;
        this.consumer = null;        
    }
    
    public abstract boolean nextImage(byte [] pixels);
    
    protected synchronized void initialized(boolean succes) { 
        this.initialized = true;
        this.succes = succes;
        this.hasImages = true;
        notifyAll();
    }
    
    protected synchronized void done() { 
        hasImages = false;
    }
    
    public synchronized boolean hasImages() { 
        return hasImages;
    }
    
    private synchronized void waitForInitialization() {
        
        while (!initialized) { 
            try { 
                wait();
            } catch (Exception e) {
                // ignore
            }
        }
    }
    
    public void run() { 

        waitForInitialization();
        
        if (!succes) { 
            return;
        }
        
        System.out.println("Media running");
           
        int i=0;
        
        System.err.println("Got pixels!" + i++);
        
        while (true) {                   
            
            if (nextImage(pixels)) {
                consumer.gotImage(pixels, width, height);              
            } else {
                System.out.println("Failed to grab pixels!");
            }

            if (delay > 0) { 
                try { 
                    Thread.sleep(delay);
                } catch (Exception e) { 
                    //ignore
                }
            }
        }
    }
}
