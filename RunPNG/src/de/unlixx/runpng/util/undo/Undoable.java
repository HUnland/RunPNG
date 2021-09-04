package de.unlixx.runpng.util.undo;

/**
 * An abstract Undoable class. Implementors need to add a void undoAction()
 * and a void redoAction() to complete the class instantiation.
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
 * @param <T> The source class type.
 */
public abstract class Undoable<T>
{
	public enum STATUS
	{
		INITIAL,
		UNDONE,
		REDONE
	}

	private STATUS m_status = STATUS.INITIAL;
	private String m_strActionId;
	private String[] m_args;
	private Runnable m_runFinally;
	private T m_source;

	/**
	 * Constructor for this Undoable object.
	 *
	 * @param source The typed source which caused this Undoable. Or null, if needed.
	 * @param strActionId A string containing an action id for lookup in the language resources.
	 * @param args A variable arguments list with arguments to add to the action string.
	 */
	public Undoable(T source, String strActionId, String... args)
	{
		m_strActionId = strActionId;
		m_args = args;
		m_source = source;
	}

	/**
	 * Set a {@link Runnable} which will be invoked after each undo or redo.
	 * This strategy can be used if an override of {@link Undoable#runFinally()} makes no sense
	 * at the point of Undoable creation.
	 *
	 * @param runFinally The runnable to invoke.
	 */
	public void setRunFinally(Runnable runFinally)
	{
		m_runFinally = runFinally;
	}

	/**
	 * Get the source which caused this Undoable. Or null, if not set.
	 * @return An object of type &lt;T&gt;.
	 */
	public T getSource()
	{
		return null;
	}

	/**
	 * Invoked by the UndoManager.
	 */
	final void undo()
	{
		if (m_status == STATUS.INITIAL || m_status == STATUS.REDONE)
		{
			undoAction();
			m_status = STATUS.UNDONE;

			if (m_runFinally != null)
			{
				m_runFinally.run();
			}

			runFinally();
		}
	}

	/**
	 * Must be implemented by developer.
	 * This method will be invoked in case of an undo action.
	 */
	public abstract void undoAction();

	/**
	 * Invoked by the UndoManager.
	 */
	final void redo()
	{
		if (m_status == STATUS.UNDONE)
		{
			redoAction();
			m_status = STATUS.REDONE;

			if (m_runFinally != null)
			{
				m_runFinally.run();
			}

			runFinally();
		}
	}

	/**
	 * Must be implemented by developer.
	 * This method will be invoked in case of a redo action.
	 */
	public abstract void redoAction();

	/**
	 * This method will be invoked after each undo or redo.
	 * It can be overridden by developer. Initally it does nothing.
	 */
	public void runFinally() { }

	/**
	 * Get the current status of this Undoable object.
	 *
	 * @return One of INITIAL, UNDONE, REDONE.
	 */
	public STATUS getStatus()
	{
		return m_status;
	}

	/**
	 * Get the stored action id of this Undoable object.
	 *
	 * @return A string containing the action id, an empty string or null.
	 */
	public String getActionId()
	{
		return m_strActionId;
	}

	/**
	 * Get the stored action arguments.
	 *
	 * @return Either a string array or null.
	 */
	public String[] getActionArgs()
	{
		return m_args;
	}
}
