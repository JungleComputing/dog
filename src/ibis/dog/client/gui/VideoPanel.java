/**
 * 
 */
package ibis.dog.client.gui;

import ibis.dog.client.Client;
import ibis.media.imaging.Format;
import ibis.media.imaging.Image;
import ibis.media.imaging.Imaging;
import ibis.util.ThreadPool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.MemoryImageSource;
import java.nio.ByteBuffer;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VideoPanel extends JPanel implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(VideoPanel.class);

	// Generated
	private static final long serialVersionUID = 1L;

	// public static final int WIDTH = 352;
	// public static final int HEIGHT = 288;

	// public static final int WIDTH = 400;
	// public static final int HEIGHT = 300;

	public static final int WIDTH = 450;
	public static final int HEIGHT = 337;

	private final Client client;
	private int imageIndex;
	
	private boolean imageValid;

	private int[] pixels;
	private MemoryImageSource source;
	private java.awt.Image offscreen;
	
	public VideoPanel(Client client) {
		this.client = client;
		imageIndex = 0;
		
		imageValid = false;

		pixels = new int[WIDTH * HEIGHT];

		source = new MemoryImageSource(WIDTH, HEIGHT, pixels, 0, WIDTH);
		source.setAnimated(true);
		source.setFullBufferUpdates(true);

		offscreen = createImage(source);

		setBackground(Color.white);

		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));

		ThreadPool.createNew(this, "video stream");
	}

	public VideoPanel(Client client, int index){
		this(client);
		imageIndex = index;
	}
	
	public void setIndex(int index){
		imageIndex = index;
	}
	
	public void paint(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;

		Dimension d = getSize();

		FontRenderContext frc = g2.getFontRenderContext();

		int x = d.width / 2;
		int y = d.height / 2;

		if (imageValid) {
			g2.drawImage(offscreen, 0, 0, this);
		} else {
			Color edge = Color.GRAY;
			Color fill = Color.WHITE;

			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, d.width, d.height);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			Font f = getFont().deriveFont(Font.BOLD);
			TextLayout tl = new TextLayout("No Image Available", f, frc);

			float sw = (float) tl.getBounds().getWidth();
			float sh = (float) tl.getBounds().getHeight();

			Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x
					- sw / 2, y + sh / 2));
			g2.setColor(edge);
			g2.draw(sha);
			g2.setColor(fill);
			g2.fill(sha);
		}

		// Draw targeting brackets on image

		g2.setColor(Color.BLACK);

		int w = d.width;
		int h = d.height;
		
		//thickness of line
		int t = 5;

		// top-left
		g2.fillRect(w / 4, h / 4, w / 8, t);
		g2.fillRect(w / 4, (h / 4) + t, t, h / 8);

		// top-right
		g2.fillRect(w - (w / 4) - (w / 8) + t, h / 4, w / 8, t);
		g2.fillRect(w - w / 4, (h / 4) + t, t, h / 8);

		// bottom-left
		g2.fillRect(w / 4, h - (h / 4), w / 8, t);
		g2.fillRect(w / 4, h - (h / 4) - (h / 8), t, h / 8);

		// top-right
		g2.fillRect(w - (w / 4) - (w / 8) + t, h - (h / 4), w / 8, t);
		g2.fillRect(w - w / 4, h - (h / 4) - (h / 8), t, h / 8);

		
	}

	public synchronized boolean isValid() {
		return imageValid;
	}

	public synchronized void setInvalid() {
		imageValid = false;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Image image = client.getDisplayImage(imageIndex);

				if (image == null) {
					logger.debug("videostream exiting");
					return;
				} else {

					Image argb32;

					if (image.getFormat() == Format.ARGB32) {
						argb32 = image;
					} else {
						argb32 = Imaging.convert(image, Format.ARGB32);
					}

					Image scaled;
					if (image.getWidth() == WIDTH
							&& image.getHeight() == HEIGHT) {
						scaled = argb32;
					} else {
						scaled = Imaging.scale(argb32, WIDTH, HEIGHT);
					}

					ByteBuffer buffer = scaled.getData();
					buffer.clear();
					synchronized (this) {
						buffer.asIntBuffer().get(pixels);
						imageValid = true;
					}
				}

				// notify we have new pixels available
				source.newPixels(0, 0, WIDTH, HEIGHT);
				repaint();

			} catch (Exception e) {
				logger.error("Error wwhile displaying stream", e);
			}
		}
	}
}