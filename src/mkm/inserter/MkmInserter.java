/**
 * 
 */
package mkm.inserter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mkm.cache.MkmLocalDatabaseCache;
import mkm.config.MkmConfig;
import mkm.connect.MkmConnector;
import mkm.data.Article;
import mkm.data.MtgCard;
import mkm.data.multilingual.Language;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 * Main class for functionality for inserting cards
 *
 * @author Kenny
 * @since 07.01.2016
 */
public class MkmInserter
{

	public static final boolean PERFORM_EXACT_SEARCH = Boolean.parseBoolean(MkmConfig.getConfig("searchForExactCardName"));

	public static final String[] SEARCH_LANGUAGES = MkmConfig.getConfig("searchForLanguages").split(";");

	private MkmConnector connector;

	private MkmLocalDatabaseCache localDbCache;

	private boolean nameBufferWasBuild = false;

	/**
	 * Use {@link #buildCardBuffers()} to fill cache
	 */
	public MkmInserter(final MkmConnector con)
	{
		this.connector = con;
		this.localDbCache = new MkmLocalDatabaseCache();
	}

	protected String[] nameBuffer = new String[] {};

	protected Set<String> nameList = new TreeSet<>();

	protected Map<String, MtgCard> cardBuffer = new HashMap<>();

	/**
	 * Reads the product part of the cache dir and processes the information into the internal cache
	 * 
	 * @return the number of read items
	 */
	public int buildCardBuffers()
	{
		List<MtgCard> allCachedCards = this.localDbCache.getAllCardsFromCache();

		for (MtgCard card : allCachedCards)
		{
			addMtgCardToBuffer(card);
		}

		rebuildNameBuffer();

		return allCachedCards.size();

		// int retVal = 0;
		// File cacheRoot = new File(MkmConnector.CACHE_ROOT_DIR + "/product");
		// if (cacheRoot.exists())
		// {
		// File[] allCacheDirs = cacheRoot.listFiles();
		//
		// // if (allCacheFiles.length == 1)
		// // allCacheFiles = allCacheFiles[0].listFiles();
		// boolean useThreadded = Boolean.parseBoolean(MkmConnector.mkm_config.getString("buildCacheThreadded"));
		//
		// if (useThreadded)
		// {
		// // construct and start the cache builders
		// CacheBuilderThreaded[] runnables = new CacheBuilderThreaded[allCacheDirs.length];
		// Thread[] allDirs = new Thread[allCacheDirs.length];
		// for (int i = 0; i < allDirs.length; i++)
		// {
		// runnables[i] = new CacheBuilderThreaded(allCacheDirs[i]);
		// allDirs[i] = new Thread(runnables[i]);
		// allDirs[i].start();
		// }
		//
		// // wait for all to finish
		// boolean allTerminated = false;
		// while (!allTerminated)
		// {
		// allTerminated = true;
		// for (Thread t : allDirs)
		// {
		// if (t.getState() != State.TERMINATED)
		// {
		// allTerminated = false;
		// try
		// {
		// Thread.currentThread().sleep(200);
		// }
		// catch (InterruptedException e)
		// {
		// e.printStackTrace();
		// }
		// }
		// }
		// }
		//
		// // add all cards to the parent
		// for (CacheBuilderThreaded cacheBuilder : runnables)
		// {
		// for (MtgCard card : cacheBuilder.getBuffer())
		// {
		// addMtgCardToBuffer(card);
		// }
		// retVal += cacheBuilder.getBuffer().size();
		// }
		// }
		// // non-threadded
		// else
		// {
		// for (File cacheDir : allCacheDirs)
		// {
		// // add all cards to the buffer and build the nameList
		// for (File current : cacheDir.listFiles())
		// {
		// String xml = null;
		// try
		// {
		// xml = FileHelper.readFile(current.getAbsolutePath());
		//
		// if (xml != null)
		// {
		// MtgCard currentCard = new MtgCard(xml, false, Integer.parseInt(current.getName().substring(0, current.getName().indexOf("_response"))));
		//
		// addMtgCardToBuffer(currentCard);
		// }
		//
		// retVal++;
		// }
		// catch (IOException e)
		// {
		// if (e.getCause() instanceof SAXParseException && xml != null)
		// {
		// MtgCard currentCard = new MtgCard(xml);
		//
		// addMtgCardToBuffer(currentCard);
		//
		// retVal++;
		// }
		// else
		// {
		// LoggingHelper.logException(LogLevel.Info, e, "Unable to read file: ", current.getAbsolutePath());
		// }
		// }
		// }
		// }
		// }
		//
		// // sort and store
		// rebuildNameBuffer();
		// }
		// else
		// {
		// LoggingHelper.logForLevel(LogLevel.Critical, "Invalid chache dir, no program cache build!");
		// }
		// return retVal;
	}

