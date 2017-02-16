/**
 * 
 */
package mkm.cache;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import mkm.connect.MkmConnector;
import mkm.file.FileHelper;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 * Implementation of the {@link IMkmCacheInstance} interface to build a cache within the file system
 *
 * @author Kenny
 * @since 29.12.2016
 */
class MkmFileCache implements IMkmCacheInstance
{

	public static String[] PERMANENT_CACHE_ENTRIES = new String[] {
			// product <-> id will not change too often
			MkmConnector.GET_PRODUCT_FOR_ID.substring(0, MkmConnector.GET_PRODUCT_FOR_ID.indexOf('/', 1)),
			// cards of an expansion won't change
			"/expansion/1/",
			// user <-> id
			MkmConnector.GET_USER_FOR_ID.substring(0, MkmConnector.GET_USER_FOR_ID.indexOf('/', 1)),

			// format
	};

	/**
	 * 
	 */
	public MkmFileCache()
	{
		// default constructor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mkm.cache.IMkmCacheInstance#getCacheContent(java.lang.String)
	 */
	@Override
	public String getCacheContent(final String personalizedParams)
	{
		String fileName = getFileNameForRequest(personalizedParams);
		return getFileContent(fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mkm.cache.IMkmCacheInstance#writeToCache(java.lang.String, java.lang.String)
	 */
	@Override
	public void writeToCache(final String personalizedParam, final String content)
	{
		// build the cache-key name
		String cacheFileName = getFileNameForRequest(personalizedParam);
		try
		{
			FileHelper.writeToFile(cacheFileName, content, false);
		}
		catch (IOException e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Error during writing cache file for: ", personalizedParam, ". Content: ", content);
		}
	}

	/**
	 * Retrieves the content of the given file or null in case of a cache miss
	 * 
	 * @param cacheFileName
	 *          the filename of the cache file
	 * @return the content of the given file or null if it does not exist
	 */
	public static String getFileContent(final String cacheFileName)
	{
		if (new File(cacheFileName).exists())
		{
			try
			{
				return FileHelper.readFile(cacheFileName);
			}
			catch (IOException ex)
			{
				LoggingHelper.logException(LogLevel.Error, ex, "Error while reading cache");
				return null;
			}
		}

		// cache miss
		return null;
	}

	/**
	 * Creates the filename for a given personalized query (without the base string)
	 * 
	 * @param personalizedParams
	 *          the request without the base string with all placeholders replaced
	 * @return the filename for the cache
	 */
	public static String getFileNameForRequest(final String personalizedParams)
	{
		StringBuilder fileName = null;

		// differentiate between permanent and temporary cache
		if (isPermanentCacheEntry(personalizedParams))
		{
			// if its a permanent entry, do not add the date
			fileName = new StringBuilder(MkmConnector.CACHE_ROOT_DIR);

			// make subfolders for product
			if (personalizedParams.startsWith(PERMANENT_CACHE_ENTRIES[0]))
			{
				String[] parts = personalizedParams.split("/");
				for (int i = 0; i < parts.length; i++)
				{
					if (parts[i] == null || "".equals(parts[i]))
						continue;

					if (i == parts.length - 1)
					{
						fileName.append('/').append(parts[i].charAt(0));
					}
					fileName.append('/');
					fileName.append(parts[i]);
				}
			}
			else
			{
				fileName.append(personalizedParams);
			}

		}
		else
		{
			// if its a temporary entry, cache into response dir for 1h
			fileName = new StringBuilder(MkmConnector.RESPONSE_LOG_ROOT_DIR);
			fileName.append(personalizedParams);
			fileName.append('/').append(new SimpleDateFormat(MkmCache.getDateFormatForEntry(personalizedParams)).format(Calendar.getInstance().getTime()));
		}
		fileName.append("_response.xml");

		return FileHelper.clearFileName(fileName, 1, fileName.length() - 5).toString();
	}

	/**
	 * Checks if a request is a permanent cache candidate
	 * 
	 * @param personalizedParams
	 * @return
	 */
	public static boolean isPermanentCacheEntry(final String personalizedParams)
	{
		if (personalizedParams == null || personalizedParams.length() == 0)
		{
			LoggingHelper.logForLevel(LogLevel.Warning, "MkmCache#isPermanentCacheEntry called with empty parameter. Returning false.");
			return false;
		}

		for (String s : PERMANENT_CACHE_ENTRIES)
		{
			if (personalizedParams.startsWith(s))
				return true;
		}
		return false;
	}
}
