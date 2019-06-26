package ca.footeware.backgrounder.listeners;

import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;

/**
 * Responds to dispose event by disposing of provided images.
 * 
 * @author Footeware.ca
 *
 */
public class ImageDisposingDisposeListener implements DisposeListener {

	private List<Image> images;

	/**
	 * Constructor.
	 * 
	 * @param imagesToDispose {@link List} of {@link Image} array
	 */
	public ImageDisposingDisposeListener(List<Image> imagesToDispose) {
		this.images = imagesToDispose;
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