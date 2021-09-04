package de.unlixx.runpng.util.undo;

import javafx.scene.control.TextInputControl;

/**
 * This is an {@link Undoable} which
 * is specialized for the use with {@link TextInputControl} objects.
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
public class UndoableTextInput extends Undoable<TextInputControl>
{
	final TextInputControl m_control;
	final String m_strOld;
	final String m_strNew;

	/**
	 * Constructor of this UndoableTextInput object.
	 *
	 * @param control The {@link TextInputControl} object which caused this Undoable creation.
	 * @param strOld The old string content of the TextInputControl object.
	 * @param strNew The new string content of the TextInputControl object.
	 * @param strActionId A string containing an action id for lookup in the language resources.
	 * @param args A variable arguments list with arguments to add to the action string.
	 */
	public UndoableTextInput(TextInputControl control, String strOld, String strNew, String strActionId, String... args)
	{
		super(control, strActionId, args);

		m_control = control;
		m_strOld = strOld;
		m_strNew = strNew;
	}

	/**
	 * Implementation of the undo action.
	 */
	@Override
	public void undoAction()
	{
		m_control.setText(m_strOld);
	}

	/**
	 * Implementation of the redo action.
	 */
	@Override
	public void redoAction()
	{
		m_control.setText(m_strNew);
	}
}
