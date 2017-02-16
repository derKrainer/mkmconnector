/**
 * 
 */
package mkm.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import mkm.exception.MissingConfigException;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 *
 *
 * @author Kenny
 * @since 15.01.2017
 */
public class MkmConfig
{

	public static final String CONFIG_FILE_NAME = "mkmConnectorConfig.properties";

	static final ResourceBundle BASE_CONFIGURATION = ResourceBundle.getBundle("mkmConnector");

	private static MkmConfig instance = null;

	private Map<String, String> configEntries;

	/**
	 * 
	 */
	private MkmConfig()
	{
		// hidden
		this.configEntries = new HashMap<>();
	}

	/**
	 * Makes sure the config file is present
	 */
	private void setup()
	{
		File externalConfigFile = new File(CONFIG_FILE_NAME);

		if (!externalConfigFile.exists())
		{
			// start setup
			new ConfigSetup();
			throw new MissingConfigException("No config file present");
		}
		else
		{
			// read file content
			this.addConfigFile(externalConfigFile);
		}
	}

	/**
	 * Reads the given config file (consisting of lines which are in the form of key=value) and adds those pairs to the local config. If any keys are already
	 * existing in the local config, they will be replaced
	 * 
	 * @param configFileToRead
	 *          the file to be processed and added to the local config. Lines must consist of key=value
	 */
	public void addConfigFile(final File configFileToRead)
	{
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(configFileToRead));

			String line;
			while ((line = reader.readLine()) != null)
			{
				String key = line.substring(0, line.indexOf('='));
				this.configEntries.put(key, line.substring(line.indexOf('=') + 1));
			}
		}
		catch (IOException ex)
		{
			LoggingHelper.logException(LogLevel.Critical, ex,
					"Unable to read the personalized configuration file. Exiting as we do not have any informations this way.");
			System.exit(1);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					LoggingHelper.logForLevel(LogLevel.Info, e, "Unable to close config file reader.");
				}
			}
		}

	}

	/**
	 * Writes the given Map to a specified config file. The lines in the file will have the form of key=value
	 * 
	 * @param toWriteTo
	 *          the file to be written (if it does not exist, it will be created). Any existing content will be overwritten.
	 * @param configToWrite
	 *          the configuration entries to be written
	 */
	public void writeToConfigFile(final File toWriteTo, final Map<String, String> configToWrite)
	{
		if (!toWriteTo.exists())
		{
			// check if the folder is available
			if (toWriteTo.getParentFile() != null && !toWriteTo.getParentFile().exists())
				toWriteTo.getParentFile().mkdirs();

			try
			{
				toWriteTo.createNewFile();
			}
			catch (IOException e)
			{
				LoggingHelper.logException(LogLevel.Critical, e,
						"Unable to create the personalized configuration file. Exiting as we do not have any informations this way.");
				System.exit(1);
			}
		}

		int i = 0;
		String[] sortedKeys = new String[configToWrite.keySet().size()];
		for (String key : configToWrite.keySet())
		{
			sortedKeys[i++] = key;
		}

		Arrays.sort(sortedKeys);

		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(toWriteTo));
			for (String key : sortedKeys)
			{
				writer.write(LoggingHelper.concat(key, "=", configToWrite.get(key), "\n"));
			}
		}
		catch (IOException ex)
		{
			LoggingHelper.logException(LogLevel.Critical, ex, "Error during writing config file, please fix the cause of the exception and restart.");
			System.exit(1);
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.flush();
					writer.close();
				}
				catch (IOException e)
				{
					LoggingHelper.logForLevel(LogLevel.Info, e, "Unable to close config file writer.");
				}
			}
		}
	}

	/**
	 * Retrieves a local config entry
	 * 
	 * @param key
	 *          the key of the config entry
	 * @return the value of the config entry (null if not found)
	 */
	public String getConfigEntry(final String key)
	{
		return this.configEntries.get(key);
	}

	/**
	 * Checks the config for containing a certain key
	 * 
	 * @param key
	 *          the key in question
	 * @return true if the key has a value, false if not
	 */
	public boolean hasConfigEntry(final String key)
	{
		return this.configEntries.containsKey(key);
	}

	static MkmConfig getInstance()
	{
		MissingConfigException toThrow = null;
		synchronized (CONFIG_FILE_NAME)
		{
			if (instance == null)
			{
				try
				{
					instance = new MkmConfig();
					instance.setup();
				}
				catch (MissingConfigException ex)
				{
					toThrow = ex;
				}
			}
		}

		if (toThrow != null)
		{
			throw toThrow;
		}

		return instance;
	}

	/**
	 * Retrieves a config entry
	 * 
	 * @param key
	 *          the key of the entry
	 * @return the value behind the given key
	 */
	public static String getConfig(final String key)
	{
		if (getInstance().hasConfigEntry(key))
		{
			return getInstance().getConfigEntry(key);
		}
		else if (BASE_CONFIGURATION.containsKey(key))
		{
			return BASE_CONFIGURATION.getString(key);
		}
		else
		{
			throw new MissingConfigException(key + " has no config.");
		}
	}

}
