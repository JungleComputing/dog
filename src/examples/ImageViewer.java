package examples;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class ImageViewer extends JFrame implements ActionListener {
  public ImageViewer() {
    setTitle("ImageViewer");
    setSize(300, 400);

    JMenuBar mbar = new JMenuBar();
    JMenu m = new JMenu("File");
    openItem = new JMenuItem("Open");
    openItem.addActionListener(this);
    m.add(openItem);
    exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(this);
    m.add(exitItem);
    mbar.add(m);
    setJMenuBar(mbar);

    label = new JLabel();
    Container contentPane = getContentPane();
    contentPane.add(label, "Center");
  }

  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if (source == openItem) {
      JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new File("."));

      chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
        public boolean accept(File f) {
          return f.getName().toLowerCase().endsWith(".gif")
              || f.isDirectory();
        }

        public String getDescription() {
          return "GIF Images";
        }
      });

      int r = chooser.showOpenDialog(this);
      if (r == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().getName();
        label.setIcon(new ImageIcon(name));
      }
    } else if (source == exitItem)
      System.exit(0);
  }

  public static void main(String[] args) {
    JFrame frame = new ImageViewer();
    frame.show();
  }

  private JLabel label;

  private JMenuItem openItem;

  private JMenuItem exitItem;
}