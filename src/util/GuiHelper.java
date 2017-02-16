/**
 * 
 */
package util;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mkm.localization.Localization;

/**
 *
 *
 * @author Kenny
 * @since 11.12.2016
 */
public class GuiHelper
{

	private GuiHelper()
	{
	}

	/**
	 * Wraps the label and text into a Panel for layout purposes
	 * 
	 * @param label
	 *          the text describing the component
	 * @param toAdd
	 *          the component to be added
	 * @return the panel holding both
	 */
	public static JPanel createLabelAndComponentInPanel(final String labelText, final JComponent toAdd)
	{
		return createLabelAndComponentInPanel(labelText, toAdd, 125, 250, 30);
	}

	/**
	 * Wraps the label and text into a Panel for layout purposes
	 * 
	 * @param label
	 *          the text describing the component
	 * @param toAdd
	 *          the component to be added
	 * @return the panel holding both
	 */
	public static JPanel createLabelAndComponentInPanel(final String labelText, final JComponent toAdd, final int componentWidth, final int totalWidth,
			final int height)
	{
		JPanel retVal = new JPanel();
		retVal.setLayout(null);
		// search field
		JLabel label = new JLabel(Localization.getLocalizedString(labelText));
		retVal.add(label);
		label.setBounds(0, 0, totalWidth - componentWidth, height);

		retVal.add(toAdd);
		toAdd.setBounds(totalWidth - componentWidth, 0, componentWidth, height);

		return retVal;
	}
}
