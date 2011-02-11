package ibis.dog.client.gui;

import ibis.dog.client.Client;
import ibis.dog.client.ProgressListener;
import ibis.dog.client.Voter;
import ibis.dog.client.Voter.RecognizeResult;
import ibis.util.ThreadPool;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class LearnRecognizeDialog extends JFrame implements ProgressListener,
		Runnable {

	private static final long serialVersionUID = 1L;

	private final Client client;
	private final Speech speech;
	private final JProgressBar progressBar;
	private final JTextArea text;

	private String itemName;

	private boolean learning = false;
	private boolean recognizing = false;

	LearnRecognizeDialog(Client client, Speech speech) {
		this.client = client;
		this.speech = speech;

		// we catch the close event, cancel and dispose in the cancel method.
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// create status dialog
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		Font font = new Font(null, Font.BOLD, 16);

		text = new JTextArea(1, 25);
		text.setFont(font);
		text.setOpaque(false);
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(text);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);

		panel.add(progressBar);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		// cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 50, 10,
		// 50));
		cancelButton.setFont(font);
		cancelButton.setAlignmentX(CENTER_ALIGNMENT);

		panel.add(cancelButton);

		setContentPane(panel);

		// setPreferredSize(new Dimension(300, 200));

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				cancel();
			}
		});

		this.setAlwaysOnTop(true);

		pack();
		setVisible(false);
	}

	public synchronized boolean isLearning() {
		return learning;
	}

	public synchronized boolean isRecognizing() {
		return recognizing;
	}

	public void cancel() {
		synchronized (this) {
			learning = false;
			recognizing = false;

			notifyAll();
		}

		dispose();
		setVisible(false);
		repaint();

	}

	@Override
	public void message(String message) {
		text.append(message + "\n");

		repaint();
	}

	@Override
	public boolean progress(double progress) {
		if (progress > 0.0) {
			progressBar.setValue((int) (progress * 100));
			progressBar.setIndeterminate(false);
			repaint();
		}

		return isLearning() || isRecognizing();
	}

	public void learn(String itemName) {
		synchronized (this) {
			if (isLearning() || isRecognizing()) {
				return;
			}
		}

		this.itemName = itemName;

		learning = true;

		text.setRows(2);
		text.setText("Learning new object \"" + itemName + "\"");

		progressBar.setValue(0);
		progressBar.setIndeterminate(true);

		pack();
		setVisible(true);

		ThreadPool.createNew(this, "lerning thread");

	}

	public void recognize() {
		synchronized (this) {
			if (isLearning() || isRecognizing()) {
				return;
			}

			recognizing = true;
		}

		text.setRows(Voter.RESULT_SET_SIZE + 2);
		text.setText("Recognizing object:\n\n");

		progressBar.setValue(0);
		progressBar.setIndeterminate(true);

		pack();

		setVisible(true);

		ThreadPool.createNew(this, "recognize thread");
	}

	private synchronized String getItemName() {
		return itemName;
	}

	@Override
	public void run() {

		if (isLearning()) {
			boolean result = false;

			// learn
			try {
				result = client.learn(getItemName(), this);
				dispose();
				setVisible(false);
				if (result) {
					String text = "I have just learned a new object called: \""
							+ itemName + "\".";
					speech.speak(text);
					JOptionPane.showMessageDialog(this, text);
				} else if (isLearning()) {
					String text = "I failed to learn object called: \""
							+ itemName + "\".";
					speech.speak(text);
					JOptionPane.showMessageDialog(this, text);
				}
			} catch (Exception e) {
				dispose();
				setVisible(false);
				String text = "I failed to learn object called: \"" + itemName
						+ "\".";
				speech.speak(text);
				JOptionPane.showMessageDialog(this, text + "\n"
						+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				cancel();
			}

		} else if (isRecognizing()) {
			try {
				RecognizeResult result = client.recognize(this);

				dispose();

				String text;
				if (result != null && result.getItem() != null) {
					if (result.getConfidence() >= 0.7) {
						text = "This object is a \""
								+ result.getItem().getName() + "\"";
					} else if (result.getConfidence() >= 0.4) {
						text = "This seems to be a \""
								+ result.getItem().getName() + "\"";
					} else {
						text = "I do not recognize this object";
					}
					speech.speak(text);
					JOptionPane.showMessageDialog(this, text);
				} else if (isRecognizing()) {
					text = "I do not recognize this object";
					speech.speak(text);
					JOptionPane.showMessageDialog(this, text, "Warning",
							JOptionPane.WARNING_MESSAGE);
				}
			} catch (Exception e) {
				dispose();
				JOptionPane
						.showMessageDialog(this, e.getMessage(),
								"Could not recognize Object",
								JOptionPane.ERROR_MESSAGE);
			} finally {
				cancel();
			}
		}
	}

}
