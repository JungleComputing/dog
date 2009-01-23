package ibis.dog.shared;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
        // TODO: image should not be scaled down here, but by the application,
        // however since we convert the image here anyway it's efficient to do
        // it here
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 25;
        Bitmap bitmap = BitmapFactory.decodeByteArray(cdata, 0, cdata.length,
                options);
        RGB24Image result = new RGB24Image(options.outWidth, options.outHeight);
        for (int h = 0; h < result.height; h++) {
            for (int w = 0; w < result.width; w++) {
                // pixel = ARGB?
                int argbPixel = bitmap.getPixel(w, h);
                result.pixels[h * result.width * 3 + w * 3] = (byte) ((argbPixel & 0x00FF0000) >> 16);
                result.pixels[h * result.width * 3 + w * 3 + 1] = (byte) ((argbPixel & 0x0000FF00) >> 8);
                result.pixels[h * result.width * 3 + w * 3 + 2] = (byte) ((argbPixel & 0x0000FF00));
            }
        }
        return result;
    }
}
