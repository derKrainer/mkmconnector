package mkm.data;

import java.io.IOException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import mkm.XmlConstants;
import mkm.connect.MkmConnector;
import mkm.connect.MkmServiceHandler;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.parser.XmlParser;

/**
 *
 *
 * @author Kenny
 * @since 13.12.2015
 */
public class Article implements Comparable<Article>, Cloneable
{
	public static final String CSV_SEPERATOR = ";";

	protected static final String XML_IS_FOIL = XmlConstants.getString("Article.isFoil");

	protected static final String XML_CONDITION = XmlConstants.getString("Article.condition");

	protected static final String XML_PRODUCT_ID = XmlConstants.getString("Article.productId");

	protected static final String XML_METAPRODUCT_ID = XmlConstants.getString("Article.metaproductId");

	protected static final String XML_ARTICLE_ID = XmlConstants.getString("Article.articleId");

	protected static final String XML_COUNT = XmlConstants.getString("Article.count");

	protected static final String XML_PRICE = XmlConstants.getString("Article.price");

	protected static final String XML_LANG = XmlConstants.getString("Article.language");

	protected static final String XML_LANG_NAME = XmlConstants.getString("Article.languageName");

	protected static final String XML_LANG_ID = XmlConstants.getString("Article.languageId");

	private long articleId = -1, metaproductId = -1;

	private Condition condition;

	private boolean foil, signed;

	private MtgCard product;

	private int amount, productId, languageId;

	private String languageName;

	private double price;

	public Article()
	{
		// default constructor
	}

	public Article(final Node articleNode, final MkmConnector parent) throws Exception
	{
		Node child = null;
		for (int i = 0; i < articleNode.getChildNodes().getLength(); i++)
		{
			child = articleNode.getChildNodes().item(i);

			if (XML_IS_FOIL.equals(child.getNodeName()))
			{
				if ("true".equals(XmlParser.getNodeTextValue(child)))
				{
					this.setFoil(true);
				}
			}
			else if (XML_CONDITION.equals(child.getNodeName()))
			{
				this.setCondition(Condition.getConditionForAbbreviation(XmlParser.getNodeTextValue(child)));
			}
			else if (XML_PRODUCT_ID.equals(child.getNodeName()))
			{
				this.productId = Integer.parseInt(XmlParser.getNodeTextValue(child));
			}
			else if (XML_METAPRODUCT_ID.equals(child.getNodeName()))
			{
				this.metaproductId = Integer.parseInt(XmlParser.getNodeTextValue(child));
			}
			else if (XML_ARTICLE_ID.equals(child.getNodeName()))
			{
				this.setArticleId(Long.parseLong(XmlParser.getNodeTextValue(child)));
			}
			else if (XML_COUNT.equals(child.getNodeName()))
			{
				this.setAmount(Integer.parseInt(XmlParser.getNodeTextValue(child)));
			}
			else if (XML_PRICE.equals(child.getNodeName()))
			{
				this.setPrice(Double.parseDouble(XmlParser.getNodeTextValue(child)));
			}
			else if (XML_LANG.equals(child.getNodeName()))
			{
				NodeList langNodes = child.getChildNodes();
				for (int langIter = 0; langIter < langNodes.getLength(); langIter++)
				{
					if (XML_LANG_NAME.equals(langNodes.item(langIter).getNodeName()))
					{
						this.languageName = XmlParser.getNodeTextValue(langNodes.item(langIter));
					}
					else if (XML_LANG_ID.equals(langNodes.item(langIter).getNodeName()))
					{
						this.languageId = Integer.parseInt(XmlParser.getNodeTextValue(langNodes.item(langIter)));
					}
				}
			}
		}
		// after everything is done, read the card
		// metaproductId == 0 means, that the product is no card (eg: booster / life counter / dice ... )

		MtgCard product = new MkmServiceHandler(parent).getCardForId(productId, false);
		if (product != null)
		{
			this.setProduct(product);
		}
		else
		{
			String cardXml = parent.performGetRequest(MkmConnector.GET_PRODUCT_FOR_ID, ":id", Integer.toString(productId));
			try
			{
				this.setProduct(new MtgCard(cardXml, metaproductId == 0, this.productId));
			}
			catch (IOException ex)
			{
				if (ex.getCause() instanceof SAXParseException)
				{
					this.setProduct(new MtgCard(cardXml));
				}
				else
				{
					throw ex;
				}
			}
		}

	}

