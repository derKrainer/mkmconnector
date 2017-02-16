/**
 * 
 */
package mkm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import mkm.connect.MkmConnector;

/**
 *
 *
 * @author Kenny
 * @since 02.01.2016
 */
public class XmlConstants
{
	private static final String BUNDLE_NAME = "xmlConstants"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private static final ResourceBundle VERSION_INFO = ResourceBundle.getBundle("version/versionHistory");

	private XmlConstants()
	{
	}

	public static String getString(final String key)
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

	/**
	 * Retrieves the whole version history
	 * 
	 * @return
	 */
	public static String getVersionHistory()
	{
		java.util.List<String> allVersions = new ArrayList<>(VERSION_INFO.keySet().size());
		for (String s : VERSION_INFO.keySet())
		{
			allVersions.add(s);
		}

		String[] sortedKeys = new String[allVersions.size()];
		allVersions.toArray(sortedKeys);
		Arrays.sort(sortedKeys);

		StringBuffer retVal = new StringBuffer();
		for (int i = sortedKeys.length - 1; i >= 0; i--)
		{
			retVal.append("\n -- Version ").append(sortedKeys[i]).append(":\n");
			retVal.append(VERSION_INFO.getString(sortedKeys[i]));
			if (i > 0)
				retVal.append("\n\t");
		}

		return retVal.toString();
	}

	/**
	 * Retrieves current version info
	 * 
	 * @return a verion history string
	 */
	public static String getVersionInfo()
	{
		return getVersionInfo(MkmConnector.VERSION);
	}

	/**
	 * Retrieves a certain version info for a given number
	 * 
	 * @param versionNumber
	 *          the version number to retrieve
	 * @return the version info for the given number
	 */
	public static String getVersionInfo(final String versionNumber)
	{
		try
		{
			return VERSION_INFO.getString(versionNumber);
		}
		catch (MissingResourceException ex)
		{
			return "No info for this version";
		}
	}

}
