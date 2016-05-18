package misc;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import model.Change;
import model.Model;
import model.Model.messageconstant;
import view.AboutDialog;
import view.MainWindow;

/**
 * Connects View and Model
 * 
 * @author Ulic Quel-droma
 * @version 1.5
 */
public class Controller implements DocumentListener {
	private static final Logger logger;
	private static Handler fHandler;
	/**
	 * The name of the project and of its logger.
	 */
	public static final String projectName = Model.projectName;
	private static final Level logLevel = Level.INFO;
	/**
	 * The version of the program.
	 */
	public static final String version;
	/**
	 * The name of the base file of the string resources.
	 */
	public static final String baseName = "resources.SrM";

	static {
		logger = Logger.getLogger(Controller.projectName + "." + Controller.class.getCanonicalName());
		// creates Handlers for project wide logger
		final Logger projectLogger = Logger.getLogger(Controller.projectName);
		projectLogger.setUseParentHandlers(false);
		projectLogger.setLevel(Level.ALL);
		Handler cHandler = new AutoFlushStreamHandler(System.out, new SimpleFormatter());
		cHandler.setLevel(Level.ALL); // only for testing
		projectLogger.addHandler(cHandler);
		try {
			Controller.fHandler = new FileHandler(Installer.getInstaller().projectDirectory + "/Log_%g.log", 1000000, 2, true);
			Controller.fHandler.setLevel(Controller.logLevel);
			Controller.fHandler.setFormatter(new SimpleFormatter());
			projectLogger.addHandler(Controller.fHandler);
		} catch (SecurityException e) {
			Controller.logger.log(Level.WARNING, "Filehandler could not be created", e);
		} catch (IOException e) {
			Controller.logger.log(Level.WARNING, "Filehandler could not be created", e);
		}
		String temp = "0.0";
		try {
			temp = new Updater().getVersion();
		} catch (IOException ignored) {
		}
		version = temp;
	}

	private MainWindow mainWindow;
	private Model model;
	private JFileChooser fc;
	private Thread worker;
	private Thread updater;
	private boolean valuesEndState;
	private Settings settings;

