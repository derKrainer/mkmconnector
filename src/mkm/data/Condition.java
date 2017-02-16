/**
 * 
 */
package mkm.data;

/**
 * Enumeration to indicate the quality a card is in
 *
 * @author Kenny
 * @since 13.12.2015
 */
public enum Condition
{
	Mint
	{
		@Override
		public String getAbbreviation()
		{
			return "MT";
		}
	},

	NearMint
	{
		@Override
		public String getAbbreviation()
		{
			return "NM";
		}
	},

	Excellent
	{
		@Override
		public String getAbbreviation()
		{
			return "EX";
		}
	},

	Good
	{
		@Override
		public String getAbbreviation()
		{
			return "GD";
		}
	},

	LightPlayed
	{
		@Override
		public String getAbbreviation()
		{
			return "LP";
		}
	},

	Played
	{
		@Override
		public String getAbbreviation()
		{
			return "PL";
		}
	},

	Poor
	{
		@Override
		public String getAbbreviation()
		{
			return "PO";
		}
	};

	public static Condition getConditionForAbbreviation(final String abbrev)
	{
		if (NearMint.getAbbreviation().equals(abbrev))
			return NearMint;
		else if (Mint.getAbbreviation().equals(abbrev))
			return Mint;
		else if (Excellent.getAbbreviation().equals(abbrev))
			return Excellent;
		else if (Good.getAbbreviation().equals(abbrev))
			return Good;
		else if (LightPlayed.getAbbreviation().equals(abbrev))
			return LightPlayed;
		else if (Played.getAbbreviation().equals(abbrev))
			return Played;
		else if (Poor.getAbbreviation().equals(abbrev))
			return Poor;
		else
			throw new IllegalArgumentException("Unknown condition: " + abbrev);
	}

	/**
	 * Retrieves the two characters abbreviating the current condition
	 * 
	 * @return <ul>
	 *         <li>MT - Mint</li>
	 *         <li>NM - NearMint</li>
	 *         <li>EX - Excellent</li>
	 *         <li>GD - Good</li>
	 *         <li>LP - LightPlayed</li>
	 *         <li>PL - Played</li>
	 *         <li>PO - Poor</li>
	 *         </ul>
	 */
	public String getAbbreviation()
	{
		throw new IllegalAccessError("Unexpected access of non-instance call to Rarity");
	}

	/**
	 * Retrieves all abbreviations
	 */
	public static String[] getAllConditions()
	{
		String[] retVal = new String[Condition.values().length];
		for (int i = 0; i < retVal.length; i++)
		{
			retVal[i] = Condition.values()[i].getAbbreviation();
		}
		return retVal;

	}

}
