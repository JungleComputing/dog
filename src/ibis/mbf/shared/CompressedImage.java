package ibis.mbf.shared;


import ibis.mbf.media.ImageUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import java.io.Serializable;
import java.io.IOException;

import com.sun.image.codec.jpeg.ImageFormatException;



public class CompressedImage implements Serializable
{
	public int width  = 0;
	public int height = 0;
	public byte [] cdata = null;

	public CompressedImage(Image image)
	{
		width  = image.width;
		height = image.height;
		BufferedImage bimg = new BufferedImage(width, height,
											BufferedImage.TYPE_INT_RGB);
		DataBufferInt db =
						(DataBufferInt)bimg.getRaster().getDataBuffer();
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


	public Image uncompress()
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
			return new Image(width, height, data);

		} catch (ImageFormatException ife) {
			System.out.println("CompressedImage: ImageFormatException");
		} catch (IOException ioe) {
			System.out.println("CompressedImage: IOException");
		}
		return null;
	}
}
