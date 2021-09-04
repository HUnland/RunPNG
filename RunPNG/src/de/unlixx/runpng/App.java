package de.unlixx.runpng;

import java.util.Locale;

import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.FramePane;
import de.unlixx.runpng.scene.FramesBox;
import de.unlixx.runpng.scene.ImageCanvas;
import de.unlixx.runpng.scene.settings.EffectsSettingsPane;
import de.unlixx.runpng.scene.settings.FileSettingsPane;
import de.unlixx.runpng.scene.settings.FrameSettingsPane;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.MenuTool;
import de.unlixx.runpng.util.Player;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.Version;
import de.unlixx.runpng.util.undo.UndoEvent;
import de.unlixx.runpng.util.undo.UndoListener;
import de.unlixx.runpng.util.undo.UndoManager;
import de.unlixx.runpng.util.undo.Undoable;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Implementation of the main App class derived from Application.
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
public class App extends Application
{
	public static final String APP_NAME = "RunPNG";
	public static final Version APP_VERSION = new Version(0, 1);

	public static final int MIN_IMAGE_WIDTH = 1;
	public static final int MIN_IMAGE_HEIGHT = 1;
	public static final int MAX_IMAGE_WIDTH = 4096;
	public static final int MAX_IMAGE_HEIGHT = 4096;
	public static final int DEFAULT_IMAGE_WIDTH = 512;
	public static final int DEFAULT_IMAGE_HEIGHT = 512;
	public static final int PREVIEW_WIDTH = 256;
	public static final int PREVIEW_HEIGHT = 256;
	public static final String RESOURCE_PATH = "de/unlixx/runpng/resources/";

	UndoManager m_undoManager = new UndoManager();
	MenuTool m_menutool = new MenuTool();

	static App m_appMain;
	static Image m_appIcon = new Image("de/unlixx/runpng/resources/app_icon.png");

	Stage m_stageApp;
	Scene m_sceneApp;
	BorderPane m_paneRoot;

	AppFileManager m_filemanager;
	AppSideBar m_sideBar;
	AppMainToolBar m_toolbarMain;
	AppMainMenuBar m_menubarMain;
	AppLowerToolBar m_toolbarLower;
	Player m_player;
	ImageCanvas m_imageCanvas;
	AppFramesView m_framesView;
	AppCenterView m_centerView;

	int m_nWidth = DEFAULT_IMAGE_WIDTH;
	int m_nHeight = DEFAULT_IMAGE_HEIGHT;

	/**
	 * Constructor of this App.
	 */
	public App()
	{
		m_appMain = this;
	}

	/**
	 * Gets the static reference of this class.
	 *
	 * @return An object of type {@link App}.
	 */
	public static App getMainApp()
	{
		return m_appMain;
	}

	/**
	 * Gets the static reference of the image icon of this App.
	 *
	 * @return An object of type {@link Image}.
	 */
	public static Image getAppIcon()
	{
		return m_appIcon;
	}

	/**
	 * Updates the title bar of this App.
	 */
	public void updateTitle()
	{
		StringBuffer buff = new StringBuffer();
		if (m_undoManager.isDirty())
		{
			buff.append("*");
		}

		buff.append(Loc.getString("app.title"));
		buff.append(" - ");
		if (m_filemanager.isProject())
		{
			buff.append(m_filemanager.getProjectFilename()).append(" - ").append(Loc.getString("label.project"));
		}
		else
		{
			buff.append(m_filemanager.getFilename());
		}

		buff.append(" (").append(String.valueOf(m_nWidth)).append("x").append(String.valueOf(m_nHeight)).append(")");

		m_stageApp.setTitle(buff.toString());
	}

	/**
	 * Clears the central image if there is one.
	 */
	void clearImage()
	{
		m_imageCanvas.clearImage();
	}

	/**
	 * Gets the common application image width.
	 *
	 * @return An int containing the width.
	 */
	public int getImageWidth()
	{
		return m_nWidth;
	}

