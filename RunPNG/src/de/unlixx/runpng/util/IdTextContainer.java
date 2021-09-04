package de.unlixx.runpng.util;

/**
 * Container to keep a value together with a translatable
 * id-text combination. This can be used e. g. for lists,
 * combo boxes, etc.
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
 * @param <V> The data type to keep.
 */
public class IdTextContainer<V>
{
	protected String m_strId;
	protected String m_strText;
	protected V m_value;

	/**
	 * Constructor for this class.
	 *
	 * @param strId A string with an id for language translation.
	 * @param value The value to keep.
	 */
	public IdTextContainer(String strId, V value)
	{
		m_strId = strId;
		m_value = value;
	}

	/**
	 * Returns the id of this object.
	 *
	 * @return A string containing the id.
	 */
	public String getId()
	{
		return m_strId;
	}

	/**
	 * Sets the text of this object. This text will be set by the
	 * location handler.
	 *
	 * @param strText A simple text string, obtained from string resources.
	 */
	public void setText(String strText)
	{
		m_strText = strText;
	}

	/**
	 * Returns the text of this object. This text will be set by the
	 * location handler.
	 *
	 * @return A simple text string.
	 */
	public String getText()
	{
		return m_strText != null ? m_strText : "";
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

	/**
	 * Sets a value of the given type. Null is allowed here.
	 *
	 * @param value A value of the given type.
	 */
	public void setValue(V value)
	{
		m_value = value;
	}

	@Override
	public String toString()
	{
		return getText();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof IdTextContainer<?> && m_strId.equals(((IdTextContainer<?>)obj).getId());
	}
}
