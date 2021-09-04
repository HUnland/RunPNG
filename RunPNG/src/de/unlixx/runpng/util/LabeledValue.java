package de.unlixx.runpng.util;

/**
 * Container for a immutable label-value combination. This can be used e. g. for lists,
 * combo boxes, etc. This is intended for use where localization is not needed.
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
 * @param <V> The class type of the value.
 */
public class LabeledValue<V>
{
	final String m_strText;
	final V m_value;

	/**
	 * Constructor for this object.
	 *
	 * @param strText A string containing the textual representation.
	 * @param value A value of given type.
	 */
	public LabeledValue(String strText, V value)
	{
		m_strText = strText;
		m_value = value;
	}

	/**
	 * Returns the contained value or null.
	 *
	 * @return Either an object of the given type or null.
	 */
	public V getValue()
	{
		return m_value;
	}

	@Override
	public String toString()
	{
		return m_strText;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof LabeledValue<?> && ((LabeledValue<?>)obj).getValue() == m_value;
	}
}
