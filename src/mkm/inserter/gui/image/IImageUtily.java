package mkm.inserter.gui.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

/**
 * Utility for handling images
 * 
 * @author kenny
 */
public interface IImageUtily
{

	/**
	 * Reads an image from a given File
	 * 
	 * @param fileToRead
	 *          the image file
	 * @return the BufferedImage instance of the given file
	 */
	BufferedImage readImage(String fileToRead);

	/**
	 * Rotates the given image by the given degrees and returns a new instance
	 * 
	 * @param toRotate
	 *          the image to rotate
	 * @param degrees
	 *          the degrees to be rotated by
	 * @return a new instance of the parameter image rotated by the given degrees
	 */
	BufferedImage rotateImage(BufferedImage toRotate, int degrees);

	/**
	 * Resizes an image to a given size
	 * 
	 * @param toRezize
	 *          the image to resize
	 * @param targetDimension
	 *          the desired size of the image
	 * @param highQuality
	 *          should the quality of the image be high (true) or should the processing be fast (false)
	 * @return the passed image in the given size
	 */
	BufferedImage resizeToDimension(BufferedImage toRezize, Dimension targetDimension, boolean highQuality);

	/**
	 * Tries to convert the given icon to a {@link BufferedImage}
	 * 
	 * @param toConvert
	 *          the icon to be converted
	 * @return the BufferedImage for further use
	 */
	BufferedImage convertToBufferedImage(Icon toConvert);

	/**
	 * Resizes an image to a given size
	 * 
	 * @param toRezize
	 *          the image to resize
	 * @param desiredWidth
	 *          the desired width in pixels
	 * @param desiredHeight
	 *          the desired height in pixels
	 * @param highQuality
	 *          should the quality of the image be high (true) or should the processing be fast (false)
	 * @return the passed image in the given size
	 */
	BufferedImage resizeToDimension(BufferedImage toRezize, int desiredWidth, int desiredHeight, boolean highQuality);
}
