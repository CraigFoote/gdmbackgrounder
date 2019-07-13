package ca.footeware.backgrounder.painters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import com.steadystate.css.format.CSSFormat;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

/**
 * Responds to clicking of Set Image button by writing the selected image path
 * into the gdm3.css file, setting its background.
 * 
 * @author Footeware.ca
 *
 */
public class LoginBackgroundPainter {
	private Path cssPath;
	private Path imagePath;
	private String option;

	/**
	 * Constructor.
	 * 
	 * @param cssPath   {@link Path}
	 * @param imagePath {@link Path}
	 * @param option    {@link String}
	 */
	public LoginBackgroundPainter(Path cssPath, Path imagePath, String option) {
		this.cssPath = cssPath;
		this.imagePath = imagePath;
		this.option = option;
	}

	/**
	 * Writes a copy of the file at the provided path in the user's home folder.
	 * 
	 * @param path {@link Path}
	 */
	private void backupFile(Path path) {
		try {
			File backup = new File(
					"/home/" + System.getProperty("user.name") + File.separator + path.getFileName() + ".bak");
			if (backup.exists()) {
				boolean deleted = false;
				deleted = Files.deleteIfExists(backup.toPath());
				if (!deleted) {
					throw new IllegalStateException("Error deleting previous backup file.");
				}
			}
			Files.copy(path, Paths.get(backup.toURI()), StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e3) {
			throw new IllegalStateException("Error creating backup file.", e3);
		}
	}

	/**
	 * Reads the provided text file.
	 * 
	 * @param file {@link File}
	 * @return {@link String}
	 */
	private String getFileContents(File file) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat " + file.getAbsolutePath());
		} catch (IOException e1) {
			throw new IllegalStateException("Error occurred reading CSS file at " + file.getAbsolutePath(), e1);
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			return reader.lines().collect(Collectors.joining("\n"));
		} catch (Exception e2) {
			throw new IllegalStateException("An error occurred reading the stylesheet.", e2);
		}
	}

	/**
	 * Gets the owner of the file at the provided path.
	 * 
	 * @param path {@link Path}
	 * @return {@link UserPrincipal}
	 */
	private UserPrincipal getOwner(Path path) {
		try {
			return Files.getOwner(path);
		} catch (IOException e3) {
			throw new IllegalStateException("Unable to get file owner.", e3);
		}
	}

	/**
	 * Get the CSS rule governing GDM background.
	 * 
	 * @param stylesheet {@link CSSStyleSheetImpl}.
	 * 
	 * @return {@link CSSRule}
	 */
	private CSSRule getRule(CSSStyleSheetImpl stylesheet) {
		CSSRuleList cssRules = stylesheet.getCssRules();
		CSSRule rule = null;
		for (int i = 0; i < cssRules.getLength(); i++) {
			rule = cssRules.item(i);
			if (rule.getCssText().startsWith("#lockDialogGroup")) {
				break;
			}
		}
		if (rule == null) {
			throw new IllegalStateException("Could not find CSS rule for GDM background.");
		}
		return rule;
	}

	/**
	 * Parse the provided file into a CSS stylesheet.
	 * 
	 * @param file {@link File}
	 * @return {@link CSSStyleSheetImpl}
	 */
	private CSSStyleSheetImpl getStylesheet(File file) {
		String contents = getFileContents(file);
		InputSource inputSource = new InputSource(new StringReader(contents));
		CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
		try {
			return (CSSStyleSheetImpl) parser.parseStyleSheet(inputSource, null, null);
		} catch (IOException e1) {
			throw new IllegalStateException("An error occurred parsing the stylesheet.", e1);
		}
	}

	/**
	 * Gets the logged-in user.
	 * 
	 * @return {@link UserPrincipal}
	 */
	private UserPrincipal getUser() {
		UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
		try {
			return lookupService.lookupPrincipalByName(System.getProperty("user.name"));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to get user.", e);
		}
	}

	/**
	 * Makes the provided file writable by provided user.
	 * 
	 * @param user         {@link UserPrincipal}
	 * @param owner        {@link UserPrincipal}
	 * @param file         {@link File}
	 * @param fileLocation {@link String}
	 * @throws InterruptedException when shit goes south
	 */
	private void makeWritable(UserPrincipal user, UserPrincipal owner, File file) throws InterruptedException {
		try {
			if (!file.exists()) {
				throw new IllegalStateException("CSS file doesn't seem to exist. Black hole?");
			}
			if (!file.canWrite()) {
				Process process = Runtime.getRuntime().exec("pkexec chmod +w " + file.getAbsolutePath());
				int retVal = process.waitFor();
				if (retVal != 0) {
					throw new IllegalStateException("Couldn't chmod the file to writable.");
				}
				process = Runtime.getRuntime().exec("pkexec chown " + user + " " + file.getAbsolutePath());
				retVal = process.waitFor();
				if (retVal != 0) {
					throw new IllegalStateException("Couldn't chown the file temporarily to write to it.");
				}
				if (Files.getOwner(file.toPath()).getName().equals(owner.getName())) {
					throw new IllegalStateException("Chown of file did not work.");
				}
			}
		} catch (IOException e2) {
			throw new IllegalStateException(e2);
		} catch (InterruptedException e2) {
			throw e2;
		}

		if (!file.canWrite()) {
			throw new IllegalStateException("Can't write to file.");
		}
	}

	/**
	 * Write the image to CSS file
	 */
	public void paint() {
		backupFile(cssPath);
		File cssFile = new File(cssPath.toString());
		UserPrincipal owner = getOwner(cssPath);
		UserPrincipal user = getUser();
		try {
			makeWritable(user, owner, cssFile);
		} catch (InterruptedException e1) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e1);
		}
		CSSStyleSheetImpl stylesheet = getStylesheet(cssFile);
		CSSRule rule = getRule(stylesheet);
		setCSSRule(cssFile, rule, stylesheet);
		setOwner(cssFile, owner);
	}

	/**
	 * Set the provided rule in the provided stylesheet in the provided file.
	 * 
	 * @param file       {@link File}
	 * @param rule       {@link CSSRule}
	 * @param stylesheet {@link CSSStyleSheetImpl}
	 */
	private void setCSSRule(File file, CSSRule rule, CSSStyleSheetImpl stylesheet) {
		String css = "#lockDialogGroup {background: url('file://" + imagePath
				+ "'); background-repeat: no-repeat; background-size: " + option + "; background-position: center}";
		rule.setCssText(css);
		CSSFormat format = new CSSFormat();
		format.setRgbAsHex(true);
		List<String> formatList = Arrays.asList(stylesheet.getCssText(format));
		try {
			Files.write(file.toPath(), formatList, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException ioe) {
			throw new IllegalStateException("An error occurred writing new stylesheet to file.", ioe);
		}
	}

	/**
	 * Sets the provided principal as owner of the provided file.
	 * 
	 * @param file      {@link File}
	 * @param principal {@link UserPrincipal}
	 */
	private void setOwner(File file, UserPrincipal principal) {
		try {
			Files.setOwner(file.toPath(), principal);
		} catch (IOException e1) {
			throw new IllegalStateException("Can't chown file back to " + principal.getName() + ".", e1);
		}
	}

}
