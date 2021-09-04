package de.unlixx.runpng.util.undo;

/**
 * A simple event object which is passed to listeners
 * which implement the {@link UndoListener} interface
 * and are added to the {@link UndoManager}.
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
public class UndoEvent
{
	final Undoable<?> m_undo;

	/**
	 * Event constructor.
	 * @param undo The involved {@link Undoable} object.
	 */
	public UndoEvent(Undoable<?> undo)
	{
		m_undo = undo;
	}

	/**
	 * Gets the involved Undoable object.
	 * @return The involved {@link Undoable} object.
	 */
	public Undoable<?> getUndo()
	{
		return m_undo;
	}

	/**
	 * Gets the source object from the Undoable object.
	 *
	 * @return The source Object of the Undoable object. Or null if none is set.
	 */
	public Object getSource()
	{
		return m_undo.getSource();
	}
}
