/**
 * 
 */
package mkm.cache.builder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import mkm.XmlConstants;
import mkm.cache.MkmCache;
import mkm.connect.MkmConnector;
import mkm.exception.CertRefreshNeededException;
import mkm.exception.Http400Exception;
import mkm.file.FileHelper;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 *
 *
 * @author Kenny
 * @since 11.01.2016
 */
public class MkmCacheBuilder
{

	public static final int CARD_MAX_ID = Integer.parseInt(XmlConstants.getString("Cache.maxCardNumber"));

	public static final String BLACKLIST_NAME = new StringBuffer(MkmConnector.CACHE_ROOT_DIR).append("/productBlacklist.csv").toString();

	private MkmConnector parent;

	private int maxIterations;

	private int latestIndex = 1;

	private LogLevel systemLevel = LoggingHelper.SYSTEM_LEVEL;

	public Set<Integer> blackList = new HashSet<>();

	/**
	 * 
	 */
	public MkmCacheBuilder(final MkmConnector parent, final int maxIterations)
	{
		this.parent = parent;
		// +1 because iteration starts at 1 as product(id=0) does not exists
		this.maxIterations = maxIterations + 1;

		buildBlackList();
	}

	/**
	 * Builds the blacklist for this class
	 */
	private void buildBlackList()
	{
		try
		{
			String[] elements = FileHelper.readFile(BLACKLIST_NAME).split(";");
			for (String s : elements)
			{
				this.blackList.add(Integer.parseInt(s));
			}
		}
		catch (IOException e)
		{
			System.err.println("Unable to create blacklist. Exiting before creating senseless requests.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void buildCardCache(final boolean verbouse)
	{

		if (verbouse)
		{
			LoggingHelper.SYSTEM_LEVEL = LogLevel.Info;
		}

		int cacheHits = 0;
		int count400 = 0;
		int currentMaxIndex = latestIndex;
		for (int i = latestIndex; i < CARD_MAX_ID && i < (maxIterations + cacheHits + currentMaxIndex); i++)
		{
			// skip blacklist entries
			if (this.blackList.contains(i))
			{
				LoggingHelper.logForLevel(LogLevel.Info, "Skipping entry " + i, " because it is blacklisted.");
				cacheHits++;
				continue;
			}

			// build the cache key-base
			String personalizedParams = MkmConnector.getPersonalizedParams(MkmConnector.GET_PRODUCT_FOR_ID, new String[] { ":id" },
					new String[] { Integer.toString(i) });

			// check if the cache entry alredy exists
			if (MkmCache.getCacheContent(personalizedParams) != null)
			{
				// do one round more
				LoggingHelper.logForLevel(LogLevel.Detailed, "cache hit for:", personalizedParams, ". Incrementing maxIterations.");
				cacheHits++;
			}
			else
			{
				// contact server and write entry
				LoggingHelper.logForLevel(LogLevel.Info, "Writing cache for request: ", personalizedParams);
				try
				{
					parent.performGetRequest(MkmConnector.GET_PRODUCT_FOR_ID, ":id", Integer.toString(i));
					LoggingHelper.logForLevel(LogLevel.Info, "Successfully written cache entry for:  ", personalizedParams);
				}
				catch (Http400Exception ex)
				{
					// add to blacklist
					LoggingHelper.logForLevel(LogLevel.Info, "Server returned 400. Adding " + i, " to the blacklist.");
					count400++;
					addBlackListEntry(i);
				}
				catch (CertRefreshNeededException ex)
				{
					// the certificate is not up to date, print error and exit
					LoggingHelper.logForLevel(LogLevel.None, "MKM Certificate is not registered at your current java version. Run install_cert.bat");
					System.exit(0);
				}
				catch (Exception e)
				{
					LoggingHelper.logException(LogLevel.Error, e, "Cannot write cache entry for: ", personalizedParams);
				}
			}

			latestIndex = i;
		}

		LoggingHelper.logForLevel(systemLevel, "Number of 400 return codes: " + count400);

		// reset log level
		LoggingHelper.SYSTEM_LEVEL = systemLevel;

	}

	public void addBlackListEntry(final Integer cardNumber)
	{
		this.blackList.add(cardNumber);

		FileWriter writer = null;
		try
		{
			writer = new FileWriter(BLACKLIST_NAME, true);
			writer.append(";" + cardNumber);
			writer.flush();
		}
		catch (IOException ex)
		{
			LoggingHelper.logException(LogLevel.Critical, ex, "Unable to add " + cardNumber, " to the blacklist!");
		}
		finally
		{
			try
			{
				if (writer != null)
					writer.close();
			}
			catch (IOException e)
			{
				LoggingHelper.logException(LogLevel.Critical, e, "Unable to close blacklist file stream");
			}
		}
	}

}
