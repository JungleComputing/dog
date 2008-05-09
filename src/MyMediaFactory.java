import ibis.mbf.media.LinuxWebcam;
import ibis.mbf.media.Media;
import ibis.mbf.media.MediaConsumer;
import ibis.mbf.media.MediaFactory;


import ibis.mbf.media.MediaImage;


public class MyMediaFactory implements MediaFactory
{

    /* This is a BIG HACK!!! We have no alternative at the moment since
	 * we do not have any other way to access the native methods in
	 * MyApp!!
     */

    public Media getMedia(MediaConsumer consumer, String description,
            int width, int height, int buffers, int delay)
	{        
        System.err.println("Failed to load media: " + description);
        return null;
    }

    public Media getMedia(String description, int width, int height)
	{
        Media media = null;
        try {
            if (description == null) { 
                description = "file://Images/testbeeld.jpg";
            }
            if (description.equalsIgnoreCase("webcam://")) {
           
                String os = System.getProperty("os.name");
                
                if (os.equalsIgnoreCase("linux")) { 
                    media = new LinuxWebcam(description, width, height);
                } else if (os.equalsIgnoreCase("windows xp")) { 
                    media = new MyApp(width, height);
                } else { 
                    System.err.println("Unknown operating system: " + os);
                    media = null;
                }
            
            } else if (description.toLowerCase().endsWith(".jpg")) {  
                media = new MediaImage(width, height, description);
            } else if (description.equals("Noise")) {  
                media = new MediaImage(width, height, null);
            } else { 
                System.err.println("Unknown media: " + description);
                media = null;
            }
        } catch (Throwable e) {
            System.err.println("Failed to load media: " + description);
            e.printStackTrace(System.err);
            media = null;
        }
        return media;
    }
}