	/**
	 * Gets the common application image height.
	 *
	 * @return An int containing the height.
	 */
	public int getImageHeight()
	{
		return m_nHeight;
	}

	/**
	 * Sets the common application image dimensions.
	 *
	 * @param nWidth An int containing the width.
	 * @param nHeight An int containing the height.
	 */
	public void setAppSize(int nWidth, int nHeight)
	{
		m_nWidth = nWidth;
		m_nHeight = nHeight;

		m_centerView.setBackgroundSize(m_nWidth, m_nHeight);
		m_framesView.appSizeChanged(m_nWidth, m_nHeight);
		m_sideBar.appSizeChanged(m_nWidth, m_nHeight);

		updateTitle();
	}

	/**
	 * Sets the undo manager to clean status (e. g. after save)
	 * without deleting the stored undoables.
	 */
	void setClean()
	{
		m_undoManager.setClean();

		updateTitle();
		validateToolBars();
	}

	/**
	 * Adds an Undoable to the undo manager.
	 *
	 * @param undo The {@link Undoable} object.
	 */
	public void addUndo(Undoable<?> undo)
	{
		m_undoManager.add(undo);
	}

	/**
	 * Adds an UndoListener to the undo manager.
	 *
	 * @param listener {@link UndoListener}
	 */
	public void addUndoListener(UndoListener listener)
	{
		m_undoManager.addUndoListener(listener);
	}

	/**
	 * Checks whether the undo manager is currently active with undo or redo.
	 *
	 * @return True if the undo manager is currently active with undo or redo.
	 */
	public boolean isUndoManagerActive()
	{
		return m_undoManager.isActive();
	}

	/**
	 * Gets the main Window.
	 *
	 * @return The main {@link Window} object.
	 */
	public Window getMainWindow()
	{
		return m_stageApp;
	}

	/**
	 * Gets the AppFramesView.
	 *
	 * @return The {@link AppFramesView} object.
	 */
	public AppFramesView getFramesView()
	{
		return m_framesView;
	}

	/**
	 * Gets the FramesBox of the AppFramesView.
	 *
	 * @return The {@link FramesBox} object.
	 */
	public FramesBox getFramesBox()
	{
		return m_framesView.getFramesBox();
	}

	/**
	 * Gets the MenuTool.
	 *
	 * @return The {@link MenuTool} object.
	 */
	public MenuTool getMenuTool()
	{
		return m_menutool;
	}

	/**
	 * Gets the AppFileManager.
	 *
	 * @return The {@link AppFileManager} object.
	 */
	public AppFileManager getFileManager()
	{
		return m_filemanager;
	}

	/**
	 * Gets the UndoManager.
	 *
	 * @return The {@link UndoManager} object.
	 */
	UndoManager getUndoManager()
	{
		return m_undoManager;
	}

	/**
	 * Gets the AppMainMenuBar.
	 *
	 * @return The {@link AppMainMenuBar} object.
	 */
	AppMainMenuBar getMainMenuBar()
	{
		return m_menubarMain;
	}

	/**
	 * Gets the AppMainToolBar.
	 *
	 * @return The {@link AppMainToolBar} object.
	 */
	AppMainToolBar getMainToolBar()
	{
		return m_toolbarMain;
	}

	/**
	 * Gets the AppCenterView.
	 *
	 * @return The {@link AppCenterView} object.
	 */
	AppCenterView getCenterView()
	{
		return m_centerView;
	}

	/**
	 * Gets the Player task if one is created.
	 *
	 * @return The {@link Player} object.
	 * Or null if there is none created yet.
	 */
	public Player getPlayer()
	{
		return m_player;
	}

	/**
	 * Gets the AppSideBar.
	 *
	 * @return The {@link AppSideBar} object.
	 */
	public AppSideBar getSideBar()
	{
		return m_sideBar;
	}

