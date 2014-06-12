package de.e_nexus.eclipse.plugins.eclipselink;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements
		EclipseLinkURLListener, IStartup {

	
	public static EclipseLinkThread thread;

	// The plug-in ID
	public static final String PLUGIN_ID = "eclipselink"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		getThread().listeners.add(this);
		if (!getThread().isAlive())
			getThread().start();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		getThread().listeners.remove(this);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public void notifyURL(String value) {
		String url = value.split("\n")[0];
		url = url.substring(4, url.length() - 9).trim();

		String[] parts = url.split(":/");
		url = parts[parts.length - 1];
		parts = url.split(":");
		int line = -1;
		int col = -1;

		if (parts.length == 2) {
			url = parts[0];
			line = Integer.parseInt(parts[1]);
		}
		if (parts.length == 3) {
			url = parts[0];
			line = Integer.parseInt(parts[1]);
			col = Integer.parseInt(parts[2]);
		}

		for (final IWorkbenchWindow window : getWorkbench()
				.getWorkbenchWindows()) {
			Display display = getWorkbench().getDisplay();

			final int myLine = line, myCol = col;
			final String myUrl = url;
			display.syncExec(new Runnable() {
				public void run() {
					try {
						IProject[] projects = ResourcesPlugin.getWorkspace()
								.getRoot().getProjects();
						IEditorPart openEditor = null;
						String currentURL = myUrl + "";
						int count = 0;
						while (openEditor == null) {
							if (count++ > 50)
								break;

							openEditor = tryOpen(currentURL, window, projects);
							if (currentURL.indexOf('/') == -1)
								break;
							currentURL = currentURL.substring(currentURL
									.indexOf('/') + 1);
						}
						if (openEditor != null)
							select(myLine, myCol, openEditor);

					} catch (BadLocationException e) {

					}
				}

				private IEditorPart tryOpen(final String url,
						final IWorkbenchWindow window, IProject[] projects) {

					try {
						IPath fromOSString = Path.fromOSString(url);

						for (IProject p : projects) {

							final IFile file = p.getFile(fromOSString);
							if (file.isAccessible())
								return IDE.openEditor(window.getActivePage(),
										file);
						}
					} catch (Exception e) {

					}
					return null;
				}
			});

		}

	}

	private void select(final int myLine, final int myCol,
			IEditorPart openEditor) throws BadLocationException {
		if (myLine != -1) {
			if (openEditor instanceof ITextEditor) {
				ITextEditor edit = (ITextEditor) openEditor;
				IDocumentProvider documentProvider = edit.getDocumentProvider();
				IRegion lineInformation = documentProvider.getDocument(
						openEditor.getEditorInput()).getLineInformation(myLine);

				if (myCol == -1) {
					edit.selectAndReveal(lineInformation.getOffset(),
							lineInformation.getLength());

				} else {
					edit.selectAndReveal(lineInformation.getOffset(), myCol);
				}
			}
		}
	}

	@Override
	public void earlyStartup() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					getThread().start();
				} catch (IOException e) {
					throw new IllegalStateException("Plugin unable to load!", e);
				}
			}
		});

	}

	

	public static EclipseLinkThread getThread() throws IOException {
		if (thread == null)
			thread = new EclipseLinkThread();
		return thread;
	}
}
