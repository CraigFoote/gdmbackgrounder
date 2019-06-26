package ca.footeware.backgrounder.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Displays a shell modal to the provided shell that displays the provided
 * message.
 * 
 * @author Footeware.ca
 *
 */
public class ErrorDialog {

	private Shell dialog;

	/**
	 * Constructor.
	 * 
	 * @param shell   {@link Shell}
	 * @param message {@link String}
	 */
	public ErrorDialog(Shell shell, String message) {
		dialog = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.spacing = 10;
		dialog.setLayout(layout);
		dialog.setText("Error");
		Text error = new Text(dialog, SWT.MULTI | SWT.WRAP);
		error.setText(message);
		error.setEditable(false);
		Button closeButton = new Button(dialog, SWT.PUSH);
		closeButton.setText("Close");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.close();
			}
		});
		error.pack();
		dialog.pack();
	}

	/**
	 * Closes the dialog.
	 */
	public void open() {
		dialog.open();
	}
}