package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The model of the simulated register machine
 * @author Ulic Quel-droma
 * @version 1.1
 */
public class Model {
	private static final Logger logger;
	/**The systems line-separator*/
	public static final char separator = System.getProperty("line.separator").toCharArray() [0];
	/**
	 * The name of the project and of its logger.
	 */
	public static final String projectName = "seRmS-GUI";
	
	static{
		logger = Logger.getLogger(projectName + "." + Model.class.getCanonicalName());
	}
	
	//Message constants
	/**
	 * Contains constants for messages the model would like to pass to the user.
	 * @author Ulic Quel-droma
	 * @version 1.0
	 */
	public static enum messageconstant {
		/**The program is stopped.*/
		STOPPED , 
		/**The saving was successful.*/
		SAVESUCCESSFUL, 
		/**There is an error in the code.*/
		CODEERROR, 
		/**No message has to be displayed.*/
		NONE, 
		/**There are no more commands, but there was no end command.*/
		UNEXPECTEDEND, 
		/**The line demanded by lineCounter does not exist.*/
		NOSUCHLINE, 
		/**The current work was finished.*/
		FINISHED, 
		/**The command end was reached.*/
		PROGRAMMFINISHED, 
		/**There was a division by zero*/
		DIVISIONBYNULL}
	
	private AssemblerCommand [] commands;
	private Compiler compiler;
	private boolean outOfFile;
	private File source;
	private int accumulator, lineCounter;
	private AssemblerCommand lastCommand;
	private int [] register;
	private boolean upToDate, finished;
	private messageconstant message;
	/**
	 * Creates new Model.
	 */
	public Model(){
		compiler = new Compiler();
		outOfFile = false;
		register = new int [16];
		finished = false;
		message = messageconstant.NONE;
	}
	/**
	 * Executes the whole code.
	 * @throws InterruptedException If the execution was interrupted.
	 */
	public synchronized void run() throws InterruptedException {
		message = messageconstant.NONE;//will be overwritten
		do
			step();
		while(!isEndReached());
		if(message == messageconstant.FINISHED){
			message = messageconstant.PROGRAMMFINISHED;
			if(Thread.currentThread().isInterrupted())
				throw new InterruptedException();
		}
	}
	/**
	 * Executes the given number of lines of the code.
	 * @param lines The lines to execute.
	 * @throws InterruptedException If the execution was interrupted.
	 */
	public synchronized void skipLines(int lines) throws InterruptedException {
		if(lines == 0){
			message = messageconstant.NONE;
			return;
		}
		message = messageconstant.NONE;
		for(int i = 0; i < lines && !isEndReached(); i++)
			step();
		if(Thread.currentThread().isInterrupted())
			throw new InterruptedException();
	}
	/**
	 * Executes one line.
	 * @return If the registers have changed.
	 * @throws InterruptedException If the execution was interrupted.
	 */
	public synchronized boolean step() throws InterruptedException {
		update();
		message = messageconstant.NONE;
		if(commands.length == 0){
			message = messageconstant.UNEXPECTEDEND;
			finished = true;
			return false;
		}
		AssemblerCommand command = null;
		if(commands.length > lineCounter){
			command = commands [lineCounter - 1];
		}
		if(command == null || command.getLine() != lineCounter){
			if(commands [commands.length - 1].getLine() < lineCounter){
				message = messageconstant.UNEXPECTEDEND;
				finished = true;
				return false;
			}
			try{
				command = commands [findIndexOfCommand(lineCounter, 0) - 1];
			} catch(NoSuchElementException e){
				message = messageconstant.NOSUCHLINE;
				finished = true;
				return false;
			}
		}
		boolean returnVal = executeCommand(command);
		if(finished){
			message = messageconstant.PROGRAMMFINISHED;
		}
		else
			message = messageconstant.FINISHED;
		return returnVal;
	}
	/**
	 * Resets all values.
	 */
	public synchronized void reset() {
		accumulator = 0;
		lineCounter = 1;
		for(int i = 0; i < 16; i++){
			register [i] = 0;
		}
		finished = false;
		lastCommand = null;
		message = messageconstant.STOPPED;
	}
	/**
	 * Finds the Index of the specific command whose line is the same as the searched one.
	 * @param searchedLine The line to find.
	 * @param secure A security counter that ends the search after 500 tries to prevent StackOverFlowErrors.
	 * @return The index of the command in commands.
	 * @throws InterruptedException If the search was interrupted.
	 */
	public int findIndexOfCommand(int searchedLine, int secure) throws InterruptedException{
		if(secure > 500)
			throw new NoSuchElementException();
		if(Thread.currentThread().isInterrupted())
			throw new InterruptedException();
		if(searchedLine >= commands.length)
			searchedLine = commands.length;
		int line = commands [searchedLine - 1].getLine();
		if(finished){
			if(lineCounter - 1 == line)
				return searchedLine;
		} else {
			if(lineCounter == line)
				return searchedLine;
		}
		secure++;
		return findIndexOfCommand(searchedLine - (line - lineCounter), secure);
	}
	/**
	 * Executes a command-
	 * @param command The command to execute.
	 * @return If the register have to be updated too.
	 */
	private boolean executeCommand(AssemblerCommand command) {
		boolean returnVal = false;
		lastCommand = command;
		switch(command.getCommand()){
		case AssemblerCommand.DLOAD:
			accumulator = command.getValue();
			lineCounter++;
			break;
		case AssemblerCommand.LOAD:
			accumulator = register[command.getValue()];
			lineCounter++;
			break;
		case AssemblerCommand.STORE:
			register [command.getValue()] = accumulator;
			lineCounter++;
			returnVal = true;
			break;
		case AssemblerCommand.ADD:
			accumulator += register[command.getValue()];
			lineCounter++;
			break;
		case AssemblerCommand.SUB:
			accumulator -= register[command.getValue()];
			lineCounter++;
			break;
		case AssemblerCommand.MULT:
			accumulator *= register[command.getValue()];
			lineCounter++;
			break;
		case AssemblerCommand.DIV:
			if(register[command.getValue()]==0){
				message = messageconstant.DIVISIONBYNULL;
				finished = true;
			}
			else{
				accumulator /= register[command.getValue()];
			}
			lineCounter++;
			break;
		case AssemblerCommand.JUMP:
			lineCounter = command.getValue();
			break;
		case AssemblerCommand.JGE:
			if(accumulator >= 0)
				lineCounter = command.getValue();
			else
				lineCounter++;
			break;
		case AssemblerCommand.JGT:
			if(accumulator > 0)
				lineCounter = command.getValue();
			else
				lineCounter++;
			break;
		case AssemblerCommand.JLE:
			if(accumulator <= 0)
				lineCounter = command.getValue();
			else
				lineCounter++;
			break;
		case AssemblerCommand.JLT:
			if(accumulator < 0)
				lineCounter = command.getValue();
			else
				lineCounter++;
			break;
		case AssemblerCommand.JEQ:
			if(accumulator == 0)
				lineCounter = command.getValue();
			else
				lineCounter++;
			break;
		case AssemblerCommand.JNE:
			if(accumulator != 0)
				lineCounter = command.getValue();
			else
				lineCounter++;
			break;
		case AssemblerCommand.END:
			lineCounter++;
			finished = true;
			message = messageconstant.NONE;
			break;
		}
		return returnVal;
	}
	/**
	 * Loads code out of a file.
	 * @param f The file containing the code to load.
	 * @return The loaded code.
	 * @throws FileNotFoundException If the file was not found.
	 * @throws IOException If the file could not be read.
	 */
	public String load(File f) throws IOException{
		FileReader fr = null;
		StringBuilder sb = new StringBuilder();
		try {
			fr = new FileReader(f);
			for(int i = fr.read(); i != -1; i = fr.read()){
				sb.append((char) i);
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "Could not find file " + f.getAbsolutePath(), e);
			throw e;
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not read file " + f.getAbsolutePath(), e);
			throw e;
		} finally{
			if(fr != null)
				try {
					fr.close();
				} catch (IOException e) {
					logger.log(Level.CONFIG, "FileReader on file " + f.getAbsolutePath() + " could not be closed.", e);
				}
		}
		outOfFile = true;
		source = f;
		upToDate = false;
		String code = sb.toString().replace(separator, '\n');
		return code;
	}
	/**
	 * Saves the code into a file. The next time save will enough
	 * @param destination The file to save in.
	 */
	public void saveAs(File destination) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(destination, false);
			String code = compiler.getCode().replace('\n', separator);
			fw.write(code);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not write on " + destination.getAbsolutePath(), e);
		} finally {
			if(fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, "Could not close FileWriter on File " + destination.getAbsolutePath(), e);
				}
		}
		source = destination;
		outOfFile = true;
		message = messageconstant.SAVESUCCESSFUL;
	}
	/**
	 * Saves the code to it's source file.
	 */
	public void save() {
		saveAs(source);
	}
	/**
	 * @return The value of the accumulator.
	 */
	public int getAccumulator() {
		return accumulator;
	}
	/**
	 * @return The value of the line Counter.
	 */
	public int getLineCounter() {
		return lineCounter;
	}
	/**
	 * @return The registers
	 */
	public int [] getRegisters() {
		return register;
	}
	/**
	 * @return The offset and the length of the active line.
	 */
	public int [] getActiveLine() {
		if(lastCommand == null)
			return new int [] {0,0};
		return new int [] {lastCommand.getOffset(), lastCommand.getLength()};
	}
	/**
	 * @return The code
	 */
	public String getCode(){
		return compiler.getCode();
	}
	/**
	 * Only returns errors of completed lines.
	 * @return The errors.
	 */
	public LinkedList <Error> getErrors(){
		return compiler.getErrors();
	}
	/**
	 * @return The message the model want's to tell the user.
	 * @see messageconstant
	 */
	public messageconstant getMessage() {
		return message;
	}
	/**
	 * @return The compiler.
	 */
	public Compiler getCompiler(){
		return compiler;
	}
	/**
	 * @return <code>true</code> if the current code has a file connected to it.
	 */
	public boolean isOutOfFile(){
		return outOfFile;
	}
	/**
	 * @return <code>true</code> if the list of commands and errors is up to date.
	 */
	public boolean isUptoDate(){
		return upToDate;
	}
	/**
	 * @return <code>true</code> if the command end was reached.
	 */
	public boolean isEndReached(){
		return finished;
	}
	/**
	 * @return <code>true</code> if there are no errors in the code.
	 */
	public boolean canRun() {
		update();
		return compiler.canRun();
	}
	/**
	 * Updates the list of commands.
	 */
	public void update(){
		if(isUptoDate())
			return;
		commands = compiler.getCommands();
		lineCounter = 1;
		upToDate = true;
	}
	/**
	 * Rebuilds the compiler.
	 * @param code The code if there is some.
	 */
	public void resetCompiler(String code){
		compiler = new Compiler(code);
	}
	/**
	 * Invoked if the user inserted something in the code.
	 * @param offset The offset of the inserting.
	 * @param s The inserted String.
	 * @return The changes performed.
	 */
	public Change insertUpdate(int offset, String s) {
		if(s.isEmpty())
			return null;
		upToDate = false;
		Change result = compiler.insertUpdate(offset, s);
		if(compiler.canRun())
			message = messageconstant.NONE;
		else
			message = messageconstant.CODEERROR;
		return result;
	}
	/**
	 * Invoked if the user removed something from the code.
	 * @param offset The offset of the removed code.
	 * @param length The length of the removed code.
	 * @return The changes performed.
	 */
	public Change removeUpdate(int offset, int length) {
		upToDate = false;
		Change result = compiler.removeUpdate(offset, length);
		if(compiler.canRun())
			message = messageconstant.NONE;
		else
			message = messageconstant.CODEERROR;
		return result;
	}
	/**
	 * Invoked if the user changed something in the code.
	 * @param offset The begin of the changing
	 * @param length The length of the text changed before it was changed.
	 * @param s The new text.
	 * @return The changes performed.
	 */
	/*public Change changedUpdate(int offset, int length, String s) {
		upToDate = false;
		Change result = compiler.changedUpdate(offset, length, s);
		if(compiler.canRun())
			message = messageconstant.NONE;
		else
			message = messageconstant.CODEERROR;
		return result;
	}*/
}
