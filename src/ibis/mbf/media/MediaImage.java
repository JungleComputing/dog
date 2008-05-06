package ibis.mbf.media;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;


public class MediaImage extends Media
{
    // private BufferedImage image;
    private byte [] image;

    
    public MediaImage(MediaConsumer consumer, int width, int height,
					  int delay, String filename)
	{
        super(consumer, width, height, delay);
        generateImage(filename);
        initialized(true);
    }

    
    public MediaImage(int width, int height, String filename)
	{
        super(width, height);
        generateImage(filename);
        initialized(true);
    }
    
    
    private void generateImage(String filename)
	{ 
        try { 
/*
            BufferedImage bi = ImageIO.read(new File(filename));
            bi = ImageUtils.scale(bi, width, height, false);
            image = new byte[3*width*height];
            bi.getData().getDataElements(0, 0, width, height, image);
*/
			BufferedImage bi = ImageIO.read(new File(filename));
			bi = ImageUtils.scale(bi, width, height, false);
			int[] argbs = new int[width*height];
			bi.getRGB(0,0, width, height, argbs, 0, width);
			image = new byte[3*width*height];
			for (int i=0; i<width*height; i++) {
				image[i*3]   = (byte)((argbs[i] & 0x00FF0000) >> 16);
				image[i*3+1] = (byte)((argbs[i] & 0x0000FF00) >> 8);
				image[i*3+2] = (byte)((argbs[i] & 0x000000FF));
			}
        } catch (Exception e) {
            System.err.println("Failed to load test card!");
        }
    }


    public boolean nextImage(byte [] pixels)
	{
        try {
            if (image != null) { 
                System.arraycopy(image, 0, pixels, 0, 
                        Math.min(image.length, pixels.length));
            } else { 
                // generate noise....
                for (int i=0;i<pixels.length;i+=3) {
                    pixels[i] = (byte)(Math.random ()*255);
                    pixels[i+1] = (byte)(Math.random ()*255);
                    pixels[i+2] = (byte)(Math.random ()*255);
                    //pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
                }
            }
        } catch (Exception e) { 
            System.err.println("MediaImage: Failed to generate pixels!" + e);
            return false;
        }
        
        return true;
    }
}
