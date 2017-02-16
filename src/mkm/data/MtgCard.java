/**
 * 
 */
package mkm.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mkm.XmlConstants;
import mkm.cache.MkmLocalDatabaseCache;
import mkm.data.multilingual.Language;
import mkm.data.multilingual.MultilingualName;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.parser.XmlParser;

/**
 *
 *
 * @author Kenny
 * @since 13.12.2015
 */
public class MtgCard implements Comparable<MtgCard>
{
	public static final String PATH_PRODUCT_ID = XmlConstants.getString("MtgCard.idProduct");

	public static final String PATH_CARD_EXPANSION = XmlConstants.getString("MtgCard.expansion");

	public static final String PATH_CARD_NAME = XmlConstants.getString("MtgCard.cardname");

	public static final String PATH_CARD_COLLECTORS_NUMBER = XmlConstants.getString("MtgCard.collectorsNumber");

	public static final String PATH_CARD_RARITY = XmlConstants.getString("MtgCard.rarity");

	public static final String PATH_CARD_IMAGE = XmlConstants.getString("MtgCard.image");

	public static final String PATH_CARD_PRICE = XmlConstants.getString("MtgCard.price");

	/**
	 * 1 == english
	 */
	protected static final String DEFAULT_LANG_CODE = "1";

	/**
	 * name of the language tag<br>
	 * default = idLanguage
	 */
	protected static final String XML_ID_LANGUAGE = XmlConstants.getString("MtgCard.languageId");

	/**
	 * name of the languageName tag<br>
	 * default = languageName
	 */
	protected static final String XML_LANGUAGENAME = XmlConstants.getString("MtgCard.languageName");

	/**
	 * name of the productName tag<br>
	 * default = productName
	 */
	protected static final String XML_PRODUCT_NAME = XmlConstants.getString("MtgCard.productName");

	public static final int DEFAULT_COLLECTORS_NUMBER = 1111;

	// ##########################################################################
	// ################################# FIELDS #################################
	// ##########################################################################

	private String edition, name;

	private Language language;

	private String imageLocation;

	private MultilingualName allNames = null;

	private int cardId, collectorsNumber, gameId, metaproductId;

	private Rarity rarity;

	private ProductPrice priceInfo;

	private Calendar createdAt, updatedAt;

