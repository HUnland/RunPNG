package de.unlixx.runpng.png;

import de.unlixx.runpng.png.chunks.PngFrameControl;

/**
 * Simple container to transport and calculate delay fraction.
 * It is a convenience class for {@link PngFrameControl}.
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
 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>
 */
public class PngDelayFraction
{
	int m_nDelayNum;
	int m_nDelayDen;

	/**
	 * Constructs the PngDelayFraction object.
	 *
	 * @param nDelayNum The numerator of the fraction. It must be in the range 0 to 65535. Afterwards it will be clipped.
	 * @param nDelayDen Then denominator of the fraction. It must be in the range 0 to 1000. Afterwards it will be clipped.
	 * 			In case of 0 it will be set to 100. (see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>)
	 */
	public PngDelayFraction(int nDelayNum, int nDelayDen)
	{
		setDelayNum(nDelayNum);
		setDelayDen(nDelayDen);
	}

	/**
	 * Constructs the PngDelayFraction object.
	 *
	 * @param nDelayMillis The milliseconds given from 0 to 65535000. If out of bounds then the value will be clipped.
	 */
	public PngDelayFraction(int nDelayMillis)
	{
		setMilliseconds(nDelayMillis);
	}

	/**
	 * Sets the delay in milliseconds. If out of bounds then the value will be clipped.
	 * The fraction (delay_num / delay_den) will be calculated automatically.
	 *
	 * @param nDelayMillis The milliseconds given from 0 to 65535000.
	 */
	public void setMilliseconds(int nDelayMillis)
	{
		int nDelayNum,
			nDelayDen;

		nDelayMillis = Math.max(0, Math.min(65535000, nDelayMillis));

		if (nDelayMillis == 0)
		{
			// Zero delay shall be set to 0 / 100 according spec.
			nDelayNum = 0;
			nDelayDen = 100;
		}
		else
		{
			// Ok, simple minded. But it works and no better idea yet.
			if (nDelayMillis <= 65535)
			{
				nDelayNum = nDelayMillis;
				nDelayDen = 1000;
			}
			else if (nDelayMillis <= 655350)
			{
				nDelayNum = nDelayMillis / 10;
				nDelayDen = 100;
			}
			else if (nDelayMillis <= 6553500)
			{
				nDelayNum = nDelayMillis / 100;
				nDelayDen = 10;
			}
			else
			{
				nDelayNum = nDelayMillis / 1000;
				nDelayDen = 1;
			}

			// Shorten the fraction by power of 10.
			for (int n = (int)Math.log10(nDelayDen); n > 0; n--)
			{
				if (nDelayNum % 10 == 0)
				{
					nDelayNum /= 10;
					nDelayDen /= 10;
				}
				else
				{
					break;
				}
			}
		}

		m_nDelayNum = nDelayNum;
		m_nDelayDen = nDelayDen;
	}

	/**
	 * Gets the delay in milliseconds.
	 *
	 * @return An int containing the delay in milliseconds.
	 */
	public int getDelayMillis()
	{
		return (int)(1000d / m_nDelayDen * m_nDelayNum);
	}

	/**
	 * Sets the delay_num (delay numerator).
	 *
	 * @param nDelayNum An int containing the delay_num.
	 */
	public void setDelayNum(int nDelayNum)
	{
		m_nDelayNum = Math.max(0, Math.min(65535, nDelayNum));
	}

	/**
	 * Gets the delay_num (delay numerator).
	 *
	 * @return An int containing the delay_num.
	 */
	public int getDelayNum()
	{
		return m_nDelayNum;
	}

	/**
	 * Sets the delay_den (delay denominator).
	 *
	 * @param nDelayDen An int containing the new delay_den.
	 */
	public void setDelayDen(int nDelayDen)
	{
		m_nDelayDen = nDelayDen == 0 ? 100 : Math.max(1, Math.min(1000, nDelayDen));
	}

	/**
	 * Gets the delay_den (delay denominator).
	 *
	 * @return An int containing the delay_den.
	 */
	public int getDelayDen()
	{
		return m_nDelayDen;
	}

	@Override
	public PngDelayFraction clone()
	{
		return new PngDelayFraction(m_nDelayNum, m_nDelayDen);
	}
}
