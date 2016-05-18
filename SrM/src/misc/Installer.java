package misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Unpacks program data at first start.
 * 
 * @author Ulic Quel-droma
 * @version 1.5
 */
public class Installer {
	/**
	 * The installer object
	 */
	private static Installer installer;
	/**
	 * The directory in which the program files lie.
	 */
	public final File projectDirectory;

	private final Logger logger;
	/**
	 * The path of the help file.
	 */
	private File helpFile;
	/**
	 * The File containing the uninstaller script or the readme text file
	 */
	private File uninstaller;
	/**
	 * If the uninstaller has to change.
	 */
	private boolean changed;
	/**
	 * The updater jar-file
	 */
	private File updaterFile;

	private Thread createHelpFile, createUpdaterFile;

	/**
	 * Creates new Installer and finds the appdata directory.
	 */
	public Installer() {
		projectDirectory = getAppDateDir();
		logger = Logger.getLogger(Controller.projectName + "."
			+ Installer.class.getCanonicalName());
	}

	/**
	 * @return The global Installer.
	 */
	public static Installer getInstaller() {
		if (Installer.installer == null)
			Installer.installer = new Installer();
		return Installer.installer;
	}

	/**
	 * Creates the uninstaller in a new Thread.
	 */
	public void createUninstaller() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					uninstaller = prCreateUninstaller();
				} catch (Exception e) {
					// only in IDE there are unexpected Exceptions because there is
					// no jar file
					logger.log(Level.CONFIG, "Could not create file.", e);
				}
			}
		});
		t.start();
	}

	private File prCreateUninstaller() {
		try {
			createHelpFile.join();
			createUpdaterFile.join();
		} catch (InterruptedException e) {
			logger.log(Level.FINER, "Interrupted the joining of the other files.",
				e);
		}
		File f = null, jarFile = null;
		PrintStream writer = null;
		try {
			jarFile = new File(System.getProperty("java.class.path"));
			f = jarFile.getParentFile();
			String os = System.getProperty("os.name");
			if (os.contains("Windows")) {
				f = new File(f, "uninstall.bat");
				if (f.exists() && !changed)
					return f;
				writer = new PrintStream(f);
				logger.log(
					Level.FINEST,
					"Projectdirectory: "
						+ Boolean.toString(projectDirectory == null)
						+ " helpFile: " + Boolean.toString(helpFile == null)
						+ " updaterFile: "
						+ Boolean.toString(updaterFile == null));
				writer.printf(Installer.windowsFile, projectDirectory.getCanonicalPath(),
					helpFile.getCanonicalPath(), jarFile.getCanonicalPath(), f.getCanonicalPath());
			} else if (os.equalsIgnoreCase("Linux") || os.contains("Mac OS")) {
				f = new File(f, "uninstall.scp");
				if (f.exists() && !changed)
					return f;
				else if (f.exists())
					f.delete();
				writer = new PrintStream(f);
				writer.printf(Installer.unixFile,
					projectDirectory.getAbsolutePath(),
					helpFile.getAbsolutePath(), updaterFile.getAbsolutePath(),
					f.getAbsolutePath());
				writer.close();
				Runtime.getRuntime().exec(
					"chmod u+x \"" + f.getAbsolutePath() + "\"");
			} else {
				f = new File(f, "uninstall_Readme.txt");
				if (f.exists() && !changed)
					return f;
				writer = new PrintStream(f);
				writer.printf(Installer.textFile,
					projectDirectory.getAbsolutePath(),
					helpFile.getAbsolutePath(), updaterFile.getAbsolutePath(),
					f.getAbsolutePath());
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not create Uninstaller.", e);
			return null;
		} finally {
			if (writer != null)
				writer.close();
		}
		return f;
	}

	/**
	 * Deletes the uninstaller in a new Thread.
	 */
	public void deleteUninstaller() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (uninstaller != null)
					uninstaller.delete();
			}
		});
		t.start();
	}

	private static final String sep = System.lineSeparator();
	private static final String windowsFile = "del \"%s\"" + Installer.sep + "del \"%s\""
		+ Installer.sep + "del \"%s\"" + Installer.sep + "del \"%s\"";
	private static final String unixFile = "Rm \"%s\"" + Installer.sep + "Rm \"%s\""
		+ Installer.sep + "Rm \"%s\"" + Installer.sep + "Rm \"%s\"";
	private static final String textFile = "To uninstall the program besides deleting the program jar-file itself, "
		+ "you have to delete all those files:"
		+ Installer.sep
		+ "%s"
		+ Installer.sep
		+ "%s"
		+ Installer.sep + "%s" + Installer.sep + "%s";

	/**
	 * Unpacks the help in a new Thread.
	 */
	public void unpackHelp() {
		createHelpFile = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					helpFile = prUnpackHelp();
					synchronized (helpFile) {
						helpFile.notify();
					}
				} catch (Exception e) {
					// only in IDE there are unexpected Exceptions because there is
					// no jar file
					logger.log(Level.CONFIG, "Could not create file.", e);
				}
			}
		});
		createHelpFile.start();
	}

	private File prUnpackHelp() {
		File f = null;
		JarFile jf = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			f = new File(System.getProperty("java.class.path"));
			jf = new JarFile(f);
			ZipEntry fileEntry = jf.getEntry("data/help.html");
			is = jf.getInputStream(fileEntry);
			f = f.getParentFile();
			f = new File(f, "help.html");
			if (f.exists()) {
				logger.log(Level.FINEST,
					"Help file exists already. Existing: " + f.lastModified()
					+ " Packed: " + fileEntry.getTime());
				if (fileEntry.getTime() > f.lastModified())
					f.delete();
				else
					return f;
			}
			changed = true;
			os = new FileOutputStream(f, true);
			int i = 0;
			while ((i = is.read()) != -1)
				os.write(i);
			f.setLastModified(fileEntry.getTime());
		} catch (IOException e) {
			logger.log(Level.WARNING, "Unable to read from the Jarfile.", e);
			return null;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					logger.log(Level.FINE, "Could not close help.html inputstream.",
						e);
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					logger.log(Level.WARNING,
						"Could not close help.html outputstream.", e);
				}
			if (jf != null)
				try {
					jf.close();
				} catch (IOException e) {
					logger.log(Level.FINE, "Could not close the jar file.", e);
				}
		}
		return f;
	}

	/**
	 * Versucht das Verzeichnis für die Anwendungsdaten zu finden und dort einen
	 * Ordner "Recoil" zu erstellen. Bei Fehlschlag wird der Nutzer zur Manuellen
	 * Auswahl eines Ordners für die Anwendungsdaten aufgefordert. Der Ordner
	 * wird in der Klassenvariable anwendungsDaten gesichert.
	 * 
	 * @return Das Anwendungsverzeichnis.
	 */
	public File getAppDateDir() {
		File foundFile = null;
		try {
			File userHome = new File(System.getProperty("user.home"));
			String os = System.getProperty("os.name");
			if (os.equalsIgnoreCase("Windows 7")
				|| os.equalsIgnoreCase("Windows Vista"))
				foundFile = new File(userHome, "AppData/Local/"
					+ Controller.projectName);
			else if (os.equalsIgnoreCase("Windows XP"))
				foundFile = new File(userHome, "Anwendungsdaten/"
					+ Controller.projectName);
			else if (os.equalsIgnoreCase("Linux"))
				foundFile = new File(userHome, ".local/" + Controller.projectName);
			else
				foundFile = new File(userHome, Controller.projectName);
			if (!foundFile.exists()) {
				changed = true;
				if (!foundFile.mkdirs())
					throw new FileNotFoundException();
			}
			if (!foundFile.canWrite()) {
				foundFile = null;
				JOptionPane.showMessageDialog(null,
					"Unable to use Appdata directory. "
						+ "Please choose another directory.");
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,
				"Unable to find Appdata directory. "
					+ "Please choose another directory.\n" + e.toString());
			foundFile = null;
		}
		if (foundFile == null) {
			JFileChooser fc = new JFileChooser(new File(
				System.getProperty("user.home")));
			fc.setDialogTitle("Please choose appdata directory");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				foundFile = fc.getSelectedFile();
			else
				logger.log(Level.CONFIG, "User cancled open AppData dialog.",
					returnVal);
		}
		return foundFile;
	}

	/**
	 * Unpacks the updater-jar-file.
	 */
	public synchronized void createUpdater() {
		createUpdaterFile = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					updaterFile = priCreateUpdater();
					synchronized (updaterFile) {
						updaterFile.notify();
					}
				} catch (Exception e) {
					// only in IDE there are unexpected Exceptions because there is
					// no jar file
					logger.log(Level.CONFIG, "Could not create updater file.", e);
				}
			}
		});
		createUpdaterFile.start();
	}

	private File priCreateUpdater() {
		File f = null;
		JarFile jf = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			f = new File(projectDirectory, "updater.jar");
			jf = new JarFile(new File(System.getProperty("java.class.path")));
			ZipEntry fileEntry = jf.getEntry("data/updater.jar");
			is = jf.getInputStream(fileEntry);
			if (f.exists()) {
				logger.log(Level.FINEST, "Updater file exists already. Existing: "
					+ f.lastModified() + " Packed: " + fileEntry.getTime());
				if (fileEntry.getTime() > f.lastModified())
					f.delete();
				else
					return f;
			}
			changed = true;
			os = new FileOutputStream(f, true);
			int i = 0;
			while ((i = is.read()) != -1)
				os.write(i);
			f.setLastModified(fileEntry.getTime());
		} catch (IOException e) {
			logger.log(Level.WARNING, "Unable to read from the Jarfile.", e);
			return null;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					logger.log(Level.FINE,
						"Could not close updater.jar inputstream.", e);
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					logger.log(Level.WARNING,
						"Could not close updater.jar outputstream.", e);
				}
			if (jf != null)
				try {
					jf.close();
				} catch (IOException e) {
					logger.log(Level.FINE, "Could not close the jar file.", e);
				}
		}
		return f;
	}

	/**
	 * @return the helpFile.
	 */
	public File getHelpFile() {
		return helpFile;
	}

	/**
	 * @return the uninstaller-file.
	 */
	public File getUninstaller() {
		return uninstaller;
	}

	/**
	 * @return The path of the updater-jar.
	 */
	public File getUpdaterFile() {
		return updaterFile;
	}

	/**
	 * @return the projectDirectory
	 */
	public File getProjectDirectory() {
		return projectDirectory;
	}
}