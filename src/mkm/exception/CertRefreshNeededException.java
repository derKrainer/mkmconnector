/**
 * 
 */
package mkm.exception;

/**
 * Exception to signal a need to re-install the mkm certs
 *
 * @author Kenny
 * @since 01.05.2016
 */
public class CertRefreshNeededException extends Exception
{

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "CertRefreshNeededException_2016_05_01".hashCode();

	/**
	 * @param cause
	 */
	public CertRefreshNeededException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CertRefreshNeededException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CertRefreshNeededException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
