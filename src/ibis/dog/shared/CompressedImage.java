package ibis.dog.shared;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.Serializable;

import com.sun.image.codec.jpeg.ImageFormatException;

public class CompressedImage implements Serializable
{
	// Generated
    private static final long serialVersionUID = -7040256139874935059L;
   
    public int width  = 0;
	public int height = 0;
	public byte [] cdata = null;

	public CompressedImage(RGB24Image image)
	{
		width  = image.width;
		height = image.height;
		BufferedImage bimg = new BufferedImage(width, height,
											BufferedImage.TYPE_INT_RGB);
        
		DataBufferInt db = (DataBufferInt)bimg.getRaster().getDataBuffer();
		int[] data = db.getData(0);
		for (int i=0; i<width*height; i++) {
			int r = image.pixels[i*3]   & 0xFF;
			int g = image.pixels[i*3+1] & 0xFF;
			int b = image.pixels[i*3+2] & 0xFF;
			data[i] = (r << 16) + (g << 8) + b;
		}
		try {
			cdata = ImageUtils.encodeJPEG(bimg, 100);
		} catch (ImageFormatException ife) {
			System.out.println("CompressedImage: ImageFormatException");
		} catch (IOException ioe) {
			System.out.println("CompressedImage: IOException");
		}
	}

    public CompressedImage(RGB32Image image)
    {
        width  = image.width;
        height = image.height;
        BufferedImage bimg = new BufferedImage(width, height,
                                            BufferedImage.TYPE_INT_RGB);
        
        DataBufferInt db = (DataBufferInt)bimg.getRaster().getDataBuffer();
        System.arraycopy(image.pixels, 0, db.getData(0), 0, image.pixels.length);
      
        try {
            cdata = ImageUtils.encodeJPEG(bimg, 100);
        } catch (ImageFormatException ife) {
            System.out.println("CompressedImage: ImageFormatException");
        } catch (IOException ioe) {
            System.out.println("CompressedImage: IOException");
        }
    }
    
	public RGB24Image uncompress()
	{
		try {
			BufferedImage bimg = ImageUtils.decodeJPEG(cdata);
			int[] argbs = new int[width*height];
			bimg.getRGB(0, 0, width, height, argbs, 0, width);
			byte [] data = new byte[width*height*3];
			for (int i=0; i<width*height; i++) {
				data[i*3]   = (byte)((argbs[i] & 0x00FF0000) >> 16);
				data[i*3+1] = (byte)((argbs[i] & 0x0000FF00) >> 8);
				data[i*3+2] = (byte)((argbs[i] & 0x000000FF));
			}
			return new RGB24Image(width, height, data);

		} catch (ImageFormatException ife) {
			System.out.println("CompressedImage: ImageFormatException");
		} catch (IOException ioe) {
			System.out.println("CompressedImage: IOException");
		}
		return null;
	}
}
