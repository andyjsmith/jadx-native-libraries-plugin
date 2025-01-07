package org.ajsmith.jadx.plugins.nativelibraries;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;

public class ProgressDialog extends JDialog {
	private final JLabel message;

	public ProgressDialog(Component parent, String message) {
		super((Window) parent, "Loading", ModalityType.MODELESS);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		setResizable(false);
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.message = new JLabel(message);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(this.message);
		panel.add(Box.createRigidArea(new Dimension(0, 10)));
		panel.add(progressBar);
		add(panel);
		pack();
		setLocationRelativeTo(parent);
	}

	public void setMessage(String message) {
		this.message.setText(message);
	}

	public void close() {
		setVisible(false);
		dispose();
	}
}
