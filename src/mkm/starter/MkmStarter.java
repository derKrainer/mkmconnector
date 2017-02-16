/**
 * 
 */
package mkm.starter;

import java.io.IOException;

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

			String[] params = args;

			if (params == null || params.length == 0)
			{
				params = new String[] { "-verbouse", "-startInserter" };
			}

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
			}

			//
			MkmConnector c = new MkmConnector();
			// MkmServiceHandler handler = new MkmServiceHandler(c);
			// handler.performNameSearch("Island", Language.English, true, null);

			if (startManager)
			{
				if (verbouse)
					LoggingHelper.SYSTEM_LEVEL = LogLevel.Info;
				else
					LoggingHelper.SYSTEM_LEVEL = LogLevel.Warning;

				try
				{
					new MkmManager(startManager, startInserter, c);
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
				startInserter(verbouse, c);
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
		MkmConnector con = new MkmConnector();
		new MkmCacheBuilder(con, 500).buildCardCache(verbouse);
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
			return;
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
			System.err.println("Config missing. Please use Setup.");
			return false;
		}

		return true;
	}
}
