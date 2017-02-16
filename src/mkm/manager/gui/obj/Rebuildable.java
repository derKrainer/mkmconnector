/**
 * 
 */
package mkm.manager.gui.obj;

import mkm.exception.EmptyResponseException;

/**
 *
 *
 * @author Kenny
 * @since 16.02.2016
 */
public interface Rebuildable
{
	/**
	 * Reconstructs the whole frame
	 * 
	 * @throws EmptyResponseException
	 *           if no product has been found the reubild is cancelled and this exception is thrown
	 */
	public void rebuildGui() throws EmptyResponseException;
}
