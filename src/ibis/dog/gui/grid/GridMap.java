package ibis.dog.gui.grid;

import ibis.deploy.Job;
import ibis.dog.client.ComputeResource;
import ibis.dog.client.Deployment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class GridMap extends JComponent 

implements MouseInputListener, MouseWheelListener {

    private static final boolean SITES_TRANSPARENT = true;

    private static final int BLINK_TIME = 10 * 1000;
    private static final int GRID_CIRCLE_SIZE = 30;
    
    private static final int MAX_SCALE  = 300;
    private static final int SCALE_STEP = 10;
    private static       int MIN_SCALE  = 10; // This one depends on image size!   
    
    private static final BasicStroke stroke = new BasicStroke(1.5f);
    private static final BasicStroke thinStroke = new BasicStroke(0.75f);
   
    private static final Color[] COLORS = new Color[] { 
        Color.ORANGE, 
        Color.RED,
        Color.YELLOW, 
        Color.MAGENTA,
        Color.CYAN, 
        Color.PINK,
        Color.BLACK 
    };
    
    private BufferedImage map;
    
    private int imageW; 
    private int imageH;

    private int posX = 4200;
    private int posY = 950;
    
    private int preferredSizeW = 800;
    private int preferredSizeH = 800;
    
    private int mouseLocationX = -1;
    private int mouseLocationY = -1;
    
    private double scale = 100;
    
    private Deployment deploy;
    
    public GridMap(Deployment deploy) {
       
        this.deploy = deploy;
        
        try {
            map = ImageIO.read(new File("images/world-large.jpg"));
          
            imageW = map.getWidth(null);
            imageH = map.getHeight(null);
            
            if (map.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bi2 =
                    new BufferedImage(imageW, imageH, BufferedImage.TYPE_INT_RGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(map, 0, 0, null);
                map = bi2;
            }
            
            imageW = map.getWidth(null);
            imageH = map.getHeight(null);
        
            System.out.println("ImageWidth = "+ imageW);
            System.out.println("ImageHeight = "+ imageH);            
            
            double ratio = (double) imageW / (double) imageH;
            
            if (ratio > 1.0) { 
                // The image is wide
                preferredSizeH = (int) (preferredSizeH / ratio);
            } else { 
                // The image is high
                preferredSizeW = (int) (preferredSizeW * ratio);
            }
            
            int minScaleX = (100 * preferredSizeW) / imageW;
            int minScaleY = (100 * preferredSizeH) / imageH;
            
            if (minScaleX < minScaleY) { 
                MIN_SCALE = minScaleX;
            } else { 
                MIN_SCALE = minScaleY;
            }
            
            if (MIN_SCALE <= 0) { 
                MIN_SCALE = 1;
            }
            
            System.out.println("MIN_SCALE set to " + MIN_SCALE);
            
            addMouseMotionListener(this);
            addMouseListener(this);
            addMouseWheelListener(this);
            
        } catch (IOException e) {
            System.out.println("RGB24Image could not be read");
            System.exit(1);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(preferredSizeW, preferredSizeH);
    }
    
    private Color getColor(int site) {
        return COLORS[site % COLORS.length];
    }
    
    /*
    private String getSiteStateString(ComputeResource c) {
        String res = "IDLE";

        ArrayList<Job> jobList = c.getJobList();
        
        for (int x = 0; x < jobList.size(); x++) {

            Job j = jobList.get(x);
            
           
            
            
            String stateString = j.getStateString();

            if (stateString.equals("RUNNING")) {
                if (res.equals("IDLE")) {
                    res = "RUNNING";
                }
            } else if (stateString.equals("SUBMITTING")) {
                res = "SUBMITTING";
            }
        }

        return res;
    }
*/
    
    public void paint(Graphics g) {
        
        Graphics2D tmp = (Graphics2D) map.getGraphics();
        drawSites(tmp);
        
        drawMap((Graphics2D) g);
    } 
    
    private void drawMap(Graphics2D g) {
        
        System.out.println("SCALE " + scale);
        
        double resize = scale / 100.0;
        
    //    System.out.println("Resize = " + resize);
        
        int w = (int) (preferredSizeW / (2 * resize));
        int h = (int) (preferredSizeH / (2 * resize));
        
        int startX = posX - w;
        int startY = posY - h;
        
        int endX = posX + w;
        int endY = posY + h;
        
     //   System.out.println("x " + startX + " " + endX);
     //   System.out.println("y " + startY + " " + endY);
                
        g.drawImage(map,
                0, 0, preferredSizeW, preferredSizeH,   // dst rectangle 
                startX, startY, endX, endY,             // src area of image
                null);
    }
    
    private void drawSites(Graphics2D g2) {
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        FontRenderContext frc = g2.getFontRenderContext();
        Font f = new Font("SansSerif", Font.BOLD, 20); 
        
        ComputeResource [] machines = deploy.getComputeResources();
        
        for (int i = 0; i < machines.length; i++) {
            ComputeResource m = machines[i];
            
            g2.setColor(getColor(i));

            /*
            if (m.getLoadInfo().size() > 0) {
                
                long time = (m.getLoadInfo().get(m.getLoadInfo().size() - 1)).timestamp;
                long curr = System.currentTimeMillis();
                
                if (curr - time < BLINK_TIME && (((curr - time) / 500) % 2 == 0)) {
                    if (getSiteStateString(m).equals("RUNNING")) {
                        g2.setColor(Color.GREEN);
                    } else if (getSiteStateString(m).equals("SUBMITTING")) {
                        g2.setColor(Color.BLUE);
                    } else {
                        g2.setColor(getColor(i));
                    }
                }
            }*/

            // make color transparent
            Color orig = g2.getColor();
            
            if (SITES_TRANSPARENT) {
                g2.setColor(new Color(orig.getRed(), orig.getGreen(), 
                        orig.getBlue(), 150));
            } else {
                g2.setColor(orig);
            }

            g2.setStroke(stroke);
            
            int x = m.getX();
            int y = m.getY();
            
            Ellipse2D elipse = new Ellipse2D.Double(x, y,  
                    GRID_CIRCLE_SIZE, GRID_CIRCLE_SIZE);
            
            System.out.println("Elipse at " + x + " " + y);
            
            g2.fill(elipse);
            
            g2.setColor(Color.black);
            g2.draw(elipse);
            
            g2.setColor(Color.WHITE);
           
            TextLayout tl = new TextLayout("" + m.getJobCount(), f, frc);
            
            float sw = (float) tl.getBounds().getWidth();
            float sh = (float) tl.getBounds().getHeight();
        
            x = (m.getJobCount() < 10 ? x + 9 : x + 2);
            y = y + 22;
            
            Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
            
            g2.setColor(Color.black);
            g2.draw(sha);
            g2.setColor(Color.white);
            g2.fill(sha);
        }
    }
    
    private ComputeResource getComputeResource(MouseEvent e) {
        ComputeResource [] machines = deploy.getComputeResources();

        for (ComputeResource m : machines) {
            // TODO: Fix to incorperate postion and scaling
            if (m.getX() < e.getX() && e.getX() < m.getX() + GRID_CIRCLE_SIZE
                && m.getY() < e.getY()
                && e.getY() < m.getY() + GRID_CIRCLE_SIZE) {
                return m;
            }
        }
        return null;
    }
    
    
    public void mouseClicked(MouseEvent e) {
        ComputeResource m = getComputeResource(e);
        
        if (m != null) {
            if (e.getButton() == MouseEvent.BUTTON1) {
   //             gridRunner.clickedAddComputeResource(m);
            } else {
    //            gridRunner.clickedRemoveComputeResource(m);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
        // Unused
    }

    public void mouseExited(MouseEvent arg0) {
        // Unused
    }

    public void mousePressed(MouseEvent e) {
        mouseLocationX = e.getX();
        mouseLocationY = e.getY(); 
    }

    public void mouseReleased(MouseEvent e) {
        mouseLocationX = -1;
        mouseLocationY = -1;
    }

    public void mouseDragged(MouseEvent e) {
        
        if (mouseLocationX == -1) { 
            System.out.println("EEP!");
            return;
        }
        
        int currentX = e.getX();
        int currentY = e.getY();
         
        int dx = currentX - mouseLocationX;
        int dy = currentY - mouseLocationY;
        
        int newX = posX - dx; 
        int newY = posY - dy;
        
        if (newX < (imageW - preferredSizeW/2) && newX > (preferredSizeW /2)) { 
            posX = newX;
        }
        
        if (newY < (imageH - preferredSizeH/2) && newY > (preferredSizeH /2)) { 
            posY = newY;
        }
        
        mouseLocationX = currentX;
        mouseLocationY = currentY;
 
        repaint();
    }

    public void mouseMoved(MouseEvent arg0) {
        // Unused
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
     
      //  System.out.println("Mouse wheel: " + e);
        
        int rotation = e.getWheelRotation();
        
        if (rotation > 0) {
            scale += SCALE_STEP;
           
            if (scale > MAX_SCALE) { 
                scale = MAX_SCALE;
            }
            
            repaint();
        } else if (rotation < 0) {
            scale -= SCALE_STEP;
            
            if (scale < MIN_SCALE) { 
                scale = MIN_SCALE;
            }
            
            repaint();
        }
        
       // System.out.println("scale = " + scale + " rotation " + rotation);
    }
}
