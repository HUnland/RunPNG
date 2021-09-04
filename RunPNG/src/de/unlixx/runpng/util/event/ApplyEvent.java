package de.unlixx.runpng.util.event;

import javafx.event.ActionEvent;

/**
 * Simple extension of {@link ActionEvent}
 * to inform about an apply with undo text id.
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
public class ApplyEvent extends ActionEvent
{
	protected String m_strUndoId;

	/**
	 * Constructor for this event.
	 *
	 * @param source The object which caused this event.
	 * @param strUndoId A string containing an undo id for textual information.
	 */
	public ApplyEvent(Object source, String strUndoId)
	{
		super(source, ActionEvent.NULL_SOURCE_TARGET);
		m_strUndoId = strUndoId;
	}

	/**
	 * Gets the undo id for textual information.
	 *
	 * @return The undo id string.
	 */
	public String getUndoId()
	{
		return m_strUndoId;
	}
}
