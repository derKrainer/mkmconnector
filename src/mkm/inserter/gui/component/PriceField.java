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
 * @since 09.01.2016
 */
public class PriceField extends JFormattedTextField
{

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "PriceField".hashCode();

	public static final NumberFormat FORMAT = NumberFormat.getCurrencyInstance();

	/**
	 * 
	 */
	public PriceField()
	{
		this(0.0);
	}

	/**
	 * @param value
	 */
	public PriceField(final Double value)
	{
		super(new NumberFormatter(FORMAT));

		if (value != null)
			this.setText(FORMAT.format(value));
		else
			this.setText(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JFormattedTextField#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(final Object value)
	{
		Double val = 0.0;
		if (value != null)
		{
			val = parseDouble(value.toString());
			super.setValue(val);
		}
		else
		{
			super.setValue(0.0);
		}
		setText(FORMAT.format(val));
	}

	private Double parseDouble(final String val)
	{
		try
		{
			Number retVal = FORMAT.parse(val);

			if (retVal instanceof Long)
				return 0.0 + (Long) retVal;
			else
				return (Double) FORMAT.parse(val);
		}
		catch (ParseException e)
		{
			try
			{
				return Double.parseDouble(val);
			}
			catch (Exception ex2)
			{
				// ignore
			}

			// try parsing a double value
			try
			{
				return NumberFormat.getNumberInstance().parse(val).doubleValue();
			}
			catch (ParseException e1)
			{
				LoggingHelper.logException(LogLevel.Critical, e1, "Invalid double: ", this.getText());
				return 0.0;
			}

		}
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
				// try parsing a double value
				try
				{
					return NumberFormat.getNumberInstance().parse(getText()).doubleValue();
				}
				catch (ParseException e1)
				{
					LoggingHelper.logException(LogLevel.Critical, e1, "Invalid double: ", this.getText());
					return 0.0;
				}

			}
		}
		else
		{
			return 0.0;
		}
	}

}
