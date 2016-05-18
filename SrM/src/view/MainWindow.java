package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.JTextComponent.AccessibleJTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.UndoManager;
import misc.Controller;
import misc.Settings;
import misc.Strings;
import model.Change;
import model.Error;

/**
 * The main window of the program.
 * @author Ulic Quel-droma
 * @version 1.4.2
 */
public class MainWindow extends JFrame implements ActionListener, KeyListener {

	private static final long serialVersionUID = 4173524293906823362L;
	private static final Logger logger;
	private static final ResourceBundle bundle = Strings.bundle;
	
	static{
		logger = Logger.getLogger(Controller.projectName + "." + MainWindow.class.getCanonicalName());
	}
	
	private final Controller controller;
	
	//GUI variables
	private JButton stop, step, run, skip;
	private JTextPane code;
	private JLabel accumulator, lineCounter, accTitle, lineCTitle, message;
	private JLabel [] register;
	private JLabel [] registerTitles;
	private JMenuItem load, save, saveAs, create;
	private JCheckBoxMenuItem highLogLevel, mediumLogLevel, lowLogLevel, logLevelOff, createUninstaller, autoUpdate;
	private UndoManager undoM;
	
	/**
	 * Creates new MainWindow.
	 * @param controller The controller.
	 * @throws HeadlessException see {@link JFrame#JFrame()}
	 */
	public MainWindow(Controller controller) throws HeadlessException {
		super();
		try{UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");}
        catch(Exception e){logger.log(Level.FINE,"Nimbus look could not be set", e);}
		this.controller = controller;
		initizializeLayout();
		initializeMenuBar();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new MyWindowListener());
		setLocationByPlatform(true);
		updateMessage(null);
	}
	
