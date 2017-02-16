/**
 * 
 */
package mkm.exception;

import java.io.IOException;

/**
 *
 *
 * @author Kenny
 * @since 11.01.2016
 */
public class Http400Exception extends IOException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = "Http400Exception".hashCode();

	/**
	 * 
	 */
	public Http400Exception()
	{
		super();
	}

	/**
	 * @param message
	 */
	public Http400Exception(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public Http400Exception(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public Http400Exception(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
