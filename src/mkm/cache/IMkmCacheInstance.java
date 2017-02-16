/**
 * 
 */
package mkm.cache;

/**
 *
 *
 * @author Kenny
 * @since 29.12.2016
 */
public interface IMkmCacheInstance
{

	/**
	 * Retrieves data from the cache for the given personalized request string
	 * 
	 * @param personalizedParam
	 *          the request string with all infos replaced
	 * @return the content of the cache or null for a cache miss
	 */
	String getCacheContent(final String personalizedParam);

	/**
	 * Stores the content in the cache for the personalized string provided
	 * 
	 * @param personalizedParam
	 *          the request string with all information already replaced (userId/cardId/productId...)
	 * @param content
	 *          the response of the mkm server for the given request string
	 */
	void writeToCache(final String personalizedParam, final String content);
}
