package de.unlixx.runpng.bitmap;

import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * {@link Scanline32} implementation for greyscale color types 0 or 5.
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
public class Scanline32Greyscale extends Scanline32
{
	int m_ntRNS = -1;

	// Faster than using PngPalette
	static final int[] GREY4 =
	{
		0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
		0x88, 0x99, 0xaa, 0xbb, 0xcc, 0xdd, 0xee, 0xff
	};

	/**
	 * Package private constructor for Scanline32Greyscale. This will be invoked by a static
	 * method of the {@link Scanline32} super class.
	 *
	 * @param colorType A {@link PngColorType} enum type.
	 * @param nBitDepth An int containing the bit depth of the color.
	 * @param bitmap A {@link Bitmap32} object.
	 */
	Scanline32Greyscale(PngColorType colorType, int nBitDepth, Bitmap32 bitmap)
	{
		super(colorType, nBitDepth, bitmap);
	}

	@Override
	public void write(byte[] abSrc, int nSrcIdx, int nOffsX, int nStepX, int nLine)
	{
		final int[] anDest = m_bitmap.getPixels();
		final int nWidth = m_bitmap.getWidth();

		if (m_colorType.hasAlpha())
		{
			switch (m_nBitDepth)
			{
			case 4:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					// Upper nibble = luminance index, lower nibble = alpha index
					final int nIdx = abSrc[nSrcIdx++];
					final int nL = GREY4[(nIdx >> 4) & 0x0f];
					final int nA = GREY4[nIdx & 0x0f];

					anDest[nLine * nWidth + nX] = nA << 24 | nL << 16 | nL << 8 | nL;
				}
				break;

			case 8:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nL = abSrc[nSrcIdx++] & 0xff;
					final int nA = abSrc[nSrcIdx++] & 0xff;

					anDest[nLine * nWidth + nX] = nA << 24 | nL << 16 | nL << 8 | nL;
				}
				break;

			case 16:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nL = abSrc[nSrcIdx++] & 0xff;
					nSrcIdx++;
					final int nA = abSrc[nSrcIdx++] & 0xff;
					nSrcIdx++;

					anDest[nLine * nWidth + nX] = nA << 24 | nL << 16 | nL << 8 | nL;
				}
				break;

			default:
				throw new Failure("failure.wrong.bitdepth");
			}
		}
		else
		{
			switch (m_nBitDepth)
			{
			case 8:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nL0 = (abSrc[nSrcIdx++] & 0xff);
					final boolean b = m_ntRNS == nL0;
					final int nL = b ? 0 : nL0,
							nA = b ? 0 : 0xff000000;

					anDest[nLine * nWidth + nX] = nA | nL << 16 | nL << 8 | nL;
				}
				break;

			case 16:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nL0 = (abSrc[nSrcIdx++] & 0xff);
					final int nL1 = (abSrc[nSrcIdx++] & 0xff);
					final boolean b = m_ntRNS == ((nL0 << 8) | nL1);
					final int nL = b ? 0 : nL0,
							nA = b ? 0 : 0xff000000;

					anDest[nLine * nWidth + nX] = nA | nL << 16 | nL << 8 | nL;
				}
				break;

			default:
				throw new Failure("failure.wrong.bitdepth");
			}
		}
	}

	@Override
	public void read(byte[] abDest, int nDestIdx, int nOffsX, int nStepX, int nLine)
	{
		final int[] anSrc = m_bitmap.getPixels();
		final int nWidth = m_bitmap.getWidth();

		if (m_colorType.hasAlpha())
		{
			switch (m_nBitDepth)
			{
			case 4:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nARGB = anSrc[nLine * nWidth + nX],
							nL = rgbToLuminosity(nARGB & 0xffffff),
							nA = ((nARGB >>> 24) & 0xff);

					// Upper nibble = luminance index, lower nibble = alpha index
					abDest[nDestIdx++] = (byte)(((nearestGrey4Index(nL) << 4) & 0xf0) | (nearestGrey4Index(nA) & 0x0f));
				}
				break;

			case 8:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nARGB = anSrc[nLine * nWidth + nX];

					// ARGB to luminosity with alpha
					abDest[nDestIdx++] = (byte)(rgbToLuminosity(nARGB & 0x00ffffff) & 0xff);
					abDest[nDestIdx++] = (byte)((nARGB >>> 24) & 0xff);
				}
				break;

			default:
				throw new Failure("failure.wrong.bitdepth");
			}
		}
		else
		{
			switch (m_nBitDepth)
			{
			case 8:
				// TODO: This needs a tRNS chunk and decision which color.
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nARGB = anSrc[nLine * nWidth + nX],
							nL = rgbToLuminosity(nARGB & 0xffffff);
					abDest[nDestIdx++] = (byte)(nL & 0xff);
				}
				break;

			default:
				throw new Failure("failure.wrong.bitdepth");
			}
		}
	}

	@Override
	public void setTransparency(PngTransparency tRNS)
	{
		m_ntRNS = tRNS != null ? (int)tRNS.gettRNS() : -1;
	}

	/**
	 * Find the nearest index in the GREY4 array above.
	 *
	 * @param nX An int containing a luminance or alpha value.
	 * @return An int containing the nearest index of a value in the GREY4 array above.
	 */
	static int nearestGrey4Index(final int nX)
	{
		// Reminder: Checked with Grey4Test in eval

		final int nIdx = (nX >> 4) & 0x0f,
				nDiff = nX - GREY4[nIdx];

		if (nDiff > 0)
		{
			return nDiff < GREY4[nIdx + 1] - nX ? nIdx : nIdx + 1;
		}
		else if (nDiff < 0)
		{
			return nDiff > GREY4[nIdx - 1] - nX ? nIdx : nIdx - 1;
		}

		return nIdx;
	}

	/**
	 * Converts an RGB color into grey.
	 *
	 * @param nRGB An int containing an RGB value in it's lower three bytes.
	 * @return An int with the greyscale value in the lowest byte.
	 */
	static int rgbToLuminosity(final int nRGB)
	{
		// TODO: There are many articles in the web describing luminosity weighting. Not sure here.
		final int nR = ((nRGB >>> 16) & 0xff),
				nG = ((nRGB >>> 8) & 0xff),
				nB = (nRGB & 0xff),
				nL = (nR == nG && nG == nB) ? nR : // Is already grey
					(int)(Math.min(0.299 * nR + 0.587 * nG + 0.114 * nB, 255)) & 0xff; // Luminosity weighting
		return nL;
	}
}
