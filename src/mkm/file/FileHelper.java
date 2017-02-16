/**
 * 
 */
package mkm.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 *
 *
 * @author Kenny
 * @since 05.12.2015
 */
public class FileHelper
{

	private FileHelper()
	{
		// hidden
	}

	/**
	 * Writes the newContent into the file. If the file has not existed jet, it will be created. If it has existed, all content will be replaced!
	 * 
	 * @param fileName
	 *          the name of the target file
	 * @param newContent
	 *          the content to be written into the file
	 * @throws IOException
	 *           anything goes wrong during writing/creating (eg. missing permission)
	 */
	public static void writeToFile(final String fileName, final String newContent) throws IOException
	{
		writeToFile(fileName, newContent, false);
	}

	/**
	 * Writes the newContent into the file. If the file has not existed jet, it will be created. If it has existed, all content wil be either replaced (append ==
	 * false) or inserted at the end (append == true)
	 * 
	 * @param fileName
	 *          the name of the target file
	 * @param newContent
	 *          the content to be written into the file
	 * @param append
	 *          should the content be written at the end of the file (true) or should the file be re-written (false)
	 * @throws IOException
	 *           anything goes wrong during writing/creating (eg. missing permission)
	 */
	public static void writeToFile(final String fileName, final String newContent, final boolean append) throws IOException
	{
		if (fileName == null)
		{
			LoggingHelper.logException(LogLevel.Error, "No file to write into given.");
		}
		if (newContent == null)
		{
			LoggingHelper.logException(LogLevel.Error, "No content to write into file given:", fileName);
		}
		if (fileName == null || newContent == null)
		{
			return;
		}

		File target = createFile(new File(fileName));

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target, append));
		// UTF8OutputStreamWriter writer = new UTF8OutputStreamWriter(new FileOutputStream(target, false));

		try
		{
			writer.write(newContent);
		}
		finally
		{
			writer.close();
		}

		// RandomAccessFile raf = new RandomAccessFile(target, "rw");
		//
		// try
		// {
		// raf.write(newContent.getBytes(Charset.forName("UTF-8")));
		// }
		// finally
		// {
		// raf.close();
		// }
	}

	/**
	 * reads the content of the toRead file and returns it in string form
	 * 
	 * @param toRead
	 *          the file to read
	 * @return the content of the toRead file
	 * @throws IOException
	 *           anything goes wrong during reading (missing permission/file...)
	 */
	public static String readFile(final String toRead) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(toRead))));
		// BufferedReader reader = new BufferedReader(new UTF8Reader(new FileInputStream(toRead)));

		StringBuilder retVal = new StringBuilder();

		try
		{
			String currentLine = reader.readLine();
			while (currentLine != null)
			{
				retVal.append(currentLine);
				currentLine = reader.readLine();
			}
		}
		finally
		{
			reader.close();
		}

		return retVal.toString();
	}

	/**
	 * Creates a file and all of its necessary parents
	 * 
	 * @param fileToCreate
	 *          the file to be created
	 * @return the created file
	 * @throws IOException
	 *           if anything goes wrong (e.g: no permission)
	 */
	public static File createFile(final File fileToCreate) throws IOException
	{
		if (!fileToCreate.exists())
		{
			if (!fileToCreate.getParentFile().exists())
			{
				createFile(fileToCreate.getParentFile());
			}

			if (fileToCreate.isDirectory() || fileToCreate.getName().indexOf('.') == -1)
				fileToCreate.mkdir();
			else
			{
				if (!fileToCreate.createNewFile())
				{
					LoggingHelper.logException(LogLevel.Critical, "Unable to create file: " + fileToCreate.getAbsolutePath());
				}
			}
		}
		return fileToCreate;
	}

	public static String clearFileName(final String fileToClean, final int fromIndex, final int toIndex)
	{
		return clearFileName(new StringBuilder(fileToClean), fromIndex, toIndex).toString();
	}

	/**
	 * removes special chars the operating systems cant handle
	 */
	public static StringBuilder clearFileName(final StringBuilder fileName, final int fromIndex, final int toIndex)
	{
		if (fileName == null)
			return null;

		StringBuilder retVal = new StringBuilder(fileName.length());
		char current = 'a';
		for (int i = 0; i < fileName.length(); i++)
		{
			if (i >= fromIndex && i <= toIndex)
			{
				current = fileName.charAt(i);
				// remove all .
				if (current == ' ')
					retVal.append('_');
				if (current != '.' && current != ':')
					retVal.append(fileName.charAt(i));
			}
			else
			{
				retVal.append(fileName.charAt(i));
			}
		}
		return retVal;
	}

}
