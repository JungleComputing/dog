package ibis.dog.shared;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageUtils {
    
    public static byte [] encode(int [] pixels, int w, int h, int quality) 
        throws ImageFormatException, IOException {
        
        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        b.setRGB(0, 0, w, h, pixels, 0, w);
    
        return encodeJPEG(b, quality);
    }
    
    public static byte [] encodeJPEG(BufferedImage image, int quality) 
        throws ImageFormatException, IOException {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);

        quality = Math.max(0, Math.min(quality, 100));
        
        param.setQuality((float)quality / 100.0f, false);
        
        encoder.setJPEGEncodeParam(param);
        encoder.encode(image);

        return out.toByteArray();
    }
    
    public static BufferedImage decodeJPEG(byte [] bytes) 
        throws ImageFormatException, IOException {
        
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
        
        return decoder.decodeAsBufferedImage();
    }
        
    public static BufferedImage scale(BufferedImage bi, int maxW, int maxH, 
            boolean keepAspect) { 
        
        int height = bi.getHeight();
        int width = bi.getWidth();
        
        // pixels must be scaled...
        double scaleH = ((double)maxH) / height;
        double scaleW = ((double)maxW) / width;

        int newW = -1; 
        int newH = -1;
        
        if (keepAspect) { 
            if (scaleH < scaleW) { 
                newH = maxH;
                newW = (int) (scaleH * width);
                scaleW = scaleH;
            } else { 
                newW = maxW;
                newH = (int) (scaleW * height);
                scaleH = scaleW;
            }
        } else { 
            newW = maxW;
            newH = maxH;
        }
        
        BufferedImage bdest = 
            new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = bdest.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(scaleW, scaleH);

        g.drawRenderedImage(bi,at);

        return bdest;
    }
}
