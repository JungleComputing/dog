/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.imageio.ImageIO;


public class JxImage
{
	/*** Private Properties *******************************************/

	private int		width	= 0;
	private int		height	= 0;
	private byte[]	data	= null;


	/*** Public Methods ***********************************************/

	public void readFile(String filename)
	{
		try {
			File file = new File(filename);
			BufferedImage img = ImageIO.read(file);
			width  = img.getWidth();
			height = img.getHeight();
			int[] argbs = new int[width*height];
			img.getRGB(0, 0, width, height, argbs, 0, width);
			data = new byte[width*height*3];
			for (int i=0; i<width*height; i++) {
				data[i*3]   = (byte)((argbs[i] & 0x00FF0000) >> 16);
				data[i*3+1] = (byte)((argbs[i] & 0x0000FF00) >> 8);
				data[i*3+2] = (byte)((argbs[i] & 0x000000FF));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void writeFile(String filename)
	{
		BufferedImage bimg = new BufferedImage(width, height,
											BufferedImage.TYPE_INT_RGB);
		DataBufferInt db =
						(DataBufferInt)bimg.getRaster().getDataBuffer();
		int[] pixels = db.getData(0);
		for (int i=0; i<width*height; i++) {
			int r = data[i*3]   & 0xFF;
			int g = data[i*3+1] & 0xFF;
			int b = data[i*3+2] & 0xFF;
			pixels[i] = (r << 16) + (g << 8) + b;
		}
		int pos = filename.indexOf(".");
		String type = filename.substring(pos+1);
		File file = new File(filename);

		try {
			ImageIO.write(bimg, type, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public int getWidth()
	{
		return width;
	}


	public int getHeight()
	{
		return height;
	}


	public byte[] getData()
	{
		return data;
	}


	public void setData(int w, int h, byte[] array)
	{
		width = w;
		width = h;
		data  = array;
	}
}
