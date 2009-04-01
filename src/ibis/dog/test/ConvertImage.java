package ibis.dog.test;

import java.io.File;
import java.io.FileOutputStream;

import ibis.dog.shared.ImageUtils;
import ibis.dog.shared.RGB24Image;
import ibis.dog.shared.RGB32Image;

public class ConvertImage {
    
    public static void main(String[] args) {
        try {

            if (args.length != 2) { 
                System.err.println("Usage Main.main <in.png> <out.bytes>");
                System.exit(1); 
            }

            RGB32Image image32 = ImageUtils.load(new File(args[0]));
            RGB24Image image24 = image32.toRGB24();

            FileOutputStream out = new FileOutputStream(args[1]);
            
            out.write(image24.pixels);
            out.close();
        
        } catch (Exception e) {
            System.out.println("FATAL");
            e.printStackTrace();
        }
    } 
}
