/**
 * 
 */
package mkm.inserter.threaded;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.xml.sax.SAXParseException;

import mkm.data.MtgCard;
import mkm.file.FileHelper;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 *
 *
 * @author Kenny
 * @since 08.02.2016
 */
public class CacheBuilderThreaded implements Runnable
{

	private Vector<MtgCard> buffer;

	private File rootDir;

	private int failedCards = 0;

	/**
	 * 
	 */
	public CacheBuilderThreaded(final File cacheDir)
	{
		this.buffer = new Vector<MtgCard>();
		this.rootDir = cacheDir;
	}

	public List<MtgCard> getBuffer()
	{
		return this.buffer;
	}

	public int getFailedCards()
	{
		return this.failedCards;
	}

	@Override
	public void run()
	{
		if (rootDir.isDirectory())
		{

			for (File tmp : rootDir.listFiles())
			{
				String xml = null;
				try
				{
					xml = FileHelper.readFile(tmp.getAbsolutePath());

					if (xml != null)
					{
						MtgCard currentCard = new MtgCard(xml, false, Integer.parseInt(tmp.getName().substring(0, tmp.getName().indexOf("_response"))));
						this.buffer.add(currentCard);
					}
				}
				catch (IOException e)
				{
					if (e.getCause() instanceof SAXParseException && xml != null)
					{
						MtgCard currentCard = new MtgCard(xml);
						this.buffer.add(currentCard);
					}
					else
					{
						LoggingHelper.logException(LogLevel.Error, e, "Unable to read file: ", tmp.getAbsolutePath());
						failedCards++;
					}
				}
			}
		}
	}

}
