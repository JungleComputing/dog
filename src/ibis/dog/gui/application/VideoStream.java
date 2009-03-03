package ibis.dog.gui.application;

import ibis.video4j.VideoConsumer;
import ibis.video4j.VideoDeviceFactory;
import ibis.video4j.devices.VideoSource;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

class VideoStream extends JPanel implements VideoConsumer { 

    // Generated
    private static final long serialVersionUID = 1L;

    private VideoSource webcam;
    
    private int camWidth = 352;
    private int camHeight = 288;

    private int [] pixels;
    
    private Image offscreen;
    private MemoryImageSource source;
    
    private long image = 0;

    private long start;
    
    private String message = "No webcam selected";
    
    private FramerateConsumer framerate;
    
    private VideoConsumer nextConsumer;
    
    public VideoStream(int width, int height, FramerateConsumer f, 
            VideoConsumer nextConsumer) { 
        setBackground(Color.white);
        
        this.camWidth = width;
        this.camHeight = height;
        this.framerate = f;
        this.nextConsumer = nextConsumer;
    
        pixels = new int[width*height];
        
        source = new MemoryImageSource(width, height, pixels, 0, width);
        source.setAnimated(true);
        source.setFullBufferUpdates(true);
        
        offscreen = createImage(source);
    
        setMinimumSize(new Dimension(352, 288));
        setPreferredSize(new Dimension(352, 288));
        setMaximumSize(new Dimension(352, 288));
    }
    
    public void selectDevice(int device) throws Exception {  
  
        if (webcam != null) { 
            // Stop the existing device
            webcam.close();
        }
        
        if (device >= 0) { 
            webcam = VideoDeviceFactory.openDevice(this, device, camWidth, 
                    camHeight, 4);            
            webcam.start();
            
            image = 0;
        } else { 
            webcam = null;
            message = "No webcam selected";
        }
   
        framerate.setInFramerate(0.0);
        
        repaint();
    }

    public int [] getBuffer(int w, int h, int index) { 
        return pixels;
    }
    
    public void gotImage(int [] pixels, int index) {
        
        source.newPixels(0, 0, camWidth, camHeight);
    
        if (image == 0) { 
            start = System.currentTimeMillis();
        } else if (image >= 25) {
            long now = System.currentTimeMillis();
            framerate.setInFramerate(25 * 1000.0 / (now-start));
            start = now;
            image = 0;
        }
        
        image++; 
        repaint();
        
        if (nextConsumer != null) { 
            
            int [] buffer = nextConsumer.getBuffer(camWidth, camHeight, 0);
            
            if (buffer != null) { 
                System.arraycopy(pixels, 0, buffer, 0, pixels.length);
                nextConsumer.gotImage(buffer, 0);
            }
        }
    }

   
    void setMessage(String message) { 
        this.message = message;
        repaint();
    }
    
    public void paint(Graphics g) {
      
        Graphics2D g2 = (Graphics2D) g;
        
        if (webcam != null) { 
            g2.drawImage(offscreen, 0, 0, this);
        } else { 
            Dimension d = getSize();
            
            FontRenderContext frc = g2.getFontRenderContext();
                 
            int x = d.width / 2;
            int y = d.height / 2;
            
            Color edge = Color.GRAY;
            Color fill = Color.WHITE;        
    
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, d.width, d.height);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        
            Font f = getFont().deriveFont(Font.BOLD);
            TextLayout tl = new TextLayout(message, f, frc);
            
            float sw = (float) tl.getBounds().getWidth();
            float sh = (float) tl.getBounds().getHeight();
        
            Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(
                    x-sw/2, y+sh/2));
            g2.setColor(edge);
            g2.draw(sha);
            g2.setColor(fill);
            g2.fill(sha);
        }
    }
}