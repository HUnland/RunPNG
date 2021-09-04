package de.unlixx.runpng.util.undo;

import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * This is an {@link Undoable} which
 * is specialized for the use with {@link ToggleGroup} objects.
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
 *
 */
public class UndoableToggleGroup extends Undoable<ToggleGroup>
{
	final ToggleGroup m_toggles;
	final Toggle m_toggleOld;
	final Toggle m_toggleNew;

	/**
	 * Constructor of this UndoableToggleGroup object.
	 *
	 * @param toggles The {@link ToggleGroup} object which caused this Undoable creation.
	 * @param toggleOld The old object which implements the {@link Toggle} interface.
	 * @param toggleNew The new object which implements the {@link Toggle} interface.
	 * @param strActionId A string containing an action id for lookup in the language resources.
	 * @param args A variable arguments list with arguments to add to the action string.
	 */
	public UndoableToggleGroup(ToggleGroup toggles, Toggle toggleOld, Toggle toggleNew,
			String strActionId, String... args)
	{
		super(toggles, strActionId, args);

		m_toggles = toggles;
		m_toggleOld = toggleOld;
		m_toggleNew = toggleNew;
	}

	/**
	 * Implementation of the undo action.
	 */
	@Override
	public void undoAction()
	{
		if (m_toggleOld != null)
		{
			m_toggleOld.setSelected(true);
		}
	}

	/**
	 * Implementation of the redo action.
	 */
	@Override
	public void redoAction()
	{
		if (m_toggleNew != null)
		{
			m_toggleNew.setSelected(true);
		}
	}

}
