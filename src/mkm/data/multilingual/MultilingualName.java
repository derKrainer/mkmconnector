/**
 * 
 */
package mkm.data.multilingual;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author Kenny
 * @since 07.01.2016
 */
public class MultilingualName
{

	public static final Language[] SORTED_LANGUAGES = new Language[] { Language.English, Language.German, Language.French, Language.Spanish, Language.Italian };

	private Map<Language, String> languageNames = new HashMap<Language, String>(10);

	/**
	 * 
	 */
	public MultilingualName()
	{

	}

	public void addName(final Language lang, final String cardName)
	{
		this.languageNames.put(lang, cardName);
	}

	public void addName(final String languageName, final String cardName)
	{
		this.languageNames.put(Language.getLanguageForName(languageName), cardName);
	}

	public String getAllLanguages()
	{
		StringBuffer retVal = new StringBuffer();

		for (Language lang : SORTED_LANGUAGES)
		{
			if (languageNames.get(lang) != null)
			{
				retVal.append(lang.getLanguageName()).append(" - ").append(this.languageNames.get(lang)).append('\n');
			}
		}

		return retVal.toString();
	}

	public String[] getAvailableLanguages()
	{
		String[] retVal = new String[this.languageNames.size()];

		int i = 0;
		for (Language lang : this.languageNames.keySet())
		{
			retVal[i++] = lang.getLanguageName();
		}

		return retVal;
	}

	public Set<Language> getLanguageSet()
	{
		return this.languageNames.keySet();
	}

	public String[] getAvailableNames()
	{
		String[] retVal = new String[this.languageNames.size()];

		int i = 0;
		for (String s : this.languageNames.values())
		{
			retVal[i++] = s;
		}

		return retVal;
	}

	public String getNameForLang(final String lang)
	{
		return this.languageNames.get(Language.getLanguageForName(lang));
	}

}
