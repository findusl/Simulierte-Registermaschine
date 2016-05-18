package misc;

import java.util.ResourceBundle;

/**
 * Contains merged strings should only be used for merged strings, all other should be loaded out of the properties file.
 * @author Ulic Quel-droma
 * @version 1.3
 */
@SuppressWarnings("javadoc")
public abstract class Strings {
	public static final ResourceBundle bundle = ResourceBundle.getBundle(Controller.baseName);
	public static final String applicationName = bundle.getString("applicationName");
	public static final String stopButton = bundle.getString("stopButton");
	public static final String stepButton = bundle.getString("stepButton");
	public static final String runButton = bundle.getString("runButton");
	public static final String skipButton = bundle.getString("skipButton");
	public static final String accumulatorToolTip = bundle.getString("accumulatorToolTip");
	public static final String lineCounterToolTip = bundle.getString("lineCounterToolTip");
	public static final String valueToolTip = bundle.getString("valueToolTip");
	public static final String runToolTip = bundle.getString("runToolTip");
	public static final String skipToolTip = bundle.getString("skipToolTip");
	public static final String stepToolTip = bundle.getString("stepToolTip");
	public static final String stopToolTip = bundle.getString("stopToolTip");
	public static final String fileMenuName = bundle.getString("fileMenuName");
	public static final String createMenuItem = bundle.getString("createMenuItem");
	public static final String openMenuItem = bundle.getString("openMenuItem");
	public static final String saveMenuItem = bundle.getString("saveMenuItem");
	public static final String saveAsMenuItem = bundle.getString("saveAsMenuItem");
	public static final String endMenuItem = bundle.getString("endMenuItem");
	public static final String helpMenuName = bundle.getString("helpMenuName");
	public static final String helpMenuItem = bundle.getString("helpMenuItem");
	public static final String aboutMenuItem = bundle.getString("aboutMenuItem");
	public static final String updateMenuItem = bundle.getString("updateMenuItem");
	public static final String skipLinesMessage = bundle.getString("skipLinesMessage");
	public static final String skipLinesInputError = bundle.getString("skipLinesInputError");
	public static final String error = bundle.getString("error");
	public static final String brutalWorkerStop = bundle.getString("brutalWorkerStop");
	public static final String openError = bundle.getString("openError");
	public static final String about = String.format(bundle.getString("about"), applicationName, Controller.version, Installer.getInstaller().projectDirectory.getAbsolutePath());
	public static final String fileNotValid = bundle.getString("fileNotValid");
	public static final String finished = bundle.getString("finished");
	public static final String saveSuccess = bundle.getString("saveSuccess");
	public static final String codeError = bundle.getString("codeError");
	public static final String stopped = bundle.getString("stopped");
	public static final String unexpectedEnd = bundle.getString("unexpectedEnd");
	public static final String noSuchLine = bundle.getString("noSuchLine");
	public static final String cantRun = bundle.getString("cantRun");
	public static final String programFinished = bundle.getString("programFinished");
	public static final String helpError = bundle.getString("helpError");
	public static final String updateStarted = bundle.getString("updateStarted");
	public static final String update = bundle.getString("update");
	public static final String noUpdate = bundle.getString("noUpdate");
	public static final String updateFound = bundle.getString("updateFound");
	public static final String updateFinished = bundle.getString("updateFinished");
	public static final String updateError = String.format(bundle.getString("updateError"), Installer.getInstaller().projectDirectory.getAbsolutePath());
	public static final String updateRuns = bundle.getString("updateRuns");
	public static final String configMenuName = bundle.getString("configMenuName");
	public static final String logMenuName = bundle.getString("logMenuName");
	public static final String highLogLevel = bundle.getString("highLogLevel");
	public static final String mediumLogLevel = bundle.getString("mediumLogLevel");
	public static final String lowLogLevel = bundle.getString("lowLogLevel");
	public static final String logLevelOff = bundle.getString("logLevelOff");
	public static final String createUninstallerMenuItem = bundle.getString("createUninstallerMenuItem");
}
