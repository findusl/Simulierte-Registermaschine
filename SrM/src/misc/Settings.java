package misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulic Quel-droma
 * @version 1.3
 */
public class Settings{
	private static final Logger logger = Logger.getLogger(Controller.projectName + "." + Settings.class.getSimpleName());
	private File settings;
	private boolean createUninstaller;
	private Level logLevel;
	private boolean autoUpdate;
	/**
	 * Creates new Settings. Takes the File out of the appdata directory
	 */
	public Settings(){
		settings = new File(Installer.getInstaller().projectDirectory.getAbsolutePath() + "/settings.set");
		//initialize standards
		createUninstaller = true;
		autoUpdate = false;
		logLevel = Level.FINE;
		FileInputStream fs = null;
		ObjectInputStream os = null;
		try {
			boolean reSave = false;
			fs = new FileInputStream(settings);
			os = new ObjectInputStream(fs);
			os.readObject();//the version. might be usefull when changing file.
			try {
				createUninstaller = os.readBoolean();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not read if to create the uninstaller.", e);
				reSave = true;
			}
			try {
				logLevel = (Level) os.readObject();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not read the logging Level.", e);
				reSave = true;
			}
			try {
				autoUpdate = os.readBoolean();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not read if to create the uninstaller.", e);
				reSave = true;
			}
			if(reSave)
				save();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not read settings file.", e);
		} finally {
			if(fs != null)
				try {
					fs.close();
				} catch (IOException e) {
					logger.log(Level.FINE, "Could not close FileInputStream.", e);
				}
		}
		if(logLevel == null){
			logLevel = Level.FINE;
		}
	}
	/**
	 * @return If an uninstaller-file should be created.
	 */
	public boolean isCreateUninstaller() {
		return createUninstaller;
	}
	/**
	 * @param createUninstaller If an uninstaller-file should be created.
	 */
	public void setCreateUninstaller(boolean createUninstaller) {
		this.createUninstaller = createUninstaller;
		save();
	}
	/**
	 * @return The logLevel.
	 */
	public Level getLogLevel() {
		return logLevel;
	}
	/**
	 * @param logLevel The logLevel.
	 */
	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
		save();
	}
	
	/**
	 * @return If updates should be searched automatically.
	 */
	public boolean isAutoUpdate() {
		return autoUpdate;
	}
	/**
	 * @param autoUpdate If updates should be searched automatically.
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		save();
	}
	
	private synchronized void save(){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ObjectOutputStream os = null;
				FileOutputStream fs = null;
				try{
					fs = new FileOutputStream(settings);
					os = new ObjectOutputStream(fs);
					os.writeObject(Controller.version);
					os.writeBoolean(createUninstaller);
					os.writeObject(logLevel);
					os.writeBoolean(autoUpdate);
				}  catch (IOException e) {
					logger.log(Level.WARNING, "Could not save Settings", e);
				} finally {
					if(os != null)
						try {
							os.close();
						} catch (IOException e) {
							logger.log(Level.CONFIG, "Could not close ObjectOutputStream.", e);
						}
					if(fs != null)
						try {
							fs.close();
						} catch (IOException e) {
							logger.log(Level.WARNING, "Could not close FileOutputStream.", e);
						}
				}
			}
		});
		t.start();
	}
}
