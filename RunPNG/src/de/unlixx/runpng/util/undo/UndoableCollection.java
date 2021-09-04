package de.unlixx.runpng.util.undo;

import java.util.Iterator;
import java.util.Vector;

/**
 * This is an {@link Undoable} which
 * collects Undoables for the sake to undo or redo them all at once. In the case
 * of undo, the collected Undoables will be invoked in reverse order.
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
public class UndoableCollection extends Undoable<Void> implements Iterable<Undoable<?>>
{
	final Vector<Undoable<?>> m_vec = new Vector<Undoable<?>>();

	/**
	 * Constructs this UndoableCollection object.
	 *
	 * @param strActionId A string containing an action id for lookup in the language resources.
	 * @param args A variable arguments list with arguments to add to the action string.
	 */
	public UndoableCollection(String strActionId, String... args)
	{
		super(null, strActionId, args);
	}

	/**
	 * Adds an Undoable object to this collection.
	 *
	 * @param undo An {@link Undoable} object.
	 */
	public void add(Undoable<?> undo)
	{
		m_vec.add(undo);
	}

	/**
	 * Gets the size of the underlying list of Undoable objects.
	 *
	 * @return An int containing the size of the underlying list of Undoable objects.
	 */
	public int size()
	{
		return m_vec.size();
	}

	/**
	 * Gets an Undoable object from this collection by index.
	 * @param nIdx An int containing the index.
	 * @return The {@link Undoable} object
	 * of this index. Or null if the index is out of range.
	 */
	public Undoable<?> getUndoable(int nIdx)
	{
		if (nIdx >= 0 && nIdx < m_vec.size())
			return m_vec.get(nIdx);
		return null;
	}

	/**
	 * Implementation of the redo action.
	 */
	@Override
	public void redoAction()
	{
		for (Undoable<?> undo : m_vec)
		{
			undo.redo();
		}
	}

	/**
	 * Implementation of the undo action.
	 */
	@Override
	public void undoAction()
	{
		for (int n = m_vec.size() - 1; n >= 0; n--)
		{
			m_vec.get(n).undo();
		}
	}

	@Override
	public Iterator<Undoable<?>> iterator()
	{
		return m_vec.iterator();
	}
}
