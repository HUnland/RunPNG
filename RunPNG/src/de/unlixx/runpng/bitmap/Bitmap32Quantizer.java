package de.unlixx.runpng.bitmap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import de.unlixx.runpng.util.ARGB;
import de.unlixx.runpng.util.HashGrouper;
import de.unlixx.runpng.util.PivotSorter;

/**
 * Experimental Bitmap32Quantizer. It is used to reduce the color range
 * of an ARGB array for color palette. Which means 256 distinct colors max.
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
public class Bitmap32Quantizer
{
	final ARGB[] m_aARGB;

	/**
	 * Constructor for this Bitmap32Quantizer.
	 *
	 * @param aARGB An array of {@link ARGB} objects.
	 */
	public Bitmap32Quantizer(ARGB[] aARGB)
	{
		m_aARGB = aARGB;
	}

	/**
	 * Calculates the euclidean distance of two {@link ARGB} objects
	 * by regarding the alpha value.
	 *
	 * @param argb1 The first ARGB object.
	 * @param argb2 The second ARGB object.
	 * @return A double with the calculated distance.
	 */
	public static double euclideanDistanceARGB(final ARGB argb1, final ARGB argb2)
	{
		final int nA = argb1.alpha - argb2.alpha,
				nR = argb1.red - argb2.red,
				nG = argb1.green - argb2.green,
				nB = argb1.blue - argb2.blue;
		return Math.sqrt(nA * nA + nR * nR + nG * nG + nB * nB);
	}

	/**
	 * Calculates the euclidean distance of two {@link ARGB} objects
	 * by ignoring a possible alpha value.
	 *
	 * @param argb1 The first ARGB object.
	 * @param argb2 The second ARGB object.
	 * @return A double with the calculated distance.
	 */
	public static double euclideanDistanceRGB(final ARGB argb1, final ARGB argb2)
	{
		final int nR = argb1.red - argb2.red,
				nG = argb1.green - argb2.green,
				nB = argb1.blue - argb2.blue;
		return Math.sqrt(nR * nR + nG * nG + nB * nB);
	}

	/**
	 * Calculates the manhattan distance of two {@link ARGB} objects
	 * by regarding the alpha value.
	 *
	 * @param argb1 The first ARGB object.
	 * @param argb2 The second ARGB object.
	 * @return An int with the calculated distance.
	 */
	public static int manhattanDistanceARGB(final ARGB argb1, final ARGB argb2)
	{
		return Math.abs(argb1.alpha - argb2.alpha) + Math.abs(argb1.red - argb2.red) + Math.abs(argb1.green - argb2.green) + Math.abs(argb1.blue - argb2.blue);
	}

	/**
	 * Calculates the manhattan distance of two {@link ARGB} objects
	 * by ignoring a possible alpha value.
	 *
	 * @param argb1 The first ARGB object.
	 * @param argb2 The second ARGB object.
	 * @return An int with the calculated distance.
	 */
	public static int manhattanDistanceRGB(final ARGB argb1, final ARGB argb2)
	{
		return Math.abs(argb1.red - argb2.red) + Math.abs(argb1.green - argb2.green) + Math.abs(argb1.blue - argb2.blue);
	}

	/**
	 * Sorts an ARGB array of distinct colors by their frequency of usage.
	 * This is an in-place operation.
	 *
	 * @param aARGB An array of {@link ARGB} objects.
	 * @param bAsc True, if it shall sort in ascending order.
	 */
	public static void sortByFrequency(final ARGB[] aARGB, final boolean bAsc)
	{
		// Group the ARGB objects by frequency.
		HashGrouper<Integer, ARGB> grouper = new HashGrouper<>();
		for (ARGB argb : aARGB)
		{
			grouper.putValue(argb.getFrequency(), argb);
		}

		// Separate the Integer keys and make an array of them.
		Set<Integer> set = grouper.keySet();
		Integer[] anKeys = new Integer[set.size()];
		anKeys = set.toArray(anKeys);

		// Sort the Integers.
		PivotSorter.sort(anKeys, new Comparator<Integer>()
		{
			@Override
			public int compare(final Integer n1, final Integer n2)
			{
				return bAsc ? (n1 < n2 ? -1 : n1 > n2 ? 1 : 0) : (n1 < n2 ? 1 : n1 > n2 ? -1 : 0);
			}
		});

		// Update the given ARGB array.
		int n = 0;
		ARGB[] ataste = new ARGB[0];
		for (Integer nKey : anKeys)
		{
			ARGB[] aARGBgrp = grouper.getValueArray(nKey, ataste);
			if (aARGBgrp != null)
			{
				for (ARGB argb : aARGBgrp)
				{
					aARGB[n++] = argb;
				}
			}
		}
	}

	/**
	 * Find the nearest color index in an array of ARGB color objects.
	 *
	 * @param aARGB An array of {@link ARGB} objects.
	 * @param argb The ARGB color object to test with.
	 * @param nMaxSize The maximum size to run the search.
	 * @return An int containing the index of the nearest color.
	 */
	public static int findNearestColorIdx(final ARGB[] aARGB, final ARGB argb, final int nMaxSize)
	{
		double dDistMin = Double.MAX_VALUE;
		int nIdxMin = -1;

		for (int n = 0; n < nMaxSize; n++)
		{
			final double dDist = manhattanDistanceARGB(argb, aARGB[n]);
			if (dDist < dDistMin)
			{
				dDistMin = dDist;
				nIdxMin = n;
			}
		}

		return nIdxMin;
	}

	/**
	 * This method relocates all colors with an index above max size
	 * to the nearest color index &lt;= max size.
	 *
	 * @param nMaxSize The desired maximum size.
	 * @return An array of {@link ARGB} objects.
	 */
	public ARGB[] rollup(int nMaxSize)
	{
		if (m_aARGB.length > nMaxSize)
		{
			ARGB[] aARGB = Arrays.copyOf(m_aARGB, m_aARGB.length);
			sortByFrequency(aARGB, false);

			for (int n = aARGB.length - 1; n >= nMaxSize; n--)
			{
				ARGB argb = aARGB[n];
				argb.setRelocation(findNearestColorIdx(aARGB, argb, nMaxSize));
			}

			// TODO: remove
			boolean bPrint = false;
			if (bPrint)
			{
				printColors(aARGB);
			}

			return aARGB;
		}

		return m_aARGB;
	}

	// TODO: remove
	public static void printColors(ARGB[] aARGB)
	{
		System.out.println("\nBitmap32Quantizer Color Array length = " + aARGB.length);
		System.out.println("================================================================================");

		int n = 0;
		for (ARGB argb: aARGB)
		{
			System.out.println(String.format("idx: %7d, alpha: %02x, red: %02x, green: %02x, blue: %02x, freq: %7d, new idx: %7d", n,
					argb.alpha, argb.red, argb.green, argb.blue, argb.getFrequency(), argb.getRelocation()));
			n++;
		}
	}
}
