package ibis.dog.cli;

import ibis.dog.client.Client;
import ibis.dog.client.ServerHandler;
import ibis.dog.server.Server;
import ibis.imaging4j.Format;
import ibis.imaging4j.Image;


public class Main {
    
//    public static RGB32Image load(File file) throws IOException { 
//        
//        BufferedImage image = ImageIO.read(file);
//        
//        final int width = image.getWidth();
//        final int height = image.getHeight();
//        
//        int [] argbs = new int[width*height];
//        
//        image.getRGB(0, 0, width, height, argbs, 0, width);
//        
//        return new RGB32Image(width,height,argbs);
//}
    
    
    private static final class ShutDown extends Thread {
        private final Client client;

        ShutDown(Client client) {
            this.client = client;
        }

        public void run() {
            client.end();
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) { 
                System.err.println("Usage Main.main <image.png>");
                System.exit(1);	
            }

            //FIXME: actually load a picture here!
            Image image = new Image(Format.RGB24, Server.IMAGE_WIDTH, Server.IMAGE_HEIGHT);

            Client client = new Client(null);
            
            // Install a shutdown hook that terminates ibis.
            Runtime.getRuntime().addShutdownHook(new Main.ShutDown(client));
            
            //supply a single image
            client.gotImage(image);
            
            //do nothing
            while(true) {
                Thread.sleep(1000);
                System.err.println("Frames/Sec = " + client.getAndResetFPS());
                
                for(ServerHandler handler: client.getServers()) {
                    handler.setEnabled(true);
                }
            }
            
        } catch (Exception e) {
            System.out.println("FATAL");
            e.printStackTrace();
        }
    } 
}
