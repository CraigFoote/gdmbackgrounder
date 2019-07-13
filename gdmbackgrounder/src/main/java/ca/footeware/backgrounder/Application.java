package ca.footeware.backgrounder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ca.footeware.backgrounder.dialogs.ErrorDialog;
import ca.footeware.backgrounder.listeners.BrowseButtonListener;
import ca.footeware.backgrounder.listeners.ImageDisposingDisposeListener;
import ca.footeware.backgrounder.painters.LockscreenBackgroundPainter;
import ca.footeware.backgrounder.painters.LoginBackgroundPainter;
import ca.footeware.backgrounder.painters.WallpaperPainter;

/**
 * Provides a GUI to select an image file and set it as your desktop wallpaper,
 * Gnome Display Manager (GDM) login screen background, and lock screen
 * background.
 * 
 * @author Footeware.ca
 *
 */
public class Application {

	private Canvas canvas;
	private List<Image> imagesToDispose;
	private Shell shell;
	private Text text;
	private static final String IMAGE_ICON = "image-24.png";
	private Button desktopWallpaper;
	private Button lockScreenBackground;
	private Button loginScreenBackground;
	private static final String[] PICTURE_OPTIONS = new String[] { "none", "wallpaper", "centered", "scaled",
			"stretched", "zoom", "spanned" };

	/**
	 * Constructor.
	 */
	public Application() {
		// shell
		final Display display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("Backgrounder");
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(2, false));

		// List of images to dispose of later
		imagesToDispose = new ArrayList<>();
		shell.addDisposeListener(new ImageDisposingDisposeListener(imagesToDispose));

		// window icon
		shell.setImage(getImage("programmer.png"));

		// description
		Label label = new Label(shell, SWT.WRAP);
		label.setText(
				"Browse to an image and set it as your desktop wallpaper, lock screen background and login screen background.");
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 0));

		// text box
		text = new Text(shell, SWT.SEARCH);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 0, 0));
		text.setEditable(false);
		text.addModifyListener(e -> enableButtons(text.getText().length() > 0));

		// browse button
		Button browseButton = new Button(shell, SWT.PUSH);
		browseButton.setImage(getImage("search-folder-24.png"));
		browseButton.setText("Browse");

		// scroll
		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayout(new GridLayout(1, false));
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// image area
		canvas = new Canvas(scrolledComposite, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		scrolledComposite.setContent(canvas);

		// buttons
		createButtonPanel();

		browseButton.addSelectionListener(new BrowseButtonListener(shell, text, canvas));

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
	 * @param b
	 */
	protected void enableButtons(boolean b) {
		desktopWallpaper.setEnabled(b);
		lockScreenBackground.setEnabled(b);
		loginScreenBackground.setEnabled(b);
	}

	/**
	 * Creates the panel with its buttons.
	 */
	private void createButtonPanel() {
		createWallpaperControls();
		createLockScreenControls();
		createLoginScreenControls();

		Button closeBtn = new Button(shell, SWT.PUSH | SWT.WRAP);
		closeBtn.setText("&Close");
		closeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		closeBtn.setImage(getImage("close-window-24.png"));
		closeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
	}

	/**
	 */
	private void createLoginScreenControls() {
		Group group = new Group(shell, SWT.NONE);
		group.setText("Login Screen Background");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		group.setLayout(new GridLayout(2, false));

		Combo loginscreenCombo = new Combo(group, SWT.SIMPLE | SWT.DROP_DOWN | SWT.READ_ONLY);
		loginscreenCombo.setItems(new String[] { "contain", "cover" });
		loginscreenCombo.setText("cover");
		loginscreenCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		loginScreenBackground = new Button(group, SWT.PUSH | SWT.WRAP);
		loginScreenBackground.setText("Set login screen background");
		loginScreenBackground.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		loginScreenBackground.setImage(getImage(IMAGE_ICON));
		loginScreenBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Path cssPath = FileSystems.getDefault().getPath("/usr/share/gnome-shell/theme/gdm3.css");
				Path imagePath = FileSystems.getDefault().getPath(text.getText().trim());
				LoginBackgroundPainter painter = new LoginBackgroundPainter(cssPath, imagePath, loginscreenCombo.getText());
				painter.paint();
			}
		});
		loginScreenBackground.setEnabled(false);
	}

	/**
	 */
	private void createLockScreenControls() {
		Group group = new Group(shell, SWT.NONE);
		group.setText("Lock Screen Background");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		group.setLayout(new GridLayout(2, false));

		Combo lockscreenCombo = new Combo(group, SWT.SIMPLE | SWT.DROP_DOWN | SWT.READ_ONLY);
		lockscreenCombo.setItems(PICTURE_OPTIONS);
		lockscreenCombo.setText("zoom");
		lockscreenCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		lockScreenBackground = new Button(group, SWT.PUSH | SWT.WRAP);
		lockScreenBackground.setText("Set lock screen background");
		lockScreenBackground.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		lockScreenBackground.setImage(getImage(IMAGE_ICON));
		lockScreenBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Path path = FileSystems.getDefault().getPath(text.getText().trim());
				LockscreenBackgroundPainter painter = new LockscreenBackgroundPainter(path, lockscreenCombo.getText());
				try {
					painter.paint();
				} catch (IOException | InterruptedException e1) {
					new ErrorDialog(shell, "An error occurred setting the lockscreen background." + e1.getMessage())
							.open();
					Thread.currentThread().interrupt();
				}
			}
		});
		lockScreenBackground.setEnabled(false);
	}

	/**
	 */
	private void createWallpaperControls() {
		Group group = new Group(shell, SWT.NONE);
		group.setText("Desktop Wallpaper");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		group.setLayout(new GridLayout(2, false));

		Combo wallpaperCombo = new Combo(group, SWT.SIMPLE | SWT.DROP_DOWN | SWT.READ_ONLY);
		wallpaperCombo.setItems(PICTURE_OPTIONS);
		wallpaperCombo.setText("zoom");
		wallpaperCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		desktopWallpaper = new Button(group, SWT.PUSH | SWT.WRAP);
		desktopWallpaper.setText("Set desktop wallpaper");
		desktopWallpaper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		desktopWallpaper.setImage(getImage(IMAGE_ICON));
		desktopWallpaper.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Path path = FileSystems.getDefault().getPath(text.getText().trim());
				WallpaperPainter painter = new WallpaperPainter(path, wallpaperCombo.getText());
				try {
					painter.paint();
				} catch (IOException | InterruptedException e1) {
					new ErrorDialog(shell, "An error occurred setting the wallpaper." + e1.getMessage()).open();
					Thread.currentThread().interrupt();
				}
			}
		});
		desktopWallpaper.setEnabled(false);
	}

	/**
	 * Gets the image of the provided filename.
	 * 
	 * @param filename {@link String}
	 * @return {@link Image}
	 */
	public Image getImage(String filename) {
		Image image = null;
		try (InputStream stream = Application.class.getClassLoader().getResourceAsStream(filename)) {
			image = new Image(shell.getDisplay(), stream);
		} catch (IOException | IllegalArgumentException e) {
			new ErrorDialog(shell, "An error occurred getting the image mutter mumble... " + e.getMessage()).open();
		}
		imagesToDispose.add(image);
		return image;
	}

}
