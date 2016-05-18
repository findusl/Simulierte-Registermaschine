package model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the data of an assembler code. This class is thread-safe
 * 
 * @author Ulic Quel-droma
 * @version 1.21
 */
public class Compiler {
	private static final Logger logger = Logger.getLogger(Model.projectName
			+ "." + Compiler.class.getCanonicalName());
	
	private LinkedList<Error> errors;
	private final StringBuilder code;
	private LinkedList<AssemblerCommand> commands;
	private boolean isCompiled;
	
	/**
	 * Creates new Compiler with empty code.
	 */
	public Compiler() {
		this(new String());
	}
	
	/**
	 * Creates new Compiler.
	 * 
	 * @param s
	 *           The code that exists up to now.
	 */
	public Compiler(String s) {
		errors = new LinkedList<Error>();
		code = new StringBuilder(s);
		commands = new LinkedList<AssemblerCommand>();
		isCompiled = false;
	}
	
	/**
	 * Compiles the code into commands.
	 */
	public synchronized void compile() {
		if (isCompiled)
			return;
		commands = new LinkedList<AssemblerCommand>();
		errors = new LinkedList<Error>();
		Scanner sc = new Scanner(code.toString());
		int counter = 0;
		int offset = -1;
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			counter++;
			offset++;
			if (line.isEmpty())
				continue;
			try {
				compile(line, counter, offset);
				offset += line.length();
			} catch (RuntimeException e) {
				errors.add(new Error(offset, line.length(), Error.fatalError));
			}
		}
		sc.close();
		Collections.sort(errors);
		Collections.sort(commands);
		isCompiled = true;
		Compiler.logger.log(Level.FINE, "Code compiled");
	}
	
	/**
	 * compiles a single line and returns the command element.
	 * 
	 * @param line
	 *           The whole code line.
	 * @param counter
	 *           The real line number of the line.
	 * @param offset
	 *           The start of the line in characters counted from the begin of
	 *           the document.
	 */
	private void compile(String line, int counter, int offset) {
		int charCounter;
		Scanner sc = new Scanner(line);
		int lineNumber;
		if (sc.hasNext("\\d+:")) {
			String prefix = sc.next("\\d+:");
			charCounter = prefix.length();
			lineNumber = Integer
					.parseInt(prefix.substring(0, prefix.length() - 1));
		} else if (sc.hasNext("--.*")) {
			sc.close();
			return;
		} else {
			Compiler.logger.log(Level.FINE, line + " has no line number.");
			errors.add(new Error(offset, line.length(), Error.noLineNumber));
			sc.close();
			return;
		}
		byte command;
		try {
			String s = sc.next();
			command = AssemblerCommand.convertCommand(s);
			charCounter += s.length();
		} catch (IllegalArgumentException e) {
			Compiler.logger.log(Level.FINE, line + " has a unknown command.");
			errors.add(new Error(offset + charCounter,
					line.length() - charCounter, Error.unknownCommand));
			sc.close();
			return;
		}
		int value = 0;
		if (command != AssemblerCommand.END) {
			if (sc.hasNextInt())
				value = sc.nextInt();
			else {
				Compiler.logger.log(Level.FINE, line + " has no parameter.");
				errors.add(new Error(offset + charCounter, line.length()
						- charCounter, Error.missingValue));
				sc.close();
				return;
			}
			switch (command) {
			case AssemblerCommand.LOAD:
			case AssemblerCommand.STORE:
			case AssemblerCommand.ADD:
			case AssemblerCommand.SUB:
			case AssemblerCommand.MULT:
			case AssemblerCommand.DIV:
				if (value < 0 || value > 15) {
					Compiler.logger.log(Level.FINE, line
							+ " has a parameter out of range.");
					errors.add(new Error(offset + charCounter, Integer.toString(
							value).length(), Error.invalidRegister));
					sc.close();
					return;
				}
			}
			charCounter += Integer.toString(value).length();
		}
		if (sc.hasNext())
			if (!sc.hasNext("--.*")) {
				Compiler.logger.log(Level.FINE, line
						+ " has text that is not a comment");
				errors.add(new Error(offset + charCounter, line.length()
						- charCounter, Error.notaComment));
				sc.close();
				return;
			}
		sc.close();
		commands.add(new AssemblerCommand(command, lineNumber, counter, value,
				offset, line.length()));
	}
	
	/**
	 * @return <code>true</code> if there are no code errors
	 */
	public synchronized boolean canRun() {
		return errors.isEmpty();
	}
	
	/**
	 * @return The code.
	 */
	public synchronized String getCode() {
		return code.toString();
	}
	
	/**
	 * @return The errors that were found
	 */
	public synchronized LinkedList<Error> getErrors() {
		if (!isCompiled)
			compile();
		return errors;
	}
	
	/**
	 * @return The list of assembler commands.
	 */
	public synchronized AssemblerCommand[] getCommands() {
		if (!isCompiled)
			compile();
		return commands.toArray(new AssemblerCommand[commands.size()]);
	}
	
	/**
	 * Inserts a String. If the string begins a new line the list of errors is
	 * updated.
	 * 
	 * @param offset
	 *           The start of the String.
	 * @param s
	 *           The String.
	 * @return The changes performed.
	 */
	public synchronized Change insertUpdate(int offset, String s) {
		Compiler.logger.log(Level.FINEST, s + " at " + offset);
		isCompiled = false;
		code.insert(offset, s);
		compile();
		Change result = null;
		extras: if (canRun() && commands.size() > 0)
			if (s.equals("\n"))
				if (offset == code.length() - 1) {
					String newLine = (commands.getLast().getLine() + 1) + ": ";
					result = new Change(offset + s.length(), 0, newLine, offset
							+ s.length() + newLine.length());
				} else {
					Iterator<AssemblerCommand> comIt = commands.iterator();
					AssemblerCommand before = comIt.next(), after = null;
					while (comIt.hasNext()) {
						after = comIt.next();
						if (after.getOffset() > offset)
							break;
						before = after;
						after = null;
					}
					if (after == null)
						break extras;
					String newLine = Integer.toString(before.getLine() + 1) + ": ";
					int extraLength = newLine.length();
					result = new Change(offset + 1, 0, newLine, offset + 1
							+ extraLength);
					int lengthOldNumber = Integer.toString(after.getLine()).length();
					newLine = Integer.toString(before.getLine() + 2);
					Change last = new Change(after.getOffset() + extraLength,
							lengthOldNumber, newLine);
					extraLength += newLine.length() - lengthOldNumber;
					result.setNext(last);
					while (comIt.hasNext()) {
						before = after;
						after = comIt.next();
						lengthOldNumber = Integer.toString(after.getLine()).length();
						newLine = Integer.toString(before.getLine() + 2);
						Change temp = new Change(after.getOffset() + extraLength,
								lengthOldNumber, newLine);
						extraLength += newLine.length() - lengthOldNumber;
						last.setNext(temp);
						last = temp;
					}
				}
		return result;
	}
	
	/**
	 * Removes a String.
	 * 
	 * @param offset
	 *           The offset of the removed String.
	 * @param length
	 *           The length of the removed String.
	 * @return The changes performed.
	 */
	public synchronized Change removeUpdate(int offset, int length) {
		Compiler.logger.log(Level.FINEST, "Delete from " + offset + " to "
				+ (offset + length));
		isCompiled = false;
		String s = code.substring(offset, offset + length);
		code.delete(offset, offset + length);
		compile();
		Change result = null;
		extras: if (canRun() && commands.size() > 0)
			if (s.equals("\n")) {
				Iterator<AssemblerCommand> comIt = commands.iterator();
				AssemblerCommand before = comIt.next(), after = null;
				while (comIt.hasNext()) {
					after = comIt.next();
					if (after.getOffset() > offset)
						break;
					before = after;
					after = null;
				}
				if (after == null)
					break extras;
				int lengthOldNumber = Integer.toString(after.getLine()).length();
				String newLine = Integer.toString(before.getLine() + 1);
				result = new Change(after.getOffset(), lengthOldNumber, newLine);
				Change last = result;
				int extraLength = newLine.length() - lengthOldNumber;
				while (comIt.hasNext()) {
					before = after;
					after = comIt.next();
					lengthOldNumber = Integer.toString(after.getLine()).length();
					newLine = Integer.toString(before.getLine() + 1);
					Change temp = new Change(after.getOffset() + extraLength,
							lengthOldNumber, newLine);
					extraLength += newLine.length() - lengthOldNumber;
					last.setNext(temp);
					last = temp;
				}
			}
		return result;
	}
	/**
	 * Informs about a change.
	 * 
	 * @param offset
	 *           The offset of the change.
	 * @param length
	 *           The length of the change.
	 * @param s
	 *           The new String.
	 * @return The changes performed.
	 */
	/*
	 * public synchronized Change changedUpdate(int offset, int length, String s)
	 * { logger.log(Level.FINER, "Replace from " + offset + " to " + (offset +
	 * length) + " with " + s); isCompiled = false; code.replace(offset, offset +
	 * length, s); compile(); Change result = null; return result; }
	 */
}
