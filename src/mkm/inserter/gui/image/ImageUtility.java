package mkm.inserter.gui.image;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 * Default implementation of the {@link IImageUtily}
 * 
 * @author kenny
 */
public class ImageUtility implements IImageUtily
{

	/**
	 * Default constructor
	 */
	public ImageUtility()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.image.IImageUtily#readImage(java.lang.String)
	 */
	@Override
	public BufferedImage readImage(final String fileToRead)
	{
		try
		{
			return ImageIO.read(new File(fileToRead));
		}
		catch (IOException ex)
		{
			LoggingHelper.logException(LogLevel.Error, ex, "Could not read: ", fileToRead);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.image.IImageUtily#resizeToDimension(java.awt.image.BufferedImage, java.awt.Dimension)
	 */
	@Override
	public BufferedImage resizeToDimension(final BufferedImage toRezize, final Dimension targetDimension, final boolean highQuality)
	{
		return resizeToDimension(toRezize, targetDimension.width, targetDimension.height, highQuality);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.image.IImageUtily#resizeToDimension(java.awt.image.BufferedImage, int, int)
	 */
	@Override
	public BufferedImage resizeToDimension(final BufferedImage toRezize, final int desiredWidth, final int desiredHeight, final boolean highQuality)
	{
		BufferedImage retVal = new BufferedImage(desiredWidth, desiredHeight, toRezize.getType());
		Graphics2D g = retVal.createGraphics();
		g.setComposite(AlphaComposite.Src);
		if (highQuality)
		{
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else
		{
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}

		g.drawImage(toRezize, 0, 0, desiredWidth, desiredHeight, null);
		g.dispose();

		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.image.IImageUtily#rotateImage(java.awt.image.BufferedImage, int)
	 */
	@Override
	public BufferedImage rotateImage(final BufferedImage toRotate, final int degrees)
	{
		BufferedImage retVal = new BufferedImage(toRotate.getWidth(), toRotate.getHeight(), toRotate.getType());
		// The required drawing location
		int drawLocationX = toRotate.getWidth();
		int drawLocationY = toRotate.getHeight();

		// Rotation information
		double rotationRequired = Math.toRadians(degrees);
		double locationX = toRotate.getWidth() / 2;
		double locationY = toRotate.getHeight() / 2;

		// create transform filter
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);

		// Drawing the rotated image into the ret val image
		retVal.createGraphics().drawImage(op.filter(toRotate, retVal), drawLocationX, drawLocationY, null);

		return retVal;
	}

	@Override
	public BufferedImage convertToBufferedImage(final Icon toConvert)
	{
		BufferedImage retVal = null;

		if (toConvert != null && toConvert instanceof ImageIcon)
		{
			retVal = (BufferedImage) ((ImageIcon) toConvert).getImage();
		}

		return retVal;
	}

}
