package de.unlixx.runpng.util;

/**
 * Representation of an ARGB-pixel with frequency and
 * a relocation index for palette optimization.
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
 */
public class ARGB
{
	public final int argb;
	public final int alpha;
	public final int red;
	public final int green;
	public final int blue;

	int m_nFrequency;
	int m_nRelocation;

	/**
	 * Constructor for a single ARGB-pixel with a frequency value of 0
	 * and no relocation (-1).
	 *
	 * @param nARGB An integer containing the ARGB value.
	 */
	public ARGB(int nARGB)
	{
		this(nARGB, 0);
	}

	/**
	 * Constructor for a single ARGB-pixel with a given frequency
	 * and no relocation (-1).
	 *
	 * @param nARGB An integer containing the ARGB value.
	 * @param nFrequency An integer containing the frequency of use.
	 */
	public ARGB(int nARGB, int nFrequency)
	{
		argb = nARGB;
		alpha = ((nARGB >>> 24) & 0xff);
		red = ((nARGB >>> 16) & 0xff);
		green = ((nARGB >>> 8) & 0xff);
		blue = (nARGB & 0xff);

		m_nFrequency = nFrequency;
		m_nRelocation = -1;
	}

	/**
	 * Get the full ARGB content.
	 *
	 * @return An integer containing the ARGB value.
	 */
	public int getARGB()
	{
		return argb;
	}

	/**
	 * Get the alpha value of the pixel.
	 *
	 * @return An integer containing the alpha value of the pixel in it's LSB.
	 */
	public int getAlpha()
	{
		return alpha;
	}

	/**
	 * Get the red value of the pixel.
	 *
	 * @return An integer containing the red value of the pixel in it's LSB.
	 */
	public int getRed()
	{
		return red;
	}

	/**
	 * Get the green value of the pixel.
	 *
	 * @return An integer containing the green value of the pixel in it's LSB.
	 */
	public int getGreen()
	{
		return green;
	}

	/**
	 * Get the blue value of the pixel.
	 *
	 * @return An integer containing the blue value of the pixel in it's LSB.
	 */
	public int getBlue()
	{
		return blue;
	}

	/**
	 * This increases the frequency value by one.
	 */
	public void incFrequency()
	{
		m_nFrequency++;
	}

	/**
	 * Get the frequency value of this pixel color.
	 *
	 * @return An integer containing the frequence value.
	 */
	public int getFrequency()
	{
		return m_nFrequency;
	}

	/**
	 * Sets the relocation index of this pixel for palette optimization.
	 *
	 * @param nRelocation The relocated index.
	 */
	public void setRelocation(int nRelocation)
	{
		m_nRelocation = nRelocation;
	}

	/**
	 * Get the relocation index of this pixel for palette optimization.
	 *
	 * @return The relocated index of this pixel.
	 */
	public int getRelocation()
	{
		return m_nRelocation;
	}
}
