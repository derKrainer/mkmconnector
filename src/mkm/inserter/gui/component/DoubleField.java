/**
 * 
 */
package mkm.inserter.gui.component;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 *
 *
 * @author Kenny
 * @since 14.01.2016
 */
public class DoubleField extends JFormattedTextField
{

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "PriceField".hashCode();

	public static final NumberFormat FORMAT = NumberFormat.getNumberInstance();

	/**
	 * 
	 */
	public DoubleField()
	{
		this(0.0);
	}

	/**
	 * @param value
	 */
	public DoubleField(final Double value)
	{
		super(new NumberFormatter(FORMAT));

		if (value != null)
			this.setText(FORMAT.format(value));
		else
			this.setText(null);
	}

	@Override
	public Double getValue()
	{
		if (getText() != null && !"".equals(getText()))
		{
			try
			{
				Number retVal = FORMAT.parse(getText());

				if (retVal instanceof Long)
					return 0.0 + (Long) retVal;
				else
					return (Double) FORMAT.parse(getText());
			}
			catch (ParseException e)
			{
				LoggingHelper.logException(LogLevel.Critical, e, "Invalid double: ", this.getText());
				return 0.0;
			}
		}
		else
		{
			return 0.0;
		}
	}

}
