package de.unlixx.runpng.png.chunks;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.util.SortedList;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * Implements the png palette chunk (PLTE) with some additions for greyscale.
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
 * @see <a href="https://www.w3.org/TR/PNG/#11PLTE">https://www.w3.org/TR/PNG/#11PLTE</a>
 */
public class PngPalette
{
	public static final int MAX_SIZE = 256;

	int[] m_anARGB;

	boolean m_bPrebuiltGreyScale;
	static final Map<Integer, PngPalette> m_palettesGreyscale = new HashMap<>();

	static
	{
		m_palettesGreyscale.put(1, prepareGreyscale(2, 0xff));
		m_palettesGreyscale.put(2, prepareGreyscale(4, 0x55));
		m_palettesGreyscale.put(4, prepareGreyscale(16, 0x11));
		m_palettesGreyscale.put(8, prepareGreyscale(256, 0x01)); // Hm, not very heavily used
	}

	/**
	 * Internal palette creator for greyscale.
	 *
	 * @param nLen The desired length of the palette.
	 * @param nIncr The incremental steps.
	 * @return A PngPalette object.
	 */
	static PngPalette prepareGreyscale(int nLen, int nIncr)
	{
		final int[] an = new int[nLen];
		final int nA = 0xff << 24;
		int nL = 0x00;

		for (int n = 0; n < nLen; n++)
		{
			an[n] = nA | nL << 16 | nL << 8 | nL;
			nL = (nL + nIncr) & 0xff;
		}

		return new PngPalette(an, true);
	}

	/**
	 * Constructor for an ARGB palette.
	 *
	 * @param anARGB An int array containing ARGB values.
	 */
	public PngPalette(int[] anARGB)
	{
		this(anARGB, false);
	}

	/**
	 * Internal constructor for an ARGB palette.
	 *
	 * @param anARGB An int array containing ARGB values.
	 * @param bPrebuiltGreyScale True, if this is a prebuilt greyscale.
	 */
	PngPalette(int[] anARGB, boolean bPrebuiltGreyScale)
	{
		m_anARGB = anARGB;
		m_bPrebuiltGreyScale = bPrebuiltGreyScale;
	}

	/**
	 * Gets an ARGB value by index.
	 *
	 * @param nIdx The index of the ARBG value.
	 * @return An int containing the ARGB value.
	 */
	public int get(int nIdx)
	{
		return m_anARGB[nIdx];
	}

	/**
	 * Sets a new ARGB value for the given index.
	 * @param nIdx The index of the ARBG value.
	 * @param nARGB An int containing the new ARGB value.
	 */
	public void set(int nIdx, int nARGB)
	{
		m_anARGB[nIdx] = nARGB;
	}

	/**
	 * Finds the index of a specific ARGB value. Or -1 if it does not exist.
	 *
	 * @param nARGB An int containing the ARGB value.
	 * @return An int containing the index or -1 if it does not exist.
	 */
	public int find(int nARGB)
	{
		for (int n = 0, nLen = m_anARGB.length; n < nLen; n++)
		{
			if (nARGB == m_anARGB[n])
			{
				return n;
			}
		}

		return -1;
	}

	/**
	 * Gets the number of palette entries.
	 *
	 * @return An int containing the number of palette entries.
	 */
	public int length()
	{
		return m_anARGB.length;
	}

	/**
	 * Gets all palette entries splitted into their RGB components.
	 *
	 * @return A byte array with the RGB values of all palette entries.
	 */
	public byte[] getBytes()
	{
		byte[] abRGB = new byte[m_anARGB.length * 3];
		int n = 0;
		for (int nARGB : m_anARGB)
		{
			abRGB[n++] = (byte)((nARGB >> 16) & 0xff);
			abRGB[n++] = (byte)((nARGB >> 8) & 0xff);
			abRGB[n++] = (byte)(nARGB & 0xff);
		}

		return abRGB;
	}

