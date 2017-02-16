package mkm.data;

/**
 *
 *
 * @author Kenny
 * @since 13.12.2015
 */
public enum Rarity
{
	Common
	{
		@Override
		public String getAbbreviation()
		{
			return "Common";
		};

		@Override
		public int getRarityValue()
		{
			return VALUE_COMMON;
		}

	},

	Uncommon
	{
		@Override
		public String getAbbreviation()
		{
			return "Uncommon";
		};

		@Override
		public int getRarityValue()
		{
			return VALUE_UNCOMMON;
		}
	},

	Rare
	{
		@Override
		public String getAbbreviation()
		{
			return "Rare";
		};

		@Override
		public int getRarityValue()
		{
			return VALUE_RARE;
		}
	},

	Mythic
	{
		@Override
		public String getAbbreviation()
		{
			return "Mythic";
		};

		@Override
		public int getRarityValue()
		{
			return VALUE_MYTHIC;
		}
	},
	Land
	{
		@Override
		public String getAbbreviation()
		{
			return "Land";
		}

		@Override
		public int getRarityValue()
		{
			return VALUE_LAND;
		}
	},
	Token
	{
		@Override
		public String getAbbreviation()
		{
			return "Token";
		}

		@Override
		public int getRarityValue()
		{
			return VALUE_TOKEN;
		}
	},
	TimeShifted
	{
		@Override
		public String getAbbreviation()
		{
			return "Time Shifted";
		}

		@Override
		public int getRarityValue()
		{
			return Rarity.VALUE_TIMESHIFTED;
		}
	},
	Special
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.data.Rarity#getAbbreviation()
		 */
		@Override
		public String getAbbreviation()
		{
			return "Special";
		}

		@Override
		public int getRarityValue()
		{
			return VALUE_SPECIAL;
		}
	},
	None
	{
		@Override
		public String getAbbreviation()
		{
			return "None";
		}

		@Override
		public int getRarityValue()
		{
			return Rarity.VALUE_NONE;
		}
	};

	/**
	 * Retrieves the correct rarity for the given abbriviation
	 * 
	 * @param abbrev
	 *          the abbreviation of the rarity
	 * @return the Enum rarity
	 */
	public static Rarity getRarityForAbbreviation(final String abbrev)
	{
		if (Common.getAbbreviation().equals(abbrev))
		{
			return Common;
		}
		else if (Uncommon.getAbbreviation().equals(abbrev))
		{
			return Uncommon;
		}
		else if (Rare.getAbbreviation().equals(abbrev))
		{
			return Rare;
		}
		else if (Mythic.getAbbreviation().equals(abbrev))
		{
			return Mythic;
		}
		else if (Land.getAbbreviation().equals(abbrev))
		{
			return Land;
		}
		else if (TimeShifted.getAbbreviation().equals(abbrev))
		{
			return TimeShifted;
		}
		else if (Token.getAbbreviation().equals(abbrev))
		{
			return Token;
		}
		else if (None.getAbbreviation().equals(abbrev))
		{
			return None;
		}
		else if (Special.getAbbreviation().equals(abbrev))
		{
			return Special;
		}
		else
		{
			throw new IllegalArgumentException("Unknown Rarity: " + abbrev);
		}
	}

	/**
	 * Retrieves the singe character abbreviating the current rarity
	 * 
	 * @return
	 * 				<ul>
	 *         <li>Common</li> <liUncommon</li>
	 *         <li>Rare</li>
	 *         <li>Mythic</li>
	 *         </ul>
	 */
	public String getAbbreviation()
	{
		throw new IllegalAccessError("Unexpected access of non-instance call to Rarity");
	}

	/**
	 * Retrieves the integer value of the rarity which can be used for sorting.
	 * 
	 * @return one of the following values in ascending order:
	 *         <ul>
	 *         <li>{@link #VALUE_NONE}</li>
	 *         <li>{@link #VALUE_SPECIAL}</li>
	 *         <li>{@link #VALUE_TOKEN}</li>
	 *         <li>{@link #VALUE_TIMESHIFTED}</li>
	 *         <li>{@link #VALUE_LAND}</li>
	 *         <li>{@link #VALUE_COMMON}</li>
	 *         <li>{@link #VALUE_UNCOMMON}</li>
	 *         <li>{@link #VALUE_RARE}</li>
	 *         <li>{@link #VALUE_MYTHIC}</li>
	 *         </ul>
	 */
	public int getRarityValue()
	{
		throw new IllegalAccessError("Unexpected access of non-instance call to Rarity");
	}

	/**
	 * Retrieves all rarity abbreviations
	 */
	public static String[] getAllAbbreviations()
	{
		String[] retVal = new String[Rarity.values().length];
		for (int i = 0; i < retVal.length; i++)
		{
			retVal[i] = Rarity.values()[i].getAbbreviation();
		}
		return retVal;
	}

	public static final int VALUE_COMMON = 1;

	public static final int VALUE_UNCOMMON = 2;

	public static final int VALUE_RARE = 3;

	public static final int VALUE_MYTHIC = 4;

	public static final int VALUE_LAND = -1;

	public static final int VALUE_TIMESHIFTED = -2;

	public static final int VALUE_TOKEN = -3;

	public static final int VALUE_SPECIAL = -4;

	public static final int VALUE_NONE = -5;
}
