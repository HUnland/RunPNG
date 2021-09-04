package de.unlixx.runpng;

import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.FramePane;
import de.unlixx.runpng.scene.settings.EffectsSettingsPane;
import de.unlixx.runpng.scene.settings.FileSettingsPane;
import de.unlixx.runpng.scene.settings.FrameSettingsPane;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.undo.Undoable;
import de.unlixx.runpng.util.undo.UndoableCollection;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Implementation of a AppTabbedSideBar. It is derived from {@link TabPane} and embeds
 * several settings dialogs.
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
class AppSideBar extends TabPane
{
	static final int MAX_WIDTH = 330;

	final App m_app = App.getMainApp();
	final FileSettingsPane m_settingsFile;
	final FrameSettingsPane m_settingsFrame;
	final EffectsSettingsPane m_settingsEffects;

	/**
	 * Constructor of this AppTabbedSideBar.
	 */
	AppSideBar()
	{
		setMaxWidth(MAX_WIDTH);

		ScrollPane scroller;

		// Settings panes creation. Each pane in a tab.

		final Tab tabFile = new Tab();
		tabFile.setClosable(false);
		tabFile.setId("tab.pngfile");
		Loc.addIdTextObject(tabFile);

		m_settingsFile = new FileSettingsPane(tabFile);
		scroller = new ScrollPane(m_settingsFile);
		scroller.setHbarPolicy(ScrollBarPolicy.NEVER);
		tabFile.setContent(scroller);

		final Tab tabFrame = new Tab();
		tabFrame.setClosable(false);
		tabFrame.setId("tab.singleframe");
		Loc.addIdTextObject(tabFrame);

		m_settingsFrame = new FrameSettingsPane(tabFrame);
		scroller = new ScrollPane(m_settingsFrame);
		scroller.setHbarPolicy(ScrollBarPolicy.NEVER);
		tabFrame.setContent(scroller);

		final Tab tabEffects = new Tab();
		tabEffects.setClosable(false);
		tabEffects.setId("tab.effects");
		Loc.addIdTextObject(tabEffects);

		m_settingsEffects = new EffectsSettingsPane(tabEffects);
		tabEffects.setContent(m_settingsEffects);

		getTabs().addAll(tabFile, tabFrame, tabEffects);

		// Event handlers

		m_settingsFile.setOnAnimationType(action -> onAnimationType(action));

		m_settingsFrame.setOnButtonSpreadDelay(action -> onButtonSpreadDelay(action));

		m_settingsFrame.setOnDelayFraction(action -> onDelayFraction(action));

		m_settingsEffects.setOnEffectChange(action -> onEffectChange(action));
	}

	/**
	 * This will be invoked to collect data for project saving from
	 * the embedded settings panes.
	 *
	 * @param project The {@link PngProject} object.
	 */
	void collectProject(PngProject project)
	{
		m_settingsFile.collectProject(project);
		m_settingsFrame.collectProject(project);
		m_settingsEffects.collectProject(project);
	}

	/**
	 * This will be invoked to apply data from a project object to
	 * the embedded settings panes.
	 *
	 * @param project The {@link PngProject} object.
	 */
	public void applyProject(PngProject project)
	{
		m_settingsFile.applyProject(project);
		m_settingsFrame.applyProject(project);
		m_settingsEffects.applyProject(project);
	}

	/**
	 * Gets the embedded FileSettingsPane.
	 *
	 * @return The {@link FileSettingsPane} object.
	 */
	FileSettingsPane getFileSettings()
	{
		return m_settingsFile;
	}

	/**
	 * Gets the embedded FrameSettingsPane.
	 *
	 * @return The {@link FrameSettingsPane} object.
	 */
	FrameSettingsPane getFrameSettings()
	{
		return m_settingsFrame;
	}

	/**
	 * Gets the embedded EffectsSettingsPane.
	 *
	 * @return The {@link EffectsSettingsPane} object.
	 */
	EffectsSettingsPane getEffectsSettings()
	{
		return m_settingsEffects;
	}

