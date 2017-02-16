/**
 * 
 */
package mkm.exception;

/**
 * Exception to signal missing response from MKM Service
 *
 * @author Kenny
 * @since 10.12.2016
 */
public class EmptyResponseException extends Exception
{

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "EmptyResponseException_2016.12.10".hashCode();

	/**
	 * 
	 */
	public EmptyResponseException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public EmptyResponseException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public EmptyResponseException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EmptyResponseException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public EmptyResponseException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
