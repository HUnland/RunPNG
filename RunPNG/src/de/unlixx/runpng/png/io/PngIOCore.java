package de.unlixx.runpng.png.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import de.unlixx.runpng.bitmap.Scanline32;
import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.chunks.PngHeader;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * This class combines inflating/deflating, filtering, interlacing and
 * reading/writing of the scanlines. The combination of them all at one place improves performance.
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
 * @see <a href="https://www.w3.org/TR/PNG/">https://www.w3.org/TR/PNG</a>
 */
public class PngIOCore
{
	static final int FILTER_NONE = 0;
	static final int FILTER_SUB = 1;
	static final int FILTER_UP = 2;
	static final int FILTER_AVERAGE = 3;
	static final int FILTER_PAETH = 4;

	final byte[] m_abBuffer;
	final byte[] m_abPrevLine;

	byte[][] m_aabFilterBuffers;

	final int m_nScanlineStride;
	final PngColorType m_colorType;
	final int m_nBitDepth;
	final int m_nFilterOffset;
	final int m_nBitsPerPixel;
	final int m_nInterlaceMethod;

	/**
	 * Constructor for this PngFilteringIO class.
	 *
	 * @param header A {@link PngHeader} object
	 * containing the data about the current png file or sequence.
	 */
	public PngIOCore(PngHeader header)
	{
		m_colorType = header.getColorType();
		m_nBitDepth = header.getBitDepth();

		m_nScanlineStride = header.getScanlineStride();

		m_abBuffer = new byte[m_nScanlineStride];

		m_nBitsPerPixel = header.getBitsPerPixel();
		m_nFilterOffset = (m_nBitsPerPixel + 7) / 8;

		m_nInterlaceMethod = header.getInterlaceMethod();

		m_abPrevLine = new byte[m_nScanlineStride];
	}

	/**
	 * Ensures the existence of five buffers for evaluating the filters.
	 */
	void ensureFilterBuffers()
	{
		if (m_aabFilterBuffers == null)
		{
			m_aabFilterBuffers = new byte[][]
			{
				new byte[m_nScanlineStride],
				new byte[m_nScanlineStride],
				new byte[m_nScanlineStride],
				new byte[m_nScanlineStride],
				new byte[m_nScanlineStride]
			};
		}
	}

	/*
	 * See https://www.w3.org/TR/PNG/#8Interlace
	 *
	 *  8 x 8 interlace positioning.
	 *
	 *		0 1 2 3 4 5 6 7
	 *		---------------
	 *	0 | 1 6 4 6 2 6 4 6
	 *	1 | 7 7 7 7 7 7 7 7
	 *	2 | 5 6 5 6 5 6 5 6
	 *	3 | 7 7 7 7 7 7 7 7
	 *	4 | 3 6 4 6 3 6 4 6
	 *	5 | 7 7 7 7 7 7 7 7
	 *	6 | 5 6 5 6 5 6 5 6
	 *	7 | 7 7 7 7 7 7 7 7
	 *
	 */

	/*
	 * X-axis offs x, step x, offs y, step y
	 * Y-axis are passes 0 - 7++
	 */
	static final int OFFSX = 0, STEPX = 1, OFFSY = 2, STEPY = 3;

	/**
	 * ILMX = InterLace MatriX.
	 */
	static final int[][] ILMX =
	{					// Pass
		{ 0, 1, 0, 1 }, // 0 not interlaced
		{ 0, 8, 0, 8 }, // 1
		{ 4, 8, 0, 8 }, // 2
		{ 0, 4, 4, 8 }, // 3
		{ 2, 4, 0, 4 }, // 4
		{ 0, 2, 2, 4 }, // 5
		{ 1, 2, 0, 2 }, // 6
		{ 0, 1, 1, 2 }, // 7
		{ 0, 1, 0, 1 }  // Pseudo pad for pass 7
	};