	/**
	 * Gets an int array of the given source array, sorted by their transparency
	 * in ascending order.
	 *
	 * @param anARGBSrc An array of ARGB values.
	 * @return An array of ARGB values, sorted by their transparency
	 * in ascending order.
	 */
	public static int[] sortByTransparency(int[] anARGBSrc)
	{
		SortedList<Integer> sorter = new SortedList<>(new Comparator<Integer>()
		{
			@Override
			public int compare(Integer n1, Integer n2)
			{
				int nA1 = ((n1 >>> 24) & 0xff),
					nA2 = ((n2 >>> 24) & 0xff);

				if (nA1 < nA2)
				{
					return -1;
				}
				else if (nA1 > nA2)
				{
					return 1;
				}
				// if equal, compare the non transparent part
				else if ((n1 & 0x00ffffff) < (n2 & 0x00ffffff))
				{
					return -1;
				}
				else if ((n1 & 0x00ffffff) > (n2 & 0x00ffffff))
				{
					return 1;
				}

				return 0;
			}
		});

		for (int nARGB : anARGBSrc)
		{
			sorter.add(nARGB);
		}

		List<Integer> list = sorter.toList();

		int n = 0, anARGBDest[] = new int[anARGBSrc.length];
		for (int nARGB : list)
		{
			anARGBDest[n++] = nARGB;
		}

		return anARGBDest;
	}

	/**
	 * Spawns a PngTransparency object from this PngPalette object.
	 * The current ARGB array will be sorted by transparency in order
	 * to have the same transparency sequence. So this method should
	 * not be invoked when this palette is already in use.
	 *
	 * @return A {@link PngTransparency} object.
	 * Or null if there is no transparency found in this palette.
	 */
	public PngTransparency spawnTransparency()
	{
		m_anARGB = sortByTransparency(m_anARGB);

		byte[] abtRNS = new byte[m_anARGB.length];

		int n = 0;
		for (int nARGB : m_anARGB)
		{
			int nA = ((nARGB >>> 24) & 0xff);
			if (nA == 0xff)
			{
				break;
			}

			abtRNS[n++] = (byte)(nA & 0xff);
		}

		if (n > 0)
		{
			if (n < abtRNS.length)
			{
				abtRNS = Arrays.copyOf(abtRNS, n);
			}

			return new PngTransparency(PngColorType.INDEXED, 8, abtRNS);
		}

		return null;
	}

	/**
	 * Creates a PgnPalette from a given RGB byte array.
	 *
	 * @param abSrc An RGB byte array.
	 * @return A {@link PngPalette} object.
	 */
	public static PngPalette createPalette(final byte[] abSrc)
	{
		int nLen = abSrc.length;
		if (nLen % 3 != 0)
		{
			throw new Failure("failure.malformed.palette");
		}

		final int nA = 0xff000000,
				nColors = nLen / 3;
		final int[] anARGB = new int[nColors];

		for (int nDestIdx = 0, nSrcIdx = 0; nDestIdx < nColors; nDestIdx++)
		{
			final int nR = abSrc[nSrcIdx++] & 0xff,
					nG = abSrc[nSrcIdx++] & 0xff,
					nB = abSrc[nSrcIdx++] & 0xff;
			anARGB[nDestIdx] = nA | nR << 16 | nG << 8 | nB;
		}

		return new PngPalette(anARGB, false);
	}

	/**
	 * Gets a prebuilt greyscale palette for the given bit depth.
	 *
	 * @param nBitDepth An int containing the bit depth.
	 * @return A {@link PngPalette} object.
	 * @throws Failure In the case of wrong bit depth.
	 */
	public static PngPalette getGreyscale(int nBitDepth)
	{
		PngPalette palette = m_palettesGreyscale.get(nBitDepth);
		if (palette != null)
		{
			return palette.clone();
		}

		throw new Failure("failure.wrong.bitdepth");
	}

	@Override
	public PngPalette clone()
	{
		return new PngPalette(m_anARGB, m_bPrebuiltGreyScale);
	}
}
