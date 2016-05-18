package misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Downloads and installs updates.
 * 
 * @author Ulic Quel-droma
 * @version 1.5
 */
public class Updater {

	private static Updater updater;
	private static final Logger logger;

	private final String onlineJarFile;
	private final File thisJarFile;
	private File dest;
	private boolean isUpdate;
	private boolean hasUpdated;

	static {
		logger = Logger.getLogger(Controller.projectName + "."
			+ Updater.class.getSimpleName());
	}

	/**
	 * Creates new Updater
	 */
	public Updater() {
		onlineJarFile = "http://lehrbaum.de/SrM/SrM.jar";
		thisJarFile = new File(System.getProperty("java.class.path"));
		File f = thisJarFile.getParentFile();
		dest = new File(f, "SrM.jar");
		for (int i = 0; dest.exists(); i++)
			dest = new File(f, "SRM_" + i + ".jar");
		Updater.updater = this;
	}

	/**
	 * @return The global Updater or null if none was created yet.
	 */
	public static Updater getUpdater() {
		return Updater.updater;
	}

	/**
	 * Finds out if a newer version is available.
	 * 
	 * @return <code>true</code> if and only if a newer version is available.
	 */
	public boolean isUpdate() {
		InputStream is = null;
		try {
			URL url = new URL("jar:" + onlineJarFile + "!/data/Version.txt");
			is = url.openStream();
			Scanner sc = new Scanner(is);
			String version = sc.next();
			sc.close();
			isUpdate = version.compareToIgnoreCase(Controller.version) > 0;
		} catch (IOException e) {
			Updater.logger.log(Level.CONFIG, "Could not connect to versionFile.",
				e);
			return false;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException ignored) {
				}
		}
		return isUpdate;
	}

	/**
	 * @return The actual version.
	 * @throws IOException
	 *            If the version file could not be read.
	 */
	public String getVersion() throws IOException {
		String result = "0.0";
		JarFile file = null;
		try {
			file = new JarFile(thisJarFile);
			JarEntry entry = file.getJarEntry("data/Version.txt");
			Scanner sc = new Scanner(file.getInputStream(entry));
			result = sc.next();
			sc.close();
		} catch (IOException e) {
			Updater.logger.log(Level.CONFIG, "Could not access local JarFile.", e);
			throw e;
		} finally {
			if (file != null)
				try {
					file.close();
				} catch (IOException e) {
					Updater.logger.log(Level.FINE, "Could not close the jar file.",
						e);
				}
		}
		return result;
	}

	/**
	 * @return <code>true</code> if the update was successful.
	 */
	public boolean update() {
		InputStream is = null;
		OutputStream os = null;
		try {
			if (isUpdate) {
				URL url = new URL(onlineJarFile);
				is = url.openStream();
				os = new FileOutputStream(dest, false);
				int b = is.read();
				while (b != -1) {
					os.write(b);
					b = is.read();
				}
			} else
				return false;
		} catch (IOException e) {
			Updater.logger.log(Level.CONFIG,
				"Error when reading from Updatefile.", e);
			e.printStackTrace();
			dest.deleteOnExit();
			return false;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException ignored) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					Updater.logger.log(Level.INFO, "Could not close OutputStream.",
						e);
					return false;
				}
		}
		hasUpdated = true;
		return true;
	}

	/**
	 * @return The file of the downloaded jarFile if there is one.
	 */
	public File getDest() {
		return dest;
	}

	/**
	 * @return The current Jar-File;
	 */
	public File getThisJarFile() {
		return thisJarFile;
	}

	/**
	 * @return <code>true</code> if a new file was downloaded.
	 */
	public boolean hasUpdated() {
		return hasUpdated;
	}
}
