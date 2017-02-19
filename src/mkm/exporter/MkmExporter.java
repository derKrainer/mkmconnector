/**
 * 
 */
package mkm.exporter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Node;

import mkm.connect.MkmConnector;
import mkm.data.Article;
import mkm.exception.CertRefreshNeededException;
import mkm.file.FileHelper;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.parser.XmlParser;

/**
 *
 *
 * @author Kenny
 * @since 07.01.2016
 */
public class MkmExporter
{

	private String user;

	private MkmConnector con;

	/**
	 * 
	 */
	public MkmExporter(final String user)
	{
		this.user = user;
		this.con = MkmConnector.getInstance();
	}

	public MkmExporter(final String user, final MkmConnector con)
	{
		this.user = user;
		this.con = con;
	}

	public File exportToCvs()
	{
		File retVal = null;

		try
		{
			String xml = getCollectionXmlForUser(false);

			if (xml != null)
			{
				Article[] toSort = parseUserXml(xml);

				if (toSort != null && toSort.length > 0)
				{
					StringBuilder fileContent = new StringBuilder();
					fileContent.append(toSort[0].getHeaderCSV());
					fileContent.append(MkmConnector.lineSeperator);
					for (Article a : toSort)
					{
						fileContent.append(a.toCSV()).append(MkmConnector.lineSeperator);
					}

					StringBuilder fileName = new StringBuilder(MkmConnector.OUTPUT_DIR);
					fileName.append('/').append(new SimpleDateFormat("yyyy-MM-dd_HH_mm").format(Calendar.getInstance().getTime()));
					fileName.append('_').append(this.user).append("_results.csv");
					FileHelper.writeToFile(fileName.toString(), fileContent.toString());

					retVal = new File(fileName.toString());
				}
			}
		}
		catch (Exception ex)
		{
			LoggingHelper.logException(LogLevel.Critical, ex, "Exception during contacting MKM");
		}
		return retVal;
	}

	public Article[] parseUserXml(final String collectionXml)
	{
		Article[] retVal = null;

		if (collectionXml != null)
		{
			try
			{
				XmlParser parser = new XmlParser(collectionXml);

				// List<Node> nodeList = parser.getNodesForPath("response/article/idProduct");
				List<Node> nodeList = parser.getNodesForPath("response/article");
				XmlParser.printCurrentNodes(LogLevel.Detailed, nodeList);

				if (nodeList != null && !nodeList.isEmpty())
				{
					List<Article> articles = new ArrayList<>(nodeList.size());
					List<Node> errorNodes = new ArrayList<>();

					for (Node n : nodeList)
					{
						try
						{
							articles.add(new Article(n, con));
						}
						catch (Exception e)
						{
							// only info, because error handling for xml is done inside
							LoggingHelper.logException(LogLevel.Info, e, "unable to parse article xml");
							errorNodes.add(n);
						}
					}

					if (!errorNodes.isEmpty())
					{
						StringBuilder errorContent = new StringBuilder();
						errorContent.append(XmlParser.printCurrentNodes(LogLevel.All, errorNodes));
						try
						{
							LoggingHelper.appendToLogFile(errorContent.toString());
						}
						catch (IOException e)
						{
							LoggingHelper.logException(LogLevel.Critical, "Unable to write error file");
						}
					}

					if (articles.isEmpty())
					{
						LoggingHelper.logException(LogLevel.Critical, "Retrieved no parsable cards:", collectionXml);
					}
					else
					{
						// sort collection
						Article[] toSort = new Article[articles.size()];

						articles.toArray(toSort);
						try
						{
							Arrays.sort(toSort);

						}
						catch (Exception e)
						{
							LoggingHelper.logException(LogLevel.Critical, e, "error during sorting");
						}

						retVal = toSort;
					}
				}
			}
			catch (IOException ex)
			{
				LoggingHelper.logException(LogLevel.Critical, ex, "Exception during parsing xml");
			}
		}
		return retVal;
	}

	/**
	 * Retrieves the XML data for all articles for the current user
	 * 
	 * @param ignoreCache
	 *          should the cache be ignored or not?
	 * @return XML String with all {@link Article} data
	 * @throws CertRefreshNeededException
	 *           thrown if the SSL Handshake did not go through
	 */
	public String getCollectionXmlForUser(final boolean ignoreCache) throws CertRefreshNeededException
	{
		String xml = null;
		try
		{
			xml = con.performGetRequest(MkmConnector.GET_COLLECTION_FOR_USER, new String[] { ":idUser" }, new String[] { this.user }, ignoreCache, false);
		}
		catch (CertRefreshNeededException ex)
		{
			LoggingHelper.logException(LogLevel.None, "Update the certificates as there has been an update which removed the old infos");

			throw ex;
		}
		catch (Exception ex)
		{
			LoggingHelper.logException(LogLevel.Critical, ex, "Exception during contacting MKM");
		}

		return xml;
	}

	/**
	 * @return the user
	 */
	public String getUser()
	{
		return user;
	}

	/**
	 * @param user
	 *          the user to set
	 */
	public void setUser(final String user)
	{
		this.user = user;
	}

}
