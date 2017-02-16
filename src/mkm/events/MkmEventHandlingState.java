/**
 * 
 */
package mkm.events;

/**
 * Flags for the status of event handling
 *
 * @author Kenny
 * @since 30.12.2016
 */
public enum MkmEventHandlingState
{
	unhandled,

	continue_handling,

	finished_handling,

	error
}