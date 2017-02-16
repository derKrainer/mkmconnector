/**
 * 
 */
package mkm.config;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mkm.localization.Localization;

/**
 *
 *
 * @author Kenny
 * @since 15.02.2017
 */
public class ConfigSetup extends JFrame
{

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "ConfigSetup_15.02.2017".hashCode();

	private JScrollPane scrollPane;

	private JPanel contentPanel;

	private Map<String, JTextField> textFields = new HashMap<>();

	/**
	 * 
	 */
	public ConfigSetup()
	{
		super(Localization.getLocalizedString("MKM Setup"));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new FlowLayout(FlowLayout.LEADING));

		this.contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		this.scrollPane = new JScrollPane(contentPanel);
		this.add(this.scrollPane);

		String[] sortedKeys = new String[MkmConfig.BASE_CONFIGURATION.keySet().size()];
		int i = 0;
		for (String s : MkmConfig.BASE_CONFIGURATION.keySet())
		{
			sortedKeys[i++] = s;
		}
		Arrays.sort(sortedKeys);

		for (String key : sortedKeys)
		{
			String confValue = MkmConfig.BASE_CONFIGURATION.getString(key);
			if (confValue == null || "".equals(confValue))
			{
				addLabelTextField(key, confValue);
			}
		}

		JButton save = new JButton(Localization.getLocalizedString("Save"));
		this.add(save);
		save.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				Map<String, String> newConfig = new HashMap<>();
				for (String s : sortedKeys)
				{
					if (textFields.containsKey(s))
					{
						newConfig.put(s, textFields.get(s).getText());
					}
				}

				MkmConfig.getInstance().writeToConfigFile(new File(MkmConfig.CONFIG_FILE_NAME), newConfig);
				exit();
			}
		});

		this.setSize(650, 900);
		this.setVisible(true);
	}

	public void exit()
	{
		this.dispose();
	}

	private JPanel addLabelTextField(final String label, final String defaultText)
	{
		Dimension size = new Dimension(300, 30);
		JPanel container = new JPanel();
		JLabel text = new JLabel(Localization.getLocalizedString(label));
		text.setPreferredSize(size);
		container.add(text);
		JTextField inputField = new JTextField(defaultText);
		inputField.setName(label);
		inputField.setPreferredSize(size);
		if (MkmConfig.BASE_CONFIGURATION.containsKey(label + "_tooltip"))
		{
			text.setToolTipText(Localization.getLocalizedString(MkmConfig.BASE_CONFIGURATION.getString(label + "_tooltip")));
			inputField.setToolTipText(Localization.getLocalizedString(MkmConfig.BASE_CONFIGURATION.getString(label + "_tooltip")));
		}
		textFields.put(label, inputField);
		container.add(inputField);

		this.contentPanel.add(container);

		return container;
	}

}
