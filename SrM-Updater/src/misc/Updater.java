package misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Copys a file.
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class Updater {
	
	private File source;
	private File dest;
	/**
	 * Creates new Updater
	 * @param source The source file.
	 * @param dest The destination file.
	 */
	public Updater(File source, File dest){
		this.source = source;
		this.dest = dest;
	}
	/**
	 * Copys the file. Overrides any existing file.
	 */
	public void copy(){
		FileInputStream is = null;
		FileOutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			if(dest.exists())
				dest.delete();
			int i = -1;
			while((i = is.read()) != -1)
				os.write(i);
			source.deleteOnExit();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch (IOException ignored) {}
			if(os != null)
				try {
					os.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, e);
				}
		}
	}
}
