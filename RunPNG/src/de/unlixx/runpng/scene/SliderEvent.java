package de.unlixx.runpng.scene;

import javafx.event.ActionEvent;
import javafx.scene.control.Slider;

/**
 * The SliderEvent transports a double value and a commit flag.
 * It is used to transport a value and to signal whether this value is final.
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
 */
public class SliderEvent extends ActionEvent
{
	protected double m_dValue;
	protected boolean m_bCommit;

	/**
	 * Contructor of this SliderEvent.
	 *
	 * @param source The {@link Slider} which caused the event.
	 * @param dValue A double containing the value.
	 * @param bCommit True if the value is final.
	 */
	public SliderEvent(Slider source, double dValue, boolean bCommit)
	{
		super(source, ActionEvent.NULL_SOURCE_TARGET);

		m_dValue = dValue;
		m_bCommit = bCommit;
	}

	@Override
	public Slider getSource()
	{
		return (Slider)super.getSource();
	}

	/**
	 * The value to transport.
	 *
	 * @return A double containing the value.
	 */
	public double getValue()
	{
		return m_dValue;
	}

	/**
	 * Checks whether the transported value is final.
	 *
	 * @return True if the value is final. I. e. if the user ends sliding.
	 */
	public boolean getCommit()
	{
		return m_bCommit;
	}
}