	/**
	 * Constructs a card from a response/product/prouctId.xml response
	 * 
	 * @param cardXml
	 *          the xml returned by the MKM server for a given cardID
	 * @param isOtherProduct
	 *          true if the product is no magic card per se, but something like a booster / life counter
	 * @param productId
	 *          the id of the current product (for logging errors)
	 * @throws IOException
	 */
	public MtgCard(final String cardXml, final boolean isOtherProduct, final int productId) throws IOException
	{
		XmlParser cardParser = new XmlParser(cardXml);
		List<Node> nodeList = new ArrayList<>();
		try
		{
			cardParser.catalogNodes();

			nodeList = cardParser.getNodesForPath(PATH_PRODUCT_ID);
		}
		catch (IOException ex)
		{
			LoggingHelper.logException(LogLevel.Error, "Error during parsing of the xml of productId: " + productId,
					". Possibly because of special characters. You could open the file " + productId
							+ "_response.xml and remove all special chars manually to prevent this error from occuring again (for this file)");

			LoggingHelper.logException(LogLevel.Info, ex);

			throw ex;
		}

		if (nodeList.isEmpty())
		{
			// fallback try with productNode
			nodeList = cardParser.getNodeCatalog().get("product");
			if (nodeList.size() == 1)
				init(nodeList.get(0));
		}

		else
		{
			if (isSingleEntry(nodeList, "productId", cardXml, isOtherProduct))
			{
				this.setCardId(Integer.parseInt(XmlParser.getNodeTextValue(nodeList.get(0))));
			}

			nodeList = cardParser.getNodesForPath(PATH_CARD_EXPANSION);
			if (isSingleEntry(nodeList, "expansion", cardXml, isOtherProduct))
			{
				this.setEdition(XmlParser.getNodeTextValue(nodeList.get(0)));
			}

			nodeList = cardParser.getNodesForPath(PATH_CARD_NAME);
			for (Node n : nodeList)
			{
				String productName = null;
				boolean isEnglish = false;
				NodeList nl = n.getChildNodes();
				String nonEnglishLanguage = null;
				for (int i = 0; i < nl.getLength(); i++)
				{
					Node tmpChild = nl.item(i);
					String nodeValue = XmlParser.getNodeTextValue(tmpChild);
					if (XML_ID_LANGUAGE.equals(tmpChild.getNodeName()) && Integer.toString(Language.English.getIntValue()).equals(nodeValue))
					{
						isEnglish = true;
					}
					else if (XML_LANGUAGENAME.equals(tmpChild.getNodeName()) && Language.English.equals(nodeValue))
					{
						isEnglish = true;
					}
					else if (XML_LANGUAGENAME.equals(tmpChild.getNodeName()))
					{
						nonEnglishLanguage = nodeValue;
					}
					else if (XML_PRODUCT_NAME.equals(tmpChild.getNodeName()))
					{
						productName = nodeValue;
					}

					if (this.allNames == null)
						this.allNames = new MultilingualName();

					if (isEnglish && productName != null)
					{
						this.setName(productName);
						this.setLanguage(Language.English);

						this.allNames.addName(Language.English, productName);
					}
					else if (nonEnglishLanguage != null && productName != null)
					{
						this.allNames.addName(nonEnglishLanguage, productName);
					}
				}
			}

			nodeList = cardParser.getNodesForPath(PATH_CARD_COLLECTORS_NUMBER);
			if (isSingleEntry(nodeList, "collectorsNumber", cardXml, isOtherProduct))
			{
				if (XmlParser.getNodeTextValue(nodeList.get(0)) != null)
				{
					try
					{
						this.setCollectorsNumber(Integer.parseInt(XmlParser.getNodeTextValue(nodeList.get(0))));
					}
					catch (NumberFormatException ex)
					{
						this.setCollectorsNumber(DEFAULT_COLLECTORS_NUMBER);
					}
				}
				else
					this.setCollectorsNumber(DEFAULT_COLLECTORS_NUMBER);
			}

			nodeList = cardParser.getNodesForPath(PATH_CARD_RARITY);
			if (isSingleEntry(nodeList, "Rarity", cardXml, isOtherProduct))
			{
				this.setRarity(Rarity.getRarityForAbbreviation(XmlParser.getNodeTextValue(nodeList.get(0))));
			}
			else
			{
				this.setRarity(Rarity.None);
			}

			nodeList = cardParser.getNodesForPath(PATH_CARD_IMAGE);
			if (isSingleEntry(nodeList, "Image ", cardXml, true))
			{
				this.setImageLocation(XmlParser.getNodeTextValue(nodeList.get(0)));
			}

			nodeList = cardParser.getNodesForPath(PATH_CARD_PRICE);
			if (isSingleEntry(nodeList, "Price ", cardXml, true))
			{
				parsePriceGuide(nodeList.get(0));
			}
		}
	}

	public MtgCard(final Node productNode, final boolean isOtherProduct, final int productId)
	{
		init(productNode);
	}

