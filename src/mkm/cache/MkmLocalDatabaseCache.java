/**
 * 
 */
package mkm.cache;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import mkm.connect.MkmConnector;
import mkm.data.MtgCard;
import mkm.data.ProductPrice;
import mkm.data.Rarity;
import mkm.data.multilingual.Language;
import mkm.data.multilingual.MultilingualName;
import mkm.exception.SQLSetupException;
import mkm.file.FileHelper;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.parser.XmlParser;

/**
 * Implementation of the {@link IMkmCacheInstance} using a local database (on a file) approach
 *
 * @author Kenny
 * @since 29.12.2016
 */
public class MkmLocalDatabaseCache implements MkmLocalDatabaseConstants
{

	public static final String DB_FILENAME = "mkmdatabase.db";

	public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected Connection connection = null;

	/**
	 * 
	 */
	public MkmLocalDatabaseCache()
	{
		try
		{
			if (new File(DB_FILENAME).exists())
			{
				connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILENAME);
			}
			else
			{
				// set up database
				connection = initDatabase();
			}
		}
		catch (SQLException e)
		{
			throw new SQLSetupException(e);
		}
		catch (IOException e)
		{
			throw new SQLSetupException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		if (this.connection != null)
			this.connection.close();

		super.finalize();
	}

	protected Connection initDatabase() throws SQLException, IOException
	{
		Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILENAME);
		Statement stmt = connection.createStatement();
		String sql = FileHelper.readFile("./sql/setup_cache.sql");

		stmt.execute(sql);

		// copy all existing data into the database:
		copyFileCacheIntoDB(connection);

