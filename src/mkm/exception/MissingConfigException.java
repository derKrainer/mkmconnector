/**
 * 
 */
package mkm.exception;

/**
 * Exception to signal a missing mandatory configuration
 *
 * @author Kenny
 * @since 15.02.2017
 */
public class MissingConfigException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = "MissingConfigException_15.02.2017".hashCode();

	/**
	 * 
	 */
	public MissingConfigException()
	{
	}

	/**
	 * @param message
	 */
	public MissingConfigException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public MissingConfigException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MissingConfigException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public MissingConfigException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