	/**
	 * Retrieves a clone of the current article
	 * 
	 * @return a clone of the article
	 */
	public Article getClonedArticle()
	{
		try
		{
			return (Article) this.clone();
		}
		catch (CloneNotSupportedException e)
		{
			// should not happen as we implement cloneable
			LoggingHelper.logException(LogLevel.Fatal, e, "Unable to clone article");
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @return the quality
	 */
	public Condition getCondition()
	{
		return condition;
	}

	/**
	 * @param quality
	 *          the quality to set
	 */
	public void setCondition(final Condition quality)
	{
		this.condition = quality;
	}

	/**
	 * @return the foil
	 */
	public boolean isFoil()
	{
		return foil;
	}

	/**
	 * @param foil
	 *          the foil to set
	 */
	public void setFoil(final boolean foil)
	{
		this.foil = foil;
	}

	/**
	 * @return the articleId
	 */
	public long getArticleId()
	{
		return articleId;
	}

	/**
	 * @param articleId
	 *          the articleId to set
	 */
	public void setArticleId(final long articleId)
	{
		this.articleId = articleId;
	}

	/**
	 * @return the product
	 */
	public MtgCard getProduct()
	{
		return product;
	}

	/**
	 * @param product
	 *          the product to set
	 */
	public void setProduct(final MtgCard product)
	{
		this.product = product;
	}

	/**
	 * @return the languageName
	 */
	public String getLanguageName()
	{
		return languageName;
	}

	/**
	 * @param languageName
	 *          the languageName to set
	 */
	public void setLanguageName(final String languageName)
	{
		this.languageName = languageName;
	}

	/**
	 * @return the metaproductId
	 */
	public long getMetaproductId()
	{
		return metaproductId;
	}

	/**
	 * @param metaproductId
	 *          the metaproductId to set
	 */
	public void setMetaproductId(final long metaproductId)
	{
		this.metaproductId = metaproductId;
	}

	/**
	 * @return the languageId
	 */
	public int getLanguageId()
	{
		return languageId;
	}

	/**
	 * @param languageId
	 *          the languageId to set
	 */
	public void setLanguageId(final int languageId)
	{
		this.languageId = languageId;
	}

	/**
	 * @return the productId
	 */
	public int getProductId()
	{
		return productId;
	}

	/**
	 * @param productId
	 *          the productId to set
	 */
	public void setProductId(final int productId)
	{
		this.productId = productId;
	}

	/**
	 * @return the price
	 */
	public double getPrice()
	{
		return price;
	}

	/**
	 * @param price
	 *          the price to set
	 */
	public void setPrice(final double price)
	{
		this.price = price;
	}

	/**
	 * @return the amount
	 */
	public int getAmount()
	{
		return amount;
	}

	/**
	 * @param amount
	 *          the amount to set
	 */
	public void setAmount(final int amount)
	{
		this.amount = amount;
	}

	/**
	 * @return the signed
	 */
	public boolean isSigned()
	{
		return signed;
	}

	/**
	 * @param signed
	 *          the signed to set
	 */
	public void setSigned(final boolean signed)
	{
		this.signed = signed;
	}

	public String toCSV()
	{
		StringBuilder retVal = new StringBuilder();

		retVal.append(this.getProduct().getName()).append(CSV_SEPERATOR);
		retVal.append(this.getProduct().getCollectorsNumber()).append(CSV_SEPERATOR);
		retVal.append(this.getAmount()).append(CSV_SEPERATOR);
		retVal.append(this.getCondition()).append(CSV_SEPERATOR);
		retVal.append(this.getProduct().getRarity().toString()).append(CSV_SEPERATOR);
		retVal.append(Boolean.toString(this.isFoil())).append(CSV_SEPERATOR);
		retVal.append(this.getProduct().getEdition()).append(CSV_SEPERATOR);
		retVal.append(this.getLanguageName()).append(CSV_SEPERATOR);
		retVal.append(this.getPrice());

		return retVal.toString();
	}

	public String getHeaderCSV()
	{
		StringBuilder retVal = new StringBuilder();

		retVal.append("Name").append(CSV_SEPERATOR);
		retVal.append("Collectors Number").append(CSV_SEPERATOR);
		retVal.append("Amount").append(CSV_SEPERATOR);
		retVal.append("Condition").append(CSV_SEPERATOR);
		retVal.append("Rarity").append(CSV_SEPERATOR);
		retVal.append("Foil").append(CSV_SEPERATOR);
		retVal.append("Expansion").append(CSV_SEPERATOR);
		retVal.append("Language").append(CSV_SEPERATOR);
		retVal.append("Price");

		return retVal.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder retVal = new StringBuilder();
		retVal.append("Name: ");
		retVal.append(this.getProduct().getName()).append(CSV_SEPERATOR);
		retVal.append("\tCollectorsNumber: ");
		retVal.append(this.getProduct().getCollectorsNumber()).append(CSV_SEPERATOR);
		retVal.append("\tAmount: ");
		retVal.append(this.getAmount()).append(CSV_SEPERATOR);
		retVal.append("\tCondition: ");
		retVal.append(this.getCondition()).append(CSV_SEPERATOR);
		retVal.append("\tFoil: ");
		retVal.append(Boolean.toString(this.isFoil())).append(CSV_SEPERATOR);
		retVal.append("\tEdition: ");
		retVal.append(this.getProduct().getEdition());
		retVal.append("\tLanguage Name: ");
		retVal.append(this.getLanguageName());

		return retVal.toString();
	}

	/**
	 * request><br>
	 * article><br>
	 * idArticle (optional)<br/>
	 * idProduct>100569</idProduct> (optional)<br>
	 * idLanguage>1</idLanguage><br>
	 * comments>Inserted through the API</comments><br>
	 * count>1</count><br>
	 * price>4</price><br>
	 * condition>EX</condition><br>
	 * isFoil>true</isFoil><br>
	 * isSigned>false</isSigned><br>
	 * isPlayset>false</isPlayset><br>
	 * /article><br>
	 * /request><br>
	 */
	public String toSellPostXML(final boolean appendSurroundingTags, final boolean includeArticleId, final boolean includeProductId)
	{
		String newLine = "\n\t";

		StringBuffer retVal = new StringBuffer();

		if (appendSurroundingTags)
		{
			retVal.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			retVal.append("<request>").append(newLine);
		}

		newLine = "\n\t\t";
		retVal.append("<article>").append(newLine);

		if (includeArticleId)
		{
			retVal.append("<idArticle>");
			retVal.append(this.getArticleId());
			retVal.append("</idArticle>").append(newLine);
		}

		if (includeProductId)
		{
			retVal.append("<idProduct>");
			retVal.append(this.getProductId());
			retVal.append("</idProduct>").append(newLine);
		}

		retVal.append("<idLanguage>");
		retVal.append(this.getLanguageId());
		retVal.append("</idLanguage>").append(newLine);

		// retVal.append("<comments>");
		// retVal.append("inserted with mkmConnector");
		// retVal.append("</comments>").append(newLine);

		retVal.append("<count>");
		retVal.append(this.getAmount());
		retVal.append("</count>").append(newLine);

		retVal.append("<price>");
		retVal.append(this.getPrice());
		retVal.append("</price>").append(newLine);

		retVal.append("<condition>");
		retVal.append(this.getCondition().getAbbreviation());
		retVal.append("</condition>").append(newLine);

		retVal.append("<isFoil>");
		retVal.append(this.isFoil());
		retVal.append("</isFoil>").append(newLine);

		retVal.append("<isSigned>");
		retVal.append(this.isSigned());
		retVal.append("</isSigned>").append(newLine);

		newLine = "\n\t";
		retVal.append("<isPlayset>false</isPlayset>").append(newLine);
		retVal.append("</article>").append("\n");

		if (appendSurroundingTags)
		{
			retVal.append("</request>");
		}

		return retVal.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Article o)
	{
		int retVal = 0;

		if (this.getProduct() != null && o.getProduct() != null)
		{
			retVal = this.getProduct().compareTo(o.getProduct());
		}
		else if (this.getProduct() != null)
		{
			retVal = 1;
		}
		else if (o.getProduct() != null)
		{
			retVal = -1;
		}
		else
		{
			retVal = (int) (this.getMetaproductId() - o.getMetaproductId());
		}

		// if everything else is equal, sort by amount
		if (retVal == 0)
		{
			retVal = this.getAmount() - o.getAmount();
		}

		return retVal;
	}

}