	/**
	 * Gets the FileSettingsPane from the side bar.
	 *
	 * @return The {@link FileSettingsPane} object.
	 */
	public FileSettingsPane getFileSettings()
	{
		return m_sideBar.getFileSettings();
	}

	/**
	 * Gets the FrameSettingsPane from the side bar.
	 * @return The {@link FrameSettingsPane} object.
	 */
	public FrameSettingsPane getFrameSettings()
	{
		return m_sideBar.getFrameSettings();
	}

	/**
	 * Gets the EffectsSettingsPane from the side bar.
	 *
	 * @return The {@link EffectsSettingsPane} object.
	 */
	public EffectsSettingsPane getEffectSettings()
	{
		return m_sideBar.getEffectsSettings();
	}

	/**
	 * Gets the ProgressBar from the lower tool bar.
	 *
	 * @return The {@link ProgressBar} object.
	 */
	public ProgressBar getProgressBar()
	{
		return m_toolbarLower.m_progressbar;
	}

	/**
	 * Sets the central image from a given FramePane and updates the side bar panes
	 * as needed.
	 *
	 * @param pane The {@link FramePane} object.
	 */
	void setCenterImage(FramePane pane)
	{
		m_imageCanvas.setImage(pane.getViewImage());

		m_centerView.setBackgroundSize(m_nWidth, m_nHeight);

		getFrameSettings().setFrameIndex(pane.getIndex());
		getFrameSettings().setDelayFraction(pane.getDelayFraction());

		PngAnimationType animType = getFileSettings().getAnimationType();

		if ((animType == PngAnimationType.SKIPFIRST && pane.getIndex() == 0)
				|| animType == PngAnimationType.NONE)
		{
			getFrameSettings().setDisable(true);
		}
		else
		{
			getFrameSettings().setDisable(false);
		}
	}

	/**
	 * Sets the wait cursor in case of background operations.
	 */
	public void setWaitCursor()
	{
		m_sceneApp.setCursor(Cursor.WAIT);

		// TODO: Block GUI interactions.
	}

	/**
	 * Sets the default cursor.
	 */
	public void setDefaultCursor()
	{
		m_sceneApp.setCursor(Cursor.DEFAULT);

		// TODO: Unblock GUI interactions.
	}

	/**
	 * Gets the PngAnimationType from the file settings pane.
	 *
	 * @return A {@link PngAnimationType} object.
	 */
	public PngAnimationType getAnimationType()
	{
		return getFileSettings().getAnimationType();
	}

	/**
	 * Sets the PngAnimationType to the file settings pane.
	 *
	 * @param animType A {@link PngAnimationType} object.
	 */
	public void setAnimationType(PngAnimationType animType)
	{
		getFileSettings().setAnimationType(animType);
	}

	/**
	 * Looks for changes and saves the file if wanted.
	 *
	 * @return True if the app can continue.
	 */
	boolean checkForChanges()
	{
		if (m_undoManager.isDirty())
		{
			switch (Util.showYesNoCancel("title.confirm.filechanges", "message.confirm.filechanges"))
			{
			case NO:
				break;

			case YES:
				return m_filemanager.onSaveFile();

			default:
				return false;
			}
		}

		return true;
	}

	/**
	 * Does a cleanup with app reset.
	 *
	 * @param bCheckForChanges True if the app shall check for changes and ask the user.
	 *
	 * @return True if it could be done.
	 */
	public boolean doFileNew(boolean bCheckForChanges)
	{
		return doFileNew(bCheckForChanges, false);
	}

