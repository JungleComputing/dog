package ibis.dog.shared;

public class RGB32Image implements ConvertableImage {

    // Generated by eclipse
    private static final long serialVersionUID = -5136719712051715163L;

    public final int width;

    public final int height;

    public final int[] pixels;

    public RGB32Image(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];
    }

    public RGB32Image(int width, int height, int[] image) {
        this.width = width;
        this.height = height;
        this.pixels = image;
    }

    public RGB24Image toRGB24() {
        // TODO Auto-generated method stub
        return null;
    }
}
