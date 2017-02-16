/**
 * 
 */
package mkm.data.multilingual;

/**
 *
 *
 * @author Kenny
 * @since 07.01.2016
 */
public enum Language
{

	English
	{
		@Override
		public String getLanguageName()
		{
			return "English";
		}
	},
	French
	{
		@Override
		public String getLanguageName()
		{
			return "French";
		}
	},
	German
	{
		@Override
		public String getLanguageName()
		{
			return "German";
		}
	},
	Spanish
	{
		@Override
		public String getLanguageName()
		{
			return "Spanish";
		}
	},
	Italian
	{
		@Override
		public String getLanguageName()
		{
			return "Italian";
		}
	},
	Simplified_Chinese
	{
		@Override
		public String getLanguageName()
		{
			return "Simplified Chinese";
		}
	},
	Japanese
	{
		@Override
		public String getLanguageName()
		{
			return "Japanese";
		}
	},
	Portuguese
	{
		@Override
		public String getLanguageName()
		{
			return "Portuguese";
		}
	},
	Russian
	{
		@Override
		public String getLanguageName()
		{
			return "Russian";
		}
	},
	Korean
	{
		@Override
		public String getLanguageName()
		{
			return "Korean";
		}
	},
	Traditional_Chinese
	{
		@Override
		public String getLanguageName()
		{
			return "Traditional Chinese";
		}
	};

	/**
	 * Returns the MKM-Integer value representation of this language
	 * 
	 * @return the int value:
	 *         <ul>
	 *         <li>English - 1</li>
	 *         <li>French - 2</li>
	 *         <li>German - 3</li>
	 *         <li>Spanish - 4</li>
	 *         <li>Italian - 5</li>
	 *         <li>Simplified Chinese - 6</li>
	 *         <li>Japanese - 7</li>
	 *         <li>Portuguese - 8</li>
	 *         <li>Russian - 9</li>
	 *         <li>Korean - 10</li>
	 *         <li>Traditional Chinese - 11</li>
	 *         </ul>
	 */
	public int getIntValue()
	{
		switch (this)
		{
			case English:
				return 1;
			case French:
				return 2;
			case German:
				return 3;
			case Spanish:
				return 4;
			case Italian:
				return 5;
			case Simplified_Chinese:
				return 6;
			case Japanese:
				return 7;
			case Portuguese:
				return 8;
			case Russian:
				return 9;
			case Korean:
				return 10;
			case Traditional_Chinese:
				return 11;
			default:
				throw new IllegalStateException("Unknown Language!" + this);
		}
	}

	public static Language getLanguageForName(final String langName)
	{
		if (English.getLanguageName().equals(langName))
		{
			return English;
		}
		else if (French.getLanguageName().equals(langName))
		{
			return French;
		}
		else if (German.getLanguageName().equals(langName))
		{
			return German;
		}
		else if (Spanish.getLanguageName().equals(langName))
		{
			return Spanish;
		}
		else if (Italian.getLanguageName().equals(langName))
		{
			return Italian;
		}
		else if (Simplified_Chinese.getLanguageName().equals(langName))
		{
			return Simplified_Chinese;
		}
		else if (Japanese.getLanguageName().equals(langName))
		{
			return Japanese;
		}
		else if (Portuguese.getLanguageName().equals(langName))
		{
			return Portuguese;
		}
		else if (Russian.getLanguageName().equals(langName))
		{
			return Russian;
		}
		else if (Korean.getLanguageName().equals(langName))
		{
			return Korean;
		}
		else if (Traditional_Chinese.getLanguageName().equals(langName))
		{
			return Traditional_Chinese;
		}
		else
		{
			throw new IllegalArgumentException("Unknown language: " + langName);
		}
	}

	/**
	 * Retrieves the language name
	 * 
	 * @return the string representing the name of the language
	 */
	public String getLanguageName()
	{
		throw new IllegalStateException("Unknown Language!" + this);
	}

	/**
	 * Returns an array with all available languages
	 * 
	 * @return
	 */
	public static String[] getAllLanguages()
	{
		String[] retVal = new String[Language.values().length];
		for (int i = 0; i < retVal.length; i++)
		{
			retVal[i] = Language.values()[i].getLanguageName();
		}
		return retVal;

	}
}
