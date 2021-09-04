package de.unlixx.runpng.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A generic sorted list, which sorts it's elements while adding.
 * It implements the {@link Iterable} interface
 * in order to be used in a lambda style "for (x : y) loop".
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
 * @param <T> The class type to sort.
 */
public class SortedList<T> implements Iterable<T>
{
	protected final ArrayList<T> m_list = new ArrayList<>();
	protected final Comparator<T> m_comp;

	/**
	 * Constructor with type specific {@link Comparator}.
	 *
	 * @param comp A comparator for the actual class type.
	 */
	public SortedList(Comparator<T> comp)
	{
		m_comp = comp;
	}

	/**
	 * Constructor with type specific {@link List} for
	 * initialization and {@link Comparator}.
	 *
	 * @param list A List object containing initial data.
	 * @param comp A comparator for the actual class type.
	 */
	public SortedList(List<T> list, Comparator<T> comp)
	{
		m_comp = comp;

		for (T elem : list)
		{
			add(elem);
		}
	}

	/**
	 * Adds an element sorted to the underlying list.
	 *
	 * @param elem The element to add.
	 * @return An int containing the index of the element in the list.
	 */
	public int add(T elem)
	{
		for (int n = 0, nLen = m_list.size(); n < nLen; n++)
		{
			if (m_comp.compare(elem, m_list.get(n)) < 0)
			{
				m_list.add(n, elem);
				return n;
			}
		}

		m_list.add(elem);
		return m_list.size() - 1;
	}

	/**
	 * Removes an element by index.
	 *
	 * @param nIdx An int containing the index of the element in the list.
	 * @return The removed element.
	 */
	public T remove(int nIdx)
	{
		return m_list.remove(nIdx);
	}

	/**
	 * Removes the first occurrence of this element from the underlying list.
	 *
	 * @param elem The element to remove.
	 * @return True, if this element was found in the list and removed from it.
	 */
	public boolean remove(T elem)
	{
		return m_list.remove(elem);
	}

	/**
	 * Gets an element from the underlying list by index.
	 *
	 * @param nIdx An int containing the index of the element in the list.
	 * @return The element with this index.
	 */
	public T get(int nIdx)
	{
		return m_list.get(nIdx);
	}

	/**
	 * Search for the first occurrence of this element in the underlying list.
	 *
	 * @param elem The element to search for.
	 * @return An int containing the index of the element if found. -1 afterwards.
	 */
	public int indexOf(T elem)
	{
		return m_list.indexOf(elem);
	}

	/**
	 * Sets the size of the underlying list to 0.
	 */
	public void clear()
	{
		m_list.clear();
	}

	/**
	 * Gets the actual size of the underlying list.
	 *
	 * @return An int containing the actual size of the underlying list.
	 */
	public int size()
	{
		return m_list.size();
	}

	/**
	 * Creates a copy of the underlying list and returns it.
	 * Caveat: Only the list will be copied. The contained elements are the same.
	 *
	 * @return A new typed list of all elements.
	 */
	public List<T> toList()
	{
		return new ArrayList<>(m_list);
	}

	@Override
	public Iterator<T> iterator()
	{
		return m_list.iterator();
	}
}
