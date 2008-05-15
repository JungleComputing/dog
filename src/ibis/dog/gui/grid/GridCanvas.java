package ibis.dog.gui.grid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

class GridCanvas extends JComponent implements MouseInputListener, KeyListener {

    static final boolean LOAD_GRAPH_BACK_BACKGROUND = false;

    static final boolean LOAD_GRAPH_TRANSPARENT = false;
    
    static final boolean SITES_TRANSPARENT = true;
    
    public static final int LOAD_MEMORY = 1000 * 60; // in millis

    static final int GRID_CIRCLE_SIZE = 30;

    static final int INFO_X_OFFSET = 30;

    static final int INFO_Y_OFFSET = 30;

    static final int LOAD_GRAPH_SIZE = 200;

    static final int LOAD_GRAPH_X_OFFSET = 10;

    static final int LOAD_GRAPH_Y_OFFSET = 50;

    static final int BLINK_TIME = 10 * 1000;

    final static BasicStroke stroke = new BasicStroke(1.5f);
    final static BasicStroke thinStroke = new BasicStroke(0.75f);
    
    private int maxLoad;

    static final Color[] COLORS = new Color[] { Color.ORANGE, Color.RED,
        Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.PINK,
        Color.BLACK };

    private Image img;

    private Grid grid;

    private int mapImageWidth;

    private int mapImageHeight;

    private GridRunner gridRunner;

    private long lastPainted;
    
    public GridCanvas(GridRunner gridRunner, Grid g) {
        this.gridRunner = gridRunner;
        this.grid = g;
        img = getMapImage();
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        setPreferredSize(new Dimension(mapImageWidth, mapImageHeight));

        maxLoad = 10;

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    public void newGrid(Grid g) {
        this.grid = g;
        img = getMapImage();
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        long curr = System.currentTimeMillis();
        if(curr - lastPainted < 500) {
//            System.err.print("A");
//            return;
        }
        
        Rectangle r = g.getClipBounds();
        g.setColor(Color.BLACK);
        g.fillRect(r.x, r.y, r.width, r.height);

        // draw the map image
        g.drawImage(img, 0, 0, null);

        drawSites(g);
      // drawStatusInfo(g);
      //  drawLoadGraph(g);
        
        lastPainted = curr;
    }

    /*
    private void drawStatusInfo(Graphics g) {
        int yPos = INFO_Y_OFFSET;
        g.setColor(Color.LIGHT_GRAY);
        Font old = g.getFont();
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(grid.getJobCount() + " jobs", mapImageWidth + INFO_X_OFFSET,
            yPos);
        g.drawString("deployed on", mapImageWidth + INFO_X_OFFSET, yPos + 30);
        g.drawString(grid.getGridName(), mapImageWidth + INFO_X_OFFSET,
                yPos + 60);
        g.setFont(old);

        yPos += 90;

        ArrayList computeResources = grid.getComputeResources();
        for (int i = 0; i < computeResources.size(); i++) {
            ComputeResource c = (ComputeResource) computeResources.get(i);

            g.setColor(Color.WHITE);
            g.drawString(c.getFriendlyName() + ": " + c.getJobList().size()
                + (c.getJobList().size() == 1 ? " job:" : " jobs:"),
                mapImageWidth + INFO_X_OFFSET, yPos);

            ArrayList jobList = c.getJobList();
            for (int x = 0; x < jobList.size(); x++) {
                yPos += 20;
                JobSubmitter j = (JobSubmitter) jobList.get(x);
                String stateString = j.getStateString();

                if (stateString.equals("RUNNING")) {
                    g.setColor(Color.GREEN);
                } else if (stateString.equals("SUBMITTING")) {
                    g.setColor(Color.CYAN);
                } else {
                    g.setColor(Color.WHITE);
                }

                g.drawString(stateString, mapImageWidth + INFO_X_OFFSET + 20,
                    yPos);
            }

            yPos += 30;
        }
    }*/

    private String getSiteStateString(ComputeResource c) {
        String res = "IDLE";

        ArrayList jobList = c.getJobList();
        for (int x = 0; x < jobList.size(); x++) {
            JobSubmitter j = (JobSubmitter) jobList.get(x);
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

    private void drawSites(Graphics g) {
        
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        FontRenderContext frc = g2.getFontRenderContext();
        Font f = new Font("SansSerif", Font.BOLD, 20); 
        
        ArrayList<ComputeResource> machines = grid.getComputeResources();
        
        for (int i = 0; i < machines.size(); i++) {
            ComputeResource m = machines.get(i);
            
            g2.setColor(getColor(i));

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
            }

            
            // make color transparent
            Color orig = g2.getColor();
            
            if (SITES_TRANSPARENT) {
                g2.setColor(new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), 150));
            } else {
                g2.setColor(orig);
            }

            g2.setStroke(stroke);
            
            Ellipse2D elipse = new Ellipse2D.Double(m.getX(), m.getY(), GRID_CIRCLE_SIZE, GRID_CIRCLE_SIZE);
            
            g2.fill(elipse);
            
            g2.setColor(Color.black);
            g2.draw(elipse);
            
            g2.setColor(Color.WHITE);
           
            TextLayout tl = new TextLayout("" + m.getJobCount(), f, frc);
            
            float sw = (float) tl.getBounds().getWidth();
            float sh = (float) tl.getBounds().getHeight();
        
            int x = (m.getJobCount() < 10 ? m.getX() + 9 : m.getX() + 2);
            int y = m.getY() + 22;
            
            Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
            
            g2.setColor(Color.black);
            g2.draw(sha);
            g2.setColor(Color.white);
            g2.fill(sha);

            /*
            Font old = g.getFont();
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2.drawString("" + m.getJobCount(), ,);
            g2.setFont(old);
            */
        }
    }