	/**
	 * Creates new Controller.
	 */
	public Controller() {
		final Controller con = this;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainWindow = new MainWindow(con);
				mainWindow.setVisible(true);
				fc = new JFileChooser(new File(System.getProperty("user.home")));
				mainWindow.executeChange(new Change(0, 0, "1: ", 3));
			}
		});
		model = new Model();
		worker = new Thread();
		settings = new Settings();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainWindow.setSettings(settings);
			}
		});
		Controller.fHandler.setLevel(settings.getLogLevel());
		Installer.getInstaller().unpackHelp();
		Installer.getInstaller().createUpdater();
		if (settings.isCreateUninstaller()) Installer.getInstaller().createUninstaller();
		if (settings.isAutoUpdate()) startUpdate(false);
	}

	/**
	 * Creates new Controller.
	 * 
	 * @param source
	 *           The file containing the code to load.
	 */
	public Controller(File source) {
		this();
		if (source != null)
			try {
				final String code = model.load(source);
				Runnable r = new Runnable() {
					@Override
					public void run() {
						mainWindow.setEditorText(code);
					}
				};
				Controller.runInDepatcherThreadandWait(r);
				updateCodeEditor();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(mainWindow, Strings.fileNotValid, Strings.error, JOptionPane.ERROR_MESSAGE);
				Controller.logger.log(Level.INFO, "Could not load File " + source.getAbsolutePath(), e);
			}
	}

	/**
	 * Is only used to restart the application after an error. Creates a new
	 * Controller.
	 * 
	 * @param code
	 *           The recovered code.
	 */
	private Controller(final String code) {
		this();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				mainWindow.setEditorText(code);
			}
		};
		Controller.runInDepatcherThreadandWait(r);
		model.resetCompiler(code);
	}

	/**
	 * Is invoked when stop is pressed. Tries to stop the worker thread if
	 * running.
	 */
	@SuppressWarnings("deprecation")
	// special times request special actions
	public void stopPressed() {
		if (worker != null && worker.isAlive()) {
			worker.interrupt();
			try {
				worker.join(100);
			} catch (InterruptedException e) {
				Controller.logger.log(Level.WARNING, "Interrupted while waiting for the worker thread.", e);
				return;// for security reasons
			}
			if (worker.isAlive()) {
				Controller.logger.log(Level.WARNING, "Stopping Worker the brutal way.");
				worker.stop();// well, then with brute force
				rebuildAfterHangup();
				return;
			}
		}
		model.reset();
		setButtonsActive(true);
		setTextOptionsActive(true);
		updateFields(true);
		updateMessage();
	}

	/**
	 * Is invoked when step is pressed.
	 */
	public synchronized void stepPressed() {
		if (!canRun())
			return;
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				setButtonsActive(false);
				setTextOptionsActive(false);
				boolean updateRs;
				try {
					updateRs = model.step();
				} catch (InterruptedException e) {
					Controller.logger.log(Level.FINE, "Successfully interrupted worker-thread", e);
					return;
				}
				updateFields(updateRs);
				updateMessage();
				setButtonsActive(true);
				setTextOptionsActive(model.isEndReached());
				valuesEndState = model.isEndReached();
				if (model.isEndReached()) model.reset();
			}
		});
		worker.start();
	}

	/**
	 * Invoked when run is pressed
	 */
	public synchronized void runPressed() {
		if (!canRun())
			return;
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				setButtonsActive(false);
				setTextOptionsActive(false);
				try {
					model.run();
				} catch (InterruptedException e) {
					Controller.logger.log(Level.FINE, "Successfully interrupted worker-thread", e);
					return;
				}
				updateFields(true);
				setButtonsActive(true);
				setTextOptionsActive(true);
				updateMessage();
				valuesEndState = model.isEndReached();
				if (model.isEndReached()) model.reset();
			}
		});
		worker.start();
	}

	/**
	 * Is called when the button skip is pressed. Shows an input dialog which
	 * requests the number of lines to skip. Then skips the lines and updates the
	 * View.
	 */
	public synchronized void skipPressed() {
		if (!canRun())
			return;
		final int lines;
		try {
			String input = JOptionPane.showInputDialog(mainWindow,
				Strings.skipLinesMessage, Strings.skipButton,
				JOptionPane.QUESTION_MESSAGE);
			if (input == null || input.isEmpty())
				return;
			lines = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(mainWindow, Strings.skipLinesInputError, Strings.error,
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				setButtonsActive(false);
				setTextOptionsActive(false);
				try {
					model.skipLines(lines);
				} catch (InterruptedException e) {
					Controller.logger.log(Level.FINE, "Successfully interrupted worker-thread", e);
					return;
				}
				updateFields(true);
				setButtonsActive(true);
				setTextOptionsActive(model.isEndReached());
				updateMessage();
				valuesEndState = model.isEndReached();
				if (model.isEndReached()) model.reset();
			}
		});
		worker.start();
	}

	private boolean canRun() {
		if (!model.canRun()) {
			updateMessage();
			updateCodeEditor();
			JOptionPane.showMessageDialog(mainWindow, Strings.cantRun, Strings.error, JOptionPane.ERROR_MESSAGE);
			return false;
		} else return true;
	}

	private void setButtonsActive(final boolean active) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				mainWindow.setButtonsActive(active);
			}
		};
		Controller.runInDepatcherThreadandWait(r);
	}

	private void setTextOptionsActive(final boolean active) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				mainWindow.setTextOptionsActive(active);
			}
		};
		Controller.runInDepatcherThreadandWait(r);
	}

	/**
	 * Should restart the window if data is corrupted.
	 */
	private void rebuildAfterHangup() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					new Controller(mainWindow.getEditorText());
				} catch (Throwable t) {
					Controller.logger.log(Level.WARNING, "Could not get the code for rebuild.", t);
					new Controller();
				}
				Runtime.getRuntime().gc();
				JOptionPane.showMessageDialog(null, Strings.brutalWorkerStop);
			}
		}).start();
		mainWindow.dispose();
		mainWindow = null;
		model = null;
	}

	/**
	 * Invoked when create is pressed.
	 */
	public void createPressed() {
		model = new Model();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				mainWindow.setEditorText(new String());
				mainWindow.updateMessage(new String());
			}
		};
		Controller.runInDepatcherThreadandWait(r);
	}

	/**
	 * Invoked when open is pressed.
	 */
	public void openPressed() {
		int returnVal = fc.showOpenDialog(mainWindow);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			try {
				final String code = model.load(fc.getSelectedFile());// maybe react
				// to too slow
				// loading...
				Runnable r = new Runnable() {
					@Override
					public void run() {
						mainWindow.setEditorText(code);
					}
				};
				Controller.runInDepatcherThreadandWait(r);
				updateCodeEditor();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(mainWindow, Strings.fileNotValid, Strings.error, JOptionPane.ERROR_MESSAGE);
			}
		else {
			if (returnVal == JFileChooser.ERROR_OPTION) JOptionPane.showMessageDialog(mainWindow, Strings.openError);
			return;// otherwise the openDialog was canceled
		}
	}

	/**
	 * Invoked when save is pressed.
	 */
	public void savePressed() {
		if (model.isOutOfFile()) {
			model.save();
			updateMessage();
		} else saveAsPressed();
	}

	/**
	 * Invoked when save as is pressed.
	 */
	public void saveAsPressed() {
		fc.setSelectedFile(new File("neu.txt"));
		int returnVal = fc.showSaveDialog(mainWindow);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			model.saveAs(fc.getSelectedFile());
		else {
			if (returnVal == JFileChooser.ERROR_OPTION) JOptionPane.showMessageDialog(mainWindow, Strings.openError);
			return;// otherwise the openDialog was canceled
		}
		updateMessage();
	}

	/**
	 * @param level
	 *           The logging level.
	 * @see java.util.logging.Level
	 */
	public void setLogLevel(Level level) {
		Controller.fHandler.setLevel(level);
		settings.setLogLevel(level);
	}

	/**
	 * @param selected
	 *           <code>true</code> if an uninstaller should be created.
	 */
	public void setCreateUninstaller(boolean selected) {
		settings.setCreateUninstaller(selected);
		if (selected)
			Installer.getInstaller().createUninstaller();
		else
			Installer.getInstaller().deleteUninstaller();
	}

	/**
	 * @param autoUpdate
	 *           <code>true</code> if autoUpdate is active
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		settings.setAutoUpdate(autoUpdate);
		if (autoUpdate) startUpdate(true);
	}

	/**
	 * Invoked when help is pressed.
	 */
	public void helpPressed() {
		try {
			Desktop.getDesktop().open(Installer.getInstaller().getHelpFile());
		} catch (IOException e) {
			Controller.logger.log(Level.WARNING, "Error when opening file.", e);
		}
	}

	/**
	 * Invoked when about is pressed.
	 */
	public void aboutPressed() {
		new AboutDialog(mainWindow).setVisible(true);// EventQueue?
	}

	/**
	 * Invoked when update is pressed.
	 */
	public void updatePressed() {
		if (updater != null && updater.isAlive()) {
			JOptionPane.showMessageDialog(mainWindow, Strings.updateRuns, Strings.update, JOptionPane.WARNING_MESSAGE);
			return;
		}
		startUpdate(true);
		//JOptionPane.showMessageDialog(mainWindow, Strings.updateStarted, Strings.update, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Starts the update in the updater Thread.
	 * 
	 * @param userStarted
	 *           <code>true</code> if the user started the update. If not there
	 *           will be no message if no update is found.
	 */
	private void startUpdate(final boolean userStarted) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Updater update = new Updater();
				if (update.isUpdate()) {
					int result = JOptionPane.showConfirmDialog(mainWindow, Strings.updateFound, Strings.update,
						JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (update.update()) JOptionPane.showMessageDialog(mainWindow,
							String.format(Strings.updateFinished, update.getDest().getAbsolutePath()));
						else JOptionPane.showMessageDialog(mainWindow, Strings.updateError, Strings.update,
							JOptionPane.ERROR_MESSAGE);
					} else return;
				} else if (userStarted) JOptionPane.showMessageDialog(mainWindow, Strings.noUpdate, Strings.update,
					JOptionPane.INFORMATION_MESSAGE);
			}
		};
		updater = new Thread(r);
		updater.setDaemon(true);
		updater.start();
	}

	/**
	 * Changes states of int GUI elements and of the active line. Works in the
	 * EventQueue.
	 * 
	 * @param updateRs
	 *           <code>true</code> if the R fields should be updated too.
	 */
	private void updateFields(final boolean updateRs) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (updateRs) mainWindow.updateFields(model.getAccumulator(), model.getLineCounter(), model.getRegisters());
				else mainWindow.updateFields(model.getAccumulator(), model.getLineCounter());
				int[] activeLine = model.getActiveLine();
				mainWindow.setActiveLine(activeLine [0], activeLine [1]);
				valuesEndState = false;
			}
		};
		Controller.runInDepatcherThreadandWait(r);
	}

	/**
	 * Updates the code editor by getting changes from the model. Works in the
	 * EventQueue.
	 */
	private void updateCodeEditor() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				mainWindow.markErrors(model.getErrors());
			}
		};
		EventQueue.invokeLater(r);
	}

	private static void runInDepatcherThreadandWait(Runnable r) {
		if (!EventQueue.isDispatchThread()) try {
			EventQueue.invokeAndWait(r);
		} catch (InvocationTargetException e) {
			Controller.logger.log(Level.SEVERE, "Exception when changing GUI.", e);
		} catch (InterruptedException e) {
			Controller.logger.log(Level.WARNING, "Interruption of the EventThread.", e);
		}
		else r.run();
	}

	/**
	 * Updates the message field.
	 */
	private void updateMessage() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				messageconstant message = model.getMessage();
				switch (message) {
				case FINISHED:
					mainWindow.updateMessage(Strings.finished, JOptionPane.INFORMATION_MESSAGE);
					break;
				case SAVESUCCESSFUL:
					mainWindow.updateMessage(Strings.saveSuccess, JOptionPane.INFORMATION_MESSAGE);
					break;
				case CODEERROR:
					mainWindow.updateMessage(Strings.codeError, JOptionPane.ERROR_MESSAGE);
					break;
				case STOPPED:
					mainWindow.updateMessage(Strings.stopped, JOptionPane.INFORMATION_MESSAGE);
					break;
				case UNEXPECTEDEND:
					mainWindow.updateMessage(Strings.unexpectedEnd, JOptionPane.ERROR_MESSAGE);
					break;
				case NOSUCHLINE:
					mainWindow.updateMessage(Strings.noSuchLine, JOptionPane.ERROR_MESSAGE);
					break;
				case PROGRAMMFINISHED:
					mainWindow.updateMessage(Strings.programFinished, JOptionPane.INFORMATION_MESSAGE);
					break;
				default:
					mainWindow.updateMessage(null);
				}

			}
		};
		Controller.runInDepatcherThreadandWait(r);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (worker.isAlive()) return;
		String s;
		try {
			s = e.getDocument().getText(e.getOffset(), e.getLength());
		} catch (BadLocationException e1) {
			Controller.logger.log(Level.SEVERE, "Error when trying to get text out of document.", e);
			return;
		}
		Change change = model.insertUpdate(e.getOffset(), s);
		mainWindow.executeChange(change);
		updateCodeEditor();
		updateMessage();
		if (valuesEndState) updateFields(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (worker.isAlive()) return;
		Change change = model.removeUpdate(e.getOffset(), e.getLength());
		mainWindow.executeChange(change);
		updateCodeEditor();
		updateMessage();
		if (valuesEndState) updateFields(true);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		/*
		 * if(worker.isAlive()) return; String s; try { s =
		 * e.getDocument().getText(e.getOffset(), e.getLength()); } catch
		 * (BadLocationException e1) { logger.log(Level.SEVERE,
		 * "Error when trying to get text out of document.", e); return; } Change
		 * change = model.changedUpdate(e.getOffset(), e.getLength(), s);
		 * mainWindow.executeChange(change); updateCodeEditor(); updateMessage();
		 * if(valuesEndState) updateFields(true);
		 */
		// is not used for text changes, only for font changes. not of interest.
		// endless loup when marking errors.
	}

	/**
	 * @param status
	 *           exit status
	 * @see System#exit(int)
	 */
	public static final void exit(int status) {
		Installer ins = Installer.getInstaller();
		Updater up = Updater.getUpdater();
		if (up != null && up.hasUpdated()) try {
			Runtime.getRuntime().exec(
				"java -jar \"" + ins.getUpdaterFile().getAbsolutePath() + "\" \"" + up.getDest().getAbsolutePath()
				+ "\" \"" + up.getThisJarFile().getAbsolutePath());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
				String.format(Strings.bundle.getString("copyError"), up.getDest().getAbsolutePath()));
		}
		System.exit(status);
	}
}