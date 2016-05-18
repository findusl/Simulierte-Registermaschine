package misc;
import java.io.File;



/**
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class Main {
	
	/**
	 * Pass this option before a file path to load the file out of the file.
	 */
	public static final String pathOption = "-F";

	/**
	 * @param args There are no parameters necessary. If {@link #pathOption} is passed before 
	 * 		a file path this file is loaded.
	 */
	public static void main(String[] args) {
		String source = null;
		if(args != null && args.length != 0){
			if(args [0].equalsIgnoreCase("-h") || args [0].equalsIgnoreCase("-help") || args [0].equalsIgnoreCase("-?")){
				System.out.println(Main.options);
				return;
			}
			if(args [0].equalsIgnoreCase(Main.pathOption) && args.length >= 2){
				source = args [1];
				if(!Main.isSourceFile(new File(source)))
					return;
			}
			else{
				System.err.println(Main.options);
				return;
			}
		}
		@SuppressWarnings("unused")
		Controller controller = null;
		if(source != null)
			controller = new Controller(new File(source));
		else
			controller = new Controller();
	}
	/**
	 * Checks if the given File is a File and if it can be read from it
	 * @param source The file to check
	 * @return true if it is a sourceFile
	 */
	private static boolean isSourceFile(File source){
		if(!source.exists()) {
			System.out.println(source.getAbsolutePath() + " does not exist.");
			return false;
		} if(!source.isFile()) {
			System.out.println(source.getAbsolutePath() + " is no File");
			return false;
		} if(!source.canRead()) {
			System.out.println("Can't read from " + source.getAbsolutePath());
			return false;
		}
		return true;
	}
	/**
	 * Tells the user how to start the program.
	 */
	private static final String options = "There are no parameters necessary. If " + Main.pathOption + 
			" is passed before a file path this file is loaded.";
}