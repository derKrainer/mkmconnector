/**
 * 
 */
package mkm.events;

/**
 *
 *
 * @author Kenny
 * @since 30.12.2016
 */
public interface IMkmEventConsumer
{
	MkmEventHandlingState handleEvent(MkmEventType type, Object source);
}
