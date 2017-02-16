/**
 * 
 */
package mkm.connect;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import mkm.cache.MkmCache;
import mkm.cache.MkmLocalDatabaseCache;
import mkm.data.MtgCard;
import mkm.data.multilingual.Language;
import mkm.exception.CertRefreshNeededException;
import mkm.inserter.MkmInserter;
import mkm.install.ConfigureCertInstaller;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.parser.XmlParser;

/**
 *
 *
 * @author Kenny
 * @since 12.01.2016
 */
public class MkmServiceHandler
{

	private final MkmConnector connector;

	private final MkmLocalDatabaseCache dbCache;

	/**
	 * 
	 */
	public MkmServiceHandler(final MkmConnector con)
	{
		this.connector = con;
		this.dbCache = new MkmLocalDatabaseCache();
	}

	/**
	 * Checks if a given request is a getProductForId request
	 * 
	 * @param personalizedParams
	 *          the personalized params string with all replacements
	 * @return true if it is a getProductForId request, false otherwise
	 */
	public static boolean isGetCardForIdRequest(final String personalizedParams)
	{
		return (personalizedParams == null || personalizedParams.length() < 9) ? false : personalizedParams.startsWith("/product/");
	}

	/**
	 * Retrieves an instance of a {@link MtgCard} from either the cache or the server for the given productId
	 * 
	 * @param cardId
	 *          the productId corresponding to the {@link MtgCard}
	 * @param ignoreCache
	 *          true if no cache should be checked
	 * @return an instance of the {@link MtgCard} with the given id
	 */
	public MtgCard getCardForId(final int cardId, final boolean ignoreCache)
	{
		if (!ignoreCache)
		{
			MtgCard retVal = this.dbCache.getCardFromCache(cardId);

			if (retVal != null)
				return retVal;
		}

		try
		{
			String xml = this.connector.performGetRequest(MkmConnector.GET_PRODUCT_FOR_ID, new String[] { ":id" }, new String[] { Integer.toString(cardId) },
					ignoreCache, false);

			return new MtgCard(xml);

		}
		catch (CertRefreshNeededException e)
		{
			// the certificate is not up to date, print error and exit
			LoggingHelper.logForLevel(LogLevel.None, "MKM Certificate is not registered at your current java version. Run install_cert.bat");
			try
			{
				ConfigureCertInstaller.udpateInstallFile();
			}
			catch (IOException e1)
			{
				LoggingHelper.logException(LogLevel.Critical, e1, "Could not update install_cert.bat");
			}
			System.exit(-1);
		}
		catch (IOException e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Error during contacting MKM");
		}
		return null;
	}

	/**
	 * Searches for the given cardName in the given language. IsExcact specifies if the card name must be an exact match for the server data
	 * 
	 * @param cardName
	 *          the name to search for
	 * @param lang
	 *          the language in which to search for
	 * @param isExact
	 *          true if the cardName must be an exact match, false if partial is enough
	 * @param parent
	 *          the caller to get the cache updated
	 * @throws IOException
	 *           anything goes wrong during contacting the server or reading the cache
	 */
	public void performNameSearch(final String cardName, final Language lang, final boolean isExact, MkmInserter parent) throws IOException
	{
		try
		{
			// create dummy if param is null
			if (parent == null)
			{
				parent = new MkmInserter(connector);
			}

			// products/:name/1/:idLanguage/:isExact[/:start]
			String responseXml = null;
			try
			{
				responseXml = connector.performGetRequest(MkmConnector.GET_PRODUCT_FOR_NAME, //
						new String[] { ":name", ":idLanguage", ":isExact" }, //
						new String[] { MkmConnector.encodeUTF8(cardName), Integer.toString(lang.getIntValue()), Boolean.toString(isExact) }, true, false);
			}
			catch (CertRefreshNeededException ex)
			{
				// the certificate is not up to date, print error and exit
				LoggingHelper.logForLevel(LogLevel.None, "MKM Certificate is not registered at your current java version. Run install_cert.bat");
				System.exit(0);
			}

			XmlParser parser = new XmlParser(responseXml);

			// List<Node> nodeList = parser.getNodesForPath("response/article/idProduct");
			List<Node> nodeList = null;
			try
			{
				nodeList = parser.getNodesForPath("response/product");
			}
			catch (IOException ex)
			{
				if (ex.getCause() != null && ex.getCause() instanceof SAXParseException)
				{
					handleResponseManually(responseXml, parent);
					parent.rebuildNameBuffer();
					return;
				}
				else
				{
					LoggingHelper.logException(LogLevel.Critical, ex, "Unable to parse the server response for the card ", cardName, " returned xml: ", responseXml);
					throw ex;
				}
			}

			if (nodeList != null && !nodeList.isEmpty())
			{
				for (int i = 0; i < nodeList.size(); i++)
				{
					try
					{
						// MtgCard card = new MtgCard(cardXml, false, -1);

						MtgCard card = null;
						try
						{
							card = new MtgCard(nodeList.get(i), false, -1);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}

						if (card.getName() == null)
						{
							LoggingHelper.logNode(LogLevel.Error, nodeList.get(i), "Unable to create card out of node: ");
							continue;
						}

						addCardToCache(card, XmlParser.getNodeAsString(nodeList.get(i)), parent);

					}
					catch (Exception e)
					{
						// LoggingHelper.logException(LogLevel.Error, e, "Unable to parse card xml: ", cardXml);
						continue;
					}
				}

				// after adding rebuild the name list
				parent.rebuildNameBuffer();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	/**
	 * Adds a cache entry for the given card and updates the parent
	 * 
	 * @param toAdd
	 *          the card to add
	 * @param productNode
	 *          the raw node
	 * @param parent
	 *          the parent to get the list updated
	 */
	protected void addCardToCache(final MtgCard toAdd, final String cardXml, final MkmInserter parent)
	{
		parent.addMtgCardToBuffer(toAdd);

		// add card to cache
		String personalizedString = MkmConnector.getPersonalizedParams(toAdd);

		// insert a response tag to match the other responses
		StringBuilder sb = new StringBuilder(cardXml.length() + 25);
		// 38 == "<?xml version='1.0' encoding='UTF-8'?>".length()
		sb.append(cardXml.substring(0, 38));
		sb.append("<response>");
		sb.append(cardXml.substring(38));
		sb.append("</response>");

		String xmlToWrite = XmlParser.stripNonValidXMLCharacters(sb.toString());

		// re-write cache file because current data is more up to date
		MkmCache.writeToCache(personalizedString, xmlToWrite);
	}

	/**
	 * This method handles the parsing of the response xml of the name search manually in case of sax parse errors to at least get some information
	 * 
	 * @param parent
	 */
	protected void handleResponseManually(final String responseXml, final MkmInserter parent)
	{
		int startIndex = responseXml.indexOf("<product>");
		int endIndex = responseXml.indexOf("</product>", startIndex);

		while (startIndex > -1 && endIndex > -1)
		{
			String singleCardXml = responseXml.substring(startIndex, endIndex);

			MtgCard card = new MtgCard(singleCardXml);
			addCardToCache(card, card.toResponseXml(), parent);

			startIndex = responseXml.indexOf("<product>", endIndex);
			endIndex = responseXml.indexOf("</product>", startIndex);
		}
	}

}
