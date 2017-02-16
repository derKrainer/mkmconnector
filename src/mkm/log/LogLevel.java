/**
 * 
 */
package mkm.log;

/**
 *
 *
 * @author Kenny
 * @since 12.12.2015
 */
public enum LogLevel
{
	All
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return Integer.MIN_VALUE;
		}
	},

	Detailed
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return -500;
		}
	},

	Info
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return 0;
		}
	},

	Warning
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return 500;
		}
	},

	Error
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return 1000;
		}
	},

	Critical
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return 2000;
		}
	},
	Fatal
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return 5000;
		}
	},

	None
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see mkm.log.LogLevel#getIntValue()
		 */
		@Override
		public int getIntValue()
		{
			return Integer.MAX_VALUE;
		}
	};

	/**
	 * Retrurns an integer representation of the status
	 * 
	 * @return the integer representation of this log level. The higher the more critical
	 */
	public int getIntValue()
	{
		throw new IllegalStateException("getIntValue not overwritten");
	}

	/**
	 * Retrieves a log level for the given name of throws an exception
	 * 
	 * @param name
	 *          the name of the level
	 * @return the matching level (.toString of the LogLevel matches the name)
	 */
	public static LogLevel getLevelForString(final String name)
	{
		for (LogLevel l : LogLevel.values())
			if (l.toString().equals(name))
				return l;
		throw new IllegalArgumentException("Unknown log-level: " + name);
	}

	/**
	 * Checks if the logCallLevels enough for systemLevel
	 * 
	 * @param systemLevel
	 *          the systems current log level
	 * @param logCallLevel
	 *          the logging level for the current entry to be logged
	 * @return true if the entry should be logged, false if no logging should happen
	 */
	public static boolean shouldLog(final LogLevel systemLevel, final LogLevel logCallLevel)
	{
		if (systemLevel == null || logCallLevel == null || systemLevel == None)
		{
			return false;
		}
		else if (systemLevel == All)
		{
			return true;
		}
		else
		{
			return systemLevel.getIntValue() <= logCallLevel.getIntValue();
		}
	}
}
