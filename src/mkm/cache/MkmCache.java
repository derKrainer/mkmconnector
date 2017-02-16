/**
 * 
 */
package mkm.cache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import mkm.connect.MkmConnector;
import mkm.connect.MkmServiceHandler;
import mkm.data.MtgCard;
import mkm.file.FileHelper;
import mkm.inserter.gui.image.ImageUtility;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 * Cache util for avoiding too much server work
 *
 * @author Kenny
 * @since 05.12.2015
 */
public class MkmCache
{
	private static MkmLocalDatabaseCache dbCache = new MkmLocalDatabaseCache();

	/**
	 * Refreshes the cache content of a given MtgCard
	 * 
	 * @param toRefresh
	 *          the card to be refreshed
	 * @param connector
	 *          the current instance of the {@link MkmConnector}, if null a new one will be created
	 * @return the passed card but with refreshed values
	 */
	public static MtgCard refreshCacheContent(final MtgCard toRefresh, MkmConnector connector)
	{
		if (toRefresh == null)
		{
			throw new IllegalArgumentException("No card passed to refresh the cache!");
		}
		if (toRefresh.getCardId() < 1)
		{
			throw new IllegalArgumentException("Illegal Card id: " + toRefresh.getCardId());
		}
		// better save than sorry
		if (connector == null)
		{
			connector = new MkmConnector();
		}

		// build the cache key-base
		String personalizedParams = MkmConnector.getPersonalizedParams(MkmConnector.GET_PRODUCT_FOR_ID, new String[] { ":id" },
				new String[] { Integer.toString(toRefresh.getCardId()) });

		String result = null;
		try
		{
			// call perform getRequest with ignoreCache == true and ignoreWriteCache == false to ensure fresh data
			result = connector.performGetRequest(personalizedParams, null, null, true, false);
		}
		catch (Exception e)
		{
			LoggingHelper.logException(LogLevel.Critical, e, "Unable to refresh card: " + toRefresh.getCardId());
			return null;
		}

		try
		{
			return new MtgCard(result, false, toRefresh.getCardId());
		}
		catch (Exception e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Could not refresh card with id: " + toRefresh.getCardId());
			return null;
		}
	}

	/**
	 * Writes a cache entry for the given persolalizedParam and the given content
	 * 
	 * @param personalizedParam
	 *          the request which was sent to the mkm server
	 * @param content
	 *          the result of the server
	 */
	public static void writeToCache(final String personalizedParam, final String content)
	{
		getCacheStrategy().writeToCache(personalizedParam, content);

		if (MkmServiceHandler.isGetCardForIdRequest(personalizedParam))
		{
			dbCache.writeToCache(content);
		}
	}

	/**
	 * Retrieves data from the cache for the given personalized request string
	 * 
	 * @param personalizedParams
	 *          the request string with user/card info
	 * @return locally stored data or null if there is a cache miss
	 */
	public static String getCacheContent(final String personalizedParams)
	{
		return getCacheStrategy().getCacheContent(personalizedParams);
	}

	/**
	 * Current cache strategy TODO: implement switch / config
	 */
	private static IMkmCacheInstance stragegyInstance = new MkmFileCache();

	/**
	 * Retrieves the current strategy for caching (DB or File or whatever will come)
	 * 
	 * @return the strategy to use
	 */
	private static IMkmCacheInstance getCacheStrategy()
	{
		return stragegyInstance;
	}

	/**
	 * TODO: maybe cache different requests for different times
	 * 
	 */
	public static String getDateFormatForEntry(final String personalizedParams)
	{
		return "yyyy-MM-dd_HH";
	}

	/**
	 * Gets the image file for a given card.<br>
	 * The image location stands in {@link MtgCard#getImageLocation()}.
	 * 
	 * @param cardToGetFor
	 *          the card the image should be gotten for
	 * @return null if no card or inforamtion is present or anything went wrong during reading (either file or server). Card image otherwise
	 */
	public static BufferedImage getImage(final MtgCard cardToGetFor)
	{
		if (cardToGetFor == null || cardToGetFor.getImageLocation() == null || "".equals(cardToGetFor.getImageLocation()))
		{
			return null;
		}

		BufferedImage retVal = null;
		// check cache
		StringBuilder cacheFileBuffer = new StringBuilder();
		cacheFileBuffer.append(MkmConnector.CACHE_ROOT_DIR);
		cacheFileBuffer.append(cardToGetFor.getImageLocation().substring(1)); // substring(1) because url starts with .
		// no not clear the cache root and the file ending
		cacheFileBuffer = FileHelper.clearFileName(cacheFileBuffer, MkmConnector.CACHE_ROOT_DIR.length(), cacheFileBuffer.length() - 5);

		String cacheFile = cacheFileBuffer.toString();

		if (new File(cacheFile.toString()).exists())
		{
			retVal = new ImageUtility().readImage(cacheFile.toString());
		}

		if (retVal == null)
		{
			LoggingHelper.info("Local file not found. Loading image from Server to: " + new File(cacheFile).getAbsolutePath());

			// if cache miss: load from server and write cache file
			String url = new StringBuffer(MkmConnector.BASE_URL).append(cardToGetFor.getImageLocation()).toString();

			// read from server
			try
			{
				retVal = ImageIO.read(new URL(url));
			}
			catch (IOException e)
			{
				LoggingHelper.logException(LogLevel.Critical, e, "Unable to read image from Server. Wrong url?");
			}

			// write cache
			if (retVal != null)
			{
				try
				{
					ImageIO.write(retVal, "JPG", FileHelper.createFile(new File(cacheFile.toString())));
				}
				catch (IOException e)
				{
					LoggingHelper.logException(LogLevel.Critical, e, "Unable to write image cache file!");
				}
			}
		}

		return retVal;
	}

}
