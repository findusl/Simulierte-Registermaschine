package model;

/**
 * Contains details about an error.
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class Error implements Comparable<Error>{
	//Error Constants
	/**
	 * The command is unknown.
	 */
	public static final byte unknownCommand = 0;
	/**
	 * There is no line number.
	 */
	public static final byte noLineNumber = 1;
	/**
	 * The compilation threw an exception.
	 */
	public static final byte fatalError = 2;
	/**
	 * There is no parameter passed.
	 */
	public static final byte missingValue = 3;
	/**
	 * The register number is invalid. It has to be -1 < x < 16.
	 */
	public static final byte invalidRegister = 4;
	/**
	 * There is text following that is not a comment.
	 */
	public static final byte notaComment = 5;
	/**
	 * If this line does not exist
	 */
	public static final byte noSuchLine = 6;
	
	
	private int offset;
	private int length;
	private byte error;
	/**
	 * Creates new ErrorCommand.
	 * @param offset The begin of the error.
	 * @param length The length of the error.
	 * @param error The constants value representing the error.
	 * @throws IllegalArgumentException If error is not an error constant out of this class
	 */
	public Error(int offset, int length, byte error) {
		if(error < 0 || error > 10)
			throw new IllegalArgumentException(error + " is no valid error constant");
		this.offset = offset;
		this.length = length;
		this.error = error;
	}
	/**
	 * @return The constants value representing the error.
	 */
	public byte getError() {
		return error;
	}
	/**
	 * @return The offset of the error.
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * @return The length of the error.
	 */
	public int getLength() {
		return length;
	}
	@Override
	public int compareTo(Error o) {
		return offset - o.offset;
	}
}
