package ca.footeware.gdmbackgrounder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ca.footeware.gdmbackgrounder.dialogs.ErrorDialog;
import ca.footeware.gdmbackgrounder.listeners.BrowseButtonListener;
import ca.footeware.gdmbackgrounder.listeners.ImageDisposingShellListener;
import ca.footeware.gdmbackgrounder.listeners.LoginBackgroundWriter;

/**
 * Provides a GUI to select an image file and set it as the Gnome Display
 * Manager (GDM) login screen background.
 * 
 * @author Footeware.ca
 *
 */
public class Application {

	private Canvas canvas;
	private Image icon;
	private Image image;
	private Button setImageButton;

	/**
	 * Constructor.
	 * 
	 * @param cssFilename {@link String} location of CSS file to be modified.
	 */
	public Application(String cssFilename) {
		// shell
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("GDM Backgrounder");
		shell.setSize(500, 400);
		shell.setLayout(new GridLayout(2, false));

		// List of images to dispose of later
		List<Image> imagesToDispose = new ArrayList<>();

		// icon
		setIcon(display, shell);
		imagesToDispose.add(image);
		imagesToDispose.add(icon);

		// description
		Label label = new Label(shell, SWT.WRAP);
		label.setText("Browse to an image to set it as the background in your GDM login display. A backup copy of "
				+ cssFilename + " will be made in your home folder.");
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 0));

		// text box
		final Text text = new Text(shell, SWT.SEARCH);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 0, 0));
		text.setEditable(false);

		// browse button
		Button browseButton = new Button(shell, SWT.PUSH);
		Image browseImage = null;

		try (InputStream stream = Application.class.getClassLoader()
				.getResourceAsStream("icons8-search-folder-24.png")) {
			browseImage = new Image(display, stream);
			shell.setImage(getIcon());
		} catch (IOException | IllegalArgumentException e3) {
			new ErrorDialog(shell, "An error occurred getting the icon mutter mumble... " + e3.getMessage()).open();
		}
		browseButton.setImage(browseImage);
		imagesToDispose.add(browseImage);
		browseButton.setText("Browse");

		// scroll
		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayout(new GridLayout(1, false));
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// image area
		canvas = new Canvas(scrolledComposite, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setContent(canvas);

		// buttons
		createButtonPanel(shell, text, imagesToDispose);

		browseButton.addSelectionListener(
				new BrowseButtonListener(shell, text, display, getImage(), canvas, setImageButton));

		ImageDisposingShellListener shellListener = new ImageDisposingShellListener(imagesToDispose);
		shell.addDisposeListener(shellListener);

		// event loop
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * Creates the Set Image and Close buttons.
	 * 
	 * @param shell           {@link Shell}
	 * @param text            {@link Text}
	 * @param imagesToDispose {@link List} of {@link Image}
	 */
	private void createButtonPanel(Shell shell, Text text, List<Image> imagesToDispose) {
		Composite buttonPanel = new Composite(shell, SWT.BORDER_DOT);
		buttonPanel.setLayout(new GridLayout(2, true));
		buttonPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		setImageButton = new Button(buttonPanel, SWT.PUSH);
		setImageButton.setText("Set as Login Background");
		setImageButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		setImageButton.addSelectionListener(new LoginBackgroundWriter(shell, text, setImageButton));
		Image setImageButtonIcon = null;
		try (InputStream stream = Application.class.getClassLoader().getResourceAsStream("icons8-image-24.png")) {
			setImageButtonIcon = new Image(shell.getDisplay(), stream);
			setImageButton.setImage(setImageButtonIcon);
		} catch (IOException | IllegalArgumentException e3) {
			new ErrorDialog(shell, "An error occurred getting the icon mutter mumble... " + e3.getMessage()).open();
		}
		imagesToDispose.add(setImageButtonIcon);
		setImageButton.setEnabled(false);

		Button closeButton = new Button(buttonPanel, SWT.PUSH);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Image closeIcon = null;
		try (InputStream stream = Application.class.getClassLoader().getResourceAsStream("icons8-close-window-24.png")) {
			closeIcon = new Image(shell.getDisplay(), stream);
			closeButton.setImage(closeIcon);
		} catch (IOException | IllegalArgumentException e3) {
			new ErrorDialog(shell, "An error occurred getting the icon mutter mumble... " + e3.getMessage()).open();
		}
		imagesToDispose.add(closeIcon);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				System.exit(0);
			}
		});
	}

	/**
	 * Gets the application icon.
	 * 
	 * @return {@link Image}
	 */
	public Image getIcon() {
		return icon;
	}

	/**
	 * Gets the displayed image.
	 * 
	 * @return {@link Image}
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Sets the application icon.
	 * 
	 * @param display {@link Display}
	 * @param shell   {@link Shell}
	 */
	private void setIcon(Display display, Shell shell) {
		try (InputStream stream = Application.class.getClassLoader().getResourceAsStream("programmer.png")) {
			icon = new Image(display, stream);
			shell.setImage(getIcon());
		} catch (IOException | IllegalArgumentException e3) {
			new ErrorDialog(shell, "An error occurred getting the icon mutter mumble: " + e3.getMessage()).open();
		}
	}

	/**
	 * Sets the image displayed.
	 * 
	 * @param image {@link Image}
	 */
	public void setImage(Image image) {
		this.image = image;
	}

}
