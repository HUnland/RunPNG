package de.unlixx.runpng.bitmap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;

import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngConstants;
import de.unlixx.runpng.png.chunks.PngAnimationControl;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.png.chunks.PngHeader;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.png.chunks.PngText;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.png.io.PngIOCore;

/**
 * The Bitmap32Manager controls reading and writing
 * of Bitmap32Sequences.
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
public class Bitmap32Manager
{
	PngHeader m_header;
	PngIOCore m_pngIOCore;
	Bitmap32Sequence m_sequence;
	Scanline32 m_scanline;
	PngFrameControl m_fcTL;
	PngPalette m_palette;
	PngTransparency m_transparency;

	/**
	 * Constructor without parameter for reading a {@link Bitmap32Sequence}.
	 */
	public Bitmap32Manager()
	{
		// Sequence and scanline will be created in setHeader().
	}

	/**
	 * Constructor for writing a Bitmap32Sequence.
	 *
	 * @param sequence The {@link Bitmap32Sequence} to write.
	 */
	public Bitmap32Manager(Bitmap32Sequence sequence)
	{
		// This sequence needs to be written.
		m_sequence = sequence;

		m_header = m_sequence.getHeader();
		m_palette = m_sequence.getPalette();
		m_transparency = m_sequence.getTransparency();

		m_scanline = Scanline32.getScanlineFor(m_header.getColorType(), m_header.getBitDepth(), m_sequence.getDefaultBitmap());
		m_scanline.setPalette(m_palette);
		m_scanline.setTransparency(m_transparency);

		setHeader(sequence.getHeader());
	}

	/**
	 * Sets the PngHeader for this IO process and creates the filtering IO handler.
	 *
	 * @param header A {@link PngHeader} object.
	 */
	public void setHeader(PngHeader header)
	{
		m_header = header;
		m_pngIOCore = new PngIOCore(header);

		if (m_sequence == null)
		{
			m_sequence = new Bitmap32Sequence(header, true);
			m_scanline = Scanline32.getScanlineFor(header.getColorType(), header.getBitDepth(), m_sequence.getDefaultBitmap());
		}
	}

	/**
	 * Gets the header in use.
	 *
	 * @return A {@link PngHeader} object.
	 */
	public PngHeader getHeader()
	{
		return m_header;
	}

	/**
	 * Sets the color palette for PLTE chunk in case of indexed color type.
	 *
	 * @param palette A {@link PngPalette} object.
	 */
	public void setPalette(PngPalette palette)
	{
		m_palette = palette;
		m_scanline.setPalette(m_palette);
	}

	/**
	 * Gets the color palette if one is set.
	 *
	 * @return A {@link PngPalette} object.
	 * Or null if none is set.
	 */
	public PngPalette getPalette()
	{
		return m_palette;
	}

	/**
	 * Sets the transparency object for tRNS chunk.
	 *
	 * @param transparency A {@link PngTransparency} object.
	 */
	public void setTransparency(PngTransparency transparency)
	{
		m_transparency = transparency;
		m_scanline.setTransparency(transparency);
	}

	/**
	 * Gets the transparency object.
	 *
	 * @return A {@link PngTransparency} object.
	 * Or null if none is set.
	 */
	public PngTransparency getTransparency()
	{
		return m_transparency;
	}

	/**
	 * Deflates a single bitmap to the output stream.
	 *
	 * @param os The {@link OutputStream} to write to.
	 * @param bitmap A {@link Bitmap32} object to deflate.
	 * @throws IOException In case of IO problems while file writing.
	 */
	public void deflateBitmap(OutputStream os, Bitmap32 bitmap) throws IOException
	{
		m_scanline.setBitmap(bitmap);
		m_pngIOCore.deflate(os, m_scanline);
	}

	/**
	 * Sets the animation control chunk to the sequence currently in read.
	 *
	 * @param acTL A {@link PngAnimationControl} object.
	 */
	public void setAnimationControl(PngAnimationControl acTL)
	{
		m_sequence.getAnimationControl().setNumFrames(acTL.getNumFrames());
		m_sequence.getAnimationControl().setNumPlays(acTL.getNumPlays());
	}

	/**
	 * Sets a frame control read by the chunk reader and creates a new bitmap.
	 *
	 * @param fcTL A {@link PngFrameControl} object.
	 */
	public void setFrameControl(PngFrameControl fcTL)
	{
		m_fcTL = fcTL;
		m_scanline.setBitmap(new Bitmap32(fcTL, null));
	}

	/**
	 * Gets the still deflated bitmap data and passes it to inflate.
	 *
	 * @param nChunkCode Either IDAT or fdAT.
	 * @param abData The bitmap bytes still deflated.
	 * @throws DataFormatException In case of corrupted data.
	 */
	public void applyDeflatedBitmap(int nChunkCode, byte[] abData) throws DataFormatException
	{
		m_pngIOCore.inflate(abData, m_scanline);

		Bitmap32 bitmap = m_scanline.getBitmap();

		switch (nChunkCode)
		{
		case PngConstants.IDAT:
			m_sequence.setDefaultBitmap(bitmap);

			if (m_sequence.getAnimationType() == PngAnimationType.ANIMATED)
			{
				bitmap.setFrameControl(m_fcTL);
				//setDefaultBitmap() adds it now automatically to the frames if ANIMATED.
				//m_sequence.addFrame(bitmap);
			}
			break;

		case PngConstants.fdAT:
			bitmap.setFrameControl(m_fcTL);
			m_sequence.addFrame(bitmap);
			break;
		}
	}

	/**
	 * Sets the animation type to the actual sequence.
	 *
	 * @param animType A {@link PngAnimationType}
	 */
	public void setAnimationType(PngAnimationType animType)
	{
		m_sequence.setAnimationType(animType);
	}

	/**
	 * Adds a text chunk to the actual sequence.
	 *
	 * @param tEXt A {@link PngText} object.
	 */
	public void addTextChunk(PngText tEXt)
	{
		m_sequence.addTextChunk(tEXt);
	}

	/**
	 * Gets the Bitmap32Sequence actually in use.
	 *
	 * @return A {@link Bitmap32Sequence} object.
	 * Or null if there is actually none.
	 */
	public Bitmap32Sequence getSequence()
	{
		return m_sequence;
	}
}
