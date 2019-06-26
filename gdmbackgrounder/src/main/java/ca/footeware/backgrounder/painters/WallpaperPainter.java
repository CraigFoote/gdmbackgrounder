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
public class WallpaperPainter {

	private Path path;

	/**
	 * Constructor.
	 * 
	 * @param path {@link Path}
	 */
	public WallpaperPainter(Path path) {
		this.path = path;
	}

	/**
	 * Set the background to the image found at the provided {@link Path}.
	 * 
	 * @throws IOException          when shit goes south
	 * @throws InterruptedException when more shit goes south
	 * 
	 */
	public void paint() throws IOException, InterruptedException {
		String cmd = "gsettings set org.gnome.desktop.background picture-uri 'file://" + path + "'";
		Runtime run = Runtime.getRuntime();
		Process pr = run.exec(cmd);
		pr.waitFor();
	}

}
