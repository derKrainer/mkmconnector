/**
 * 
 */
package mkm.localization;

import java.util.ResourceBundle;

/**
 *
 *
 * @author Kenny
 * @since 11.12.2016
 */
public class Localization
{

	private static final ResourceBundle generalLocalisation = ResourceBundle.getBundle("locale.general");

	/**
	 * Hidden Constructor
	 */
	private Localization()
	{
	}

	public static final String getLocalizedString(final String toTranslate)
	{
		// TODO: implement localisation
		if (generalLocalisation.containsKey(toTranslate))
			return generalLocalisation.getString(toTranslate);

		return toTranslate;
	}

}