	@SuppressWarnings("serial")//none will be saved
	private void initizializeLayout(){
		setTitle(Strings.applicationName);
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		Insets insets = new Insets(0,4,8,4);
		byte w = 4;//The standard width
		Font f = new Font("Tahoma", Font.PLAIN, 15);//The standard Font
		byte bt = 2;//The standard border thickness
		Dimension minimumSize = new Dimension(80, 25);
		Dimension preferredSize = new Dimension(110, 30);
		//Button beginn
		stop = new JButton(Strings.stopButton); stop.setFont(f);
		stop.setMinimumSize(minimumSize); stop.setPreferredSize(preferredSize);
		stop.addKeyListener(this); stop.addActionListener(this);
		stop.setToolTipText(Strings.stopToolTip);
		addComponent(this, gbl, stop, 1, 0, w, 2, 0, 0, insets, GridBagConstraints.HORIZONTAL);
		//Button step
		step = new JButton(Strings.stepButton); step.setFont(f);
		step.setMinimumSize(minimumSize); step.setPreferredSize(preferredSize);
		step.addKeyListener(this); step.addActionListener(this);
		step.setToolTipText(Strings.stepToolTip);
		addComponent(this, gbl, step, 5, 0, w, 2, 0, 0, insets, GridBagConstraints.HORIZONTAL);
		//Button run
		run = new JButton(Strings.runButton); run.setFont(f);
		run.setMinimumSize(minimumSize); run.setPreferredSize(preferredSize);
		run.addKeyListener(this); run.addActionListener(this);
		run.setToolTipText(Strings.runToolTip);
		addComponent(this, gbl, run, 9, 0, w, 2, 0, 0, insets, GridBagConstraints.HORIZONTAL);
		//Button skip
		skip = new JButton(Strings.skipButton); skip.setFont(f);
		skip.setMinimumSize(minimumSize); skip.setPreferredSize(new Dimension(120, 30));
		skip.addKeyListener(this); skip.addActionListener(this);
		skip.setToolTipText(Strings.skipToolTip);
		addComponent(this, gbl, skip, 4, 2, w + 2, 1, 1, 0, insets, GridBagConstraints.HORIZONTAL);
		//Saves
		w = 3;
		preferredSize = new Dimension(100, 30);
		minimumSize = new Dimension(75, 25);
		Border border = new LineBorder(Color.BLUE, bt, false);
		register = new JLabel[16]; registerTitles = new JLabel [16];
		for(int i = 0; i < 16; i++){
			//The label
			JLabel label = new JLabel("R" + i);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setFont(f); registerTitles [i] = label;
			label.setMinimumSize(minimumSize); label.setPreferredSize(preferredSize);
			addComponent(this, gbl, label, 1 + (i%4)*w, 5 + Math.round(i/4)*3, w, 1, 1, 1, insets, GridBagConstraints.NONE);
			//System.out.println("Adding label at: " + 1 + (i%4)*w + " and " + 5 + Math.round(i/4)*3);
			//The Value
			JLabel value = new JLabel("0"); value.setBorder(border);
			value.setHorizontalAlignment(SwingConstants.CENTER);
			value.setOpaque(true); value.setBackground(Color.WHITE);
			value.setToolTipText(String.format(Strings.valueToolTip, i));
			value.setFont(f); register [i] = value;
			value.setMinimumSize(minimumSize); value.setPreferredSize(preferredSize);
			addComponent(this, gbl, value, 1 + (i%4)*w, 6 + Math.round(i/4)*3, w, 2, 1, 1, insets, GridBagConstraints.NONE);
		}
		//Label accumulator title
		accTitle = new JLabel("A"); accTitle.setFont(f);
		accTitle.setHorizontalAlignment(SwingConstants.CENTER);
		accTitle.setMinimumSize(minimumSize); accTitle.setPreferredSize(preferredSize);
		addComponent(this, gbl, accTitle, 1, 2, w, 1, 1, 1, insets, GridBagConstraints.NONE);
		//Label line counter title
		lineCTitle = new JLabel("BZ"); lineCTitle.setFont(f);
		lineCTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lineCTitle.setMinimumSize(minimumSize); lineCTitle.setPreferredSize(preferredSize);
		addComponent(this, gbl, lineCTitle, 10, 2, w, 1, 1, 1, insets, GridBagConstraints.NONE);
		//Label accumulator
		border = new LineBorder(Color.YELLOW, bt, false);
		accumulator = new JLabel("0"); accumulator.setBorder(border);
		accumulator.setBackground(Color.WHITE);
		accumulator.setToolTipText(Strings.accumulatorToolTip);
		accumulator.setOpaque(true); accumulator.setFont(f);
		accumulator.setHorizontalAlignment(SwingConstants.CENTER);
		accumulator.setMinimumSize(minimumSize); accumulator.setPreferredSize(preferredSize);
		addComponent(this, gbl, accumulator, 1, 3, w, 2, 1, 1, insets, GridBagConstraints.NONE);
		//Label line counter
		border = new LineBorder(Color.RED, bt, false);
		lineCounter = new JLabel("0"); lineCounter.setBorder(border);
		lineCounter.setBackground(Color.WHITE);
		lineCounter.setToolTipText(Strings.lineCounterToolTip);
		lineCounter.setOpaque(true); lineCounter.setFont(f);
		lineCounter.setHorizontalAlignment(SwingConstants.CENTER);
		lineCounter.setMinimumSize(minimumSize); lineCounter.setPreferredSize(preferredSize);
		addComponent(this, gbl, lineCounter, 10, 3, w, 2, 1, 1, insets, GridBagConstraints.NONE);
		//Label message
		border = new LineBorder(Color.BLACK, bt, false);
		message = new JLabel(""); message.setBorder(border);
		message.setBackground(Color.WHITE);
		message.setOpaque(true); message.setVisible(false);
		message.setHorizontalAlignment(SwingConstants.CENTER);
		addComponent(this, gbl, message, 1, 19, w * 4, 1, 1, 1, insets, GridBagConstraints.BOTH);
		//Code editor
		code = new JTextPane(); code.addKeyListener(this);
		code.getDocument().addDocumentListener(new CodeDocumentListener(controller));
		undoM = new UndoManager(){
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				if(e.getEdit().getPresentationName().equalsIgnoreCase("hinzufügen"))
					super.undoableEditHappened(e);
			}
			
		}; 
		undoM.setLimit(200);
		code.getDocument().addUndoableEditListener(undoM);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(code);
		scrollPane.setPreferredSize(new Dimension(500, 500));
		addComponent(this, gbl, scrollPane, 0, 0, 1, 20, 20, 20, new Insets(10,10,0,10), GridBagConstraints.BOTH);
		setMinimumSize(new Dimension(500,400));
		setPreferredSize(new Dimension(750, 500));
		pack();
		code.requestFocus();
		logger.log(Level.FINER, "GUI created");
	}
	
	private static void addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, 
			int width, int height, double weightx, double weighty, Insets insets, int fill){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = fill;
		gbc.gridx = x; gbc.gridy = y;
		gbc.gridwidth = width; gbc.gridheight = height;
		gbc.weightx = weightx; gbc.weighty = weighty;
		if(insets != null){
			gbc.insets = insets;
		}
		gbl.setConstraints(c,  gbc); cont.add(c);
	}
	
	@SuppressWarnings("serial") //none will be saved
	private void initializeMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(Strings.fileMenuName);
		Action createAction = new AbstractAction(Strings.createMenuItem) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.createPressed();
			}
		};
		create = fileMenu.add(createAction);
		create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		//open menu item
		Action openAction = new AbstractAction(Strings.openMenuItem){
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.openPressed();
			}
		};
		load = fileMenu.add(openAction);
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		fileMenu.addSeparator();
		//save menu item
		Action saveAction = new AbstractAction(Strings.saveMenuItem){
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.savePressed();
			}
		};
		save = fileMenu.add(saveAction);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		//save as menu item
		Action saveAsAction = new AbstractAction(Strings.saveAsMenuItem) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.saveAsPressed();
			}
		};
		saveAs = fileMenu.add(saveAsAction);
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		fileMenu.addSeparator();
		//end menu item
		Action endAction = new AbstractAction(Strings.endMenuItem) {
			@Override
			public void actionPerformed(ActionEvent e) {
				Controller.exit(0);
			}
		};
		fileMenu.add(endAction);
		menuBar.add(fileMenu);
		JMenu configMenu = new JMenu(Strings.configMenuName);
		//Log menu
		JMenu logMenu = new JMenu(Strings.logMenuName);
		ButtonGroup logGroup = new ButtonGroup();
		//high log level
		highLogLevel = new JCheckBoxMenuItem(
				new AbstractAction(Strings.highLogLevel) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setLogLevel(Level.ALL);
			}
		});
		logMenu.add(highLogLevel); logGroup.add(highLogLevel);
		//medium log level
		mediumLogLevel = new JCheckBoxMenuItem(
				new AbstractAction(Strings.mediumLogLevel) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setLogLevel(Level.FINE);
			}
		});
		logMenu.add(mediumLogLevel); logGroup.add(mediumLogLevel);
		//low log level
		lowLogLevel = new JCheckBoxMenuItem(
				new AbstractAction(Strings.lowLogLevel) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setLogLevel(Level.INFO);
			}
		});
		logMenu.add(lowLogLevel); logGroup.add(lowLogLevel);
		//no logging
		logLevelOff = new JCheckBoxMenuItem(
				new AbstractAction(Strings.logLevelOff) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setLogLevel(Level.OFF);
			}
		});
		logMenu.add(logLevelOff); logGroup.add(logLevelOff);
		configMenu.add(logMenu);
		//create Uninstaller
		createUninstaller = new JCheckBoxMenuItem(
				new AbstractAction(Strings.createUninstallerMenuItem) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setCreateUninstaller(((JCheckBoxMenuItem)e.getSource()).isSelected());
			}
		});
		configMenu.add(createUninstaller);
		autoUpdate = new JCheckBoxMenuItem(
				new AbstractAction(bundle.getString("autoUpdateMenuItem")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setAutoUpdate(((JCheckBoxMenuItem)e.getSource()).isSelected());
			}
		});
		configMenu.add(autoUpdate);
		menuBar.add(configMenu);
		JMenu helpMenu = new JMenu(Strings.helpMenuName);
		//help menu item
		Action helpAction = new AbstractAction(Strings.helpMenuItem){
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.helpPressed();
			}
		};
		JMenuItem help = helpMenu.add(helpAction);
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpMenu.addSeparator();
		//update menu item
		Action updateAction = new AbstractAction(Strings.updateMenuItem) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.updatePressed();
			}
		};
		helpMenu.add(updateAction);
		//about menu item
		Action aboutAction = new AbstractAction(Strings.aboutMenuItem) {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.aboutPressed();
			}
		};
		helpMenu.add(aboutAction);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
	}
	/**
	 * Sets the settings to the MenuItems.
	 * @param settings The settings to be set.
	 */
	public void setSettings(final Settings settings){
		Level level = settings.getLogLevel();
		if(level == Level.ALL)
			highLogLevel.setSelected(true);
		else if(level == Level.INFO)
			lowLogLevel.setSelected(true);
		else if(level == Level.OFF)
			logLevelOff.setSelected(true);
		else
			mediumLogLevel.setSelected(true);
		createUninstaller.setSelected(settings.isCreateUninstaller()); 
		autoUpdate.setSelected(settings.isAutoUpdate());
	}
	/**
	 * Marks the active line.
	 * @param offset The begin of the line to be marked.
	 * @param length The length of the line to be marked.
	 */
	public void setActiveLine(int offset, int length) {
		if(code.isEditable())
			return; //because otherwise cursor jumps to begin if nothing is to be selected.
		//code.select runs now too, because of JTextPane
		AccessibleJTextComponent aTC = (AccessibleJTextComponent) code.getAccessibleContext();
		if(offset <= -1 || length <= 0){
			aTC.selectText(0, 0);
			return;
		}
		aTC.selectText(offset, offset + length);
	}
	/**
	 * @param codeVal The text of the editor.
	 */
	public void setEditorText(String codeVal) {
		code.setText(codeVal);
	}
	/**
	 * @return The text of the editor
	 */
	public String getEditorText(){
		return code.getText();
	}
	/**
	 * Enables or disables every Action connected to the text, like changing text or saving it.
	 * @param active <code>true</code> if the text options should be active.
	 */
	public void setTextOptionsActive(boolean active) {
		create.setEnabled(active);
		load.setEnabled(active);
		save.setEnabled(active);
		saveAs.setEnabled(active);
		code.setEditable(active);
		if(active){
			run.requestFocus();//otherwise after changes caret is invisible
			code.requestFocus();
		}
	}
	/**
	 * Enables or disables the buttons.
	 * @param active <code>true</code> if the buttons should be active.
	 */
	public void setButtonsActive(boolean active){
		run.setEnabled(active);
		step.setEnabled(active);
		skip.setEnabled(active);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object rawSrc = e.getSource();
		if(rawSrc.equals(stop)){
			controller.stopPressed();
		} else if(rawSrc.equals(step)){
			controller.stepPressed();
		} else if(rawSrc.equals(run)){
			controller.runPressed();
		} else if(rawSrc.equals(skip)){
			controller.skipPressed();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyChar() == KeyEvent.CHAR_UNDEFINED)
			return;
		if(e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK){
			switch(e.getKeyCode()){
			case 'R':
				controller.runPressed();
				break;
			case KeyEvent.VK_END:
				controller.stopPressed();
				break;
			case '\n':
				int caretPos = code.getCaretPosition();
				executeChange(new Change(caretPos, 0, "\n ", caretPos + 1, new Change(caretPos + 1, 1, "")));
				break;
			case 'Z':
				undoM.undo();
				break;
			case 'Y':
				undoM.redo();
				break;
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
	}
	/**
	 * Updates the values of the fields.
	 * @param accumulatorVal The accumulator value
	 * @param lineCounterVal The lineCounter value
	 * @param registerVal The register values
	 */
	public void updateFields(int accumulatorVal, int lineCounterVal, int[] registerVal) {
		updateFields(accumulatorVal, lineCounterVal);
		for(int i = 0; i < 16; i++){
			register [i].setText(Integer.toString(registerVal [i]));
		}
	}
	/**
	 * Updates the values of the fields.
	 * @param accumulatorVal The accumulator value.
	 * @param lineCounterVal The lineCounter value.
	 */
	public void updateFields(int accumulatorVal, int lineCounterVal) {
		accumulator.setText(Integer.toString(accumulatorVal));
		lineCounter.setText(Integer.toString(lineCounterVal));
	}
	/**
	 * Sets the extra message shown by the frame.
	 * @param messageVal The text to show.
	 * @param messageType The type of the Message. See {@link JOptionPane}.
	 */
	public void updateMessage(String messageVal, int messageType){
		if(messageVal == null){
			message.setText(" ");
			return;
		}
		switch(messageType){
		case JOptionPane.WARNING_MESSAGE:
		case JOptionPane.ERROR_MESSAGE:
			message.setForeground(Color.RED);
			break;
		case JOptionPane.PLAIN_MESSAGE:
		default:
			message.setForeground(Color.BLACK);
		}
		message.setText(messageVal);
		message.setVisible(!messageVal.isEmpty());
	}
	/**
	 * Sets the extra message shown by the frame.
	 * @param messageVal The text to show.
	 * @see #updateMessage(String, int)
	 */
	public void updateMessage(String messageVal){
		updateMessage(messageVal, JOptionPane.PLAIN_MESSAGE);
	}
	/**
	 * Marks the errors in the code editor.
	 * @param errors The list of errors.
	 */
	public void markErrors(LinkedList<Error> errors) {
		final AccessibleJTextComponent aTC = (AccessibleJTextComponent) code.getAccessibleContext();
		SimpleAttributeSet aSet = new SimpleAttributeSet();
		aSet.addAttribute(StyleConstants.Foreground, Color.BLACK);
		aTC.setAttributes(0, code.getDocument().getLength(), aSet);
		aSet.removeAttribute(StyleConstants.Foreground);
		aSet.addAttribute(StyleConstants.Foreground, Color.RED);
		for(final Error error : errors){
			aTC.setAttributes(error.getOffset(), error.getOffset() + error.getLength(), aSet);
		}
	}
	/**
	 * @param text The code to set.
	 */
	public void setCode(String text) {
		this.code.setText(text);
	}
	/**
	 * Executes a change on the text with {@link EventQueue#invokeLater(Runnable)}.
	 * @param change The change to execute.
	 */
	public void executeChange(final Change change) {
		if(change == null)
			return;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				executeChanges(change);
				if(change.caretPos > -1)
					code.setCaretPosition(change.caretPos);
			}
		};
		EventQueue.invokeLater(r);
	}
	
	private void executeChanges(final Change change){
		if(change == null)
			return;
		try {
			if(change.length > 0)
				code.getDocument().remove(change.offset, change.length);
			if(change.s == null || !change.s.isEmpty())
				code.getDocument().insertString(change.offset, change.s, null);
		} catch (BadLocationException e) {
			logger.log(Level.WARNING, "There was an error when updating the code.\n" + change.toString(), e);
		}
		if(change.next != null)
			executeChanges(change.next);
	}
	
	private class MyWindowListener extends WindowAdapter{
		@Override
		public void windowClosing(WindowEvent e) {
			Controller.exit(0);
		}
	}
}
