/**
 * 
 */
package mkm.data;

import org.w3c.dom.Node;

import mkm.parser.XmlParser;

/**
 *
 *
 * @author Kenny
 * @since 13.01.2016
 */
public class ProductPrice
{

	private double sell = 0, low = 0, lowEx = 0, lowFoil = 0, avg = 0, trend = 0;

	/**
	 * 
	 */
	public ProductPrice()
	{

	}

	public ProductPrice(final Node priceGuildeNode)
	{
		for (int i = 0; i < priceGuildeNode.getChildNodes().getLength(); i++)
		{
			Node child = priceGuildeNode.getChildNodes().item(i);

			if ("SELL".equals(child.getNodeName()))
			{
				this.setSell(Double.parseDouble(XmlParser.getNodeTextValue(child)));
			}
			else if ("LOW".equals(child.getNodeName()))
			{
				this.setLow(Double.parseDouble(XmlParser.getNodeTextValue(child)));
			}
			else if ("LOWEX".equals(child.getNodeName()))
			{
				this.setLowEx(Double.parseDouble(XmlParser.getNodeTextValue(child)));
			}
			else if ("LOWFOIL".equals(child.getNodeName()))
			{
				this.setLowFoil(Double.parseDouble(XmlParser.getNodeTextValue(child)));
			}
			else if ("AVG".equals(child.getNodeName()))
			{
				this.setAvg(Double.parseDouble(XmlParser.getNodeTextValue(child)));
			}
			else if ("TREND".equals(child.getNodeName()))
			{
				this.setTrend(Double.parseDouble(XmlParser.getNodeTextValue(child)));
			}
		}
	}

	/**
	 * @return the sell
	 */
	public double getSell()
	{
		return sell;
	}

	/**
	 * @param sell
	 *          the sell to set
	 */
	public void setSell(final double sell)
	{
		this.sell = sell;
	}

	/**
	 * @return the low
	 */
	public double getLow()
	{
		return low;
	}

	/**
	 * @param low
	 *          the low to set
	 */
	public void setLow(final double low)
	{
		this.low = low;
	}

	/**
	 * @return the lowEx
	 */
	public double getLowEx()
	{
		return lowEx;
	}

	/**
	 * @param lowEx
	 *          the lowEx to set
	 */
	public void setLowEx(final double lowEx)
	{
		this.lowEx = lowEx;
	}

	/**
	 * @return the lowFoil
	 */
	public double getLowFoil()
	{
		return lowFoil;
	}

	/**
	 * @param lowFoil
	 *          the lowFoil to set
	 */
	public void setLowFoil(final double lowFoil)
	{
		this.lowFoil = lowFoil;
	}

	/**
	 * @return the avg
	 */
	public double getAvg()
	{
		return avg;
	}

	/**
	 * @param avg
	 *          the avg to set
	 */
	public void setAvg(final double avg)
	{
		this.avg = avg;
	}

	/**
	 * @return the trend
	 */
	public double getTrend()
	{
		return trend;
	}

	/**
	 * @param trend
	 *          the trend to set
	 */
	public void setTrend(final double trend)
	{
		this.trend = trend;
	}

	/**
	 * <priceGuide> <SELL>0.05</SELL> <LOW>0.02</LOW> <LOWEX>0.02</LOWEX> <LOWFOIL>0.07</LOWFOIL> <AVG>0.09</AVG> <TREND>0.04</TREND> </priceGuide>
	 * 
	 * @param tabPrefix
	 * @return
	 */
	public String toResponseXml(final String tabPrefix)
	{
		StringBuilder retVal = new StringBuilder();

		retVal.append(tabPrefix).append("<priceGuide>\n");

		appendTag("SELL", this.getSell(), retVal, 1, tabPrefix);
		appendTag("LOW", this.getLow(), retVal, 1, tabPrefix);
		appendTag("LOWEX", this.getLowEx(), retVal, 1, tabPrefix);
		appendTag("LOWFOIL", this.getLowFoil(), retVal, 1, tabPrefix);
		appendTag("AVG", this.getAvg(), retVal, 1, tabPrefix);
		appendTag("TREND", this.getTrend(), retVal, 1, tabPrefix);

		retVal.append(tabPrefix).append("</priceGuide>");

		return retVal.toString();
	}

	private void appendTag(final String tagName, final Double value, final StringBuilder retVal, final int tabCount, final String tabPrefix)
	{
		retVal.append(tabPrefix);

		for (int i = 0; i < tabCount; i++)
			retVal.append('\t');

		retVal.append('<').append(tagName).append('>').append(value).append("</").append(tagName).append(">\n");
	}

}