	/**
	 * Called by the main app in the case of a file new request.
	 *
	 * @param bDontChangeTab True it the selected tab shouldn't change.
	 */
	void doFileNew(boolean bDontChangeTab)
	{
		if (!bDontChangeTab)
		{
			getSelectionModel().selectFirst();
		}

		m_settingsEffects.reset();
	}

	/**
	 * Listens to the embedded file settings pane for a user change of the animation type.
	 *
	 * @param action An object of type {@link ActionEvent}.
	 */
	void onAnimationType(ActionEvent action)
	{
		PngAnimationType animType = m_settingsFile.getAnimationType();

		switch (animType)
		{
		case NONE:
			m_settingsFrame.setDisable(true);
			break;

		case ANIMATED:
			m_settingsFrame.setDisable(false);
			break;

		case SKIPFIRST:
			m_settingsFrame.setDisable(m_app.getFramesView().getSelectedIndex() == 0);
			break;
		}

		m_app.validateToolBars();
	}

	/**
	 * Listens to the embedded frames settings pane for a user click on the spread delay button.
	 *
	 * @param action An object of type {@link ActionEvent}.
	 */
	void onButtonSpreadDelay(ActionEvent action)
	{
		final PngDelayFraction fractionNew = m_settingsFrame.getDelayFraction();

		int nFrames = m_app.getFramesView().getFramesCount();
		if (nFrames > 0)
		{
			UndoableCollection coll = new UndoableCollection("label.spreaddelaytoallframes");

			for (int n = 0; n < nFrames; n++)
			{
				final FramePane pane = m_app.getFramesView().getFrame(n);
				final PngDelayFraction fractionOld = pane.getDelayFraction();
				pane.setDelayFraction(fractionNew);

				Undoable<FramePane> undo = new Undoable<FramePane>(pane, "")
				{
					@Override
					public void undoAction()
					{
						pane.setDelayFraction(fractionOld);
					}

					@Override
					public void redoAction()
					{
						pane.setDelayFraction(fractionNew);
					}
				};

				coll.add(undo);
			}

			m_app.addUndo(coll);
		}
	}

	/**
	 * Listens to the embedded frames settings pane for a user change of the delay settings.
	 *
	 * @param action An object of type {@link ActionEvent}.
	 */
	void onDelayFraction(ActionEvent action)
	{
		Object obj = action.getSource();
		if (obj instanceof Undoable)
		{
			Undoable<?> undoable = (Undoable<?>)obj;
			UndoableCollection coll = new UndoableCollection(undoable.getActionId());
			coll.add(undoable);

			final FramePane pane = m_app.getFramesView().getSelectedFrame();
			final PngDelayFraction fractionNew = m_settingsFrame.getDelayFraction(),
					fractionOld = pane.getDelayFraction();
			pane.setDelayFraction(fractionNew);

			Undoable<FramePane> undo = new Undoable<FramePane>(pane, "")
			{
				@Override
				public void undoAction()
				{
					pane.setDelayFraction(fractionOld);
				}

				@Override
				public void redoAction()
				{
					pane.setDelayFraction(fractionNew);
				}
			};

			coll.add(undo);
			m_app.addUndo(coll);
		}
	}

	/**
	 * Handles the event of a particular change of the effects settings.
	 *
	 * @param action An object of type {@link ActionEvent}.
	 */
	void onEffectChange(ActionEvent action)
	{
		m_settingsEffects.applyEffects();
	}

	/**
	 * Called by the application in case of an image size change. It dispatches the
	 * new sizes to all settings panes.
	 *
	 * @param nWidth The new image width.
	 * @param nHeight The new image height.
	 */
	void appSizeChanged(int nWidth, int nHeight)
	{
		m_settingsFile.appSizeChanged(nWidth, nHeight);
		m_settingsFrame.appSizeChanged(nWidth, nHeight);
		m_settingsEffects.appSizeChanged(nWidth, nHeight);
	}
}