	/**
	 * Inflates the given bitmap bytes, removes filtering and runs the interlacing passes if needed.
	 *
	 * @param abDeflated The deflated byte array of the bitmap.
	 * @param scanline The {@link Scanline32} object to write to.
	 * @throws DataFormatException In the case of data format problem while inflating.
	 */
	public void inflate(byte[] abDeflated, Scanline32 scanline) throws DataFormatException
	{
		final int nFullBytesPerLine = scanline.getScanlineStride(),
				nWidth = scanline.getBitmapWidth(),
				nFullHeight = scanline.getBitmapHeight();

		final Inflater inflater = new Inflater();
		inflater.setInput(abDeflated);

		int nPass = m_nInterlaceMethod == 0 ? 0 : 1;

		do
		{
			final int nOffsX = ILMX[nPass][OFFSX],
					nStepX = ILMX[nPass][STEPX],
					nPadX = ILMX[nPass + 1][STEPX] - 1,
					nOffsY = ILMX[nPass][OFFSY],
					nStepY = ILMX[nPass][STEPY],
					nPadY = ILMX[nPass + 1][STEPY] - 1,
					nBytesPerLine = nPass == 0 ? nFullBytesPerLine
									// Two steps of padding. Do not "optimize".
									: (nWidth + nPadX) / nStepX * m_nBitsPerPixel / 8 + 1,
					nHeight = nPass == 0 ? nFullHeight : (nFullHeight + nPadY) / nStepY;

			Arrays.fill(m_abPrevLine, (byte)0);

			for (int nLine = 0; nLine < nHeight; nLine++)
			{
				int nInflated = inflater.inflate(m_abBuffer, 0, nBytesPerLine);
				if (nInflated == 0)
				{
					throw new Failure("failure.unexpected.eof", "inflate");
				}

				revertFilter(nBytesPerLine);

				scanline.write(m_abBuffer, 1, nOffsX, nStepX, nLine * nStepY + nOffsY);
			}

			if (nPass == 0 || nPass == 7)
			{
				return;
			}
			else
			{
				nPass++;
			}
		}
		while (true);
	}

	/**
	 * Deflates the given bitmap bytes, applies filtering and runs the interlacing passes if needed.
	 *
	 * @param os The {@link OutputStream} to write to.
	 * @param scanline The {@link Scanline32} object to read from.
	 * @throws IOException In the case of an IO problem.
	 */
	public void deflate(OutputStream os, Scanline32 scanline) throws IOException
	{
		final int nFullBytesPerLine = scanline.getScanlineStride(),
				nWidth = scanline.getBitmapWidth(),
				nFullHeight = scanline.getBitmapHeight();

		Deflater def = new Deflater(9);
		DeflaterOutputStream dos = new DeflaterOutputStream(os, def);

		int nPass = m_nInterlaceMethod == 0 ? 0 : 1;

		do
		{
			Arrays.fill(m_abPrevLine, (byte)0);

			final int nOffsX = ILMX[nPass][OFFSX],
					nStepX = ILMX[nPass][STEPX],
					nPadX = ILMX[nPass + 1][STEPX] - 1,
					nOffsY = ILMX[nPass][OFFSY],
					nStepY = ILMX[nPass][STEPY],
					nPadY = ILMX[nPass + 1][STEPY] - 1,
					nBytesPerLine = nPass == 0 ? nFullBytesPerLine
									// Two steps of padding. Do not "optimize".
									: (nWidth + nPadX) / nStepX * m_nBitsPerPixel / 8 + 1,
					nHeight = nPass == 0 ? nFullHeight : (nFullHeight + nPadY) / nStepY;

			for (int nLine = 0; nLine < nHeight; nLine++)
			{
				scanline.read(m_abBuffer, 1, nOffsX, nStepX, nLine * nStepY + nOffsY);

				// See recommendations in https://www.w3.org/TR/PNG/#12Filter-selection
				if (m_colorType != PngColorType.INDEXED && m_nBitDepth >= 8)
				{
					applyFilter(nBytesPerLine);
				}

				dos.write(m_abBuffer, 0, nBytesPerLine);
			}

			if (nPass == 0 || nPass == 7)
			{
				break;
			}
			else
			{
				nPass++;
			}
		}
		while (true);

		dos.finish();
	}

	/**
	 * Evaluates all 5 filters according recommendation by w3.org and uses the
	 * filter buffer with the lowest sum.
	 *
	 * @param nBytesPerLine The current scanline stride.
	 *
	 * @see <a href="https://www.w3.org/TR/PNG/#12Filter-selection">https://www.w3.org/TR/PNG/#12Filter-selection</a>
	 */
	void applyFilter(int nBytesPerLine)
	{
		ensureFilterBuffers();

		final int[] anSums =
		{
			evalFilterNone(1, nBytesPerLine),
			evalFilterSub(1, nBytesPerLine),
			evalFilterUp(1, nBytesPerLine),
			evalFilterAverage(1, nBytesPerLine),
			evalFilterPaeth(1, nBytesPerLine)
		};

		int nSumBest = Integer.MAX_VALUE,
			nFilterBest = 0;

		for (int n = 0, nFilters = anSums.length; n < nFilters; n++)
		{
			if (anSums[n] < nSumBest)
			{
				nSumBest = anSums[n];
				nFilterBest = n;
			}
		}

		System.arraycopy(m_abBuffer, 1, m_abPrevLine, 0, nBytesPerLine - 1);
		System.arraycopy(m_aabFilterBuffers[nFilterBest], 0, m_abBuffer, 0, nBytesPerLine);
	}

