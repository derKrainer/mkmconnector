package mkm.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Node;

import mkm.config.MkmConfig;
import mkm.connect.MkmConnector;
import mkm.exception.Http400Exception;
import mkm.file.FileHelper;
import mkm.parser.XmlParser;

/**
 * Class to enable easier logging
 * 
 * @author kenny
 * 
 */
public class LoggingHelper
{

	public static LogLevel SYSTEM_LEVEL = LogLevel.getLevelForString(MkmConfig.getConfig("log.Log_Level"));

	public static boolean PREFIX_WITH_TIMESTAMP;

	public static SimpleDateFormat TIMESTAMP_FORMAT;

	public static String LOG_FILE;

	static
	{
		PREFIX_WITH_TIMESTAMP = Boolean.TRUE.toString().equals(MkmConfig.getConfig("log.prefix_with_timeStamp"));
		String timeStampFormat = MkmConfig.getConfig("log.time_stamp_format");

		if (PREFIX_WITH_TIMESTAMP)
		{
			try
			{
				TIMESTAMP_FORMAT = new SimpleDateFormat(timeStampFormat);
			}
			catch (Exception e)
			{
				System.err.println("Not a simple Date Format: " + timeStampFormat);
				PREFIX_WITH_TIMESTAMP = false;
				TIMESTAMP_FORMAT = null;
			}
		}

		StringBuilder fileName = new StringBuilder(MkmConnector.OUTPUT_DIR);
		fileName.append('/').append(new SimpleDateFormat("yyyy-MM-dd_HH_mm").format(Calendar.getInstance().getTime()));
		fileName.append("_log.txt");
		LOG_FILE = fileName.toString();

		if (!new File(LOG_FILE).exists())
		{
			try
			{
				FileHelper.createFile(new File(LOG_FILE));
			}
			catch (IOException e)
			{
				System.err.println("Could not create the log file!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Logs a message and the the exception to the error output if {@value #shouldPrintError} is true
	 */
	public static void logException(final LogLevel logLevel, final Throwable e, final String... messageParts)
	{
		if (shouldPrintInfo(logLevel))
		{
			logForLevel(logLevel, null, concat(messageParts));

			if (e instanceof Http400Exception)
			{
				logForLevel(logLevel, null, concat("Unable to find URL. ", e.getMessage()));

				StringBuilder stackTrace = new StringBuilder();
				for (int i = 0; i < e.getStackTrace().length && i < 7; i++)
				{
					stackTrace.append("  at: ");
					stackTrace.append(e.getStackTrace()[i].getClassName()).append(".");
					stackTrace.append(e.getStackTrace()[i].getMethodName()).append("(");
					stackTrace.append(e.getStackTrace()[i].getFileName()).append(":");
					stackTrace.append(e.getStackTrace()[i].getLineNumber()).append(")\n");

				}
				boolean bup = PREFIX_WITH_TIMESTAMP;
				PREFIX_WITH_TIMESTAMP = false;
				logForLevel(logLevel, null, stackTrace.toString());
				PREFIX_WITH_TIMESTAMP = bup;
			}
			else
			{
				logForLevel(logLevel, e, null);
			}
		}
	}

	/**
	 * Logs a message consisting of all parts to the error output if {@value #shouldPrintError} is true
	 */
	public static void logException(final LogLevel level, final String... messageParts)
	{
		// check shouldPrintInfo before concat of strings (cheaper)
		if (shouldPrintInfo(level))
			logForLevel(level, null, concat(messageParts));
	}

	/**
	 * Prints a message to the info output if {@link #shouldPrintInfo} is true
	 */
	public static void logForLevel(final LogLevel level, final String... messageParts)
	{
		// check shouldPrintInfo before concat of strings (cheaper)
		if (shouldPrintInfo(level))
			logForLevel(level, null, concat(messageParts));
	}

	/**
	 * Logs a message to all outputs for the given level
	 * 
	 * @param logLevel
	 * @param message
	 */
	public static void logForLevel(final LogLevel logLevel, final Throwable e, final String message)
	{
		if (!shouldPrintInfo(logLevel))
			return;

		List<PrintWriter> outputs = new ArrayList<>();
		switch (logLevel)
		{
			case None:
			case Critical:
			case Error:
			case Fatal:
			{
				outputs.add(new PrintWriter(new OutputStreamWriter(System.err)));
				break;
			}
			case Info:
			case Warning:
			case Detailed:
			case All:
			{
				outputs.add(new PrintWriter(new OutputStreamWriter(System.out)));
				break;
			}
		}

		try
		{
			outputs.add(new PrintWriter(new FileWriter(new File(LOG_FILE), true)));
		}
		catch (IOException ex)
		{
			System.err.println("Could not create the log file writer");
		}

		if (message != null)
		{
			for (PrintWriter out : outputs)
			{
				if (PREFIX_WITH_TIMESTAMP)
				{
					out.write(concat(getTimeStamp(), message, "\n"));
				}
				else
				{
					out.write(concat(message, "\n"));
				}
			}
		}

		if (e != null)
		{
			for (PrintWriter wrtier : outputs)
			{
				e.printStackTrace(wrtier);
			}
		}

		for (PrintWriter writer : outputs)
			writer.flush();

	}

	/**
	 * Appends a string to the logFile
	 * 
	 * @param toAppend
	 * @throws IOException
	 */
	public static void appendToLogFile(final String toAppend) throws IOException
	{
		FileHelper.writeToFile(LOG_FILE, concat(toAppend, getTimeStamp()), true);
	}

	public static final String getTimeStamp()
	{
		if (PREFIX_WITH_TIMESTAMP)
		{
			return new StringBuffer(TIMESTAMP_FORMAT.format(Calendar.getInstance().getTime())).append(": ").toString();
		}
		return null;
	}

	/**
	 * Concats a String array to a single String
	 */
	public static String concat(final String... parts)
	{
		StringBuffer retVal = new StringBuffer();

		for (String s : parts)
		{
			if (s != null)
				retVal.append(s);
		}
		return retVal.toString();
	}

	/**
	 * Checks if a message should be logged
	 * 
	 * @param msgLevel
	 * @return
	 */
	private static boolean shouldPrintInfo(final LogLevel msgLevel)
	{
		return LogLevel.shouldLog(SYSTEM_LEVEL, msgLevel);
	}

	/**
	 * Logs the content of an xml node
	 * 
	 * @param level
	 *          the level to log with
	 * @param node
	 *          the node to be logged
	 * @param string
	 *          a possible message
	 */
	public static void logNode(final LogLevel level, final Node node, final String... string)
	{
		if (shouldPrintInfo(level))
		{
			if (string != null && string.length > 0)
				logForLevel(level, null, concat(string));

			logForLevel(level, null, XmlParser.getNodeAsString(node));
		}
	}

	/**
	 * Special method, printing to System.out if the current log level does not allow LogLevel.Info
	 * 
	 * @param strings
	 *          message parts
	 */
	public static void info(final String... strings)
	{
		if (shouldPrintInfo(LogLevel.Info))
		{
			logForLevel(LogLevel.Info, null, concat(strings));
		}
		else
		{
			System.out.println(concat(strings));
		}

	}
}
