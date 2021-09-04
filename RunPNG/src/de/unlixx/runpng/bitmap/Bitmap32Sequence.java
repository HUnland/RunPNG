package de.unlixx.runpng.bitmap;

import java.util.ArrayList;
import java.util.List;

import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.chunks.PngAnimationControl;
import de.unlixx.runpng.png.chunks.PngHeader;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.png.chunks.PngText;
import de.unlixx.runpng.png.chunks.PngTransparency;

/**
 * Bitmap32Sequence for animation usage. This class is used to transport
 * single and multiple {@link Bitmap32} objects
 * from and to storage. It holds also common png chunks like PLTE, trNS, acTL, tEXt etc.
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
public class Bitmap32Sequence
{
	PngHeader m_header;
	PngAnimationControl m_animationControl = new PngAnimationControl(1, 0);
	PngAnimationType m_animationType = PngAnimationType.NONE;
	List<Bitmap32> m_frames = new ArrayList<>();
	List<PngText> m_textChunks = new ArrayList<>();
	Bitmap32 m_bitmapDefault;
	PngPalette m_palette;
	PngTransparency m_transparency;
	boolean m_bOptimized;

	/**
	 * Constructor with a default bitmap and an animation type. The png header will be built automatically.
	 *
	 * @param bitmap A {@link Bitmap32} object.
	 * @param bOptimized True if this sequence should be seen and handled as already optimized.
	 * @param animType A {@link PngAnimationType} enum type.
	 */
	public Bitmap32Sequence(Bitmap32 bitmap, boolean bOptimized, PngAnimationType animType)
	{
		m_header = new PngHeader(bitmap.getWidth(), bitmap.getHeight(), 8, PngColorType.TRUECOLOR_ALPHA, 0, 0, 0);
		m_bitmapDefault = bitmap;

		m_animationType = animType;
		if (m_animationType == PngAnimationType.ANIMATED)
		{
			addFrame(bitmap);
		}

		m_bOptimized = bOptimized;
	}

	/**
	 * Constructor with a given header. This will normally be used by the
	 * {@link Bitmap32Manager}
	 * in a read process while receiving the header chunk. The default bitmap will be built as a transparent
	 * rectangle by information from the header.
	 *
	 * @param header A {@link PngHeader} object.
	 * @param bOptimized True if this sequence should be seen and handled as already optimized.
	 */
	public Bitmap32Sequence(PngHeader header, boolean bOptimized)
	{
		m_header = new PngHeader(header.getWidth(), header.getHeight(), 8, PngColorType.TRUECOLOR_ALPHA, header.getCompressionMethod(), header.getFilterMethod(), header.getInterlaceMethod());
		m_bitmapDefault = new Bitmap32(header.getWidth(), header.getHeight());
		m_bOptimized = bOptimized;
	}

	/**
	 * Sets the header chunk (IHDR) of this Bitmap32Sequence.
	 *
	 * @param header A {@link PngHeader} object.
	 */
	public void setHeader(PngHeader header)
	{
		m_header = header;
	}

	/**
	 * Gets the header chunk (IHDR) of this Bitmap32Sequence.
	 *
	 * @return A {@link PngHeader} object.
	 */
	public PngHeader getHeader()
	{
		return m_header;
	}

	/**
	 * Sets the palette chunk (PLTE) of this Bitmap32Sequence in case of indexed color type.
	 *
	 * @param palette A {@link PngPalette} object.
	 */
	public void setPalette(PngPalette palette)
	{
		m_palette = palette;
	}

	/**
	 * Gets the palette chunk (PLTE) of this Bitmap32Sequence in case of indexed color type.
	 *
	 * @return A {@link PngPalette} object.
	 * Or null if not applicable.
	 */
	public PngPalette getPalette()
	{
		return m_palette;
	}

	/**
	 * Sets the transparency chunk (tRNS) of this Bitmap32Sequence in case of indexed color type
	 * with transparencies or in case of truecolor/greyscale color type with a transparency sample..
	 *
	 * @param transparency A {@link PngTransparency} object.
	 */
	public void setTransparency(PngTransparency transparency)
	{
		m_transparency = transparency;
	}

	/**
	 * Gets the transparency chunk (tRNS) of this Bitmap32Sequence in case of indexed color type
	 * with transparencies or in case of truecolor/greyscale color type with a transparency sample..
	 *
	 * @return A {@link PngTransparency} object.
	 * Or null if not applicable.
	 */
	public PngTransparency getTransparency()
	{
		return m_transparency;
	}

	/**
	 * Replaces the default bitmap with another one. The implementing code has to take care
	 * about the existence of a frame control (fcTL) in the bitmap object if needed.
	 *
	 * @param bitmap A {@link Bitmap32} object.
	 */
	public void setDefaultBitmap(Bitmap32 bitmap)
	{
		m_frames.remove(m_bitmapDefault);

		if (m_animationType == PngAnimationType.ANIMATED)
		{
			m_frames.add(0, bitmap);
		}

		m_bitmapDefault = bitmap;
	}

	/**
	 * Gets the actual default bitmap.
	 *
	 * @return A {@link Bitmap32} object.
	 */
	public Bitmap32 getDefaultBitmap()
	{
		return m_bitmapDefault;
	}

	/**
	 * Sets the optimized flag of this sequence.
	 *
	 * @param bOptimized True if this sequence should be seen and handled as already optimized.
	 */
	public void setOptimized(boolean bOptimized)
	{
		m_bOptimized = bOptimized;
		m_animationControl.setNumFrames(m_animationType != PngAnimationType.NONE ? m_frames.size() : 0);
	}

	/**
	 * Gets the optimized flag of this sequence.
	 *
	 * @return True if this sequence should be seen and handled as already optimized.
	 */
	public boolean isOptimized()
	{
		return m_bOptimized;
	}

	/**
	 * Checks whether this sequence should be seen as animated.
	 *
	 * @return True, if this sequence has the flag PngAnimationType.ANIMATED
	 * or PngAnimationType.SKIPFIRST.
	 */
	public boolean isAnimated()
	{
		return m_animationType != PngAnimationType.NONE;
	}

	/**
	 * Sets the animation type of this sequence.
	 *
	 * @param type A {@link PngAnimationType} enum type.
	 */
	public void setAnimationType(PngAnimationType type)
	{
		m_animationType = type;
	}

	/**
	 * Gets the animation type of this sequence.
	 *
	 * @return A {@link PngAnimationType} enum type.
	 */
	public PngAnimationType getAnimationType()
	{
		return m_animationType;
	}

	/**
	 * Gets the animation control (acTL) of this sequence.
	 *
	 * @return A {@link PngAnimationControl} object.
	 */
	public PngAnimationControl getAnimationControl()
	{
		return m_animationControl;
	}

	/**
	 * Adds a text chunk (tEXt, zTXt or iTXt) to this sequence.
	 *
	 * @param text A {@link PngText} object.
	 */
	public void addTextChunk(PngText text)
	{
		m_textChunks.add(text);
	}

	/**
	 * Gets the text chunk count of this sequence.
	 *
	 * @return An int value with the text chunk count.
	 */
	public int getTextChunksCount()
	{
		return m_textChunks.size();
	}

	/**
	 * Gets a text chunk (tEXt) by index.
	 *
	 * @param nIdx An int containing the index.
	 * @return A {@link PngText} object.
	 */
	public PngText getTextChunk(int nIdx)
	{
		return m_textChunks.get(nIdx);
	}

	/**
	 * Convenience method to gather all bitmaps consecutive in one array.
	 *
	 * @return An array of {@link Bitmap32} objects.
	 */
	public Bitmap32[] getBitmaps()
	{
		int nFrames = getFramesCount(),
			nOffs = 0;

		if (m_animationType == PngAnimationType.NONE || m_animationType == PngAnimationType.SKIPFIRST)
		{
			nFrames++;
			nOffs++;
		}

		Bitmap32[] aBitmaps = new Bitmap32[nFrames];
		if (nOffs > 0)
		{
			aBitmaps[0] = m_bitmapDefault;
		}

		for (Bitmap32 bitmap : m_frames)
		{
			aBitmaps[nOffs++] = bitmap;
		}

		return aBitmaps;
	}

	/**
	 * Gets the count of animated frames (bitmaps). Not including the default bitmap
	 * in case of PngAnimationType.NONE or PngAnimationType.SKIPFIRST.
	 *
	 * @return An int containing the number of animated frames.
	 */
	public int getFramesCount()
	{
		return m_frames.size();
	}

	/**
	 * Gets a frame (bitmap) from the list of frames by index.
	 *
	 * @param nIdx An int containing the index.
	 * @return A {@link Bitmap32} object.
	 */
	public Bitmap32 getFrame(int nIdx)
	{
		return m_frames.get(nIdx);
	}

	/**
	 * Adds a frame (bitmap) to the list of frames.
	 *
	 * @param bitmap A {@link Bitmap32} object.
	 */
	public void addFrame(Bitmap32 bitmap)
	{
		m_frames.add(bitmap);
		m_animationControl.setNumFrames(getFramesCount());
	}

	/**
	 * Removes a frame (bitmap) from the list of frames.
	 *
	 * @param nIdx An int containing the index of the frame.
	 * @return A {@link Bitmap32} object.
	 */
	public Bitmap32 removeFrame(int nIdx)
	{
		return m_frames.remove(nIdx);
	}

	/**
	 * Replaces a frame (bitmap) in the list of frames.
	 *
	 * @param nIdx An int containing the index of the frame to replace
	 * @param bitmapNew A {@link Bitmap32} object.
	 * @return The old frame (bitmap) of this index.
	 */
	public Bitmap32 replaceFrame(int nIdx, Bitmap32 bitmapNew)
	{
		Bitmap32 bitmapOld = null;

		if (nIdx < m_frames.size()) // TODO: Hm, shouldn't be a question.
		{
			bitmapOld = m_frames.get(nIdx);
		}

		m_frames.set(nIdx, bitmapNew);

		return bitmapOld;
	}
}
