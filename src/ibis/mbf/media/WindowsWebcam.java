package ibis.mbf.media;

public abstract class WindowsWebcam extends Media {
    
    private static boolean libraryInitialized = false;
    
   // private byte [] tmp;
    
    private int w;
    private int h;
    
    protected abstract int OpenVideo(String device);
    protected abstract int GetFrameWidth();
    protected abstract int GetFrameHeight();
    protected abstract void NextFrame();
    protected abstract void GetFrameData(byte [] pixels);
    
    protected WindowsWebcam(MediaConsumer consumer, int width, int height, int delay) {
        super(consumer, width, height, delay);

        initialiseLibrary();
        
        System.out.println("Creating windows webcam " + width + "x" + height);
        
        OpenVideo("camera");
        
        w = GetFrameWidth();
        h = GetFrameHeight();
        
     //   tmp = new byte[w*h*4];

        System.out.println("Camera output " + w + "x" + h);
        
        initialized(true);
    }

    protected WindowsWebcam(int width, int height) {
        super(width, height);

        initialiseLibrary();
        
        System.out.println("Creating windows webcam " + width + "x" + height);
        
        OpenVideo("camera");
        
        w = GetFrameWidth();
        h = GetFrameHeight();
        
//        tmp = new byte[w*h*4];

        System.out.println("Camera output " + w + "x" + h);
        
        initialized(true);
    }

    
    @Override
    public boolean nextImage(byte [] pixels) {
        
        NextFrame();
        GetFrameData(pixels);
        /*
        int index = 0;
        
        for (int i=0;i<w*h*3;i++) { 
            pixels[i] = 0xFF << 24 | 
                        (((int) tmp[index]) & 0xFF) << 16 | 
                        (((int) tmp[index+1]) & 0xFF) << 8 | 
                        (((int) tmp[index+2]) & 0xFF);
            index += 3;
        }*/
        
        return true;
    }   
    
    
    
    private synchronized static void initialiseLibrary() { 

        if (libraryInitialized) { 
            return;
        }
        
        boolean done = false;

        String sep = java.io.File.separator;
        
        if (!done) { 
            String lib = System.getProperty("user.dir") + sep + "nativemedia.dll";

            try { 
                System.load(lib);
                done = true;
            } catch (Throwable e) { 
                System.err.println("Failed to load " + lib);
                e.printStackTrace(System.err);
            }
        }

        if (!done) { 
            String lib = System.getProperty("user.home") + sep +"nativemedia.dll";

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
                System.loadLibrary("nativemedia");
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

}
