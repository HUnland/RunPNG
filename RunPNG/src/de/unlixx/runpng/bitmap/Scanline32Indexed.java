package de.unlixx.runpng.bitmap;

import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * {@link Scanline32} implementation for indexed color type 3.
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
public class Scanline32Indexed extends Scanline32
{
	final int m_nMaxBit;
	final int m_nBitMask;
	final int[] m_anShifts;

	boolean m_bTransparencyApplied;

	/**
	 * Package private constructor for Scanline32Indexed. This will be invoked by a static
	 * method of the {@link Scanline32} super class.
	 *
	 * @param colorType A {@link PngColorType} enum type.
	 * @param nBitDepth An int containing the bit depth of the color.
	 * @param bitmap A {@link Bitmap32} object.
	 */
	Scanline32Indexed(PngColorType colorType, int nBitDepth, Bitmap32 bitmap)
	{
		super(colorType, nBitDepth, bitmap);

		switch (m_colorType)
		{
		case GREYSCALE:
			switch (m_nBitDepth)
			{
			case 1: m_palette = PngPalette.getGreyscale(1); break;
			case 2: m_palette = PngPalette.getGreyscale(2); break;
			case 4: m_palette = PngPalette.getGreyscale(4); break;
			case 8: // Shouldn't be possible
			default: throw new Failure("failure.wrong.bitdepth");
			}
			break;

		case INDEXED:
		default: break; // Needs PLTE chunk
		}

		switch (nBitDepth)
		{
		case 1:
			m_nMaxBit = 7;
			m_nBitMask = 1;
			m_anShifts = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7 };
			break;

		case 2:
			m_nMaxBit = 3;
			m_nBitMask = 3;
			m_anShifts = new int[]{ 0, 2, 4, 6 };
			break;

		case 4:
			m_nMaxBit = 1;
			m_nBitMask = 15;
			m_anShifts = new int[]{ 0, 4 };
			break;

		default:
			m_nMaxBit = 0;
			m_nBitMask = 0;
			m_anShifts = null;
			break;
		}
	}

	@Override
	public void write(byte[] abSrc, int nSrcIdx, int nOffsX, int nStepX, int nLine)
	{
		final int[] anDest = m_bitmap.getPixels();
		final int nWidth = m_bitmap.getWidth();

		if (m_palette == null)
		{
			throw new Failure("failure.missing.palette");
		}

		if (!m_bTransparencyApplied)
		{
			applyTransparency();
		}

		switch (m_nBitDepth)
		{
		case 1:
		case 2:
		case 4:
			int nBit = m_nMaxBit;

			for (int nX = nOffsX; nX < nWidth; nX += nStepX)
			{
				final int nIdx = (abSrc[nSrcIdx] >> m_anShifts[nBit]) & m_nBitMask;
				anDest[nLine * nWidth + nX] = m_palette.get(nIdx);

				if (nBit == 0)
				{
					nSrcIdx++;
					nBit = m_nMaxBit;
				}
				else
				{
					nBit--;
				}
			}
			break;

		case 8:
			for (int nX = nOffsX; nX < nWidth; nX += nStepX)
			{
				final int nIdx = abSrc[nSrcIdx++] & 0xff;
				anDest[nLine * nWidth + nX] = m_palette.get(nIdx);
			}
			break;

		default:
			throw new Failure("failure.wrong.bitdepth");
		}
	}

	@Override
	public void read(byte[] abDest, int nDestIdx, int nOffsX, int nStepX, int nLine)
	{
		if (m_bTransparencyApplied)
		{
			throw new RuntimeException("Transparency applied for writing a picture?");
		}

		final int[] anSrc = m_bitmap.getPixels();
		final int nWidth = m_bitmap.getWidth();

		// TODO: Lower bit depth etc.

		switch (m_nBitDepth)
		{
		case 8:
			for (int nX = nOffsX; nX < nWidth; nX += nStepX)
			{
				final int nARGB = anSrc[nLine * nWidth + nX],
						nIdx = m_palette.find(nARGB);
				abDest[nDestIdx++] = (byte)(nIdx & 0xff);
			}
			break;

		default:
			throw new Failure("failure.wrong.bitdepth");
		}
	}

	@Override
	public void setTransparency(PngTransparency transparency)
	{
		m_transparency = transparency;
	}

	/**
	 * Tries to apply a transparency chunk to the palette. This all in case both exist.
	 * @see <a href="https://www.w3.org/TR/PNG/#11PLTE">https://www.w3.org/TR/PNG/#11PLTE</a>
	 * @see <a href="https://www.w3.org/TR/PNG/#11tRNS">https://www.w3.org/TR/PNG/#11tRNS</a>
	 */
	void applyTransparency()
	{
		if (m_transparency != null)
		{
			byte[] abTrans = m_transparency.getBytes();
			int nLen = abTrans.length;

			if (m_palette == null)
			{
				throw new Failure("failure.missing.palette");
			}

			if (nLen > m_palette.length())
			{
				throw new Failure("failure.wrong.transparency.length", m_palette.length(), nLen);
			}

			// In case the length of tRNS is shorter than palette length, the palette is
			// already initialized fully opaque.
			for (int n = 0; n < nLen; n++)
			{
				m_palette.set(n, (abTrans[n] & 0xff) << 24 | m_palette.get(n) & 0x00ffffff);
			}
		}

		m_bTransparencyApplied = true;
	}
}
