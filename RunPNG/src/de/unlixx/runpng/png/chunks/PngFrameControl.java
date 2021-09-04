package de.unlixx.runpng.png.chunks;

import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.png.io.PngChunkInputStream;

/**
 * Implements the frame control chunk (fcTL). This chunk must appear before each animated frame (bitmap).
 * This shows also the type of animation. If the first fcTL chunk appears before the IDAT block, then all
 * frames (bitmaps) are included in the animation. If not then the first frame will be skipped
 * in the loop.
 * PngFrameControl gets all data from the {@link PngChunkInputStream}
 * while reading from file, except the sequence number which is never needed after.
 * Because the animated frames are optimized delta bitmaps in many cases, they are smaller sized with
 * an xy-offset. This information and how the dispose and blend operations shall be handled is also
 * stored in the PngFrameControl.
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
 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>
 *
 */
public class PngFrameControl
{
	public static final int DISPOSE_OP_NONE = 0;
	public static final int DISPOSE_OP_BACKGROUND = 1;
	public static final int DISPOSE_OP_PREVIOUS = 2;

	public static final int BLEND_OP_SOURCE = 0;
	public static final int BLEND_OP_OVER = 1;

	int m_nWidth;
	int m_nHeight;
	int m_nXOffset;
	int m_nYOffset;
	final PngDelayFraction m_fractionDelay;
	int m_nDisposeOp;
	int m_nBlendOp;

	/**
	 * Truncated constructor with default dispose and blend operators.
	 *
	 * @param nWidth The width of the frame, probably smaller than the header width.
	 * @param nHeight The height of the frame, probably smaller than the header height.
	 * @param nXOffset The horizontal offset of the frame in the whole png size.
	 * @param nYOffset The vertical offset of the frame in the whole png size.
	 * @param nDelayNum The delay numerator.
	 * @param nDelayDen The delay denominator.
	 *
	 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>
	 */
	public PngFrameControl(int nWidth, int nHeight, int nXOffset, int nYOffset, int nDelayNum, int nDelayDen)
	{
		this(nWidth, nHeight, nXOffset, nYOffset, nDelayNum, nDelayDen, DISPOSE_OP_BACKGROUND, BLEND_OP_SOURCE);
	}

	/**
	 * Constructor for the fcTL chunk as read from input stream. The sequence number
	 * of the fcTL chunk is never needed after and will be discarded.
	 *
	 * @param nWidth The width of the frame, probably smaller than the header width.
	 * @param nHeight The height of the frame, probably smaller than the header height.
	 * @param nXOffset The horizontal offset of the frame in the whole png size.
	 * @param nYOffset The vertical offset of the frame in the whole png size.
	 * @param nDelayNum The delay numerator.
	 * @param nDelayDen The delay denominator.
	 * @param nDisposeOp The dispose operator.
	 * @param nBlendOp The blend operator.
	 *
	 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>
	 */
	public PngFrameControl(int nWidth, int nHeight, int nXOffset, int nYOffset,
						int nDelayNum, int nDelayDen, int nDisposeOp, int nBlendOp)
	{
		m_nWidth = nWidth;
		m_nHeight = nHeight;
		m_nXOffset = nXOffset;
		m_nYOffset = nYOffset;
		m_fractionDelay = new PngDelayFraction(nDelayNum, nDelayDen);
		m_nDisposeOp = nDisposeOp;
		m_nBlendOp = nBlendOp;
	}

	/**
	 * Gets the width of the frame, which is probably smaller than the header width.
	 *
	 * @return An int containing the width of the frame.
	 */
	public int getWidth()
	{
		return m_nWidth;
	}

	/**
	 * Gets the height of the frame, which is probably smaller than the header height.
	 *
	 * @return An int containing the width of the frame.
	 */
	public int getHeight()
	{
		return m_nHeight;
	}

	/**
	 * Gets the x_offset of the frame.
	 *
	 * @return An int containing the x_offset of the frame.
	 */
	public int getXOffset()
	{
		return m_nXOffset;
	}

	/**
	 * Gets the y_offset of the frame.
	 *
	 * @return An int containing the y_offset of the frame.
	 */
	public int getYOffset()
	{
		return m_nYOffset;
	}

	/**
	 * Gets the delay_num (delay numerator) of the frame.
	 *
	 * @return An int containing the delay_num of the frame.
	 */
	public int getDelayNum()
	{
		return m_fractionDelay.getDelayNum();
	}

	/**
	 * Gets the delay_den (delay denominator) of the frame.
	 *
	 * @return An int containing the delay_den of the frame.
	 */
	public int getDelayDen()
	{
		return m_fractionDelay.getDelayDen();
	}

	/**
	 * Gets the currently used PngDelayFraction object.
	 *
	 * @return A {@link PngDelayFraction} object.
	 */
	public PngDelayFraction getDelayFraction()
	{
		return m_fractionDelay;
	}

	/**
	 * Gets the dispose_op (dispose operator) for the frame.
	 *
	 * @return An int containing the dispose_op of the frame.
	 */
	public int getDisposeOp()
	{
		return m_nDisposeOp;
	}

	/**
	 * Sets the dispose_op (dispose operator) for the frame.
	 *
	 * @param nDisposeOp An int containing the new dispose_op of the frame.
	 */
	public void setDisposeOp(int nDisposeOp)
	{
		m_nDisposeOp = nDisposeOp;
	}

	/**
	 * Gets the blend_op (blend operator) for the frame.
	 *
	 * @return An int containing the blend_op of the frame.
	 */
	public int getBlendOp()
	{
		return m_nBlendOp;
	}

	/**
	 * Sets the blend_op (blend operator) for the frame.
	 *
	 * @param nBlendOp An int containing the new blend_op of the frame.
	 */
	public void setBlendOp(int nBlendOp)
	{
		m_nBlendOp = nBlendOp;
	}

	@Override
	public PngFrameControl clone()
	{
		return new PngFrameControl(m_nWidth, m_nHeight, m_nXOffset, m_nYOffset,
				m_fractionDelay.getDelayNum(), m_fractionDelay.getDelayDen(), m_nDisposeOp, m_nBlendOp);
	}
}
