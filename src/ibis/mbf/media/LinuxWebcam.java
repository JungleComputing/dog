package ibis.mbf.media;

public class LinuxWebcam extends Media {

    private static final String LIB = "libWebcam.so";
    private static boolean libraryInitialized = false;
    
    private synchronized static void initialiseLibrary() { 

        if (libraryInitialized) { 
            return;
        }
        
        boolean done = false;
        
        if (!done) { 
            String lib = System.getProperty("user.dir") + "/" + LIB;

            try { 
                System.load(lib);
                done = true;
            } catch (Throwable e) { 
                System.err.println("Failed to load " + lib);
                e.printStackTrace(System.err);
            }
        }

        if (!done) { 
            String lib = System.getProperty("user.home") + "/" + LIB;

            try { 
                System.load(lib);
                done = true;
            } catch (Throwable e) { 
                System.err.println("Failed to load " + lib);
                e.printStackTrace(System.err);
            }
        }

        if (!done) { 
            try { 
                System.loadLibrary(LIB);
                done = true;
            } catch (Throwable e) { 
                System.err.println("Failed to load native media library");
                e.printStackTrace(System.err);
            }
        }
        
        if (!done) { 
            throw new Error("Failed to load windows webcam library!!!");
        } 
 
        libraryInitialized = true;
    }

    
    private native boolean initWebcam(String device, int width, int height);
    private native boolean nextImageRGB24(byte [] dest);
    
    public LinuxWebcam(MediaConsumer consumer, String device, int width, 
            int height, int buffers, int delay) {        
        
        super(consumer, width, height, delay);
        
        initialiseLibrary();
        
        System.out.println("Creating webcam " + width + "x" + height + "x" 
                + buffers);        
        
        // FIXME: hardcoded to /dev/video0!!
        if (initWebcam("/dev/video0", width, height)) {
            System.out.println("LinuxWebcam initialized");
            initialized(true);
        } else { 
            System.out.println("Failed to init webcam!"); 
            initialized(false);
        }
    }

    public LinuxWebcam(String device, int width, int height) {        
        
        super(width, height);
        
        initialiseLibrary();
        
        System.out.println("Creating webcam " + width + "x" + height);        

        // FIXME: hardcoded to /dev/video0!!
        if (initWebcam("/dev/video0", width, height)) {
            System.out.println("LinuxWebcam initialized");
            initialized(true);
        } else { 
            System.out.println("Failed to init webcam!"); 
            initialized(false);
        }
    }
    
    public boolean nextImage(byte [] pixels) { 
        return nextImageRGB24(pixels);
    }
}