	/**
	 * Adds an {@link MtgCard} to the nameCollection and the internal buffer<br>
	 * <b>Use {@link #rebuildNameBuffer()} after adding all cards</b><br/>
	 * format: NAMEINLANGUAGE_EDITION_LANGUAGENAME_RARITY
	 * 
	 * @param nameCollection
	 *          the name collection containing all names up to now
	 * @param currentCard
	 *          the card to be added and which is used for building the cache keys
	 */
	public void addMtgCardToBuffer(final MtgCard currentCard)
	{
		for (String availableLang : currentCard.getAllNames().getAvailableLanguages())
		{
			if (Language.German.getLanguageName().equals(availableLang) || Language.English.getLanguageName().equals(availableLang))
			{

				// build the key for the map for each language
				// key: NAME_EDITION_LANGUAGENAME_RARITY
				String key = new StringBuffer(currentCard.getAllNames().getNameForLang(availableLang).toUpperCase()) // name in the given language
						.append('_').append(currentCard.getEdition()) // edition
						.append('_').append(availableLang) // which language this is
						.append('_').append(currentCard.getRarity()) // the rarity
						.toString().toUpperCase();

				nameList.add(key);
				cardBuffer.put(key, currentCard);
			}
		}

		// reset sorted flag
		if (nameList.size() > 0)
		{
			this.nameBufferWasBuild = false;
		}
	}

	/**
	 * Checks if there have been any changes to the name buffer without rebuilding it
	 * 
	 * @return true if rebuild is needed, false if current name buffer is up to date
	 */
	public boolean nameBufferNeedsRebuild()
	{
		return !this.nameBufferWasBuild;
	}

	/**
	 * Sorts and stores the given name collection
	 * 
	 * @param nameCollection
	 *          the list of all keys used by {@link #addMtgCardToBuffer(List, MtgCard)}
	 */
	public void rebuildNameBuffer()
	{
		// sorting not necessary, tree set items are already sorted
		// java.util.Collections.sort(nameList);

		this.nameBuffer = new String[nameList.size()];
		nameList.toArray(this.nameBuffer);

		// set flag to true
		this.nameBufferWasBuild = true;
	}

	/**
	 * Retrieves the current name buffer
	 * 
	 * @return the currently sorted name buffer (may not be complete unless you called {@link #rebuildNameBuffer()})
	 */
	public String[] getNameBuffer()
	{
		return this.nameBuffer;
	}

	/**
	 * Retrieves a list of cards where at least one name in any language matches the given string
	 * 
	 * @param partOfCardName
	 *          part of the searched name or edition
	 * @return a list of matches
	 */
	public List<String> getMatchingCards(final String partOfCardName)
	{
		List<String> retVal = new ArrayList<>();

		if (nameBufferNeedsRebuild())
		{
			LoggingHelper.logForLevel(LogLevel.Critical, "Rebuilding card buffer as nameBuffer has not been build since last search.");
			rebuildNameBuffer();
		}

		final String[] match = partOfCardName.toUpperCase().split(" ");

		// performance optimization for single words
		if (match.length == 1)
		{
			for (String s : this.nameBuffer)
			{
				if (s != null && s.contains(match[0]))
					retVal.add(s);
			}
		}
		else
		{
			for (String s : this.nameBuffer)
			{
				boolean isMatch = true;
				for (String m : match)
				{
					if (s == null || !s.contains(m))
					{
						isMatch = false;
						break;
					}
				}
				if (isMatch)
					retVal.add(s);
			}
		}

		return retVal;
	}

	/**
	 * Retrieves the card buffer which has been build. Maps the keys (from {@link #getMatchingCards(String)} to instances of {@link MtgCard}
	 * 
	 * @return mapping from card name to {@link MtgCard}
	 */
	public Map<String, MtgCard> getCardBuffer()
	{
		return this.cardBuffer;
	}

	public void startManualCardSearch()
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String input = "";
		do
		{
			System.out.println("String to search for: ");
			try
			{
				input = reader.readLine();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			if ("exit".equals(input))
				System.exit(0);

			System.out.println("\n\ncards found: ");
			long time = System.currentTimeMillis();
			List<String> matches = getMatchingCards(input);
			time = System.currentTimeMillis() - time;

			for (String s : matches)
			{
				MtgCard found = getCardBuffer().get(s);
				System.out.println(found.getName() + " ; " + found.getEdition());
			}
			System.out.println("Time needed: " + time);

		} while (input != null && input.length() > 0);
	}

	/**
	 * Adds the given article to the current users stock (identified by the mkm-secrets)
	 * 
	 * @param toAdd
	 *          the article to add
	 * @return null if everything is fine, the occurred exception otherwise
	 */
	public Throwable addArticleToStock(final Article toAdd)
	{
		try
		{
			getConnector().performPostRequest(MkmConnector.POST_NEW_STOCK_ITEM, toAdd.toSellPostXML(true, false, true));
			return null;
		}
		catch (Exception e)
		{
			LoggingHelper.logException(LogLevel.Critical, e, "Unable to post data!");
			return e;
		}
	}

	/**
	 * @return the connector
	 */
	public MkmConnector getConnector()
	{
		return connector;
	}

	/**
	 * @param connector
	 *          the connector to set
	 */
	public void setConnector(final MkmConnector connector)
	{
		this.connector = connector;
	}
}
