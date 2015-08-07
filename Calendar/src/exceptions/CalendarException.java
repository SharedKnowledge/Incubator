package exceptions;

/**
 * This exception is meant for error specific to the Calendar, especially 
 * underlying internal layers/APIs.
 *
 */
public class CalendarException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public CalendarException() {
		super();
	}

	public CalendarException(String message) {
		super(message);
	}

	public CalendarException(String message, Throwable cause) {
		super(message, cause);
	}

	public CalendarException(Throwable cause) {
		super(cause);
	}	
}
