package ibis.dog.shared;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import com.sun.image.codec.jpeg.ImageFormatException;

public class JPEGImage implements Serializable, ConvertableImage {
    // Generated
    private static final long serialVersionUID = -7040256139874935059L;

    public int width = 0;

    public int height = 0;

    public byte[] cdata = null;

    public JPEGImage(int width, int height, byte[] image) {
        this.width = width;
        this.height = height;
        this.cdata = image;
    }

    public RGB24Image toRGB24() {
        try {
            BufferedImage bimg = ImageUtils.decodeJPEG(cdata);
            int[] argbs = new int[width * height];
            bimg.getRGB(0, 0, width, height, argbs, 0, width);
            byte[] data = new byte[width * height * 3];
            for (int i = 0; i < width * height; i++) {
                data[i * 3] = (byte) ((argbs[i] & 0x00FF0000) >> 16);
                data[i * 3 + 1] = (byte) ((argbs[i] & 0x0000FF00) >> 8);
                data[i * 3 + 2] = (byte) ((argbs[i] & 0x000000FF));
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
