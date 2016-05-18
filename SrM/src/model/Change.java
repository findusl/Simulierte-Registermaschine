package model;

/**
 * Contains the changes made by the model.
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class Change {
	/**The offset of the new text and the overridden text.*/
	public final int offset;
	/**The length of the overridden text.*/
	public final int length;
	/**The new text.*/
	public final String s;
	/**The next change.*/
	public Change next;
	/**The position of the caret after the change.*/
	public final int caretPos;
	/**
	 * Creates new Change
	 * @param offset The offset of the new text and the overridden text.
	 * @param length The length of the overridden text.
	 * @param s The new text.
	 * @param cursorPos The position of the caret after the change.
	 * @param next The next change.
	 */
	public Change(int offset, int length, String s, int cursorPos,  Change next) {
		this.offset = offset;
		this.length = length;
		this.s = s;
		this.caretPos = cursorPos;
		this.next = next;
	}
	/**
	 * Creates new Change
	 * @param offset The offset of the new text and the overridden text.
	 * @param length The length of the overridden text.
	 * @param cursorPos The position of the caret after the change.
	 * @param s The new text.
	 */
	public Change(int offset, int length, String s, int cursorPos){
		this(offset, length, s, cursorPos, null);
	}
	/**
	 * Creates new Change
	 * @param offset The offset of the new text and the overridden text.
	 * @param length The length of the overridden text.
	 * @param s The new text.
	 * @param next The next change.
	 */
	public Change(int offset, int length, String s, Change next){
		this(offset, length, s, -1, next);
	}
	/**
	 * Creates new Change
	 * @param offset The offset of the new text and the overridden text.
	 * @param length The length of the overridden text.
	 * @param s The new text.
	 */
	public Change(int offset, int length, String s){
		this(offset, length, s, -1, null);
	}
	/**
	 * @param next the next Change
	 */
	public void setNext(Change next){
		this.next = next;
	}
	@Override
	public String toString(){
		return "Replace from " + offset + ", " + length + " signs with " + s;
	}
}
