/**
 * 
 */
package mkm.starter;

import java.io.IOException;
import java.util.ResourceBundle;

import mkm.cache.builder.MkmCacheBuilder;
import mkm.config.MkmConfig;
import mkm.connect.MkmConnector;
import mkm.exception.CertRefreshNeededException;
import mkm.exception.EmptyResponseException;
import mkm.exception.MissingConfigException;
import mkm.exporter.MkmExporter;
import mkm.inserter.MkmInserter;
import mkm.inserter.gui.MkmInserterMain;
import mkm.install.ConfigureCertInstaller;
import mkm.localization.Localization;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.manager.gui.MkmManager;

/**
 *
 *
 * @author Kenny
 * @since 07.01.2016
 */
public class MkmStarter
{
	public static String MKM_USER_NAME = null;

	private MkmStarter()
	{

	}

	public static void main(final String[] args)
	{
		if (checkPrequesites())
		{

			// if no arguments, show GUI
			if (args == null || args.length == 0)
			{
				new ApplicationChooser();
				return;
			}

			String[] params = args;

			boolean verbouse = false;
			boolean startInserter = false, startExporter = false, startCacheBuider = false, startManager = false, updateInstaller = false;
			String userId = MkmConfig.getConfig("mkmUserToExtractCollectionFor");
			for (int i = 0; i < params.length; i++)
			{
				if ("-userId".equals(params[i]))
				{
					userId = params[++i];

					MKM_USER_NAME = userId;
				}
				if ("-verbouse".equals(params[i]))
				{
					verbouse = true;
				}
				if ("-startCacheBuilder".equals(params[i]))
				{
					startCacheBuider = true;
				}
				if ("-startExporter".equals(params[i]))
				{
					startExporter = true;
				}
				if ("-startInserter".equals(params[i]))
				{
					startInserter = true;
				}
				if ("-startManager".equals(params[i]))
				{
					startManager = true;
				}
				if ("-updateInstaller".equals(params[i]))
				{
					updateInstaller = true;
				}
			}

			if (MKM_USER_NAME == null)
			{
				MKM_USER_NAME = MkmConfig.getConfig("mkmUserName");
			}

			if (updateInstaller)
			{
				updateCertFile();

				System.out.println(Localization.getLocalizedString("Updated certifican installer, please run install_cert_modified.bat in ADMIN mode."));
				System.exit(0);
			}

			if (startManager)
			{
				if (verbouse)
					LoggingHelper.SYSTEM_LEVEL = LogLevel.Info;
				else
					LoggingHelper.SYSTEM_LEVEL = LogLevel.Warning;

				try
				{
					new MkmManager(startManager, startInserter, MkmConnector.getInstance());
				}
				catch (EmptyResponseException e)
				{
					LoggingHelper.logException(LogLevel.Fatal, e, "Unable to recieve data from MKM, exiting.");

					if (e.getCause() instanceof CertRefreshNeededException)
					{
						LoggingHelper.logForLevel(LogLevel.Fatal, "Updating certificate installer...");

						updateCertFile();

						LoggingHelper.logForLevel(LogLevel.Fatal, "Installer updated, exiting.");
					}

					System.exit(-1);
				}
			}
			else if (startInserter)
			{
				startInserter(verbouse, MkmConnector.getInstance());
			}

			if (startExporter)
			{
				startExporter(userId);
			}

			if (startCacheBuider)
			{
				startCacheBuilder(verbouse);
			}
		} // end checkPrequisites
	}

	public static void startExporter(final String userForCollection)
	{
		new MkmExporter(userForCollection).exportToCvs();
	}

	public static void startCacheBuilder(final boolean verbouse)
	{
		new MkmCacheBuilder(MkmConnector.getInstance(), 500).buildCardCache(verbouse);
	}

	public static void startInserter(final boolean verbouse, final MkmConnector con)
	{
		MkmInserterMain.createMkmInserterFrame(verbouse, con, new MkmInserter(con));
	}

	public static void updateCertFile()
	{
		try
		{
			ConfigureCertInstaller.udpateInstallFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Method to check if all necessary things are present (config for example)
	 */
	public static boolean checkPrequesites()
	{
		try
		{
			// check if config is present
			MkmConfig.getConfig("mkm_app_token");
		}
		catch (MissingConfigException ex)
		{
			LoggingHelper.logForLevel(LogLevel.Fatal, "Config missing. Please use Setup.");
			return false;
		}

		boolean retVal = true;
		ResourceBundle configBundle = ResourceBundle.getBundle("mkmConnector");
		// check if all config keys have a value
		for (String s : configBundle.keySet())
		{
			if (MkmConfig.getConfig(s) == null || "".equals(MkmConfig.getConfig(s)))
			{
				LoggingHelper.logForLevel(LogLevel.Fatal, "Please add a value for the following missing config: ", s);
				retVal = false;
			}
		}

		// check if mkmConnector has an instance
		if (MkmConnector.getInstance() == null)
		{
			LoggingHelper.logForLevel(LogLevel.Fatal, "Unable to retrieve an instance of MkmConnnector");
		}

		// TODO: check mkm connnection for cert error

		return retVal;
	}
}
