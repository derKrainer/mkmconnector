/**
 * 
 */
package mkm.inserter.gui.component;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

/**
 *
 *
 * @author Kenny
 * @since 09.01.2016
 */
public class IntegerField extends JFormattedTextField
{

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "IntegerField".hashCode();

	/**
	 * 
	 */
	public IntegerField()
	{
		this(0);
	}

	/**
	 * @param value
	 */
	public IntegerField(final Integer value)
	{
		super(new NumberFormatter(NumberFormat.getInstance()));

		this.setText(Integer.toString(value));
	}

}
