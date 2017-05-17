package cc.before30.webapp.exception;

/**
 * User: before30 
 * Date: 2017. 5. 17.
 * Time: PM 12:47
 */
public class InvalidTagException extends RuntimeException {

	private static final long serialVersionUID = -2368735648807527664L;

	public InvalidTagException() {
		super();
	}

	public InvalidTagException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTagException(String message) {
		super(message);
	}

	public InvalidTagException(Throwable cause) {
		super(cause);
	}
}
