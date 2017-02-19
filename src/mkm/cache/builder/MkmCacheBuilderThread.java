/**
 * 
 */
package mkm.cache.builder;

import mkm.connect.MkmConnector;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 *
 *
 * @author Kenny
 * @since 18.02.2017
 */
public class MkmCacheBuilderThread implements Runnable
{

	private long waitTime;

	private MkmCacheBuilder builder;

	/**
	 * Creates a new {@link MkmCacheBuilderThread} able to be run
	 * 
	 * @param blockAmount
	 *          the amount of entries to be added per round
	 * @param sleepTime
	 *          the amount of milliseconds to wait between two rounds
	 */
	public MkmCacheBuilderThread(final int blockAmount, final long sleepTime)
	{
		this.waitTime = sleepTime;

		this.builder = new MkmCacheBuilder(MkmConnector.getInstance(), blockAmount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		while (true)
		{

			this.builder.buildCardCache(false);

			try
			{
				Thread.sleep(waitTime);
			}
			catch (InterruptedException e)
			{
				LoggingHelper.logException(LogLevel.Error, e, "cache building Thread was interrupted");
			}
		}
	}

}
