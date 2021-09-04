package de.unlixx.runpng.util;

import java.util.Comparator;

/**
 * Stack friendly implementation of a pivot sort algorithm. Stack friendly
 * means that it works without recursions.
 * This is a sort in-place.
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
 * @param <T> The class type to sort.
 */
public class PivotSorter<T>
{
	/**
	 * Sort the given array in-place.
	 *
	 * @param <T> The data type to sort.
	 * @param array The array to sort.
	 * @param nBegin Begin index in the given array.
	 * @param nEnd End index  in the given array.
	 * @param comparator Comparator for the data type &lt;T&gt;.
	 */
	private static <T> void sort(final T[] array, int nBegin, int nEnd, final Comparator<T> comparator)
	{
		final int[] anStack = new int[nEnd - nBegin + 1];
		int nIdx = 0;

		T tPivot, tTemp;

		anStack[nIdx] = nBegin;
		anStack[++nIdx] = nEnd;

		while (nIdx >= 0)
		{
			nEnd = anStack[nIdx--];
			nBegin = anStack[nIdx--];

			tPivot = array[nEnd];

			int nSplit = nBegin - 1;
			for (int n = nBegin; n <= nEnd - 1; n++)
			{
				if (comparator.compare(array[n], tPivot) <= 0)
				{
					tTemp = array[++nSplit];
					array[nSplit] = array[n];
					array[n] = tTemp;
				}
			}

			tTemp = array[++nSplit];
			array[nSplit] = array[nEnd];
			array[nEnd] = tTemp;

			if (nSplit - 1 > nBegin)
			{
				anStack[++nIdx] = nBegin;
				anStack[++nIdx] = nSplit - 1;
			}

			if (nSplit + 1 < nEnd)
			{
				anStack[++nIdx] = nSplit + 1;
				anStack[++nIdx] = nEnd;
			}
		}
	}

	/**
	 * Sort the given array in-place.
	 *
	 * @param <T> The data type to sort.
	 * @param array The array to sort.
	 * @param comparator Comparator for the data type &lt;T&gt;.
	 */
	public static <T> void sort(T[] array, Comparator<T> comparator)
	{
		if (array != null && array.length > 1)
		{
			sort(array, 0, array.length - 1, comparator);
		}
	}
}
