package de.unlixx.runpng.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An extension of HashMap to group values by a common key type
 * in order to make sort operations by key faster.
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
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class HashGrouper<K, V> extends HashMap<K, ArrayList<V>>
{
	/**
	 * Default constructor without specific use.
	 */
	public HashGrouper() {}

	/**
	 * This puts a key-value pair. Unlike normal behavior, this
	 * creates a 1 to n relation.
	 *
	 * @param key The key to store the value to.
	 * @param value The value to store.
	 */
	public void putValue(K key, V value)
	{
		ArrayList<V> list = get(key);
		if (list == null)
		{
			list = new ArrayList<V>();
			list.add(value);
			super.put(key, list);
		}
		else
		{
			list.add(value);
		}
	}

	/**
	 * Get an array of values for a specific key.
	 *
	 * @param key The key for the stored data.
	 * @param ataste An array of the data type &lt;V&gt;. If this array
	 * is to short, then an array with sufficient size will be created.
	 * @return An array of the stored data by key.
	 */
	public V[] getValueArray(K key, V[] ataste)
	{
		ArrayList<V> list = get(key);
		if (list != null)
		{
			return list.toArray(ataste);
		}

		return null;
	}
}
