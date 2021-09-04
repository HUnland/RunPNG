package de.unlixx.runpng.util;

import java.util.Comparator;

public class Sorter<T>
{
	public static <T> void sort(T[] arr, Comparator<T> comparator)
	{
		pivotSort(arr, 0, arr.length - 1, comparator);
	}

	protected void pivotSort(T[] arr, int nBegin, int nEnd, Comparator<T> comparator)
	{
	    if (nBegin <= nEnd)
	    {
		    int nSplit = nBegin;
		    T pivot = arr[nEnd];

		    for (int n = nBegin; n <= nEnd; n++)
		    {
		    	int nSort = comparator.compare(arr[n], pivot);
				if (nSort <= 0)
				{
					if (nSplit != n)
					{
						T tmp = arr[nSplit];
						arr[nSplit] = arr[n];
						arr[n] = tmp;
					}

					nSplit++;
				}
		    }

		    arr[nEnd] = arr[nSplit];
		    arr[nSplit] = pivot;

		    pivotSort(arr, nBegin, nSplit - 1, comparator);
		    pivotSort(arr, nSplit + 1, nEnd, comparator);
	    }
	}

	/*
	protected static <T> void pivotSort(T[] arr, int nBegin, int nEnd, Comparator<T> comparator)
	{
	    if (nEnd - nBegin <= 0)
	    {
	    	return;
	    }

	    int nSplit = nBegin;
	    T pivot = arr[nEnd];

	    for (int n = nBegin; n < nEnd; n++)
	    {
	    	int nSort = comparator.compare(arr[n], pivot);
			if (nSort <= 0)
			{
				if (nSplit != n)
				{
					T tmp = arr[nSplit];
					arr[nSplit] = arr[n];
					arr[n] = tmp;
				}

				nSplit++;
			}
	    }

	    arr[nEnd] = arr[nSplit];
	    arr[nSplit] = pivot;

	    pivotSort(arr, nBegin, nSplit - 1, comparator);
	    pivotSort(arr, nSplit + 1, nEnd, comparator);
	}
	*/
}
