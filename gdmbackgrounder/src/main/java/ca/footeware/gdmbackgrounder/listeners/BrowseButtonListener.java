package ca.footeware.gdmbackgrounder.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ca.footeware.gdmbackgrounder.dialogs.ErrorDialog;

/**
 * Responds to Browse button clicks by opening a file chooser dialog then, once
 * an image is chosen, adds the file path to the text box and displays the image
 * in the image panel.
 * 
 * @author Footeware.ca
 *
 */
public class BrowseButtonListener extends SelectionAdapter {
	private final Button button;
	private final Canvas canvas;
	private final Display display;
	private Image image;
	private final Shell shell;
	private final Text text;

	/**
	 * Constructor.
	 * 
	 * @param shell   {@link Shell}
	 * @param text    {@link Text}
	 * @param display {@link Display}
	 * @param canvas  {@link Canvas}
	 * @param image   {@link Image}
	 * @param button  {@link Button}
	 */
	public BrowseButtonListener(Shell shell, Text text, Display display, Image image, Canvas canvas, Button button) {
		this.shell = shell;
		this.text = text;
		this.display = display;
		this.image = image;
		this.canvas = canvas;
		this.button = button;
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
			if (image != null && !image.isDisposed()) {
				image.dispose();
			}
			try {
				image = new Image(display, filepath);
				canvas.pack(false);
				canvas.addPaintListener(new PaintListener() {
					@Override
					public void paintControl(PaintEvent e) {
						canvas.setSize(image.getImageData().width, image.getImageData().height);
						e.gc.drawImage(image, 0, 0);
					}
				});
				text.setText(filepath);
				button.setEnabled(true);
			} catch (SWTException | SWTError e1) {
				new ErrorDialog(shell, filepath + " is not a valid image:\n" + e1.getMessage()).open();
			}
		}
	}
}