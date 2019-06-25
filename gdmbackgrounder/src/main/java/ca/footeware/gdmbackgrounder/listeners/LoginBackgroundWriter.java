package ca.footeware.gdmbackgrounder.listeners;

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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import com.steadystate.css.format.CSSFormat;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

import ca.footeware.gdmbackgrounder.dialogs.ErrorDialog;

/**
 * Responds to clicking of Set Image button by writing the selected image path
 * into the gdm3.css file, setting its background.
 * 
 * @author Footeware.ca
 *
 */
public class LoginBackgroundWriter extends SelectionAdapter implements WriterDelegate {
	private final Button button;
	private final Shell shell;
	private final Text text;
	private static final String ERROR_MSG = "An error occurred: ";

	/**
	 * Constructor.
	 * 
	 * @param shell  {@link Shell}
	 * @param text   {@link Text}
	 * @param button {@link Button}
	 */
	public LoginBackgroundWriter(Shell shell, Text text, Button button) {
		this.shell = shell;
		this.text = text;
		this.button = button;
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
					new ErrorDialog(shell, "Error deleting previous backup file.").open();
					throw new IllegalStateException("Error deleting previous backup file.");
				}
			}
			Files.copy(path, Paths.get(backup.toURI()), StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e3) {
			new ErrorDialog(shell, "Error creating backup file.").open();
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
			new ErrorDialog(shell, ERROR_MSG + e1.getMessage()).open();
			throw new IllegalStateException("Error occurred reading CSS file at " + file.getAbsolutePath(), e1);
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			return reader.lines().collect(Collectors.joining("\n"));
		} catch (Exception e2) {
			new ErrorDialog(shell,
					"An error occurred reading the CSS file at " + file.getAbsolutePath() + ": " + e2.getMessage())
							.open();
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
			new ErrorDialog(shell, "Unable to get file owner").open();
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
			new ErrorDialog(shell, "Could not find CSS rule for GDM background.").open();
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
			new ErrorDialog(shell,
					"An error occurred parsing the CSS file at " + file.getAbsolutePath() + ": " + e1.getMessage())
							.open();
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
			new ErrorDialog(shell, "Unable to get user").open();
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
				new ErrorDialog(shell, "CSS file missing. Expected at " + file.getAbsolutePath()).open();
				throw new IllegalStateException("CSS file doesn't seem to exist. Black hole?");
			}
			if (!file.canWrite()) {
				Process process = Runtime.getRuntime().exec("pkexec chmod +w " + file.getAbsolutePath());
				int retVal = process.waitFor();
				if (retVal != 0) {
					new ErrorDialog(shell, "Couldn't chmod the CSS file at " + file.getAbsolutePath()).open();
					throw new IllegalStateException("Couldn't chmod the file to writable.");
				}
				process = Runtime.getRuntime().exec("pkexec chown " + user + " " + file.getAbsolutePath());
				retVal = process.waitFor();
				if (retVal != 0) {
					new ErrorDialog(shell, "Couldn't chown the CSS file at " + file.getAbsolutePath()).open();
					throw new IllegalStateException("Couldn't chown the file temporarily to write to it.");
				}
				if (Files.getOwner(file.toPath()).getName().equals(owner.getName())) {
					new ErrorDialog(shell, "Chmod of the CSS file at " + file.getAbsolutePath() + " did not work.")
							.open();
					throw new IllegalStateException("Chown of file did not work.");
				}
			}
		} catch (IOException e2) {
			new ErrorDialog(shell, ERROR_MSG + e2.getMessage()).open();
			throw new IllegalStateException(e2);
		} catch (InterruptedException e2) {
			new ErrorDialog(shell, ERROR_MSG + e2.getMessage()).open();
			throw e2;
		}

		if (!file.canWrite()) {
			new ErrorDialog(shell, "Couldn't write to the CSS file at " + file.getAbsolutePath()).open();
			throw new IllegalStateException("Can't write to file.");
		}
	}

	/**
	 * Set the provided rule in the provided stylesheet in the provided file.
	 * 
	 * @param file       {@link File}
	 * @param rule       {@link CSSRule}
	 * @param stylesheet {@link CSSStyleSheetImpl}
	 */
	private void setCSSRule(File file, CSSRule rule, CSSStyleSheetImpl stylesheet) {
		rule.setCssText("#lockDialogGroup {background: url(file://" + text.getText().trim()
				+ "); background-repeat: no-repeat; background-size: contain; background-position: center}");
		CSSFormat format = new CSSFormat();
		format.setRgbAsHex(true);
		List<String> formatList = Arrays.asList(stylesheet.getCssText(format));
		try {
			Files.write(file.toPath(), formatList, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException ioe) {
			new ErrorDialog(shell,
					"An error occurred writing the CSS file at " + file.getAbsolutePath() + ": " + ioe.getMessage())
							.open();
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
			new ErrorDialog(shell, "An error occurred chowning the CSS file at " + file.getAbsolutePath() + " back to "
					+ principal + ": " + e1.getMessage()).open();
			throw new IllegalStateException("Can't chown file back to " + principal.getName() + ".", e1);
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		super.widgetSelected(e);
		File cssFile = new File(text.getText().trim());
		backupFile(cssFile.toPath());
		UserPrincipal owner = getOwner(cssFile.toPath());
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
		button.setEnabled(false);
	}

}
