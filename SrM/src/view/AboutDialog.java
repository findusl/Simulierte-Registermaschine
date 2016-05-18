package view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import misc.Controller;
import misc.Installer;
import misc.Strings;

/**
 * An AboutDialog.
 * @author Ulic Quel-droma
 * @version 1.3.3
 */
public class AboutDialog extends JDialog implements ActionListener{
	private static final long serialVersionUID = 2566945927276159128L;
	private static final Logger logger = Logger.getLogger(Controller.projectName + "." + AboutDialog.class.getSimpleName());
	private static final ResourceBundle bundle = ResourceBundle.getBundle(Controller.baseName);
	
	private JButton ctc;
	private JButton ok;
	/**
	 * Creates new AboutDialog
	 * @param owner The Frame from which the dialog is displayed.
	 * @see JDialog#JDialog(Frame, String, boolean)
	 */
	public AboutDialog(Frame owner) {
		super(owner, bundle.getString("aboutDialogTitle"), true);
		initializeLayout();
		setLocationByPlatform(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private void initializeLayout(){
		JPanel panel = new JPanel();
		setText(panel, Strings.about, "\n");
		panel.setBorder(new EmptyBorder(10, 10, 10, 30));
		add(panel, BorderLayout.PAGE_START);
		ctc = new JButton(bundle.getString("ctcButton"));
		ctc.addActionListener(this);
		add(ctc, BorderLayout.WEST);
		ok = new JButton(bundle.getString("ok"));
		ok.addActionListener(this);
		add(ok, BorderLayout.CENTER);
		pack();
	}
	/**
	 * Creates JLabels for every line of the text. They are ordered, 
	 * depending on the country, from up to down or from down to up.
	 * @param panel The panel to store the labels in.
	 * @param text The text to break up.
	 * @param sep The line-separator.
	 */
	public static void setText(JPanel panel, String text, String sep){
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		String [] strings = text.split(sep);
		for(String s : strings){
			if(s.isEmpty())
				s = " ";
			panel.add(new JLabel(s));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(ctc)){
			Clipboard systemClip = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection sSel = new StringSelection(Installer.getInstaller().projectDirectory.getAbsolutePath());
			try{
				systemClip.setContents(sSel, sSel);
			} catch(IllegalStateException ex){
				logger.log(Level.INFO, "Could not copy to clipboard.", e);
				JOptionPane.showMessageDialog(this, bundle.getString("ctcError"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
			}
		} else if(e.getSource().equals(ok)){
			dispose();
		}
	}
}