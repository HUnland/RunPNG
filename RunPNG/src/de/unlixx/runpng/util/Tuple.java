package de.unlixx.runpng.util;

/**
 * A simple typed and immutable tuple. It can hold
 * two different kind of class type.
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
 * @param <V1> The type for value 1.
 * @param <V2> The type for value 2.
 */
public class Tuple<V1, V2>
{
	final V1 m_value1;
	final V2 m_value2;

	/**
	 * Contructor for both typed values.
	 *
	 * @param value1 The typed value 1.
	 * @param value2 The typed value 2.
	 */
	public Tuple(V1 value1, V2 value2)
	{
		m_value1 = value1;
		m_value2 = value2;
	}

	/**
	 * Get value 1.
	 *
	 * @return The typed value 1.
	 */
	public V1 get1()
	{
		return m_value1;
	}

	/**
	 * Get value 2.
	 *
	 * @return The typed value 2.
	 */
	public V2 get2()
	{
		return m_value2;
	}
}
