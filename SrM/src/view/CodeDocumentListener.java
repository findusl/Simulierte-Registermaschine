package view;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import misc.Controller;

/**
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class CodeDocumentListener implements DocumentListener {
	Controller controller;
	
	/**
	 * Creates new CodeDocumentListener
	 * @param controller The controller
	 */
	public CodeDocumentListener(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		controller.insertUpdate(e);
	}
	
	@Override
	public void removeUpdate(DocumentEvent e) {
		controller.removeUpdate(e);
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		//controller.changedUpdate(e);
	}
}
