package de.unlixx.runpng.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * A filter to apply to text field objects as a {@link javafx.beans.value.ChangeListener ChangeListener}.
 * It prevents the input of non numeric characters and can limit the min and max value.
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
public class PositiveIntegerFilter implements ChangeListener<String>
{
	private final String m_strPattern;
	private final TextField m_textfield;
	private final int m_nMin;
	private final int m_nMax;

	/**
	 * Constructs a filter for the given text field.
	 *
	 * @param textfield A {@link TextField} object to observe.
	 */
	public PositiveIntegerFilter(TextField textfield)
	{
		m_strPattern = "\\d*";
		m_textfield = textfield;
		m_nMin = 0;
		m_nMax = Integer.MAX_VALUE;
	}

	/**
	 * Constructs a filter for the given text field and limits the input values.
	 *
	 * @param textfield A {@link TextField} object to observe.
	 * @param nMin An int containing the minimum integer value.
	 * @param nMax An int containing the maximum integer value.
	 */
	public PositiveIntegerFilter(TextField textfield, int nMin, int nMax)
	{
		nMin = Math.max(0, nMin);
		nMax = Math.max(nMin, nMax);

		m_nMin = nMin;
		m_nMax = nMax;

		int nMinDigits = (int)Math.log10(Math.max(1, nMin)) + 1,
				nMaxDigits = (int)Math.log10(Math.max(1, nMax)) + 1;

		m_strPattern = "\\d{" + nMinDigits + "," + nMaxDigits + "}";
		m_textfield = textfield;
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String strOld, String strNew)
	{
		boolean bChange = false;

		if (!strNew.matches(m_strPattern))
		{
			strNew = strNew.replaceAll("[\\D]", "");
			bChange = true;
		}

		int nNew;

		while (strNew.startsWith("0"))
		{
			strNew = strNew.substring(1);
			bChange = true;
		}

		if (strNew.length() == 0)
		{
			nNew = m_nMin;
			bChange = true;
		}
		else
		{
			nNew = Util.toInt(strNew, 0);
			if (nNew < m_nMin || nNew > m_nMax)
			{
				bChange = true;
			}
		}

		if (bChange)
		{
			m_textfield.setText("" + Math.max(m_nMin, Math.min(m_nMax, nNew)));
			m_textfield.positionCaret(m_textfield.getLength() - 1);
		}
	}
}
