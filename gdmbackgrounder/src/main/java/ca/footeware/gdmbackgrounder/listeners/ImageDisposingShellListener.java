package ca.footeware.gdmbackgrounder.listeners;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;

/**
 * Responds to dispose event by disposing of provided images.
 * 
 * @author Footeware.ca
 *
 */
public class ImageDisposingShellListener implements DisposeListener {

	private Image[] images;

	/**
	 * Constructor.
	 * 
	 * @param images {@link Image} array
	 */
	public ImageDisposingShellListener(Image... images) {
		this.images = images;
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		for (Image image : images) {
			if (image != null && !image.isDisposed()) {
				image.dispose();
			}
		}
	}
}