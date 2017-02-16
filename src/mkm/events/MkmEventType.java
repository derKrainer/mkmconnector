/**
 * 
 */
package mkm.events;

import mkm.inserter.MkmInserter;

/**
 * A list of all types of events which are handled by the MKM Connector
 *
 * @author Kenny
 * @since 30.12.2016
 */
public enum MkmEventType
{
	/**
	 * A new Article has been inserted via the {@link MkmInserter}
	 */
	ArticleInserted,

	/**
	 * An article has been deleted from the server
	 */
	ArticleDeleted,

	/**
	 * An article on the server has been changed
	 */
	ArticleUpdated,

	/**
	 * A Product has been updated with more recent data from the server
	 */
	ProductUpdated,
}