    /*
    private void drawLoadGraph(Graphics g) {
        long time = System.currentTimeMillis();
        long begin = time - LOAD_MEMORY;

        int scaleIncrement = 10;
        if (grid.maxLoadInSeries(begin) < 100) {
            maxLoad = grid.maxLoadInSeries(begin)
                + (grid.maxLoadInSeries(begin) % 10 == 0 ? 0 : (10 - grid
                    .maxLoadInSeries(begin) % 10));
            if (maxLoad == 0) maxLoad = 10;
        } else {
            maxLoad = grid.maxLoadInSeries(begin)
                + (grid.maxLoadInSeries(begin) % 100 == 0 ? 0 : (100 - grid
                    .maxLoadInSeries(begin) % 100));
            scaleIncrement = 100;
        }

        if (LOAD_GRAPH_BACK_BACKGROUND) {
            g.setColor(Color.BLACK);
            g.fillRect(LOAD_GRAPH_X_OFFSET - 10, LOAD_GRAPH_Y_OFFSET - 10,
                LOAD_GRAPH_SIZE + 40, LOAD_GRAPH_SIZE + 20);
        }

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(LOAD_GRAPH_X_OFFSET, LOAD_GRAPH_Y_OFFSET, LOAD_GRAPH_SIZE,
            LOAD_GRAPH_SIZE);

        double Y_SCALE_FACTOR = (double) LOAD_GRAPH_SIZE / maxLoad;
        double X_SCALE_FACTOR = (double) LOAD_GRAPH_SIZE / LOAD_MEMORY;

        // draw some grid lines in the graph
        for (int i = 0; i <= maxLoad; i += scaleIncrement) {
            int y = LOAD_GRAPH_Y_OFFSET + LOAD_GRAPH_SIZE
                - (int) (i * Y_SCALE_FACTOR);
            int x = LOAD_GRAPH_X_OFFSET + LOAD_GRAPH_SIZE + 10;
            g.setColor(Color.WHITE);
            g.drawLine(LOAD_GRAPH_X_OFFSET + LOAD_GRAPH_SIZE, y,
                LOAD_GRAPH_X_OFFSET + LOAD_GRAPH_SIZE + 4, y);
            g.setColor(Color.DARK_GRAY);
            g.drawLine(LOAD_GRAPH_X_OFFSET + 1, y, LOAD_GRAPH_X_OFFSET
                + LOAD_GRAPH_SIZE - 1, y);

            g.setColor(Color.WHITE);
            g.drawString("" + i, x, y + 5);
        }

        ArrayList machines = grid.getComputeResources();

        for (int x = 1; x < LOAD_GRAPH_SIZE; x++) {
            int totalLoad = 0;
            long xTime = time - LOAD_MEMORY + (long) (x / X_SCALE_FACTOR);

            for (int i = 0; i < machines.size(); i++) {
                ComputeResource m = (ComputeResource) machines.get(i);
                int myLoad = m.getLoadAt(xTime);
                if (myLoad == 0) {
                    continue;
                }

                Color orig = getColor(i);
                if(LOAD_GRAPH_TRANSPARENT) {
                    g.setColor(new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), 128));
                } else {
                    g.setColor(orig);
                }
                g.drawLine(LOAD_GRAPH_X_OFFSET + x, LOAD_GRAPH_Y_OFFSET
                    + LOAD_GRAPH_SIZE - (int) (totalLoad * Y_SCALE_FACTOR),
                    LOAD_GRAPH_X_OFFSET + x, LOAD_GRAPH_Y_OFFSET
                        + LOAD_GRAPH_SIZE
                        - (int) ((totalLoad + myLoad) * Y_SCALE_FACTOR));
                totalLoad += myLoad;
            }
        }

        // draw title
        g.setColor(Color.WHITE);
        Font old = g.getFont();
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Number of Jobs deployed", LOAD_GRAPH_X_OFFSET,
            LOAD_GRAPH_Y_OFFSET - 30);
        g.setFont(old);

    }*/

    private Image getMapImage() {
        String filename = grid.getMapFile().getName();
        Image res = Toolkit.getDefaultToolkit().createImage(filename);

        while (res.getHeight(null) == -1) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // Ignore.
            }
        }

        mapImageHeight = res.getHeight(null);
        mapImageWidth = res.getWidth(null);

        return res;
    }

    public void mouseDragged(MouseEvent arg0) {
        // TODO Auto-generated method stub   
    }

    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mousePressed(MouseEvent e) {
//        System.err.println("clicked at " + e.getX() + ", " + e.getY());
        ComputeResource m = getComputeResource(e);
        if (m != null) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                gridRunner.clickedAddComputeResource(m);
            } else {
                gridRunner.clickedRemoveComputeResource(m);
            }
        }
    }

    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    private ComputeResource getComputeResource(MouseEvent e) {
        ArrayList machines = grid.getComputeResources();

        for (int i = 0; i < machines.size(); i++) {
            ComputeResource m = (ComputeResource) machines.get(i);

            if (m.getX() < e.getX() && e.getX() < m.getX() + GRID_CIRCLE_SIZE
                && m.getY() < e.getY()
                && e.getY() < m.getY() + GRID_CIRCLE_SIZE) {
                return m;
            }
        }
        return null;
    }

    public void mouseMoved(MouseEvent e) {
        //        ComputeResource m = getComputeResource(e);
        //        gridDetails.setDetails(m);
    }

    public void keyPressed(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void keyTyped(KeyEvent e) {
//        System.err.println(e);
        if (e.getKeyChar() == 'q') {
            gridRunner.shutdown();
            System.exit(0);
        }
    }

    Color getColor(int site) {
        return COLORS[site % COLORS.length];
    }
}
