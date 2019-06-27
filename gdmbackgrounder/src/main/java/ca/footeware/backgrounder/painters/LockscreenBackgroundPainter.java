/**
 * 
 */
package ca.footeware.backgrounder.painters;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes the image at the provided path to the desktop wallpaper.
 * 
 * @author Footeware.ca
 *
 */
public class LockscreenBackgroundPainter {

	private Path path;
	private String option;

	/**
	 * Constructor.
	 * 
	 * @param path {@link Path}
	 * @param option {@link String}
	 */
	public LockscreenBackgroundPainter(Path path, String option) {
		this.path = path;
		this.option = option;
	}

	/**
	 * Set the background to the image found at the provided {@link Path}.
	 * 
	 * @throws IOException          when shit goes south
	 * @throws InterruptedException when more shit goes south
	 * 
	 */
	public void paint() throws IOException, InterruptedException {
		String cmd = "gsettings set org.gnome.desktop.screensaver picture-uri 'file://" + path + "'";
		Runtime run = Runtime.getRuntime();
		Process pr = run.exec(cmd);
		pr.waitFor();
		
		cmd = "gsettings set org.gnome.desktop.background picture-options '" + option + "'";
		run = Runtime.getRuntime();
		pr = run.exec(cmd);
		pr.waitFor();
	}

}
