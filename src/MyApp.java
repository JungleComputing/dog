import ibis.mbf.client.Client;
import ibis.mbf.client.gui.ClientWindow;
import ibis.mbf.media.MediaConsumer;
import ibis.mbf.media.WindowsWebcam;

public class MyApp extends WindowsWebcam {

    /* FIXME FIXME FIXME FIXME FIXME 
    /* This is a BIG HACK!!! We have no alternative at the moment, since we 
     * don't have any other way to access the native methods below!!!
     * 
     * Nevertheless.. a solid 10.0 on the horrible-code-o-meter!!! 
     */
    public native int OpenVideo(String filename);
    public native int GetFrameNr();
    public native int GetFrameWidth();
    public native int GetFrameHeight();
    public native int LastFrame();
    public native void GetFrameData(byte [] pixels);
    public native void NextFrame();
    public native void GotoFrame();

    public MyApp(MediaConsumer consumer, int width, int height, int delay) {
        super(consumer, width, height, delay);
    }
   
    public MyApp(int width, int height) {
        super(width, height);
    }
    
    public static void main(String [] args) {
        
        try {
            ClientWindow w = new ClientWindow();
            
            Client c = new Client(new MyMediaFactory());            
        
            w.setClient(c);
            
            c.run();
            
        } catch (Throwable e) { 
            System.err.println("Client died unexpectedly!");
            e.printStackTrace(System.err);
        } 
    } 
}
