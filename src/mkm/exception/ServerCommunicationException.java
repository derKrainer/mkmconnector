/**
 * 
 */
package mkm.exception;

/**
 * Exception to signal server communication errors
 *
 * @author Kenny
 * @since 14.12.2016
 */
public class ServerCommunicationException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7025303150193222872L;

	/**
	 * 
	 */
	public ServerCommunicationException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public ServerCommunicationException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public ServerCommunicationException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServerCommunicationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ServerCommunicationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
