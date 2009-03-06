package ibis.dog.shared;

import java.io.Serializable;

public class YUV420SPImage implements Serializable, BaseImage {

    // Generated by eclipse
    private static final long serialVersionUID = -5048498322596744896L;

    public final int width;

    public final int height;

    public final byte[] pixels;

    public YUV420SPImage(int width, int height) {
        this.width = width;
        this.height = height;
        // YUV420SP uses 1.5 bytes per pixel (needs to be verified!)
        pixels = new byte[(int) (width * height * 1.5)];
    }

    public YUV420SPImage(int width, int height, byte[] image) {
        this.width = width;
        this.height = height;
        this.pixels = image;
    }

    public RGB24Image toRGB24() {
        final int offsetU = width * height;

        // 3 bytes per pixel in a RGB24Image
        final byte[] out = new byte[width * height * 3];
        for (int h = 0; h < height; h += 2) {
            for (int w = 0; w < width; w += 2) {
                int U = (0xff & pixels[offsetU + h * width / 2 + w]) - 128;
                int V = (0xff & pixels[offsetU + h * width / 2 + w + 1]) - 128;

                int Y1 = (0xff & pixels[h * width + w]) - 16;
                int Y2 = (0xff & pixels[h * width + (w + 1)]) - 16;
                int Y3 = (0xff & pixels[(h + 1) * width + w]) - 16;
                int Y4 = (0xff & pixels[(h + 1) * width + (w + 1)]) - 16;

                int r1 = clipAndScale((298 * Y1 + 409 * U + 128) >> 8);
                int r2 = clipAndScale((298 * Y2 + 409 * U + 128) >> 8);
                int r3 = clipAndScale((298 * Y3 + 409 * U + 128) >> 8);
                int r4 = clipAndScale((298 * Y4 + 409 * U + 128) >> 8);

                int g1 = clipAndScale((298 * Y1 - 100 * V - 208 * U + 128) >> 8);
                int g2 = clipAndScale((298 * Y2 - 100 * V - 208 * U + 128) >> 8);
                int g3 = clipAndScale((298 * Y3 - 100 * V - 208 * U + 128) >> 8);
                int g4 = clipAndScale((298 * Y4 - 100 * V - 208 * U + 128) >> 8);

                int b1 = clipAndScale((298 * Y1 + 516 * V + 128) >> 8);
                int b2 = clipAndScale((298 * Y2 + 516 * V + 128) >> 8);
                int b3 = clipAndScale((298 * Y3 + 516 * V + 128) >> 8);
                int b4 = clipAndScale((298 * Y4 + 516 * V + 128) >> 8);

                out[h * width * 3 + w * 3] = (byte) (0xff & r1);
                out[h * width * 3 + w * 3 + 1] = (byte) (0xff & g1);
                out[h * width * 3 + w * 3 + 2] = (byte) (0xff & b1);

                out[h * width * 3 + w * 3 + 3] = (byte) (0xff & r2);
                out[h * width * 3 + w * 3 + 4] = (byte) (0xff & g2);
                out[h * width * 3 + w * 3 + 5] = (byte) (0xff & b2);

                out[(h + 1) * width * 3 + w * 3] = (byte) (0xff & r3);
                out[(h + 1) * width * 3 + w * 3 + 1] = (byte) (0xff & g3);
                out[(h + 1) * width * 3 + w * 3 + 2] = (byte) (0xff & b3);

                out[(h + 1) * width * 3 + w * 3 + 3] = (byte) (0xff & r4);
                out[(h + 1) * width * 3 + w * 3 + 4] = (byte) (0xff & g4);
                out[(h + 1) * width * 3 + w * 3 + 5] = (byte) (0xff & b4);

            }
        }
        return new RGB24Image(width, height, out);
    }

    private static final byte clipAndScale(int value) {

        if (value > 255)
            value = 255;

        if (value < 0)
            value = 0;

        return (byte) (0xff & ((value * 220) / 256));
    }
}