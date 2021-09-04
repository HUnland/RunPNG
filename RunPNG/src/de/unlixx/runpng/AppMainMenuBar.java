package de.unlixx.runpng;

import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.undo.UndoManager;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

/**
 * Implementation of the AppMainMenuBar. This menubar will be created from
 * an xml file in the resource directory.
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
class AppMainMenuBar extends MenuBar
{
	final App m_app = App.getMainApp();
	final UndoManager m_undoManager;
	final AppFileManager m_fileManager;

	/**
	 * Constructor of this AppMainMenuBar. This reads it's menus and items
	 * from an xml file in the resources directory.
	 */
	AppMainMenuBar()
	{
		m_undoManager = m_app.getUndoManager();
		m_fileManager = m_app.getFileManager();

		m_app.getMenuTool().readMenuBar(this, "menusandbars.xml", "menubar.main",
				event -> handleMenuAction(event),
				event -> validate(event)
			);
	}

	/**
	 * Handles an action event either from this menu or from the
	 * {@link AppMainToolBar}.
	 *
	 * @param action An {@link ActionEvent} object.
	 */
	void handleMenuAction(ActionEvent action)
	{
		Object o = action.getSource();

		String strId = null;

		if (o instanceof MenuItem)
		{
			strId = ((MenuItem)o).getId();
		}
		else if (o instanceof Node)
		{
			strId = ((Node)o).getId();
		}

		if (strId != null)
		{
			PngAnimationType animType = m_app.getFileSettings().getAnimationType();
			int nFirstFrame = animType == PngAnimationType.ANIMATED ? 0 : 1,
				nLastFrame = m_app.getFramesView().getFramesCount() - 1,
				nSelectedFrame = m_app.getFramesView().getSelectedIndex();

			switch (strId)
			{
			case "menu.file.new":
				m_app.doFileNew(true);
				break;

			case "menu.file.open":
				m_fileManager.onFileOpen(false);
				break;

			case "menu.files.open":
				m_fileManager.onFileOpen(true);
				break;

			case "menu.file.save":
				m_fileManager.onSaveFile();
				break;

			case "menu.file.saveas":
				m_fileManager.onSavePngFileAs();
				break;

			case "menu.file.saveas.project":
				m_fileManager.onSaveProjectFileAs();
				break;

			case "menu.edit.undo":
				m_app.getUndoManager().undo();
				break;

			case "menu.edit.redo":
				m_app.getUndoManager().redo();
				break;

			case "menu.player.first":
				m_app.getFramesView().setSelectedIndex(nFirstFrame);
				break;

			case "menu.player.prev":
				m_app.getFramesView().setSelectedIndex(nSelectedFrame > nFirstFrame ? nSelectedFrame - 1 : nLastFrame);
				break;

			case "menu.player.play":
				if (m_app.getPlayer() == null)
				{
					m_app.startPlayer();
				}
				break;

			case "menu.player.pause":
				if (m_app.getPlayer() != null)
				{
					m_app.getPlayer().stop();
				}
				break;

			case "menu.player.next":
				m_app.getFramesView().setSelectedIndex(nSelectedFrame < nLastFrame ? nSelectedFrame + 1 : nFirstFrame);
				break;

			case "menu.player.last":
				m_app.getFramesView().setSelectedIndex(nLastFrame);
				break;

			case "menu.help.manual":
				m_app.menuHelpManual();
				break;

			case "menu.help.about":
				m_app.menuHelpAbout();
				break;
			}
		}
	}

	/**
	 * Validates the menu items before showing whether they are suitable
	 * for the current program status.
	 *
	 * @param event An {@link Event} object.
	 */
	void validate(Event event)
	{
		Object o = event.getSource();
		if (o instanceof MenuItem)
		{
			MenuItem item = (MenuItem)o;
			String strId = item.getId();

			switch (strId)
			{
			case "menu.file.save":
				item.setDisable(!m_undoManager.isDirty() || (m_fileManager.getPngFile() == null && m_fileManager.getProjectFile() == null));
				break;

			case "menu.file.saveas":
				item.setDisable(false);
				break;

			case "menu.file.saveas.project":
				item.setDisable(false);
				break;

			case "menu.edit.undo":
				if (m_undoManager.canUndo())
				{
					item.setDisable(false);
					item.setText(Loc.getString(strId) + ": " + m_undoManager.getUndoActionText());
				}
				else
				{
					item.setDisable(true);
					item.setText(Loc.getString(strId));
				}
				break;

			case "menu.edit.redo":
				if (m_undoManager.canRedo())
				{
					item.setDisable(false);
					item.setText(Loc.getString(strId) + ": " + m_undoManager.getRedoActionText());
				}
				else
				{
					item.setDisable(true);
					item.setText(Loc.getString(strId));
				}
				break;

			case "menu.player.first":
				item.setDisable(m_app.getPlayableFramesCount() < 1);
				break;

			case "menu.player.prev":
				item.setDisable(m_app.getPlayableFramesCount() < 1);
				break;

			case "menu.player.play":
			case "menu.player.pause":
				item.setDisable(m_app.getPlayableFramesCount() < 2);
				break;

			case "menu.player.next":
				item.setDisable(m_app.getPlayableFramesCount() < 1);
				break;

			case "menu.player.last":
				item.setDisable(m_app.getPlayableFramesCount() < 1);
				break;
			}
		}
	}
}
