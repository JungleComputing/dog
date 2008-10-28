package ibis.dog.shared;

import java.io.Serializable;

public class RGB24Image implements Serializable {
    
    // Generated by eclipse
    private static final long serialVersionUID = -5136719712051715163L;
    
    public final int width; 
    public final int height;
    
    public final byte [] pixels;
    
    public RGB24Image(int width, int height) { 
        this.width = width;
        this.height = height;
        pixels = new byte[width*height*3];
    }
    
    public RGB24Image(int width, int height, byte [] image) { 
        this.width = width;
        this.height = height;
        this.pixels = image;
    }
}