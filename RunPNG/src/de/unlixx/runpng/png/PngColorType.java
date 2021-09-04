package de.unlixx.runpng.png;

import java.util.HashMap;
import java.util.Map;

import de.unlixx.runpng.util.exceptions.Failure;

/**
 * This enumeration is a handy tool to manage the five color types
 * supported by png.
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
 * @see <a href="https://www.w3.org/TR/PNG/#6Color-values">https://www.w3.org/TR/PNG/#6Color-values</a>
 */
public enum PngColorType
{
	GREYSCALE(0, 1),		// Type 0, 1 component
	TRUECOLOR(2, 3),		// Type 2, 3 components
	INDEXED(3, 1),			// Type 3, 1 byte component
	GREYSCALE_ALPHA(4, 2),	// Type 4, 2 components
	TRUECOLOR_ALPHA(6, 4);	// Type 6, 4 components

	protected static final Map<Integer, PngColorType> m_types = new HashMap<>();

	static
	{
		m_types.put(0, GREYSCALE);
		m_types.put(2, TRUECOLOR);
		m_types.put(3, INDEXED);
		m_types.put(4, GREYSCALE_ALPHA);
		m_types.put(6, TRUECOLOR_ALPHA);
	}

	protected final int m_nType;
	protected final int m_nComponentsPerPixel;

	/**
	 * Package private constructor for the enum items.
	 *
	 * @param nType An int containing the type number.
	 * @param nComponentsPerPixel An int containing the number
	 * of components per pixel. Not necessarily the number of
	 * bytes per pixel.
	 */
	PngColorType(int nType, int nComponentsPerPixel)
	{
		m_nType = nType;
		m_nComponentsPerPixel = nComponentsPerPixel;
	}

	/**
	 * Gets the type number of a specific enum item.
	 *
	 * @return An int containing the type number.
	 */
	public int getType()
	{
		return m_nType;
	}

	/**
	 * Gets the number of components per pixel. Which is not
	 * necessarily the number of bytes per pixel. The latter
	 * depends on the bit depth.
	 *
	 * @return An int containing the number of components per pixel.
	 */
	public int getComponentsPerPixel()
	{
		return m_nComponentsPerPixel;
	}

	/**
	 * Gets the information whether a color type is indexed.
	 * This is enkeyed in bit 0 of the color type number.
	 * And that's why there are gaps in the type numbering.
	 * (0, 2, 3, 4, 6).
	 *
	 * @return True, if the color type is indexed.
	 */
	public boolean isIndexed()
	{
		return (m_nType & 0x01) > 0;
	}

	/**
	 * Gets the information whether a color type has an alpha channel.
	 * This is enkeyed in bit 2 of the color type number (see 4 or 6).
	 *
	 * @return True, if the color type has an alpha channel.
	 */
	public boolean hasAlpha()
	{
		return (m_nType & 0x04) > 0;
	}

	/**
	 * Calculates the scanline stride (bytes per scanline).
	 *
	 * @param nWidth The pixel width per line.
	 * @param bitDepth The number of bits per sample or per palette index.
	 * @return An int containing the scanline stride.
	 */
	public int calcScanlineStride(int nWidth, int bitDepth)
	{
		int nBitsPerPixel = bitDepth * m_nComponentsPerPixel,
			nBitsPerRow = nBitsPerPixel * nWidth;

		return nBitsPerRow / 8 + (nBitsPerRow % 8 == 0 ? 0 : 8 - nBitsPerRow % 8) + 1;
	}

	/**
	 * Convenience method to get one of the enum items quickly.
	 *
	 * @param nType An int containing the color type number.
	 * @return A PngColorType object if the color type number
	 * is valid. Afterwards a Failure exception will be thrown.
	 */
	public static PngColorType byType(int nType)
	{
		PngColorType type = m_types.get(nType);
		if (type != null)
		{
			return type;
		}

		throw new Failure("failure.unsupported.colortype", nType);
	}
}
