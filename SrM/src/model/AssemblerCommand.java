package model;

/**
 * Contains the data of an assembler command.
 * Counter stands for the actual program line.
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class AssemblerCommand implements Comparable<AssemblerCommand> {
	//final commands
	/**
	 * Loads the Value out of Rx. counter++
	 */
	public static final byte LOAD = 0;
	/**
	 * Loads the number. counter++
	 */
	public static final byte DLOAD = 1;
	/**
	 * Copys current value to Rx. counter++
	 */
	public static final byte STORE = 2;
	/**
	 * Adds value of Rx to the current value and stores 
	 * 		the sum in the accumulator. counter++
	 */
	public static final byte ADD = 3;
	/**
	 * Subtracts the value of Rx from the current value and
	 * 		stores the result in the accumulator. counter++
	 */
	public static final byte SUB = 4;
	/**
	 * Multiplies the value of Rx with the current value and
	 * 		stores the result in the accumulator. counter++
	 */
	public static final byte MULT = 5;
	/**
	 * Divides the value of Rx with the current value and
	 * 		stores the result in the accumulator. counter++
	 */
	public static final byte DIV = 6;
	/**
	 * Loads number to counter.
	 */
	public static final byte JUMP = 7;
	/**
	 * Jump if current value is bigger or equal zero
	 */
	public static final byte JGE = 8;
	/**
	 * Jump if current value is greater than zero
	 */
	public static final byte JGT = 9;
	/**
	 * Jump if current value is less than or equal zero
	 */
	public static final byte JLE = 10;
	/**
	 * Jump if current value is less than zero
	 */
	public static final byte JLT = 11;
	/**
	 * Jump if current value is equal zero
	 */
	public static final byte JEQ = 12;
	/**
	 * Jump if current value is unequal zero
	 */
	public static final byte JNE = 13;
	/**
	 * Ends program. Needs no Value passed
	 */
	public static final byte END = 14;
	
	//Object variables
	protected byte command;
	protected int line;
	protected int realLine;
	protected int value;
	protected int offset;
	protected int length;
	/**
	 * Creates new AssemblerCommand.
	 * @param command The constant describing the command.
	 * @param line The line told by the command.
	 * @param realLine The line of the command.
	 * @param value The passed value. zero if not necessary.
	 * @param offset The begin of the command in the whole document
	 * @param length The length of the command - line
	 * @throws IllegalArgumentException if command is invalid.
	 */
	public AssemblerCommand(byte command, int line, int realLine, int value, int offset, int length) 
			throws IllegalArgumentException{
		if(command < -1 || command > END)
			throw new IllegalArgumentException(command + " is not valid");
		this.command = command;
		this.line = line;
		this.realLine = realLine;
		this.value = value;
		this.offset = offset;
		this.length = length;
	}
	/**
	 * @return The constant describing the command.
	 */
	public byte getCommand() {
		return command;
	}
	/**
	 * @return The line given by the command.
	 */
	public int getLine() {
		return line;
	}
	/**
	 * @return The value passed.
	 */
	public int getValue() {
		return value;
	}
	/**
	 * @return The real line of the command.
	 */
	public int getRealLine(){
		return realLine;
	}
	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	
	@Override
	public int compareTo(AssemblerCommand o) {
		int returnVal = line - o.line;
		if(returnVal == 0)
			return offset - o.offset;
		return returnVal;
	}
	
	@Override
	public String toString(){
		return "line " + realLine + " contains: " + line + ": " + command + " " + value;
	}
	/**
	 * Converts the string to the command constant.
	 * @param s The assembler command.
	 * @return the command constant.
	 * @throws IllegalArgumentException If the String is no Assembler command.
	 * 		The exception has the String as message.
	 */
	public static byte convertCommand(String s){
		if(s.equalsIgnoreCase("LOAD"))
			return LOAD;
		else if(s.equalsIgnoreCase("DLOAD"))
			return DLOAD;
		else if(s.equalsIgnoreCase("STORE"))
			return STORE;
		else if(s.equalsIgnoreCase("ADD"))
			return ADD;
		else if(s.equalsIgnoreCase("SUB"))
			return SUB;
		else if(s.equalsIgnoreCase("MULT"))
			return MULT;
		else if(s.equalsIgnoreCase("DIV"))
			return DIV;
		else if(s.equalsIgnoreCase("JUMP"))
			return JUMP;
		else if(s.equalsIgnoreCase("JGE"))
			return JGE;
		else if(s.equalsIgnoreCase("JGT"))
			return JGT;
		else if(s.equalsIgnoreCase("JLE"))
			return JLE;
		else if(s.equalsIgnoreCase("JLT"))
			return JLT;
		else if(s.equalsIgnoreCase("JEQ"))
			return JEQ;
		else if(s.equalsIgnoreCase("JNE"))
			return JNE;
		else if(s.equalsIgnoreCase("END"))
			return END;
		else
			throw new IllegalArgumentException(s);
	}
}