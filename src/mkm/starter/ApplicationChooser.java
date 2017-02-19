/**
 * 
 */
package mkm.starter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import mkm.cache.builder.MkmCacheBuilderThread;
import mkm.config.MkmConfig;
import mkm.connect.MkmConnector;
import mkm.exception.EmptyResponseException;
import mkm.localization.Localization;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.manager.gui.MkmManager;

/**
 *
 *
 * @author Kenny
 * @since 18.02.2017
 */
public class ApplicationChooser
{

	protected JCheckBox verbouse, runUpdateCache, startInserter, startManager;

	/**
	 * 
	 */
	public ApplicationChooser()
	{
		JFrame main = new JFrame("Mkm Manager options");
		main.setLayout(new BoxLayout(main.getContentPane(), BoxLayout.Y_AXIS));

		verbouse = createAndAddCheckBox("Detailed Logging", main);
		runUpdateCache = createAndAddCheckBox("Update cache in background", main);
		startInserter = createAndAddCheckBox("Start MkmInserter", main);
		startManager = createAndAddCheckBox("Start manager", main);

		JButton startButton = new JButton(Localization.getLocalizedString("Start"));
		startButton.setSize(150, 35);
		startButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (verbouse.isSelected())
				{
					LoggingHelper.SYSTEM_LEVEL = LogLevel.Detailed;
				}
				else
				{
					LoggingHelper.SYSTEM_LEVEL = LogLevel.Info;
				}
				if (runUpdateCache.isSelected())
				{
					startCacheUpdater();
				}

				if (startInserter.isSelected() || startManager.isSelected())
				{
					try
					{
						new MkmManager(startManager.isSelected(), startInserter.isSelected(), MkmConnector.getInstance());
					}
					catch (EmptyResponseException e1)
					{
						LoggingHelper.logForLevel(LogLevel.Critical, e1, "Unable to start MkmManager");
						System.exit(1);
					}
				}

				main.dispose();
			}
		});
		main.add(startButton);

		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setSize(400, 600);
		main.setVisible(true);
	}

	public void startCacheUpdater()
	{
		new Thread(
				new MkmCacheBuilderThread(Integer.parseInt(MkmConfig.getConfig("cache_entries_to_add")), Long.parseLong(MkmConfig.getConfig("cachebuilder_waittime"))))
						.start();
	}

	private JCheckBox createAndAddCheckBox(final String text, final JFrame addToThis)
	{
		JCheckBox retVal = new JCheckBox(Localization.getLocalizedString(text));

		retVal.setSize(200, 25);
		retVal.setName(text);

		addToThis.add(retVal);

		return retVal;
	}

}
