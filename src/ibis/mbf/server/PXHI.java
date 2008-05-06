package ibis.mbf.server;

import java.util.Arrays;

public class PXHI {

    public static final String PXHI_LIB = "libPXHI.so";

    // Interfacing Parallel-Horus functionality
    public synchronized native void initPXHIsystem(int x, int y, int z,
                                        int root, int io,
                                        int runParallel, int runLazy);
                                        
    public synchronized native int  exitPXHISystem();
    public synchronized native void abortPXHISystem();
    public synchronized native int  getNrCPUs();
    public synchronized native int  getMyCPU();


    // Interfacing Object Recognition functionality
    public synchronized native void doRecognize(int width, int height,
                                        byte [] pixels, double[] params);
    public synchronized native int  informAll(int value);
    public synchronized native int  getNrInvariants();
    public synchronized native int  getNrReceptiveFields();


    // Interfacing TRECVID 2004 functionality
    public synchronized native void doTrecLabeling(int width,
                            int height, byte [] pixels);


    // Static methods to get a PXHI instance.
    private static PXHI instance;

    private static boolean libraryInitialized = false;
    
    private synchronized static void initialiseLibrary() { 

        if (libraryInitialized) { 
            return;
        }
        
        boolean done = false;

        if (!done) { 
            String lib = System.getProperty("user.dir") + "/" + PXHI_LIB;

            try { 
                System.load(lib);
                done = true;
            } catch (Throwable e) { 
                System.err.println("Failed to load " + lib);
                e.printStackTrace(System.err);
            }
        }

        if (!done) { 
            String lib = System.getProperty("user.home") + "/" + PXHI_LIB;

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
                System.loadLibrary(PXHI_LIB);
                done = true;
            } catch (Throwable e) { 
                System.err.println("Failed to load: " + PXHI_LIB);
                e.printStackTrace(System.err);
            }
        }
        
        if (!done) { 
            throw new Error("Failed to load server library!!!");
        } 
        
        libraryInitialized = true;
    }

    
    public static synchronized PXHI create() {
    
        if (instance == null) {
            initialiseLibrary();
            instance = new PXHI();
        }
        
        return instance;
    }
    
    // Test routine
    public static void main(String [] args) { 
        
        System.out.println("Started: " + Arrays.deepToString(args));
        
        PXHI p = create();
    
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[0]);
        int z = Integer.parseInt(args[0]);
        int root = Integer.parseInt(args[0]);
        int io = Integer.parseInt(args[0]);
        int parallel = Integer.parseInt(args[0]);
        int lazy = Integer.parseInt(args[0]);
        
        p.initPXHIsystem(x, y, z, root, io, parallel, lazy);
        
        int rank = p.getMyCPU();
        
        if (rank == 0) { 
            
            int size = p.getNrCPUs(); 
            System.err.println("I have " + size + " cpus!");
        }
        
        p.exitPXHISystem();
    }
}

