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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class GridMap extends JComponent 

implements MouseInputListener, MouseWheelListener {

    private static final boolean SITES_TRANSPARENT = true;

    private static final int BLINK_TIME = 10 * 1000;
    
    private static final int GRID_CIRCLE_SIZE = 30;
    private static final int DOT_CIRCLE_SIZE = 5;
    
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
    
    private int borderW = 50;
    private int borderH = 50;
    
    private int mouseLocationX = -1;
    private int mouseLocationY = -1;
    
    private double scale = 100;
    
    private Deployment deploy;
    
    private class Slot { 
        
        final int x; 
        final int y; 
       
        ComputeResource owner;
        double distance;
        
        /**
         * Construct a new Slot
         * 
         * @param x
         * @param y
         * @param w
         * @param h
         */
        public Slot(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private ArrayList<Slot> slots = new ArrayList<Slot>();
    
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
        
            /*
            double ratio = (double) imageW / (double) imageH;
            
            if (ratio > 1.0) { 
                // The image is wide
                preferredSizeH = (int) (preferredSizeH / ratio);
            } else { 
                // The image is high
                preferredSizeW = (int) (preferredSizeW * ratio);
            }
            */
            
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
        
        generateSlots();
    }

    private void generateSlots() { 
        
        // Note: assumes borderW and borderH fit exactly into preferedSizeW and 
        // preferredSizeH
        
        for (int w=0;w<preferredSizeW;w+=borderW) { 
            slots.add(new Slot(w, 0));
            slots.add(new Slot(w, preferredSizeH-borderH));
        }
        
        for (int h=borderH;h<preferredSizeH-borderH;h+=borderH) { 
            slots.add(new Slot(0, h));
            slots.add(new Slot(preferredSizeW-borderW, h));
        }
        
        System.out.println("Slots total " + slots.size());
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
  
        System.out.println("SCALE " + scale);
        
        double resize = scale / 100.0;
        
        int w = (int) (preferredSizeW / (2 * resize));
        int h = (int) (preferredSizeH / (2 * resize));
        
        int startX = posX - w;
        int startY = posY - h;
        
        int endX = posX + w;
        int endY = posY + h;
        
        System.out.println("Frawing map area: (" + startX + "," + startY 
                + ") to (" + endX + ", " + endY + ")");
        
        drawMap((Graphics2D) g, startX, startY, endX, endY);
        drawSites((Graphics2D) g, startX, startY, endX, endY);
    } 
    
   private void drawMap(Graphics2D g, int startX, int startY, int endX, 
            int endY) {
        
        
        g.drawImage(map,
                borderW, borderH, preferredSizeW-borderW, preferredSizeH-borderH,// dst rectangle 
                startX, startY, endX, endY,             // src area of image
                null);
    }
   
    private void drawSlot(Graphics2D g, int x, int y, int w, int h, 
            int dx, int dy, Color color, String text, FontRenderContext frc, 
            Font f) { 
   
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
        
        g.setColor(color);
        g.fill(rect);
        
        g.drawLine(x+w/2, y+h/2, dx, dy);
        
        g.setColor(Color.darkGray);        
        g.setStroke(stroke);
        
        g.draw(rect);
        
        TextLayout tl = new TextLayout(text, f, frc);
        
        float sw = (float) tl.getBounds().getWidth();
        float sh = (float) tl.getBounds().getHeight();
    
        double scale = (w-4) / sw;
       
        if (scale > 1.0) { 
            scale = 1.0;
        }
        
        AffineTransform t1 = new AffineTransform();
        t1.setToScale(scale, scale);
        
        AffineTransform t2 = new AffineTransform();
        t2.setToTranslation(x, y+w/2+(sh/2)*scale);
        
        t2.concatenate(t1);
        
        Shape sha = tl.getOutline(t2);
        
        g.setColor(Color.black);
        g.draw(sha);
        g.setColor(Color.white);
        g.fill(sha);
    }
   
    private void drawEmptySlot(Graphics2D g, int x, int y, int w, int h) { 
        g.setColor(Color.black);
        g.fill(new Rectangle(x, y, w, h));
    } 
    
    private void drawSites(Graphics2D g2, int startX, int startY,
            int endX, int endY) {
        
        double resize = (preferredSizeW-borderW*2.0)/(endX - startX); 
    
        drawEmptySlot(g2, 0, 0, preferredSizeW, borderH);
        drawEmptySlot(g2, 0, preferredSizeH-borderH, preferredSizeW, borderH);
        
        drawEmptySlot(g2, 0, borderH, borderW, preferredSizeH-borderH);
        drawEmptySlot(g2, preferredSizeW-borderW, borderH, borderW, preferredSizeH-borderH);
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        FontRenderContext frc = g2.getFontRenderContext();
        Font f = new Font("SansSerif", Font.BOLD, 20); 
        
        ComputeResource [] machines = deploy.getComputeResources();
        
        LinkedList<ComputeResource> visible = new LinkedList<ComputeResource>();
        LinkedList<Slot> used = new LinkedList<Slot>();
        
        for (int i = 0; i < machines.length; i++) {
            ComputeResource m = machines[i];
            
            int x = m.getX();
            int y = m.getY();
            
            if (x <= startX || x >= endX) { 
                System.out.println("ComputeResource: " + m.getFriendlyName() 
                        + " not visible (" + x + "!, " + y + ")");
                continue;
            }
            
            if (y <= startY || y >= endY) { 
                System.out.println("ComputeResource: " + m.getFriendlyName() 
                        + " not visible (" + x + ", " + y + "!)");
                continue;
            }
        
            System.out.println("ComputeResource: " + m.getFriendlyName() 
                    + " is visible!");
        
            visible.addLast(m);
        }
        
        while (visible.size() > 0) { 
        
            System.out.println("Size " + visible.size());
            
            ComputeResource tmp = visible.removeFirst();
        
            int x = tmp.getX();
            int y = tmp.getY();
         
            x = borderW + (int) ((x - startX) * resize);
            y = borderH + (int) ((y - startY) * resize);
            
            // Find the best slot for this resource....
            Slot best = null;
            double distance = Double.MAX_VALUE;
            
            for (Slot s : slots) {
                double d = Math.sqrt( (s.x - x) * (s.x - x) + (s.y - y) * (s.y - y));  
          
                if (d < distance) {
                    
                    // Check if the slot is aready used...
                    if (s.owner != null) { 
                        // Check if we are closer..
                        if (d + 0.00001 < s.distance ) { 
                            // We may steal this slot
                            best = s; 
                            distance = d;
                 System.out.println("option " + d + " S");
                        
                        } else { 
                            // we may not steal this slot
                        }
                    } else { 
                        // unused slot
                        best = s;
                        distance = d;
                System.out.println("option " + d + " S");
                        
                    }
                }
            }
            
            if (best != null) { 
                
                // See if we have stolen a slot ...
                if (best.owner != null) { 
                    visible.addLast(best.owner);
                    best.owner = tmp;
                    best.distance = distance;
                
                    System.out.println("BEST " + distance + " S");
                    
                } else { 
                    best.owner = tmp;
                    best.distance = distance;
             
                    System.out.println("BEST " + distance);
                    
                    used.addLast(best);
                }
            }
        }
            
        for (Slot s : used) { 
            
            ComputeResource m = s.owner;
            
            int x = m.getX();
            int y = m.getY();
            
            Color color = Color.red; // getColor(i);

            if (SITES_TRANSPARENT) {
                color = new Color(color.getRed(), color.getGreen(), 
                        color.getBlue(), 150);
            }
            
            g2.setColor(color);
            
            x = borderW + (int) ((x - startX) * resize);
            y = borderH + (int) ((y - startY) * resize);
            
            
            Ellipse2D elipse = new Ellipse2D.Double(x, y,  
                    DOT_CIRCLE_SIZE, DOT_CIRCLE_SIZE);
            
            g2.fill(elipse);
        
            g2.setColor(Color.BLACK);
            g2.setStroke(thinStroke);
            g2.draw(elipse);
            
            drawSlot(g2, s.x, s.y, borderW, borderH, x, y, color, 
                    m.getFriendlyName(), frc, f);
       
            // reset the slot
            s.owner = null;
        }
        
        
            
            
            /*
            
            int slotX = 0;
            int slotY = 0;
            
            if (x > preferredSizeW / 2) { 
                
                // Right half of the map
                if (y > preferredSizeH / 2) { 
                    
                    // Bottom right
                    int tmpX = x - preferredSizeW/2;
                    int tmpY = y - preferredSizeH/2;
                      
                    if (tmpY > tmpX) { 
                        // Bottom row, on right
                        slotX = preferredSizeW / 2;
                        slotY = preferredSizeH - borderH;
                    } else { 
                        // Right colum, on bottom
                        slotX = preferredSizeW - borderW;
                        slotY = preferredSizeH/2;
                    }
                } else { 
                    // Top right
                    int tmpX = x - preferredSizeW/2;
                    int tmpY = preferredSizeH/2 - y;
                   
                    if (tmpY > tmpX) { 
                        // Top row, on right
                        slotX = preferredSizeW / 2;
                        slotY = 0;
                    } else { 
                        // Right colum, on top
                        slotX = preferredSizeW - borderW;
                        slotY= preferredSizeH / 2 - borderH ;
                    }
                }
            } else { 
               
                // Left half of map
                if (y > preferredSizeH / 2) { 
                    
                    // Bottom left
                    int tmpX = x;
                    int tmpY = preferredSizeH - y;
                    
                    if (tmpY > tmpX) { 
                        // left colom, bottom 
                        slotX = 0;
                        slotY = preferredSizeH / 2;
                    } else { 
                        // Bottom row, left
                        slotX = preferredSizeW / 2 - borderW;
                        slotY = preferredSizeH - borderH;
                    }
                } else { 
                    // Top left
                    
                    if (y > x) { 
                        // Right column, top
                        slotX = 0;
                        slotY = preferredSizeH / 2 - borderH;
                    } else { 
                        // Top row, right
                        slotX = preferredSizeW / 2 - borderW;
                        slotY = 0;
                    }
                }
            }
            */
            
            // Note: not very efficient!
            /*
            Slot best = null;
            double distance = Double.MAX_VALUE;
            
            for (Slot s : slots) {
                double tmp = Math.sqrt( (s.x - x) * (s.x - x) + (s.y - y) * (s.y - y));  
          
                if (tmp < distance) { 
                    best = s;
                    distance = tmp;
                }
            }
        
            
            
            
            if (best != null) { 
                best.used = true;
                drawSlot(g2, best.x, best.y, borderW, borderH, x, y, color, 
                        m.getFriendlyName(), frc, f);
            }*/
            
            
            
            /*
            
            
            
            g2.setStroke(stroke);
            
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
            g2.fill(sha);*/
        
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
