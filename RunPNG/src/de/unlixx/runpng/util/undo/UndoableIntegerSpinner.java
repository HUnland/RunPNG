package de.unlixx.runpng.util.undo;

import javafx.scene.control.Spinner;

/**
 * This is an {@link Undoable} which
 * is specialized for the use with {@link Spinner} objects.
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
public class UndoableIntegerSpinner extends Undoable<Spinner<Integer>>
{
	final Spinner<Integer> m_spinner;
	final int m_nOld;
	final int m_nNew;

	/**
	 * Constructor of this UndoableIntegerSpinner object.
	 *
	 * @param spinner The {@link Spinner} object which caused this Undoable creation.
	 * @param nOld The old int value of the Spinner object.
	 * @param nNew The new int value of the Spinner object.
	 * @param strActionId A string containing an action id for lookup in the language resources.
	 * @param args A variable arguments list with arguments to add to the action string.
	 */
	public UndoableIntegerSpinner(Spinner<Integer> spinner, int nOld, int nNew, String strActionId, String... args)
	{
		super(spinner, strActionId, args);

		m_spinner = spinner;
		m_nOld = nOld;
		m_nNew = nNew;
	}

	/**
	 * Implementation of the undo action.
	 */
	@Override
	public void undoAction()
	{
		m_spinner.getValueFactory().setValue(m_nOld);
	}

	/**
	 * Implementation of the redo action.
	 */
	@Override
	public void redoAction()
	{
		m_spinner.getValueFactory().setValue(m_nNew);
	}
}