	private void init(final Node productNode)
	{
		if (productNode == null)
		{
			throw new IllegalArgumentException("Product node is null!");
		}
		if (!"product".equals(productNode.getNodeName()))
		{
			throw new IllegalArgumentException("Only call this constructor with a Node with nodeName product");
		}

		Node currentChild = null;
		for (int i = 0; i < productNode.getChildNodes().getLength(); i++)
		{
			currentChild = productNode.getChildNodes().item(i);

			if (currentChild.getNodeType() == Node.TEXT_NODE)
				continue;

			if ("idProduct".equals(currentChild.getNodeName()))
			{
				this.setCardId(Integer.parseInt(XmlParser.getNodeTextValue(currentChild)));
			}
			else if ("idMetaproduct".equals(currentChild.getNodeName()))
			{
				this.setMetaproductId(Integer.parseInt(XmlParser.getNodeTextValue(currentChild)));
			}
			else if ("idGame".equals(currentChild.getNodeName()))
			{
				this.setGameId(Integer.parseInt(XmlParser.getNodeTextValue(currentChild)));
			}
			else if ("image".equals(currentChild.getNodeName()))
			{
				this.setImageLocation(XmlParser.getNodeTextValue(currentChild));
			}
			else if ("priceGuide".equals(currentChild.getNodeName()))
			{
				parsePriceGuide(currentChild);
			}
			else if ("expansion".equals(currentChild.getNodeName()))
			{
				this.setEdition(XmlParser.getNodeTextValue(currentChild));
			}
			else if ("number".equals(currentChild.getNodeName()))
			{
				String number = XmlParser.getNodeTextValue(currentChild);
				if (number != null && !"null".equals(number))
				{
					try
					{
						this.setCollectorsNumber(Integer.parseInt(number));
					}
					catch (NumberFormatException ex)
					{
						LoggingHelper.logForLevel(LogLevel.Error, "Invalid collectors number in ", Integer.toString(getCardId()), ": ", number);
					}
				}
			}
			else if ("rarity".equals(currentChild.getNodeName()))
			{
				this.setRarity(Rarity.getRarityForAbbreviation(XmlParser.getNodeTextValue(currentChild)));
			}
			else if ("name".equals(currentChild.getNodeName()) && this.allNames == null)
			{
				parseLanguages(currentChild);
			}
		}
	}

	/**
	 * Constructs a MtgCard without the use of the XmlParser (if the parsing threw any SAXParseException for example)
	 * 
	 * @param singleCardXml
	 *          the xml representing one card
	 */
	public MtgCard(final String singleCardXml)
	{
		String nodeValue = getTagContent("idProduct", singleCardXml);
		if (nodeValue != null)
		{
			this.setCardId(Integer.parseInt(nodeValue));
		}

		nodeValue = getTagContent("idMetaproduct", singleCardXml);
		if (nodeValue != null)
		{
			this.setMetaproductId(Integer.parseInt(nodeValue));
		}

		nodeValue = getTagContent("idGame", singleCardXml);
		if (nodeValue != null)
		{
			this.setGameId(Integer.parseInt(nodeValue));
		}

		nodeValue = getTagContent("image", singleCardXml);
		if (nodeValue != null)
		{
			this.setImageLocation(nodeValue);
		}

		nodeValue = getTagContent("expansion", singleCardXml);
		if (nodeValue != null)
		{
			this.setEdition(nodeValue);
		}

		nodeValue = getTagContent("number", singleCardXml);
		if (nodeValue != null)
		{
			try
			{
				this.setCollectorsNumber(Integer.parseInt(nodeValue));
			}
			catch (NumberFormatException ex)
			{
				this.setCollectorsNumber(DEFAULT_COLLECTORS_NUMBER);
			}
		}

		nodeValue = getTagContent("rarity", singleCardXml);
		if (nodeValue != null)
		{
			this.setRarity(Rarity.getRarityForAbbreviation(nodeValue));
		}

		nodeValue = getTagContent("name", singleCardXml);
		String langName, nameInLang;
		int startPos = 0;
		this.allNames = new MultilingualName();
		while (nodeValue != null)
		{
			langName = getTagContent("languageName", nodeValue);

			// if we are parsing this card manually, one of the foreign names is full of special chars
			// only parse english, so the written cache file will be clean
			if (Language.English.getLanguageName().equals(langName))
			{
				nameInLang = getTagContent("productName", nodeValue);
				this.allNames.addName(langName, nameInLang);

				this.setName(nameInLang);
				this.setLanguage(Language.English);
			}

			// get next lang
			startPos = singleCardXml.indexOf("<name>", startPos) + nodeValue.length() + 1;
			nodeValue = getTagContent("name", singleCardXml, startPos);
		}

		nodeValue = getTagContent("priceGuide", singleCardXml);
		if (nodeValue != null)
		{
			this.priceInfo = new ProductPrice();

			this.priceInfo.setSell(Double.parseDouble(getTagContent("SELL", nodeValue)));
			this.priceInfo.setLow(Double.parseDouble(getTagContent("LOW", nodeValue)));
			this.priceInfo.setLowEx(Double.parseDouble(getTagContent("LOWEX", nodeValue)));
			this.priceInfo.setLowFoil(Double.parseDouble(getTagContent("LOWFOIL", nodeValue)));
			this.priceInfo.setAvg(Double.parseDouble(getTagContent("AVG", nodeValue)));
			this.priceInfo.setTrend(Double.parseDouble(getTagContent("TREND", nodeValue)));
		}

	}

