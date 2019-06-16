/**
 * 
 */
package ca.footeware.gdmbackgrounder.exceptions;

/**
 * Thrown when an error occurs during CSS modification.
 * 
 * @author Footeware.ca
 *
 */
public class CSSModificationException extends Exception {

	/**
	 * Default constructor.
	 */
	public CSSModificationException() {
	}

	/**
	 * @param message {@link String}
	 */
	public CSSModificationException(String message) {
		super(message);
	}

	/**
	 * @param cause {@link Throwable}
	 */
	public CSSModificationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message {@link String}
	 * @param cause   {@link Throwable}
	 */
	public CSSModificationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message            {@link String}
	 * @param cause              {@link Throwable}
	 * @param enableSuppression  boolean
	 * @param writableStackTrace boolean
	 */
	public CSSModificationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