	/**
	 * Reverts the filtering of a scanline according the first byte
	 * in the line buffer.
	 *
	 * @param nBytesPerLine The current scanline stride.
	 */
	void revertFilter(int nBytesPerLine)
	{
		switch (m_abBuffer[0])
		{
		case FILTER_NONE: revertFilterNone(1, nBytesPerLine); break;
		case FILTER_SUB: revertFilterSub(1, nBytesPerLine); break;
		case FILTER_UP: revertFilterUp(1, nBytesPerLine); break;
		case FILTER_AVERAGE: revertFilterAverage(1, nBytesPerLine); break;
		case FILTER_PAETH: revertFilterPaeth(1, nBytesPerLine); break;
		}

		System.arraycopy(m_abBuffer, 1, m_abPrevLine, 0, nBytesPerLine - 1);
	}

	/**
	 * Evaluates the none filter. In fact it just copies
	 * to the evaluation line and calculates the sum.
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 * @return An int containing the sum of the bytes.
	 */
	int evalFilterNone(int nBegin, int nEnd)
	{
		int nSum = 0;

		m_aabFilterBuffers[FILTER_NONE][0] = (byte)FILTER_NONE;

		for (int n = nBegin; n < nEnd; n++)
		{
			m_aabFilterBuffers[FILTER_NONE][n] = m_abBuffer[n];
			nSum += Math.abs(m_aabFilterBuffers[FILTER_NONE][n]);
		}

		return nSum;
	}

	/**
	 * Reverts the none filter
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 */
	void revertFilterNone(int nBegin, int nEnd)
	{
		// Just kidding :-)
	}

	/**
	 * Evaluates the sub filter and calculates the sum.
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 * @return An int containing the sum of the bytes.
	 */
	int evalFilterSub(int nBegin, int nEnd)
	{
		int nAn = nBegin - m_nFilterOffset,
			nSum = 0;

		m_aabFilterBuffers[FILTER_SUB][0] = (byte)FILTER_SUB;

		for (int n = nBegin, m = 1; n < nEnd; n++, nAn++, m++)
		{
			final int nX = (m_abBuffer[n] & 0xff),
				nA = (nAn < nBegin) ? 0 : (m_abBuffer[nAn] & 0xff);

			m_aabFilterBuffers[FILTER_SUB][m] = (byte)((nX - nA) & 0xff);
			nSum += Math.abs(m_aabFilterBuffers[FILTER_SUB][m]);
		}

		return nSum;
	}

	/**
	 * Reverts the sub filter
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 */
	void revertFilterSub(int nBegin, int nEnd)
	{
		int nAn = nBegin;

		for (int n = nBegin + m_nFilterOffset; n < nEnd; n++, nAn++)
		{
			final int nX = (m_abBuffer[n] & 0xff),
				nA = (m_abBuffer[nAn] & 0xff);

			m_abBuffer[n] = (byte)((nX + nA) & 0xff);
		}
	}

	/**
	 * Evaluates the up filter and calculates the sum.
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 * @return An int containing the sum of the bytes.
	 */
	int evalFilterUp(int nBegin, int nEnd)
	{
		int nBn = 0,
			nSum = 0;

		m_aabFilterBuffers[FILTER_UP][0] = (byte)FILTER_UP;

		for (int n = nBegin, m = 1; n < nEnd; n++, nBn++, m++)
		{
			final int nX = (m_abBuffer[n] & 0xff),
				nB = (m_abPrevLine[nBn] & 0xff);

			m_aabFilterBuffers[FILTER_UP][m] = (byte)((nX - nB) & 0xff);
			nSum += Math.abs(m_aabFilterBuffers[FILTER_UP][m]);
		}

		return nSum;
	}

	/**
	 * Reverts the up filter
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 */
	void revertFilterUp(int nBegin, int nEnd)
	{
		int nBn = 0;

		for (int n = nBegin; n < nEnd; n++, nBn++)
		{
			final int nX = (m_abBuffer[n] & 0xff),
				nB = (m_abPrevLine[nBn] & 0xff);

			m_abBuffer[n] = (byte)((nX + nB) & 0xff);
		}
	}

