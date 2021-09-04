package de.unlixx.runpng.bitmap;

import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * {@link Scanline32} implementation for true color types 2 or 6.
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
public class Scanline32TrueColor extends Scanline32
{
	// TODO: Suggested palettes (PLTE or sPLT)?

	long m_ltRNS = -1;

	/**
	 * Package private constructor for Scanline32TrueColor. This will be invoked by a static
	 * method of the {@link Scanline32} super class.
	 *
	 * @param colorType A {@link PngColorType} enum type.
	 * @param nBitDepth An int containing the bit depth of the color.
	 * @param bitmap A {@link Bitmap32} object.
	 */
	Scanline32TrueColor(PngColorType colorType, int nBitDepth, Bitmap32 bitmap)
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
			case 8:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nR = abSrc[nSrcIdx++] & 0xff;
					final int nG = abSrc[nSrcIdx++] & 0xff;
					final int nB = abSrc[nSrcIdx++] & 0xff;
					final int nA = abSrc[nSrcIdx++] & 0xff;

					anDest[nLine * nWidth + nX] = nA << 24 | nR << 16 | nG << 8 | nB;
				}
				break;

			case 16:
				// TODO: Warning for quality loss in App
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nR = abSrc[nSrcIdx++] & 0xff;
					nSrcIdx++;
					final int nG = abSrc[nSrcIdx++] & 0xff;
					nSrcIdx++;
					final int nB = abSrc[nSrcIdx++] & 0xff;
					nSrcIdx++;
					final int nA = abSrc[nSrcIdx++] & 0xff;
					nSrcIdx++;

					anDest[nLine * nWidth + nX] = nA << 24 | nR << 16 | nG << 8 | nB;
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
					final int nR = abSrc[nSrcIdx++] & 0xff;
					final int nG = abSrc[nSrcIdx++] & 0xff;
					final int nB = abSrc[nSrcIdx++] & 0xff;
					final int nRGB = nR << 16 | nG << 8 | nB;

					if (nRGB == m_ltRNS)
					{
						anDest[nLine * nWidth + nX] = 0;
					}
					else
					{
						anDest[nLine * nWidth + nX] = 0xff000000 | nR << 16 | nG << 8 | nB;
					}
				}
				break;

			case 16:
				// TODO: Warning for quality loss in App
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nR = abSrc[nSrcIdx++] & 0xff;
					final int nR1 = abSrc[nSrcIdx++] & 0xff;
					final int nG = abSrc[nSrcIdx++] & 0xff;
					final int nG1 = abSrc[nSrcIdx++] & 0xff;
					final int nB = abSrc[nSrcIdx++] & 0xff;
					final int nB1 = abSrc[nSrcIdx++] & 0xff;
					final int nRGB16 = nR << 40 | nR1 << 32 | nG << 24 | nG1 << 16 | nB << 8 | nB1;

					if (nRGB16 == m_ltRNS)
					{
						anDest[nLine * nWidth + nX] = 0;
					}
					else
					{
						anDest[nLine * nWidth + nX] = 0xff000000 | nR << 16 | nG << 8 | nB;
					}
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
			case 8:
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nARGB = anSrc[nLine * nWidth + nX];

					// ARGB to RGBA
					abDest[nDestIdx++] = (byte)((nARGB >>> 16) & 0xff);
					abDest[nDestIdx++] = (byte)((nARGB >>> 8) & 0xff);
					abDest[nDestIdx++] = (byte)(nARGB & 0xff);
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
				for (int nX = nOffsX; nX < nWidth; nX += nStepX)
				{
					final int nRGB = anSrc[nLine * nWidth + nX];

					// RGB to RGB
					abDest[nDestIdx++] = (byte)((nRGB >>> 16) & 0xff);
					abDest[nDestIdx++] = (byte)((nRGB >>> 8) & 0xff);
					abDest[nDestIdx++] = (byte)(nRGB & 0xff);
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
		m_ltRNS = tRNS != null ? tRNS.gettRNS() : -1;
	}
}
