package de.unlixx.runpng;

import de.unlixx.runpng.scene.DualUseImageButton;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.undo.UndoManager;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;

/**
 * Implementation of the AppMainToolBar. This toolbar will be created from
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
class AppMainToolBar extends ToolBar
{
	final App m_app = App.getMainApp();
	final UndoManager m_undoManager;
	final AppFileManager m_fileManager;

	/**
	 * Constructor of this AppMainToolBar. This reads it's buttons
	 * from an xml file in the resources directory. The action events
	 * of these buttons will be send to the main menu bar action handler.
	 */
	AppMainToolBar()
	{
		m_undoManager = m_app.getUndoManager();
		m_fileManager = m_app.getFileManager();

		m_app.getMenuTool().readToolBar(this, "toolbars.xml", "toolbar.main", event -> m_app.getMainMenuBar().handleMenuAction(event));
	}

	/**
	 * Validates the toolbar buttons whether they are suitable
	 * for the current program status.
	 */
	void validate()
	{
		ObservableList<Node> list = getItems();

		for (Node node : list)
		{
			String strId = node.getId();
			if (strId != null)
			{
				switch (strId)
				{
				case "menu.file.new":
					node.setDisable(false);
					break;

				case "menu.file.open":
					node.setDisable(false);
					break;

				case "menu.files.open":
					node.setDisable(false);
					break;

				case "menu.file.save":
					node.setDisable(!m_undoManager.isDirty() || (m_fileManager.getPngFile() == null && m_fileManager.getProjectFile() == null));
					break;

				case "menu.file.saveas":
					node.setDisable(false);
					break;

				case "menu.file.saveas.project":
					node.setDisable(false);
					break;

				case "menu.edit.undo":
					node.setDisable(!m_undoManager.canUndo());
					break;

				case "menu.edit.redo":
					node.setDisable(!m_undoManager.canRedo());
					break;

				case "menu.player.first":
					node.setDisable(m_app.getPlayableFramesCount() < 2);
					break;

				case "menu.player.prev":
					node.setDisable(m_app.getPlayableFramesCount() < 2);
					break;

				case "menu.player.play":
				case "menu.player.pause":
					if (m_app.getPlayer() != null)
					{
						((DualUseImageButton)node).setStatus2();
					}
					else
					{
						((DualUseImageButton)node).setStatus1();
					}
					node.setDisable(m_app.getPlayableFramesCount() < 2);
					break;

				case "menu.player.next":
					node.setDisable(m_app.getPlayableFramesCount() < 2);
					break;

				case "menu.player.last":
					node.setDisable(m_app.getPlayableFramesCount() < 2);
					break;
				}
			}

			if (node instanceof Control)
			{
				Tooltip tooltip = ((Control)node).getTooltip();
				if (tooltip != null && (strId = tooltip.getId()) != null)
				{
					switch (strId)
					{
					case "tooltip.edit.undo":
						tooltip.setText(Loc.getString(strId) + ": " + m_undoManager.getUndoActionText());
						break;

					case "tooltip.edit.redo":
						tooltip.setText(Loc.getString(strId) + ": " + m_undoManager.getRedoActionText());
						break;
					}
				}
			}
		}
	}

}