	private String getTagContent(final String tag, final String singleCardXml)
	{
		return getTagContent(tag, singleCardXml, 0);
	}

	/**
	 * retrieves the string between &lt;tag&gt; and &lt;/tag&gt;
	 */
	private String getTagContent(final String tag, final String singleCardXml, final int startPos)
	{
		String openTag = new StringBuilder(tag.length() + 2).append('<').append(tag).append('>').toString();
		String closeTag = new StringBuilder(tag.length() + 3).append("</").append(tag).append('>').toString();

		int start = singleCardXml.indexOf(openTag, startPos);
		int end = singleCardXml.indexOf(closeTag, start);

		if (start > -1 && end > -1)
		{
			start += openTag.length();
			return singleCardXml.substring(start, end);
		}
		else
		{
			// System.out.println("Could not find value for: " + openTag);
			return null;
		}
	}

	/**
	 * Parses the price info
	 * 
	 * @param priceGuideNode
	 *          the price guide node
	 */
	private void parsePriceGuide(final Node priceGuideNode)
	{
		this.priceInfo = new ProductPrice(priceGuideNode);
	}

	private void parseLanguages(final Node n)
	{
		this.allNames = new MultilingualName();
		boolean isEnglish = false;
		String productName = "";
		NodeList nl = n.getChildNodes();
		String nonEnglishLanguage = null;
		for (int i = 0; i < nl.getLength(); i++)
		{
			Node tmpChild = nl.item(i);
			String nodeValue = XmlParser.getNodeTextValue(tmpChild);
			if (XML_ID_LANGUAGE.equals(tmpChild.getNodeName()) && Integer.toString(Language.English.getIntValue()).equals(nodeValue))
			{
				isEnglish = true;
			}
			else if (XML_LANGUAGENAME.equals(tmpChild.getNodeName()) && Language.English.equals(nodeValue))
			{
				isEnglish = true;
			}
			else if (XML_LANGUAGENAME.equals(tmpChild.getNodeName()))
			{
				nonEnglishLanguage = nodeValue;
			}
			else if (XML_PRODUCT_NAME.equals(tmpChild.getNodeName()))
			{
				productName = nodeValue;
			}

			if (isEnglish && productName != null)
			{
				this.setName(productName);
				this.setLanguage(Language.English);
				this.allNames.addName(Language.English, productName);
			}
			else if (nonEnglishLanguage != null && productName != null)
			{
				this.allNames.addName(nonEnglishLanguage, productName);
			}
		}
	}

	/**
	 * Checks if a node list only has one entry
	 * 
	 * @param nodeList
	 *          the list to check
	 * @param attributeName
	 *          the attribute for logging errors
	 * @param cardXml
	 *          the complete xml for logging
	 * @param shouldLogError
	 *          should errors be written to {@link LoggingHelper} or not
	 * @return true if only one element in the list, false otherwise
	 */
	private static boolean isSingleEntry(final List<Node> nodeList, final String attributeName, final String cardXml, final boolean shouldLogError)
	{
		boolean retVal = true;
		if (nodeList == null || nodeList.isEmpty())
		{
			if (shouldLogError)
			{
				LoggingHelper.logException(LogLevel.Error, "Could not find ", attributeName, " for: ", cardXml);
			}
			retVal = false;
		}
		else if (nodeList.size() > 1)
		{
			if (shouldLogError)
			{
				LoggingHelper.logException(LogLevel.Error, "Found more than one ", attributeName, " for: ", cardXml);
			}
			retVal = false;
		}
		return retVal;
	}