	/**
	 * Evaluates the average filter and calculates the sum.
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 * @return An int containing the sum of the bytes.
	 */
	int evalFilterAverage(int nBegin, int nEnd)
	{
		int nAn = nBegin - m_nFilterOffset,
			nBn = 0,
			nSum = 0;

		m_aabFilterBuffers[FILTER_AVERAGE][0] = (byte)FILTER_AVERAGE;

		for (int n = nBegin, m = 1; n < nEnd; n++, nAn++, nBn++, m++)
		{
			final int nX = m_abBuffer[n],
				nA = (nAn < nBegin) ? 0 : (m_abBuffer[nAn] & 0xff),
				nB = (0xff & m_abPrevLine[nBn]);

			m_aabFilterBuffers[FILTER_AVERAGE][m] = (byte)(nX - ((nA + nB) / 2) & 0xff);
			nSum += Math.abs(m_aabFilterBuffers[FILTER_AVERAGE][m]);
		}

		return nSum;
	}

	/**
	 * Reverts the average filter
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 */
	void revertFilterAverage(int nBegin, int nEnd)
	{
		int nAn = nBegin - m_nFilterOffset,
			nBn = 0;

		for (int n = nBegin; n < nEnd; n++, nAn++, nBn++)
		{
			final int nX = m_abBuffer[n],
				nA = (nAn < nBegin) ? 0 : (m_abBuffer[nAn] & 0xff),
				nB = (0xff & m_abPrevLine[nBn]);

			m_abBuffer[n] = (byte)(nX + ((nA + nB) / 2) & 0xff);
		}
	}

	/**
	 * Calculates a Paeth prediction of the A, C, and B bytes.
	 *
	 * <pre>
	 *  C B
	 *  A X
	 * </pre>
	 *
	 * @param nA The byte left of nX.
	 * @param nB The byte above of nX.
	 * @param nC The byte left of nB.
	 *
	 * @return The prediction as an int.
	 *
	 * @see <a href="https://www.w3.org/TR/PNG/#9Filter-type-4-Paeth">https://www.w3.org/TR/PNG/#9Filter-type-4-Paeth</a>
	 */
	int calcPaethPrediction(int nA, int nB, int nC)
	{
		final int nP = nA + nB - nC,
				nPA = Math.abs(nP - nA),
				nPB = Math.abs(nP - nB),
				nPC = Math.abs(nP - nC);

		return ((nPA <= nPB && nPA <= nPC) ? nA : (nPB <= nPC) ? nB : nC) & 0xff;
	}

	/**
	 * Evaluates the Paeth filter and calculates the sum.
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 * @return An int containing the sum of the bytes.
	 */
	int evalFilterPaeth(int nBegin, int nEnd)
	{
		int nAn = nBegin - m_nFilterOffset,
			nBn = 0,
			nCn = -m_nFilterOffset,
			nSum = 0;

		m_aabFilterBuffers[FILTER_PAETH][0] = (byte)FILTER_PAETH;

		for (int n = nBegin, m = 1; n < nEnd; n++, nAn++, nBn++, nCn++, m++)
		{
			final int nA = (nAn < nBegin) ? 0 : (m_abBuffer[nAn] & 0xff),
				nB = m_abPrevLine[nBn] & 0xff,
				nC = (nCn < 0) ? 0 : (m_abPrevLine[nCn] & 0xff),
				nX = (m_abBuffer[n] & 0xff);

			m_aabFilterBuffers[FILTER_PAETH][m] = (byte)((nX - calcPaethPrediction(nA, nB, nC)) & 0xff);
			nSum += Math.abs(m_aabFilterBuffers[FILTER_PAETH][m]);
		}

		return nSum;
	}

	/**
	 * Reverts the Paeth filter
	 *
	 * @param nBegin Begin index in the scanline buffer.
	 * @param nEnd End index in the scanline buffer.
	 */
	void revertFilterPaeth(int nBegin, int nEnd)
	{
		int nAn = nBegin - m_nFilterOffset,
			nBn = 0,
			nCn = -m_nFilterOffset;

		for (int n = nBegin; n < nEnd; n++, nAn++, nBn++, nCn++)
		{
			final int nA = (nAn < nBegin) ? 0 : (m_abBuffer[nAn] & 0xff),
				nB = (m_abPrevLine[nBn] & 0xff),
				nC = (nAn < nBegin) ? 0 : (m_abPrevLine[nCn] & 0xff),
				nX = (m_abBuffer[n] & 0xff);

			m_abBuffer[n] = (byte)((nX + calcPaethPrediction(nA, nB, nC)) & 0xff);
		}
	}
}
