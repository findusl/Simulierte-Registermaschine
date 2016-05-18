package misc;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Works like a StreamHandler, but automatically flushes a record
 * @author Ulic Quel-droma
 * @version 1.0
 */
public class AutoFlushStreamHandler extends StreamHandler {

	/**
	 * Creates new AutoFlushStreamHanlder
	 */
	public AutoFlushStreamHandler() {
		super();
	}

	/**
	 * Creates new AutoFlushStreamHanlder
	 * @see StreamHandler#StreamHandler(OutputStream, Formatter)
	 */
	public AutoFlushStreamHandler(OutputStream out, Formatter formatter) {
		super(out, formatter);
	}
	
	@Override
	public synchronized void publish(LogRecord record) {
		super.publish(record);
		flush();
	}
	
}
