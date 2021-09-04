package de.unlixx.runpng.bitmap;

import java.util.Arrays;

import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * A simple ARGB bitmap object with an optional {@link PngFrameControl} object for APNG use.
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
public class Bitmap32
{
	int[] m_anPixels;
	int m_nWidth;
	int m_nHeight;
	PngFrameControl m_fcTL;

	/**
	 * Constructs a transparent Bitmap32 object.
	 *
	 * @param nWidth An int with the width value.
	 * @param nHeight An int with the height value.
	 */
	public Bitmap32(int nWidth, int nHeight)
	{
		this(null, nWidth, nHeight);
	}

	/**
	 * Constructs a Bitmap32 object.
	 *
	 * @param anPixels An int array with the initial pixels.
	 * If null then a transparent pixel array of width x height will be created.
	 * Afterwards the given pixel array will be copied. If it is shorter than
	 * width x height, then the copy will be padded by zeros. If it is longer
	 * then the copy will be truncated.
	 * @param nWidth An int with the width value.
	 * @param nHeight An int with the height value.
	 */
	public Bitmap32(int[] anPixels, int nWidth, int nHeight)
	{
		m_nWidth = nWidth;
		m_nHeight = nHeight;

		if (anPixels == null)
		{
			m_anPixels = new int[nWidth * nHeight];
		}
		else
		{
			m_anPixels = Arrays.copyOf(anPixels, nWidth * nHeight);
		}
	}

	/**
	 * Constructs a Bitmap32 object with dimensions based on a given
	 * PngFrameControl object.
	 *
	 * @param fcTL A {@link PngFrameControl} object.
	 * @param anPixels An int array with the initial pixels.
	 * If null then a transparent pixel array of width x height will be created.
	 * Afterwards the given pixel array will be copied. If it is shorter than
	 * width x height, then the copy will be padded by zeros. If it is longer
	 * then the copy will be truncated.
	 */
	public Bitmap32(PngFrameControl fcTL, int[] anPixels)
	{
		this(anPixels, fcTL.getWidth(), fcTL.getHeight());

		m_fcTL = fcTL;
	}

	/**
	 * Gets access to the ARGB pixel array.
	 *
	 * @return An int array with the bitmap pixels.
	 */
	public int[] getPixels()
	{
		return m_anPixels;
	}

	/**
	 * Gets the width of this bitmap object.
	 *
	 * @return An int with the width value.
	 */
	public int getWidth()
	{
		return m_nWidth;
	}

	/**
	 * Gets the height of this bitmap object.
	 *
	 * @return An int with the height value.
	 */
	public int getHeight()
	{
		return m_nHeight;
	}

	/**
	 * Sets the PngFrameControl object for this bitmap.
	 *
	 * @param fcTL A {@link PngFrameControl} object.
	 */
	public void setFrameControl(PngFrameControl fcTL)
	{
		if (fcTL.getWidth() != m_nWidth || fcTL.getHeight() != m_nHeight)
		{
			throw new Failure("failure.wrong.size", m_nWidth, m_nHeight, fcTL.getWidth(), fcTL.getHeight());
		}

		m_fcTL = fcTL;
	}

	/**
	 * Gets the PngFrameControl of this bitmap if any.
	 *
	 * @return A {@link PngFrameControl} object.
	 * Or null if not set.
	 */
	public PngFrameControl getFrameControl()
	{
		return m_fcTL;
	}

	@Override
	public Bitmap32 clone()
	{
		Bitmap32 clone = new Bitmap32(m_nWidth, m_nHeight);
		System.arraycopy(m_anPixels, 0, clone.m_anPixels, 0, m_anPixels.length);
		if (m_fcTL != null)
		{
			clone.m_fcTL = m_fcTL.clone();
		}
		return clone;
	}
}
