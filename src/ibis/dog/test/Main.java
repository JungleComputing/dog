package ibis.dog.test;

import ibis.dog.shared.ImageUtils;
import ibis.dog.shared.RGB32Image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Main {

    public static void main(String[] args) {
        try {

            if (args.length != 2) { 
                System.err.println("Usage Main.main <image.png> <count>");
                System.exit(1);	
            }

            RGB32Image image = ImageUtils.load(new File(args[0]));

            int count = Integer.parseInt(args[1]);

            Client c = new Client(image.toRGB24(), count);
            // Activate the application
            c.start();
        } catch (Exception e) {
            System.out.println("FATAL");
            e.printStackTrace();
        }
    } 
}
