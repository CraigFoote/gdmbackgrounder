package ca.footeware.backgrounder.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ca.footeware.backgrounder.dialogs.ErrorDialog;

/**
 * Responds to Browse button clicks by opening a file chooser dialog then, once
 * an image is chosen, adds the file path to the text box and displays the image
 * in the image panel.
 * 
 * @author Footeware.ca
 *
 */
public class BrowseButtonListener extends SelectionAdapter {
	private final Canvas canvas;
	private Image image;
	private final Shell shell;
	private final Text text;

	/**
	 * Constructor.
	 * 
	 * @param shell  {@link Shell}
	 * @param text   {@link Text}
	 * @param canvas {@link Canvas}
	 */
	public BrowseButtonListener(Shell shell, Text text, Canvas canvas) {
		this.shell = shell;
		this.text = text;
		this.canvas = canvas;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Select an image");
		boolean havePath = !text.getText().trim().isEmpty();
		if (havePath) {
			dialog.setFileName(text.getText().trim());
		} else {
			dialog.setFilterPath("/");
		}
		String filepath = dialog.open();
		// null if canceled
		if (filepath != null) {
			// out with the old
			if (image != null && !image.isDisposed()) {
				image.dispose();
			}
			// in with the new
			try {
				image = new Image(shell.getDisplay(), filepath);
				// draw on canvas
				canvas.addPaintListener(e1 -> {
					canvas.setSize(image.getImageData().width, image.getImageData().height);
					e1.gc.drawImage(image, 0, 0);
				});
				canvas.pack(true);
				text.setText(filepath);
			} catch (SWTException | NegativeArraySizeException e2) {
				new ErrorDialog(shell, "An error occurred creating the image: " + e2.getMessage()).open();
			}
		}
	}
}