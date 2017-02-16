/**
 * 
 */
package mkm.connect;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 *
 * @author Kenny
 * @since 03.01.2016
 */
public class WebServiceLocations
{
	private static final String BUNDLE_NAME = "webServiceEndpoints"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private WebServiceLocations()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}