	/**
	 * Does a cleanup with app reset.
	 *
	 * @param bCheckForChanges True if the app shall check for changes and ask the user.
	 * @param bDontChangeTab True if the currently selected tab must not change.
	 *
	 * @return True if it could be done.
	 */
	public boolean doFileNew(boolean bCheckForChanges, boolean bDontChangeTab)
	{
		if (bCheckForChanges && !checkForChanges())
		{
			return false;
		}

		m_filemanager.doFileNew();

		m_sideBar.doFileNew(bDontChangeTab);

		m_imageCanvas.clearImage();

		setAppSize(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

		WritableImage image = new WritableImage(m_nWidth, m_nHeight);

		m_imageCanvas.setImage(image);
		m_centerView.setBackgroundSize(m_nWidth, m_nHeight);

		getFileSettings().reset();
		getFrameSettings().reset();

		m_framesView.setAdjusting(true);
		m_framesView.clear();

		FramePane pane = m_framesView.createFrame(image, -1, getFrameSettings().getDelayFraction(), null);
		pane.setSelected(true);

		m_framesView.setAdjusting(false);

		m_undoManager.reset();

		m_toolbarLower.setZoom(100);

		validateToolBars();
		updateTitle();

		return true;
	}

	/**
	 * Collects the data of this app in the given PngProject object.
	 *
	 * @param project A {@link PngProject} object.
	 */
	void collectProject(PngProject project)
	{
		project.setMetaValue("app", "image.width", m_nWidth);
		project.setMetaValue("app", "image.height", m_nHeight);

		m_framesView.collectProject(project);
		m_sideBar.collectProject(project);
	}

	/**
	 * Applies data from a PngProject object.
	 *
	 * @param project A {@link PngProject} object.
	 */
	void applyProject(PngProject project)
	{
		int m_nImageWidth = project.getMetaValueInt("app", "image.width", DEFAULT_IMAGE_WIDTH),
			m_nImageHeight = project.getMetaValueInt("app", "image.height", DEFAULT_IMAGE_HEIGHT);

		setAppSize(m_nImageWidth, m_nImageHeight);

		m_framesView.applyProject(project);
		m_sideBar.applyProject(project);

		validateToolBars();
	}

	/**
	 * Gets the playable frames count.
	 *
	 * @return An int containing the playable frames count.
	 */
	int getPlayableFramesCount()
	{
		int nCount = m_framesView.getFramesCount();

		switch (getAnimationType())
		{
		case ANIMATED: return nCount;
		case SKIPFIRST: return nCount - 1;
		default: return 0;
		}
	}

	/**
	 * Starts the player if there are enough frames to play.
	 */
	void startPlayer()
	{
		int nPlayable = getPlayableFramesCount();
		if (nPlayable < 2)
		{
			validateToolBars();
			return;
		}

		m_menubarMain.setDisable(true);
		m_sideBar.setDisable(true);
		m_framesView.setDisable(true);

		final boolean bFrameRectWasVisible = m_centerView.isCutFrameVisible();
		if (bFrameRectWasVisible)
		{
			m_centerView.setCutFrameVisible(false);
		}

		int nFirst = getFileSettings().getAnimationType() == PngAnimationType.ANIMATED ? 0 : 1,
			nLast = nPlayable - 1,
			nIndex = Math.max(m_framesView.getSelectedIndex(), nFirst),
			nLoops = getFileSettings().getNumberOfLoops();

		m_player = new Player(nFirst, nLast, nIndex, nLoops);

		validateToolBars();

		ObservableList<Node> list = m_toolbarMain.getItems();
		for (Node node : list)
		{
			String strId = node.getId();
			if (strId != null
				&& !"menu.player.play".equals(strId)
				&& !"menu.player.pause".equals(strId))
			{
				node.setDisable(true);
			}
		}

		m_player.valueProperty().addListener(change ->
		{
			if (m_player != null)
			{
				int nFrameIndex = m_player.getValue();
				FramePane pane = m_framesView.setSelectedIndex(nFrameIndex);
				m_player.setTimeout(pane.getDelayMillis());
			}
		});

		m_player.setOnCancelled(action ->
		{
			m_player = null;

			if (bFrameRectWasVisible)
			{
				m_centerView.setCutFrameVisible(true);
			}

			m_menubarMain.setDisable(false);
			m_sideBar.setDisable(false);
			m_framesView.setDisable(false);

			validateToolBars();
		});

		m_player.start();
	}

	/**
	 * Invokes the validate method of the main tool bar.
	 */
	void validateToolBars()
	{
		m_toolbarMain.validate();
	}

	@Override
	public void start(Stage stageApp)
	{
		try
		{
			m_stageApp = stageApp;
			m_filemanager = new AppFileManager();

			stageApp.getIcons().add(m_appIcon);

			m_paneRoot = new BorderPane();
			BorderStroke stroke = new BorderStroke(Color.LIGHTGREY,  BorderStrokeStyle.SOLID, null, new BorderWidths(3));
			Border border = new Border(stroke);
			m_paneRoot.setBorder(border);

			m_sceneApp = new Scene(m_paneRoot, 1024, 768);
			m_sceneApp.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stageApp.setScene(m_sceneApp);

			VBox vbox = new VBox();
			m_paneRoot.setTop(vbox);

			m_menubarMain = new AppMainMenuBar();
			vbox.getChildren().add(m_menubarMain);

			m_toolbarMain = new AppMainToolBar();
			vbox.getChildren().add(m_toolbarMain);

			m_sideBar = new AppSideBar();
			m_paneRoot.setRight(m_sideBar);

			m_imageCanvas = new ImageCanvas(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
			m_centerView = new AppCenterView(m_imageCanvas);
			m_paneRoot.setCenter(m_centerView);

			vbox = new VBox();
			m_paneRoot.setBottom(vbox);

			m_framesView = new AppFramesView();
			vbox.getChildren().add(m_framesView);

			m_toolbarLower = new AppLowerToolBar();
			vbox.getChildren().add(m_toolbarLower);

			stageApp.setUserData("app.title");

			setAppSize(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

			UndoListener listenerUndo = new UndoListener()
			{
				@Override
				public void undoableUndone(UndoEvent event)
				{
					validateToolBars();
					updateTitle();
				}

				@Override
				public void undoableRedone(UndoEvent event)
				{
					validateToolBars();
					updateTitle();
				}

				@Override
				public void undoableAdded(UndoEvent event)
				{
					validateToolBars();
					updateTitle();
				}
			};

			m_undoManager.addUndoListener(listenerUndo);

			Loc.localeChanged(Locale.getDefault());

			stageApp.show();

			// Must be done after stageApp.show() because of layout
			m_sideBar.getSelectionModel().selectFirst();
			doFileNew(false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		stageApp.setOnCloseRequest(event ->
		{
			if (m_undoManager.isDirty())
			{
				switch (Util.showYesNoCancel("title.confirm.filechanges", "message.confirm.filechanges"))
				{
				case YES:
					if (!m_filemanager.onSaveFile())
					{
						event.consume();
						return;
					}
					break;

				case NO:
					break;

				default:
					event.consume();
					return;
				}
			}

			if (m_player != null)
			{
				m_player.stop();
			}
		});

		Util.checkForUpdate();
	}

	/**
	 * Opens the application manual in the system browser.
	 */
	public void menuHelpManual()
	{
		HostServices hostServices = getHostServices();
		String strURI = hostServices.getDocumentBase() + "doc/manual/" + Loc.getLanguage() + "/index.html";
		hostServices.showDocument(strURI);
	}

	/**
	 * Shows the help about dialog.
	 */
	public void menuHelpAbout()
	{
		Util.showHelpAbout();
	}

	/**
	 * The static main method to run the App.
	 *
	 * @param args An array of strings with optional arguments.
	 */
	public static void main(String[] args)
	{
		//System.out.println("java.runtime.version: " + System.getProperties().get("java.runtime.version"));
		//System.out.println("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));

		launch(args);
	}
}
