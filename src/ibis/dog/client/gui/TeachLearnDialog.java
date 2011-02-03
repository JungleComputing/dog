package ibis.dog.client.gui;

import ibis.dog.client.Client;
import ibis.dog.client.Voter;
import ibis.dog.database.Item;
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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class TeachLearnDialog extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private final Client client;
	private final Speech speech;
	private final String name;
	private final JComponent parentWindow;
	private final JProgressBar progressBar;
	private final JTextArea text;

	private boolean done;

	TeachLearnDialog(String name, Client client, Speech speech,
			JComponent parentWindow) {
		this.name = name;
		this.client = client;
		this.speech = speech;
		this.parentWindow = parentWindow;

		// we catch the close event, cancel and dispose in the cancel method.
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);

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

		if (name == null) {
			// recognize
			progressBar.setIndeterminate(false);
			progressBar.setMinimum(0);
			progressBar.setMaximum(Voter.RESULT_SET_SIZE);
			progressBar.setValue(0);
			
			text.setRows(Voter.RESULT_SET_SIZE + 2);

			text.setText("Recognizing object:\n\n");
		} else {
			// learn
			progressBar.setIndeterminate(true);
			text.setText("Learning new object \"" + name + "\"");
		}

		panel.add(progressBar);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));

		// JButton cancelButton = new JButton("Cancel");
		// cancelButton.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent arg0) {
		// cancel();
		// }
		// });
		// // cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 50, 10,
		// // 50));
		// cancelButton.setFont(font);
		// cancelButton.setAlignmentX(CENTER_ALIGNMENT);
		//
		// panel.add(cancelButton);

		setContentPane(panel);

		// setPreferredSize(new Dimension(300, 200));

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				cancel();
			}
		});

		this.setAlwaysOnTop(true);

		pack();

		this.setLocationRelativeTo(parentWindow);

		setVisible(true);

		ThreadPool.createNew(this, "lean/teach dialog");
	}

	private void cancel() {
		System.err.println("Cancel!");

		dispose();
	}

	public synchronized boolean isDone() {
		return done;
	}

	public synchronized void setDone() {
		done = true;
	}

	@Override
	public void run() {
		if (name == null) {
			// recognize

			Voter voter = client.getVoter();
			voter.startVote();


			for(int i = 0; i < Voter.RESULT_SET_SIZE && !isDone(); i++) {
				// update progress
				String vote = voter.waitForVote(i);
				progressBar.setValue(i + 1);
				
				text.append("Server \"" + voter.getSourceOfVote(i) + "\" says this is a \"" + vote + "\"\n");
			}

			String item = voter.getResult();
			dispose();

			String text;
			if (item != null) {
				if (voter.getConfidence() >= 0.7) {
					text = "This object is a \"" + item + "\"";
				} else if (voter.getConfidence() >= 0.4) {
					text = "This seems to be a \"" + item + "\"";
				} else {
					text = "I do not recognize this object";
				}
				speech.speak(text);
				JOptionPane.showMessageDialog(parentWindow, text);
			} else {
				text = "I do not recognize this object";
				speech.speak(text);
				JOptionPane.showMessageDialog(parentWindow, text, "Warning",
						JOptionPane.WARNING_MESSAGE);
			}

		} else {
			// learn
			try {
				client.learn(name);
				dispose();
				String text = "I have just learned a new object called: \""
						+ name + "\".";
				speech.speak(text);

				JOptionPane.showMessageDialog(parentWindow, text);
			} catch (Exception e) {
				dispose();
				String text = "I failed to learn object called: \"" + name
						+ "\".";
				speech.speak(text);
				JOptionPane.showMessageDialog(parentWindow, text + "\n"
						+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		setDone();
	}

}
