/**
 * 
 */
package mkm.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 * Class responsible for handling all events (triggering and informing)
 *
 * @author Kenny
 * @since 30.12.2016
 */
public class MkmEventHandler
{

	private static final MkmEventHandler instance = new MkmEventHandler();

	private Map<MkmEventType, Set<IMkmEventConsumer>> listeners = new HashMap<>();

	/**
	 * Hidden constructor
	 */
	private MkmEventHandler()
	{
		// init all lists
		for (MkmEventType type : MkmEventType.values())
		{
			this.listeners.put(type, new HashSet<IMkmEventConsumer>());
		}
	}

	/**
	 * retrieves the only instance of the {@link MkmEventHandler}
	 * 
	 * @return
	 */
	protected static MkmEventHandler getInstance()
	{
		return instance;
	}

	/**
	 * Handles an event of the given type and the given source. (Informs all listeners for this type)
	 * 
	 * @param type
	 *          the type if the event, as defined in {@link MkmEventType}
	 * @param source
	 *          the object triggering the event (probably the one which changed)
	 * @return the State of the event handling after all are finished
	 */
	public static MkmEventHandlingState triggerEvent(final MkmEventType type, final Object source)
	{
		return getInstance().internalHandleEvent(type, source);
	}

	/**
	 * Registers a listener for a given event type
	 * 
	 * @param consumer
	 *          the event consumer (listener)
	 * @param interestedIn
	 *          the type of event this listener should recive notifications for
	 * @return true if the event listener could be added, false if adding to the listener set was unsuccessful (probably was already added)
	 */
	public static boolean addEventListener(final IMkmEventConsumer consumer, final MkmEventType interestedIn)
	{
		return getInstance().internalAddEventListener(consumer, interestedIn);
	}

	protected boolean internalAddEventListener(final IMkmEventConsumer consumer, final MkmEventType interestedIn)
	{
		return this.listeners.get(interestedIn).add(consumer);
	}

	protected MkmEventHandlingState internalHandleEvent(final MkmEventType type, final Object source)
	{
		// default unhandled, as there might not be any listeners
		MkmEventHandlingState retVal = MkmEventHandlingState.unhandled;

		for (IMkmEventConsumer consumer : this.listeners.get(type))
		{
			retVal = consumer.handleEvent(type, source);

			if (retVal == MkmEventHandlingState.error)
			{
				LoggingHelper.logForLevel(LogLevel.Error, consumer.getClass().getName(), " caused an error during handling event: ", type.toString());
				break;
			}
			else if (retVal == MkmEventHandlingState.finished_handling)
			{
				break;
			}
		}

		return retVal;
	}
}