		return connection;
	}

	protected int copyFileCacheIntoDB(final Connection con) throws SQLException
	{
		Statement stmt = con.createStatement();

		int retVal = 0;
		File cacheRoot = new File(MkmConnector.CACHE_ROOT_DIR + "/product");
		if (cacheRoot.exists())
		{
			File[] allCacheDirs = cacheRoot.listFiles();

			for (File cacheDir : allCacheDirs)
			{
				// add all cards to the buffer and build the nameList
				for (File current : cacheDir.listFiles())
				{
					String xml = null;
					try
					{
						xml = FileHelper.readFile(current.getAbsolutePath());

						if (xml != null)
						{
							String insert = convertXmlToDbInsert(xml);
							try
							{
								stmt.executeQuery(insert);
							}
							catch (SQLException ex)
							{
								LoggingHelper.logException(LogLevel.Error, ex, "Error during executing sql:", insert);
							}
						}

						retVal++;
					}
					catch (IOException e)
					{
						LoggingHelper.logException(LogLevel.Info, e, "Unable to read file: ", current.getAbsolutePath());
					}
				}
			}

		}
		else
		{
			LoggingHelper.logForLevel(LogLevel.Critical, "Invalid chache dir, no program cache build!");
		}

		stmt.close();

		return retVal;
	}

	protected String convertXmlToDbInsert(final String cardXml) throws IOException
	{
		StringBuffer retVal = new StringBuffer();

		XmlParser cardParser = new XmlParser(cardXml);
		List<Node> nodeList = new ArrayList<>();
		try
		{
			cardParser.catalogNodes();

			nodeList = cardParser.getNodesForPath("response/product");
		}
		catch (IOException ex)
		{
			LoggingHelper.logException(LogLevel.Error, ex, "Error during parsing cardXml: ", cardXml);
			throw ex;
		}

		if (nodeList.size() == 1)
		{
			Node root = nodeList.get(0);
			nodeList = new ArrayList<>(root.getChildNodes().getLength());
			for (int i = 0; i < root.getChildNodes().getLength(); i++)
			{
				nodeList.add(root.getChildNodes().item(i));
			}
		}

		Map<String, String> values = collectValuesFromNodeList(nodeList);

		retVal.append("INSERT OR REPLACE INTO MTGCARD (");
		boolean first = true;
		StringBuffer valueBuffer = new StringBuffer();
		for (String colName : values.keySet())
		{
			if (first)
				first = false;
			else
			{
				retVal.append(", ");
				valueBuffer.append(", ");
			}
			retVal.append("`").append(colName).append("`");

			String value = values.get(colName);
			if (value != null)
				valueBuffer.append("'").append(value.replace("'", "''")).append("'");
			else
				valueBuffer.append("NULL");
		}

		retVal.append(") VALUES (");
		retVal.append(valueBuffer.toString());
		retVal.append(");");

		return retVal.toString();
	}

	/**
	 * Searches the node list for relevant nodes for an {@link MtgCard} and collects those values in a Map where the key is the appropriate database Column name
	 * 
	 * @param nodeList
	 *          the node list to search
	 * @return a map consisting of Paris of DB_COLUMN_NAME -- NODE_VALUE
	 */
	protected Map<String, String> collectValuesFromNodeList(final List<Node> nodeList)
	{
		Map<String, String> values = new HashMap<>();

		for (Node tmpNode : nodeList)
		{
			if ("idProduct".equals(tmpNode.getNodeName()))
			{
				values.put(COL_PRODUCT_ID, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("idMetaproduct".equals(tmpNode.getNodeName()))
			{
				values.put(COL_METAPRODUCT_ID, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("idGame".equals(tmpNode.getNodeName()))
			{
				values.put(COL_GAME_ID, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("countReprints".equals(tmpNode.getNodeName()))
			{
				values.put(COL_COUNT_REPRINTS, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("website".equals(tmpNode.getNodeName()))
			{
				values.put(COL_WEBSITE, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("image".equals(tmpNode.getNodeName()))
			{
				values.put(COL_IMAGE_URL, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("expansion".equals(tmpNode.getNodeName()))
			{
				values.put(COL_EXPANSION, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("expIcon".equals(tmpNode.getNodeName()))
			{
				values.put(COL_EXPANSION_ICON, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("number".equals(tmpNode.getNodeName()))
			{
				values.put(COL_COLLECTORS_NUMBER, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("rarity".equals(tmpNode.getNodeName()))
			{
				values.put(COL_RARITY, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("createdAt".equals(tmpNode.getNodeName()))
			{
				values.put(COL_CREATED_AT, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("updatedAt".equals(tmpNode.getNodeName()))
			{
				values.put(COL_UPDATED_AT, XmlParser.getNodeTextValue(tmpNode));
			}
			else if ("priceGuide".equals(tmpNode.getNodeName()))
			{
				for (int j = 0; j < tmpNode.getChildNodes().getLength(); j++)
				{
					Node child = tmpNode.getChildNodes().item(j);
					if ("SELL".equals(child.getNodeName()))
					{
						values.put(COL_PRICE_SELL, XmlParser.getNodeTextValue(child));
					}
					else if ("LOW".equals(child.getNodeName()))
					{
						values.put(COL_PRICE_LOW, XmlParser.getNodeTextValue(child));
					}
					else if ("LOWEX".equals(child.getNodeName()))
					{
						values.put(COL_PRICE_LOWEX, XmlParser.getNodeTextValue(child));
					}
					else if ("LOWFOIL".equals(child.getNodeName()))
					{
						values.put(COL_PRICE_LOWFOIL, XmlParser.getNodeTextValue(child));
					}
					else if ("AVG".equals(child.getNodeName()))
					{
						values.put(COL_PRICE_AVG, XmlParser.getNodeTextValue(child));
					}
					else if ("TREND".equals(child.getNodeName()))
					{
						values.put(COL_PRICE_TREND, XmlParser.getNodeTextValue(child));
					}
				}
			}
			else if ("name".equals(tmpNode.getNodeName()))
			{
				String languageName = null;
				String localizedName = null;
				int langId = -1;
				for (int j = 0; j < tmpNode.getChildNodes().getLength(); j++)
				{
					Node child = tmpNode.getChildNodes().item(j);
					if ("languageName".equals(child.getNodeName()))
					{
						languageName = XmlParser.getNodeTextValue(child);
					}
					else if ("productName".equals(child.getNodeName()))
					{
						localizedName = XmlParser.getNodeTextValue(child);
					}
					else if ("idLanguage".equals(child.getNodeName()))
					{
						langId = Integer.parseInt(XmlParser.getNodeTextValue(child));
					}
				}

				// only accept languages English, french, german, spanish and italian
				if (languageName != null && localizedName != null && langId > -1 && langId <= 5)
				{
					values.put("NAME_" + languageName.toUpperCase(), localizedName);
				}
			}
			else if ("category".equals(tmpNode.getNodeName()))
			{
				for (int j = 0; j < tmpNode.getChildNodes().getLength(); j++)
				{
					Node child = tmpNode.getChildNodes().item(j);
					if ("idCategory".equals(child.getNodeName()))
					{
						values.put(COL_CATEGORY_ID, XmlParser.getNodeTextValue(child));
						break;
					}
				}
			}
		}

		if (values.get(COL_CREATED_AT) == null)
		{
			values.put(COL_CREATED_AT, TIMESTAMP_FORMAT.format(new Date()));
		}
		if (values.get(COL_UPDATED_AT) == null)
		{
			values.put(COL_UPDATED_AT, TIMESTAMP_FORMAT.format(new Date()));
		}

		return values;
	}

	/**
	 * Checks the local database for an existing entry with the given productId
	 * 
	 * @param cardId
	 *          the id of the {@link MtgCard}
	 * @return a {@link MtgCard} filled with the info from the cache or null if the cache missed
	 */
	public MtgCard getCardFromCache(final int cardId)
	{
		Statement stmt = null;
		try
		{
			stmt = this.connection.createStatement();
			StringBuffer sql = new StringBuffer("select * from MTGCARD where `idProduct` = '");
			sql.append(cardId).append("';");

			ResultSet queryResult = stmt.executeQuery(sql.toString());

			if (queryResult.next())
			{
				return convertQueryResultToMtgCard(queryResult);
			}
		}
		catch (SQLException e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Error during reading cache for id: ", Integer.toString(cardId));
		}
		finally
		{
			if (stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (SQLException e)
				{
					LoggingHelper.logException(LogLevel.Error, e, "Unable to close database statement.");
				}
			}
		}

		// cache miss
		return null;
	}

	/**
	 * Retrieves all cards from the database cache
	 * 
	 * @return
	 */
	public List<MtgCard> getAllCardsFromCache()
	{
		List<MtgCard> retVal = new ArrayList<>();

		Statement stmt = null;

		try
		{
			stmt = this.connection.createStatement();

			StringBuffer sql = new StringBuffer("select * from MTGCARD;");

			ResultSet queryResult = stmt.executeQuery(sql.toString());

			while (queryResult.next())
			{
				retVal.add(convertQueryResultToMtgCard(queryResult));
			}
		}
		catch (SQLException ex)
		{
			LoggingHelper.logException(LogLevel.Error, ex, "Error during reading all cards from cache.");
		}
		finally
		{
			if (stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (SQLException e)
				{
					LoggingHelper.logException(LogLevel.Error, e, "Unable to close database statement.");
				}
			}
		}

		return retVal;
	}

	/**
	 * Converts a result set to a {@link MtgCard}
	 * 
	 * @param queryResult
	 *          the result from the db
	 * @return the mtg card with all set values
	 * @throws SQLException
	 *           anything going wrong during accessing fields
	 */
	protected MtgCard convertQueryResultToMtgCard(final ResultSet queryResult) throws SQLException
	{
		int cardNumber = queryResult.getInt(COL_PRODUCT_ID);
		String edition = queryResult.getString(COL_EXPANSION);
		String name = queryResult.getString(COL_NAME_ENGLISH);
		int colNumber = queryResult.getInt(COL_COLLECTORS_NUMBER);
		String rareStr = queryResult.getString(COL_RARITY);
		Rarity rarity = Rarity.None;
		if (rareStr != null)
			rarity = Rarity.getRarityForAbbreviation(rareStr);

		MtgCard retVal = new MtgCard(cardNumber, edition, name, colNumber, rarity);

		// construct names with fallback to english
		retVal.setAllNames(new MultilingualName());
		retVal.getAllNames().addName(Language.English, queryResult.getString(COL_NAME_ENGLISH));
		retVal.getAllNames().addName(Language.German, queryResult.getString(COL_NAME_GERMAN));
		if (retVal.getAllNames().getNameForLang(Language.German.getLanguageName()) == null)
			retVal.getAllNames().addName(Language.German, retVal.getAllNames().getNameForLang(Language.English.getLanguageName()));
		retVal.getAllNames().addName(Language.French, queryResult.getString(COL_NAME_FRENCH));
		if (retVal.getAllNames().getNameForLang(Language.French.getLanguageName()) == null)
			retVal.getAllNames().addName(Language.French, retVal.getAllNames().getNameForLang(Language.English.getLanguageName()));
		retVal.getAllNames().addName(Language.Italian, queryResult.getString(COL_NAME_ITALIAN));
		if (retVal.getAllNames().getNameForLang(Language.Italian.getLanguageName()) == null)
			retVal.getAllNames().addName(Language.Italian, retVal.getAllNames().getNameForLang(Language.English.getLanguageName()));
		retVal.getAllNames().addName(Language.Spanish, queryResult.getString(COL_NAME_SPANISH));
		if (retVal.getAllNames().getNameForLang(Language.Spanish.getLanguageName()) == null)
			retVal.getAllNames().addName(Language.Spanish, retVal.getAllNames().getNameForLang(Language.English.getLanguageName()));

		retVal.setPriceInfo(new ProductPrice());
		retVal.getPriceInfo().setAvg(queryResult.getDouble(COL_PRICE_AVG));
		retVal.getPriceInfo().setLow(queryResult.getDouble(COL_PRICE_LOW));
		retVal.getPriceInfo().setLowEx(queryResult.getDouble(COL_PRICE_LOWEX));
		retVal.getPriceInfo().setLowFoil(queryResult.getDouble(COL_PRICE_LOWFOIL));
		retVal.getPriceInfo().setSell(queryResult.getDouble(COL_PRICE_SELL));
		retVal.getPriceInfo().setTrend(queryResult.getDouble(COL_PRICE_TREND));

		retVal.setCreatedAt(getDate(queryResult, COL_CREATED_AT));
		retVal.setUpdatedAt(getDate(queryResult, COL_UPDATED_AT));

		// TODO: category?

		return retVal;
	}

	/**
	 * Parses a timestamp stored with {@link #TIMESTAMP_FORMAT}
	 * 
	 * @param queryResult
	 *          the query result containing the timestamp to be parsed
	 * @param columnName
	 *          the name of the column to be extracted
	 * @return a Calendar instance with the time matching the stored value
	 * @throws SQLException
	 *           anything going wrong during accessing the ResultSet
	 */
	private Calendar getDate(final ResultSet queryResult, final String columnName) throws SQLException
	{
		Calendar retVal = Calendar.getInstance();
		try
		{
			String timeString = queryResult.getString(columnName);
			if (timeString != null && !"".equals(timeString))
			{
				retVal.setTime(TIMESTAMP_FORMAT.parse(timeString));
			}
			else
			{
				// no time, set to 01.01.1970 (start of unix timestamp, as old as it's going to get)
				retVal.set(Calendar.YEAR, 1970);
				retVal.set(Calendar.MONTH, 0);
				retVal.set(Calendar.DAY_OF_MONTH, 1);
			}
		}
		catch (ParseException e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Unable to Parse date: ", queryResult.getString(columnName));
		}
		return retVal;
	}

	/**
	 * Updates the cache using the information from the cardXml
	 * 
	 * @param cardXml
	 *          the xml representing a {@link MtgCard}
	 */
	public void writeToCache(final String cardXml)
	{
		if (cardXml == null || cardXml.length() == 0)
		{
			LoggingHelper.logForLevel(LogLevel.Error, "Trying to write emtpy cache entry into local DB.");
			return;
		}

		Statement stmt = null;
		String sql = null;
		try
		{
			sql = convertXmlToDbInsert(cardXml);
			stmt = this.connection.createStatement();

			stmt.execute(sql);
		}
		catch (IOException e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Error during converting MtgCard to xml.");
		}
		catch (SQLException e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Error during inserting into local database: ", sql);
		}
		finally
		{
			if (stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (SQLException e)
				{
					LoggingHelper.logException(LogLevel.Error, e, "Unable to close statment after inserting to cache.");
				}
			}
		}
	}

	/**
	 * Writes the information about the passed card into the database cache
	 * 
	 * @param newerVersion
	 *          the most recent version of this card
	 */
	public void writeToCache(final MtgCard newerVersion)
	{
		if (newerVersion != null)
		{
			writeToCache(newerVersion.toResponseXml());
		}
	}

}
