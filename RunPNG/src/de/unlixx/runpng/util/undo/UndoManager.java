package de.unlixx.runpng.util.undo;

import java.util.Vector;

import de.unlixx.runpng.util.Loc;

/**
 * This is the manager class for all undo and redo actions.
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
public class UndoManager
{
	private static enum TYPE
	{
		ADD,
		UNDO,
		REDO,
	};

	private Vector<Undoable<?>> m_vecUndo;
	private Vector<UndoListener> m_vecListeners;

	private int m_nPos = -1;
	private int m_nCleanPos = -1;

	private boolean m_bUndoing;
	private boolean m_bRedoing;

	/**
	 * Constructs the UndoManager.
	 */
	public UndoManager()
	{
		m_vecUndo = new Vector<Undoable<?>>();
	}

	/**
	 * Adds an UndoListener to this manager.
	 *
	 * @param listener An object which implements the
	 * {@link UndoListener} interface.
	 */
	public void addUndoListener(UndoListener listener)
	{
		if (m_vecListeners == null)
		{
			m_vecListeners = new Vector<UndoListener>();
		}

		m_vecListeners.add(listener);
	}

	/**
	 * Removes an UndoListener from this manager.
	 *
	 * @param listener An object which implements the
	 * {@link UndoListener} interface.
	 */
	public void removeUndoListener(UndoListener listener)
	{
		if (m_vecListeners != null && m_vecListeners.contains(listener))
		{
			m_vecListeners.remove(listener);

			if (m_vecListeners.size() == 0)
			{
				m_vecListeners = null;
			}
		}
	}

	/**
	 * Adds an Undoable on top of the current position. All possible
	 * Undoables above will be discarded.
	 *
	 * @param undo An {@link Undoable} object.
	 */
	public void add(Undoable<?> undo)
	{
		if (undo != null)
		{
			m_vecUndo.setSize(++m_nPos + 1);
			m_vecUndo.set(m_nPos, undo);

			fireUndoEvent(TYPE.ADD, undo);
		}
	}

	/**
	 * Fires an {@link UndoEvent} to all listeners.
	 *
	 * @param type One of TYPE ADD, UNDO, REDO.
	 * @param undo The involved {@link Undoable} object.
	 */
	protected void fireUndoEvent(TYPE type, Undoable<?> undo)
	{
		if (m_vecListeners != null)
		{
			UndoEvent event = null;

			for (UndoListener listener : m_vecListeners)
			{
				if (event == null)
				{
					event = new UndoEvent(undo);
				}

				switch (type)
				{
				case ADD:		listener.undoableAdded(event); break;
				case UNDO:		listener.undoableUndone(event); break;
				case REDO:		listener.undoableRedone(event); break;
				}
			}
		}
	}

	/**
	 * Checks whether an undo is possible.
	 *
	 * @return True, if an undo is possible.
	 */
	public boolean canUndo()
	{
		return m_nPos >= 0;
	}

	/**
	 * Gets a string describing the next available undo action.
	 *
	 * @return A string describing the next available undo action,
	 * or the empty string if undo is not possible yet.
	 */
	public String getUndoActionText()
	{
		if (canUndo())
		{
			return buildActionText(m_vecUndo.get(m_nPos));
		}

		return "";
	}

	/**
	 * Invokes an undo action if possible.
	 */
	public void undo()
	{
		if (canUndo())
		{
			try
			{
				m_bUndoing = true;

				Undoable<?> undo = m_vecUndo.get(m_nPos--);
				undo.undo();
				fireUndoEvent(TYPE.UNDO, undo);
			}
			finally
			{
				m_bUndoing = false;
			}
		}
	}

	/**
	 * Checks whether a redo is possible.
	 *
	 * @return True, if a redo is possible.
	 */
	public boolean canRedo()
	{
		return m_nPos < m_vecUndo.size() - 1;
	}

	/**
	 * Gets a string describing the next available redo action.
	 *
	 * @return A string describing the next available redo action,
	 * or the empty string if redo is not possible yet.
	 */
	public String getRedoActionText()
	{
		if (canRedo())
		{
			return buildActionText(m_vecUndo.get(m_nPos + 1));
		}

		return "";
	}

	/**
	 * Invokes a redo action if possible.
	 */
	public void redo()
	{
		if (canRedo())
		{
			try
			{
				m_bRedoing = true;

				Undoable<?> redo = m_vecUndo.get(++m_nPos);
				redo.redo();
				fireUndoEvent(TYPE.REDO, redo);
			}
			finally
			{
				m_bRedoing = false;
			}
		}
	}

	/**
	 * Internal use.
	 *
	 * @param undo An {@link Undoable} object.
	 * @return A string containing the completed text.
	 */
	static String buildActionText(Undoable<?> undo)
	{
		String[] astr = undo.getActionArgs(),
			astrLoc = null;

		if (astr != null)
		{
			astrLoc = new String[astr.length];
			for (int n = 0; n < astr.length; n++)
			{
				String str = Loc.getString(astr[n]);
				if (str == "")
				{
					astrLoc[n] = astr[n];
				}
				else
				{
					astrLoc[n] = str;
				}
			}
		}

		return Loc.getString(undo.getActionId(), (Object[])astrLoc);
	}

	/**
	 * Resets this manager to initial state.
	 */
	public void reset()
	{
		m_vecUndo.clear();
		m_nPos = m_nCleanPos = -1;
		m_bUndoing = m_bRedoing = false;
	}

	/**
	 * Checks whether this UndoManager's current position
	 * is away from the clean position.
	 *
	 * @return True, if this UndoManager is in dirty state.
	 */
	public boolean isDirty()
	{
		return m_nPos != m_nCleanPos;
	}

	/**
	 * Checks whether this UndoManager is currently undoing.
	 *
	 * @return True, if this UndoManager is currently undoing.
	 */
	public boolean isUndoing()
	{
		return m_bUndoing;
	}

	/**
	 * Checks whether this UndoManager is currently redoing.
	 *
	 * @return True, if this UndoManager is currently redoing.
	 */
	public boolean isRedoing()
	{
		return m_bRedoing;
	}

	/**
	 * Checks whether this UndoManager is currently undoing or redoing.
	 *
	 * @return True, if this UndoManager is currently undoing or redoing.
	 */
	public boolean isActive()
	{
		return m_bUndoing || m_bRedoing;
	}

	/**
	 * Marks the current undo position as the clean position.
	 * I. e. in case the file has been saved, but there are still
	 * Undoables lingering in the list.
	 */
	public void setClean()
	{
		m_nCleanPos = m_nPos;
	}
}
