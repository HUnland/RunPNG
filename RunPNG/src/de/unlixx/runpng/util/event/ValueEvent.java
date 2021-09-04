package de.unlixx.runpng.util.event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Simple event to transport a typed value to listeners in case of a change.
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
public class ValueEvent<V> extends Event
{
	final V m_value;

	/**
	 * Constructor for this event.
	 *
	 * @param value The typed value to transport.
	 */
	public ValueEvent(V value)
	{
		super(EventType.ROOT);

		m_value = value;
	}

	/**
	 * Gets the value.
	 *
	 * @return The typed value.
	 */
	public V getValue()
	{
		return m_value;
	}
}
