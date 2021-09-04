package de.unlixx.runpng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

import org.w3c.dom.Element;

import de.unlixx.runpng.bitmap.Bitmap32;
import de.unlixx.runpng.bitmap.Bitmap32Optimizer;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.png.chunks.PngText;
import de.unlixx.runpng.png.io.PngChunkInputStream;
import de.unlixx.runpng.png.io.PngChunkOutputStream;
import de.unlixx.runpng.png.io.PngProjectInputStream;
import de.unlixx.runpng.png.io.PngProjectOutputStream;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.Progress;
import de.unlixx.runpng.util.SortedList;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.Version;
import de.unlixx.runpng.util.event.ValueEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Implements an AppFileManager used for file handling and
 * user interaction.
 *
 * @author H. Unland (https://github.com/HUnland)
 *
   <!--
   Copyright 2021 H. Unland (https://github.com/HUnland)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   -->
 */
public class AppFileManager
{
	static final String PNGKEYRECENTLY = "png/recentlyused";
	static final String PNGKEYLASTPATHPNGOPEN = "path.png.open";
	static final String PNGKEYLASTPATHPNGSAVE = "path.png.save";
	static final String PNGKEYLASTPATHPROJECTOPEN = "path.project.open";
	static final String PNGKEYLASTPATHPROJECTSAVE = "path.project.save";
	static final String PNGKEYLASTUSED = "lastused";

	App m_app = App.getMainApp();

	Preferences m_prefsRoot = Preferences.userNodeForPackage(App.class);
	Preferences m_prefsRecently = m_prefsRoot.node(PNGKEYRECENTLY);

	File m_filePng;
	File m_fileProject;

	/**
	 * Constructor for this AppFileManager.
	 */
	AppFileManager()
	{
	}

	/**
	 * Gets the current png file.
	 *
	 * @return A {@link File} object.
	 */
	File getPngFile()
	{
		return m_filePng;
	}

	/**
	 * Gets the current project file.
	 *
	 * @return A {@link File} object.
	 */
	File getProjectFile()
	{
		return m_filePng;
	}

	/**
	 * Checks whether a project is currently loaded.
	 *
	 * @return True if there is a project loaded.
	 */
	boolean isProject()
	{
		return m_fileProject != null;
	}

	/**
	 * Gets the filename (without extension) of the currently active file.
	 *
	 * @return A string containing the filename. Or the localized unnamed name
	 * if there is no file actually.
	 */
	String getFilename()
	{
		if (m_filePng == null)
		{
			return Loc.getString("file.name.unnamed");
		}

		return Util.filenameWithoutExt(m_filePng.getName());
	}

	/**
	 * Gets the filename (without extension) of the currently active project file.
	 *
	 * @return A string containing the filename of the current project file.
	 * Or the normal filename if there is no project file actually.
	 */
	String getProjectFilename()
	{
		if (m_fileProject == null)
		{
			return getFilename();
		}

		return Util.filenameWithoutExt(m_fileProject.getName());
	}

	/**
	 * Resets the actual files to null.
	 */
	void doFileNew()
	{
		m_filePng = null;
		m_fileProject = null;
	}

	/**
	 * Checks whether there is a project file loaded.
	 *
	 * @return True if there is a project file loaded.
	 */
	boolean haveProject()
	{
		return m_fileProject != null;
	}

	/**
	 * Gets the most recently file path from the preferences store.
	 *
	 * @param astrKeys A variable args list of strings containing the
	 * preference keys.
	 * @return A string with a path. Or null if none has been stored yet.
	 */
	String getPathFromPrefs(String... astrKeys)
	{
		for (String strKey : astrKeys)
		{
			String strPath = m_prefsRecently.get(strKey, null);
			if (strPath != null)
			{
				return strPath;
			}
		}

		return null;
	}

	/**
	 * Collects the current content in a {@link PngProject} object,
	 * starts a {@link Progress} task and returns.
	 * The Progress task saves the project autonomously in a GUI independent thread to the given file.
	 * If an error occurs then the user will be informed with a message box.
	 *
	 * @param file The {@link File} to save the PngProject content.
	 */
	void saveProjectFile(final File file)
	{
		m_app.setWaitCursor();

		final PngProject project = new PngProject();
		m_app.collectProject(project);

		Progress<Void> progress = new Progress<Void>(m_app.getProgressBar(), 1)
		{
			@Override
			protected Void call() throws Exception
			{
				try (PngProjectOutputStream ppos = new PngProjectOutputStream(new FileOutputStream(file)))
				{
					ppos.write(project, this);
				}

				updateProgress(100, 100); // Just to be sure

				return null;
			}
		};

		progress.setOnFailed(value ->
		{
			m_app.setDefaultCursor();

			Throwable t = progress.getException();
			if (t != null)
			{
				Util.showError(t);
			}
		});

		progress.setOnSucceeded(value ->
		{
			m_app.setDefaultCursor();
			m_app.setClean();
		});

		Thread thread = new Thread(progress);
		thread.setDaemon(false);
		thread.start();
	}

	/**
	 * Shows a save dialog to the user and starts a project save.
	 *
	 * @return True if the saving has been started. False if the user cancelled it.
	 */
	boolean onSaveProjectFileAs()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle(Loc.getString("title.file.saveas.project"));

		chooser.getExtensionFilters().add(
				new ExtensionFilter(Loc.getString("filter.file.project"), "*.rpz"));

		String strPath = getPathFromPrefs(PNGKEYLASTPATHPROJECTSAVE, PNGKEYLASTPATHPROJECTOPEN, PNGKEYLASTPATHPNGSAVE, PNGKEYLASTPATHPNGOPEN);
		if (strPath != null)
		{
			File path = new File(strPath);
			if (path.exists() && path.canWrite())
			{
				chooser.setInitialDirectory(path);
			}
		}

		String strNameProposed = getProjectFilename() + ".rpz";
		chooser.setInitialFileName(strNameProposed);

		File file = chooser.showSaveDialog(m_app.getMainWindow());
		if (file != null)
		{
			saveProjectFile(file);

			m_fileProject = file;

			m_prefsRecently.put(PNGKEYLASTUSED, file.getPath());
			m_prefsRecently.put(PNGKEYLASTPATHPROJECTSAVE, file.getParent());

			return true;
		}

		return false;
	}

	/**
	 * Saves the current project content if there is already a known file.
	 * If not then the file needs to be saved with user interaction.
	 */
	void onSaveProjectFile()
	{
		if (m_fileProject != null)
		{
			saveProjectFile(m_fileProject);
		}
		else
		{
			onSaveProjectFileAs();
		}
	}

	/**
	 * Optimizes the given Bitmap32Sequence for saving, starts a
	 * {@link Progress} task and returns.
	 * The Progress task saves the sequence autonomously in a GUI independent thread to the given file.
	 * If an error occurs then the user will be informed with a message box.
	 *
	 * @param file The {@link File} to save the Bitmap32Sequence.
	 * @param sequence A {@link Bitmap32Sequence} object.
	 * @param bSetClean If true then the "clean" flag shall be set in the undo manager after successful save.
	 */
	void savePngFile(final File file, final Bitmap32Sequence sequence, boolean bSetClean)
	{
		m_app.setWaitCursor();

		if (!sequence.isOptimized() && sequence.isAnimated())
		{
			Bitmap32Optimizer.optimize(sequence);
		}

		Bitmap32Optimizer.optimizeColorType(sequence);

		sequence.addTextChunk(new PngText("Software", Loc.getString("app.title")));
		sequence.addTextChunk(new PngText("Version", App.APP_VERSION.toString()));

		// Test
		/*
		sequence.addTextChunk(new PngText("Comment", 0, "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.")); // zTXt
		sequence.addTextChunk(new PngText("Cømment", 0, 0, "da_DK", "Cømment", "Smørre brød, smørre brød, rømmpømmpømmpømm! Lørem ipsum dølør sit amet, cønsetetur sadipscing elitr, sed diam nønumy eirmød tempør invidunt ut labøre et døløre magna aliquyam erat, sed diam vøluptua.")); // iTXt uncompressed
		sequence.addTextChunk(new PngText("Cømment", 1, 0, "da_DK", "Cømment", "Smørre brød, smørre brød, rømmpømmpømmpømm! Lørem ipsum dølør sit amet, cønsetetur sadipscing elitr, sed diam nønumy eirmød tempør invidunt ut labøre et døløre magna aliquyam erat, sed diam vøluptua.")); // iTXt compressed
		*/

		Progress<Void> progress = new Progress<Void>(m_app.getProgressBar(), PngChunkOutputStream.calcStepsForSave(sequence))
		{
			@Override
			protected Void call() throws Exception
			{
				try (PngChunkOutputStream pcos = new PngChunkOutputStream(new FileOutputStream(file)))
				{
					pcos.write(sequence, this);
				}

				return null;
			}
		};

		progress.setOnFailed(value ->
		{
			m_app.setDefaultCursor();

			Throwable t = progress.getException();
			if (t != null)
			{
				Util.showError(t);
			}
		});

		progress.setOnSucceeded(value ->
		{
			m_app.setDefaultCursor();
			if (bSetClean)
			{
				m_app.setClean();
			}
		});

		Thread thread = new Thread(progress);
		thread.setDaemon(false);
		thread.start();
	}

	/**
	 * Shows a save dialog to the user and starts a png sequence save.
	 *
	 * @return True if the saving has been started. False if the user cancelled it.
	 */
	boolean onSavePngFileAs()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle(Loc.getString("title.file.saveas"));

		chooser.getExtensionFilters().add(
				new ExtensionFilter(Loc.getString("filter.file.pngapng"), "*.png", "*.apng"));

		Preferences prefsRoot = Preferences.userNodeForPackage(App.class);
		Preferences prefsRec = prefsRoot.node(PNGKEYRECENTLY);
		String strPath = getPathFromPrefs(PNGKEYLASTPATHPNGSAVE, PNGKEYLASTPATHPNGOPEN, PNGKEYLASTPATHPROJECTSAVE, PNGKEYLASTPATHPROJECTOPEN);
		if (strPath != null)
		{
			File path = new File(strPath);
			if (path.exists() && path.canWrite())
			{
				chooser.setInitialDirectory(path);
			}
		}

		String strNameProposed = getFilename() + ".png";
		chooser.setInitialFileName(strNameProposed);

		File file = chooser.showSaveDialog(m_app.getMainWindow());
		if (file != null)
		{
			savePngFile(file, m_app.getFramesView().createSequence(false, true, false), m_fileProject == null);

			m_filePng = file;

			prefsRec.put(PNGKEYLASTUSED, file.getPath());
			prefsRec.put(PNGKEYLASTPATHPNGSAVE, file.getParent());

			return true;
		}

		return false;
	}

	/**
	 * Saves the current png sequence if there is already a known file.
	 * If not then the file needs to be saved with user interaction.
	 *
	 * @return True if the save process has not been cancelled by the user.
	 */
	boolean onSaveFile()
	{
		if (m_fileProject != null)
		{
			saveProjectFile(m_fileProject);
			return true;
		}

		if (m_filePng != null)
		{
			savePngFile(m_filePng, m_app.getFramesView().createSequence(false, true, false), true);
			return true;
		}

		return onSaveProjectFileAs();
	}

	/**
	 * Shows a save dialog to the user and starts saving a single
	 * image from FramePane under a separate filename.
	 *
	 * @param image An object of type {@link Image}.
	 * @param strNameProposed A string with a proposed filename.
	 * @return True if the saving has been started. False if the user cancelled it.
	 */
	boolean onSavePngFileAs(Image image, String strNameProposed)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle(Loc.getString("title.file.saveas"));

		chooser.getExtensionFilters().add(
				new ExtensionFilter(Loc.getString("filter.file.png"), "*.png"));

		Preferences prefsRoot = Preferences.userNodeForPackage(App.class);
		Preferences prefsRec = prefsRoot.node(PNGKEYRECENTLY);
		String strPath = getPathFromPrefs(PNGKEYLASTPATHPNGSAVE, PNGKEYLASTPATHPNGOPEN, PNGKEYLASTPATHPROJECTSAVE, PNGKEYLASTPATHPROJECTOPEN);
		if (strPath != null)
		{
			File path = new File(strPath);
			if (path.exists() && path.canWrite())
			{
				chooser.setInitialDirectory(path);
			}
		}

		if (strNameProposed != null && !strNameProposed.isEmpty())
		{
			chooser.setInitialFileName(strNameProposed);
		}

		File file = chooser.showSaveDialog(m_app.getMainWindow());
		if (file != null)
		{
			Bitmap32Sequence sequence = m_app.getFramesView().createSequence(image);
			savePngFile(file, sequence, false);

			prefsRec.put(PNGKEYLASTUSED, file.getPath());
			prefsRec.put(PNGKEYLASTPATHPNGSAVE, file.getParent());

			return true;
		}

		return false;
	}

	/**
	 * Validates a loaded PngProject for usability. Informs the user with
	 * a message box if there is a problem.
	 *
	 * @param project A {@link PngProject}.
	 * @return True if the PngProject has been successfully validated.
	 */
	boolean validateProject(PngProject project)
	{
		String strSoftware = project.getMetaSoftwareName();
		if (!App.APP_NAME.equals(strSoftware))
		{
			Util.showError("title.project.error", "message.error.project.unknownsoftware");
			return false;
		}

		Version version = project.getMetaVersion();
		if (!version.isValid())
		{
			Util.showError("title.project.error", "message.error.project.noversion");
			return false;
		}

		if (App.APP_VERSION.compareTo(version) < 0)
		{
			Util.showError("title.project.error", "message.error.project.higherversion", version.toString(), App.APP_VERSION.toString());
			return false;
		}

		List<Element> files = project.getFileDescriptions();
		for (Element file : files)
		{
			String strName = file.getAttribute("name"),
					strType = file.getAttribute("type");
			if ("png".equals(strType) && null == project.getNamedSequence(strName))
			{
				Util.showError("title.project.error", "message.error.project.missinginternalfile", strName);
				return false;
			}
		}

		return true;
	}

	/**
	 * Starts a {@link Progress} task to open a given project file and returns.
	 * The Progress task opens and loads the project autonomously in a GUI independent thread. If an error
	 * occurs then the user will be informed with a message box.
	 *
	 * @param file The {@link File} to open.
	 */
	void openProjectFile(final File file)
	{
		Progress<PngProject> progress = new Progress<PngProject>(m_app.getProgressBar(), 1)
		{
			@Override
			protected PngProject call() throws Exception
			{
				PngProject project;

				updateProgress(0, file.length());

				try (PngProjectInputStream ppis = new PngProjectInputStream(new FileInputStream(file)))
				{
					project = ppis.read(this);
				}

				updateProgress(100, 100);

				return project;
			}
		};

		m_app.setWaitCursor();

		progress.setOnFailed(value ->
		{
			m_app.setDefaultCursor();

			Throwable t = progress.getException();
			if (t != null)
			{
				Util.showError(t);
			}
		});

		progress.setOnSucceeded(value ->
		{
			m_app.setDefaultCursor();

			try
			{
				PngProject project = progress.getValue();
				if (validateProject(project))
				{
					m_fileProject = file;
					m_app.applyProject(project);
				}
			}
			catch (Throwable t)
			{
				Util.showError(t);
			}
		});

		Thread thread = new Thread(progress);
		thread.setDaemon(false);
		thread.start();
	}

	/**
	 * Starts a {@link Progress} task to open a given png file or a series
	 * of png files and returns.
	 * The Progress task opens and loads each file autonomously in a GUI independent thread. If a png file
	 * is completely loaded then a receive handler will be invoked for handover.
	 * If an error occurs then the user will be informed with a message box.
	 *
	 * <pre>
	 * See also {@link ValueEvent}.
	 * </pre>
	 *
	 * @param afiles An array of {@link File} objects to open.
	 * @param receiveHandler An object of type {@link EventHandler}.
	 */
	public void openPngFiles(File[] afiles, EventHandler<ValueEvent<Bitmap32Sequence>> receiveHandler)
	{
		final File[] afilesLocal = new File[afiles.length];
		System.arraycopy(afiles, 0, afilesLocal, 0, afiles.length);

		long lLen = 0;
		for (File file : afilesLocal)
		{
			lLen += file.length();
		}

		Progress<Bitmap32Sequence> progress = new Progress<Bitmap32Sequence>(m_app.getProgressBar(), lLen)
		{
			@Override
			protected Bitmap32Sequence call() throws Exception
			{
				for (File file : afilesLocal)
				{
					try (PngChunkInputStream pcis = new PngChunkInputStream(new FileInputStream(file)))
					{
						long lDone = getDone();

						Bitmap32Sequence sequence = pcis.read(this);

						// This corrects the progress-value for the case
						// that the file has additional data after IEND.
						updateProgress(lDone + file.length());

						clearAcknowledge();
						updateValue(sequence);

						while (!getAcknowledge())
						{
							Thread.sleep(10);
						}
					}
				}

				return null;
			}
		};

		m_app.setWaitCursor();

		progress.valueProperty().addListener(obs ->
		{
			Bitmap32Sequence sequence = progress.getValue();
			if (sequence != null)
			{
				try
				{
					ValueEvent<Bitmap32Sequence> event = new ValueEvent<Bitmap32Sequence>(sequence);
					receiveHandler.handle(event);
				}
				finally
				{
					progress.acknowledge();
				}
			}
		});

		progress.setOnFailed(value ->
		{
			m_app.setDefaultCursor();

			Throwable t = progress.getException();
			if (t != null)
			{
				Util.showError(t);
			}
		});

		progress.setOnSucceeded(value ->
		{
			m_app.setDefaultCursor();
		});

		Thread thread = new Thread(progress);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Shows a file open dialog to the user.
	 *
	 * @param strTitle A string with a localizable title id.
	 * @param bMultiple True if the user can choose multiple files.
	 * @param aFilters A variable arguments list of {@link ExtensionFilter} objects.
	 * @return An array of {@link File} object(s) or null
	 * if the user cancelled the operation.
	 */
	File[] fileOpenDialog(String strTitle, boolean bMultiple, ExtensionFilter... aFilters)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle(Loc.getString(strTitle));

		chooser.getExtensionFilters().addAll(aFilters);

		Preferences prefsRoot = Preferences.userNodeForPackage(App.class);
		Preferences prefsRec = prefsRoot.node(PNGKEYRECENTLY);
		String strPath = getPathFromPrefs(PNGKEYLASTPATHPNGOPEN, PNGKEYLASTPATHPNGSAVE, PNGKEYLASTPATHPROJECTOPEN, PNGKEYLASTPATHPROJECTSAVE);
		if (strPath != null)
		{
			File path = new File(strPath);
			if (path.exists() && path.canRead())
			{
				chooser.setInitialDirectory(path);
			}
		}

		if (bMultiple)
		{
			List<File> files = chooser.showOpenMultipleDialog(m_app.getMainWindow());
			if (files != null && files.size() > 0)
			{
				m_app.doFileNew(false);

				SortedList<File> sorted = new SortedList<>(files, new Comparator<File>()
				{
					@Override
					public int compare(File file1, File file2)
					{
						String strName1 = file1.getName(),
								strName2 = file2.getName();
						return strName1.compareToIgnoreCase(strName2);
					}
				});

				files = sorted.toList();

				prefsRec.put(PNGKEYLASTPATHPNGOPEN, files.get(0).getParent());

				File[] afiles = new File[files.size()];
				return files.toArray(afiles);
			}
		}
		else
		{
			File file = chooser.showOpenDialog(m_app.getMainWindow());
			if (file != null)
			{
				prefsRec.put(PNGKEYLASTUSED, file.getPath());
				prefsRec.put(PNGKEYLASTPATHPNGOPEN, file.getParent());

				return new File[] { file };
			}
		}

		return null;
	}

	/**
	 * Begins a file open process with user interaction. This method prepares one or more
	 * {@link ExtensionFilter ExtensionFilter} objects to be passed to a
	 * file open dialog. Then it starts the dialog and the user may choose one or more files
	 * or can cancel the process. If not cancelled then the file(s) will be loaded.
	 *
	 * @param bMultiple If true then only one or more png file(s) can be loaded. If false then
	 * only one file can be loaded, either a png or a project file.
	 */
	void onFileOpen(boolean bMultiple)
	{
		if (!m_app.checkForChanges())
		{
			return;
		}

		ExtensionFilter[] aext;

		if (bMultiple)
		{
			aext = new ExtensionFilter[] { new ExtensionFilter(Loc.getString("filter.file.pngapng"), "*.png", "*.apng") };
		}
		else
		{
			aext = new ExtensionFilter[] {
					new ExtensionFilter(Loc.getString("filter.file.pngapngproject"), "*.png", "*.apng", "*.rpz"),
					new ExtensionFilter(Loc.getString("filter.file.pngapng"), "*.png", "*.apng"),
					new ExtensionFilter(Loc.getString("filter.file.project"), "*.rpz")
				};
		}

		File[] afiles = fileOpenDialog(bMultiple ? "title.files.open" : "title.file.open", bMultiple, aext);
		if (afiles != null)
		{
			m_app.doFileNew(false);

			if (!bMultiple && afiles[0].getName().toLowerCase().endsWith(".rpz"))
			{
				openProjectFile(afiles[0]);
			}
			else
			{
				openPngFiles(afiles, new EventHandler<ValueEvent<Bitmap32Sequence>>()
				{
					boolean bFirst = true;

					@Override
					public void handle(ValueEvent<Bitmap32Sequence> event)
					{
						if (!bMultiple)
						{
							m_filePng = afiles[0];
						}

						m_app.getFramesView().applyBitmapSequence(event.getValue(), bFirst);
						bFirst = false;
					}
				});
			}
		}
	}

	/**
	 * Reads a single {@link Bitmap32} object using the file open chain.
	 *
	 * <pre>
	 * See also {@link ValueEvent}.
	 * </pre>
	 *
	 * @param handler An object of type {@link EventHandler}.
	 * @return True if the opening process started. False if the user cancelled it.
	 */
	boolean onSingleBitmapOpen(EventHandler<ValueEvent<Bitmap32>> handler)
	{
		File[] afiles = fileOpenDialog("title.file.open", false, new ExtensionFilter(Loc.getString("filter.file.png"), "*.png"));
		if (afiles != null)
		{
			openPngFiles(afiles, value ->
			{
				handler.handle(new ValueEvent<>(value.getValue().getDefaultBitmap()));
			});

			return true;
		}

		return false;
	}

	/**
	 * Reads a single {@link Image} object using the file open chain.
	 *
	 * <pre>
	 * See also {@link ValueEvent}.
	 * </pre>
	 *
	 * @param handler An object of type {@link EventHandler}.
	 * @return True if the opening process started. False if the user cancelled it.
	 */
	public boolean onSingleImageOpen(EventHandler<ValueEvent<Image>> handler)
	{
		return onSingleBitmapOpen(value ->
		{
			handler.handle(new ValueEvent<>(ImageUtil.imageFromBitmap(value.getValue())));
		});
	}
}
