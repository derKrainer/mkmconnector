/**
 * 
 */
package mkm.exception;

/**
 *
 *
 * @author Kenny
 * @since 18.02.2017
 */
public class SQLSetupException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = "SQLSetupException_18.02.2017".hashCode();

	/**
	 * 
	 */
	public SQLSetupException()
	{
	}

	/**
	 * @param message
	 */
	public SQLSetupException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public SQLSetupException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SQLSetupException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SQLSetupException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