	/**
	 * Constructs a new card
	 * 
	 * @param edition
	 * @param name
	 * @param collectorsNumber
	 * @param rarity
	 */
	public MtgCard(final int cardId, final String edition, final String name, final int collectorsNumber, final Rarity rarity)
	{
		super();
		this.cardId = cardId;
		this.edition = edition;
		this.name = name;
		this.collectorsNumber = collectorsNumber;
		this.rarity = rarity;
	}

	/**
	 * @return the edition
	 */
	public String getEdition()
	{
		return edition;
	}

	/**
	 * @param edition
	 *          the edition to set
	 */
	public void setEdition(final String edition)
	{
		this.edition = edition;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * @return the collectorsNumber
	 */
	public int getCollectorsNumber()
	{
		return collectorsNumber;
	}

	/**
	 * @param collectorsNumber
	 *          the collectorsNumber to set
	 */
	public void setCollectorsNumber(final int collectorsNumber)
	{
		this.collectorsNumber = collectorsNumber;
	}

	/**
	 * @return the rarity
	 */
	public Rarity getRarity()
	{
		return rarity;
	}

	/**
	 * @param rarity
	 *          the rarity to set
	 */
	public void setRarity(final Rarity rarity)
	{
		this.rarity = rarity;
	}

	/**
	 * @return the cardId
	 */
	public int getCardId()
	{
		return cardId;
	}

	/**
	 * @param cardId
	 *          the cardId to set
	 */
	public void setCardId(final int cardId)
	{
		this.cardId = cardId;
	}

	/**
	 * @return the imageLocation
	 */
	public String getImageLocation()
	{
		return imageLocation;
	}

	/**
	 * @param imageLocation
	 *          the imageLocation to set
	 */
	public void setImageLocation(final String imageLocation)
	{
		this.imageLocation = imageLocation;
	}

	/**
	 * @return the allNames
	 */
	public MultilingualName getAllNames()
	{
		return allNames;
	}

	/**
	 * @param allNames
	 *          the allNames to set
	 */
	public void setAllNames(final MultilingualName allNames)
	{
		this.allNames = allNames;
	}

	/**
	 * @return the gameId
	 */
	public int getGameId()
	{
		return gameId;
	}

	/**
	 * @param gameId
	 *          the gameId to set
	 */
	public void setGameId(final int gameId)
	{
		this.gameId = gameId;
	}

	/**
	 * @return the metaproductId
	 */
	public int getMetaproductId()
	{
		return metaproductId;
	}

	/**
	 * @param metaproductId
	 *          the metaproductId to set
	 */
	public void setMetaproductId(final int metaproductId)
	{
		this.metaproductId = metaproductId;
	}

	/**
	 * @return the priceInfo
	 */
	public ProductPrice getPriceInfo()
	{
		return priceInfo;
	}

	/**
	 * @param priceInfo
	 *          the priceInfo to set
	 */
	public void setPriceInfo(final ProductPrice priceInfo)
	{
		this.priceInfo = priceInfo;
	}

	/**
	 * @return the language
	 */
	public Language getLanguage()
	{
		return language;
	}

	/**
	 * @param language
	 *          the language to set
	 */
	public void setLanguage(final Language language)
	{
		this.language = language;
	}

	/**
	 * @return the createdAt
	 */
	public Calendar getCreatedAt()
	{
		return createdAt;
	}

	/**
	 * @param createdAt
	 *          the createdAt to set
	 */
	public void setCreatedAt(final Calendar createdAt)
	{
		this.createdAt = createdAt;
	}

	/**
	 * @return the updatedAt
	 */
	public Calendar getUpdatedAt()
	{
		return updatedAt;
	}

	/**
	 * @param updatedAt
	 *          the updatedAt to set
	 */
	public void setUpdatedAt(final Calendar updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final MtgCard o)
	{
		int retVal = 0;

		/*
		 * sorty Edition > rarity > collectorsNumber > Name > fallback (ID)
		 */
		if (this.getEdition() != null && o.getEdition() != null)
		{
			retVal = this.getEdition().compareTo(o.getEdition());
		}

		if (retVal == 0)
		{
			if (this.getRarity() != null && o.getRarity() != null)
				// sort descending, so other - this
				retVal = o.getRarity().getRarityValue() - this.getRarity().getRarityValue();
			else if (this.getRarity() != null)
				retVal = 1;
			else if (o.getRarity() != null)
				retVal = -1;
		}

		if (retVal == 0)
		{
			retVal = this.getCollectorsNumber() - o.getCollectorsNumber();
		}

		if (retVal == 0)
		{
			retVal = this.getName().compareTo(o.getName());
		}

		if (retVal == 0)
		{
			retVal = getCardId() - o.getCardId();
		}

		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer retVal = new StringBuffer();

		retVal.append(this.getName());
		retVal.append(" - ");
		retVal.append(this.getEdition());
		retVal.append(" - ");
		retVal.append(this.getCollectorsNumber());
		retVal.append(" - ");
		retVal.append(this.getRarity());
		retVal.append(" - ");
		retVal.append(this.getLanguage().getLanguageName());

		return retVal.toString();
	}

	public String toResponseXml()
	{
		StringBuilder retVal = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		retVal.append("<response>\n");
		retVal.append("	<product>\n");

		appendTag("idProduct", this.getCardId(), retVal, 2);
		appendTag("idMetaproduct", this.getMetaproductId(), retVal, 2);
		appendTag("idGame", this.gameId, retVal, 2);
		appendTag("image", this.getImageLocation(), retVal, 2);
		appendTag("number", this.getCollectorsNumber(), retVal, 2);
		if (this.getRarity() != null)
			appendTag("rarity", this.getRarity().getAbbreviation(), retVal, 2);
		else
			appendTag("rarity", Rarity.None.getAbbreviation(), retVal, 2);

		for (Language lang : this.allNames.getLanguageSet())
		{
			retVal.append("\t\t<name>\n");
			appendTag("idLanguage", lang.getIntValue(), retVal, 3);
			appendTag("languageName", lang.getLanguageName(), retVal, 3);
			appendTag("productName", this.allNames.getNameForLang(lang.getLanguageName()), retVal, 3);
			retVal.append("\t\t</name>\n");
		}

		retVal.append(this.priceInfo.toResponseXml("\t\t")).append('\n');

		if (this.getCreatedAt() == null)
		{
			this.createdAt = Calendar.getInstance();
		}
		appendTag("createdAt", MkmLocalDatabaseCache.TIMESTAMP_FORMAT.format(this.createdAt), retVal, 2);

		this.updatedAt = Calendar.getInstance();
		appendTag("updatedAt", MkmLocalDatabaseCache.TIMESTAMP_FORMAT.format(this.updatedAt), retVal, 2);

		retVal.append("	</product>\n");
		retVal.append("</response>");

		return retVal.toString();
	}

	private void appendTag(final String tagName, final int value, final StringBuilder retVal, final int tabCount)
	{
		this.appendTag(tagName, Integer.toString(value), retVal, tabCount);
	}

	private void appendTag(final String tagName, final String value, final StringBuilder retVal, final int tabCount)
	{
		for (int i = 0; i < tabCount; i++)
			retVal.append('\t');

		retVal.append('<').append(tagName).append('>').append(value).append("</").append(tagName).append(">\n");
	}

}