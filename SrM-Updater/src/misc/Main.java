package misc;

import java.io.File;

/**
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class Main {
	
	/**
	 * @param args
	 *           The parameters. First the source and then the destination.
	 */
	public static void main(String[] args) {
		if (args.length != 2)
			return;
		File source = new File(args[0]);
		File dest = new File(args[1]);
		Updater updater = new Updater(source, dest);
		updater.copy();
	}
	
}
