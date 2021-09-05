package de.unlixx.runpng.png.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;

import de.unlixx.runpng.bitmap.Bitmap32;
import de.unlixx.runpng.bitmap.Bitmap32Manager;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngCRC32;
import de.unlixx.runpng.png.PngConstants;
import de.unlixx.runpng.png.chunks.PngAnimationControl;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.png.chunks.PngHeader;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.png.chunks.PngText;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.util.Progress;
import de.unlixx.runpng.util.Util;

/**
 * PngChunkOutputStream based on DataOutputStream. This class writes
 * a chunked png file (animated or not) to an output stream.
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
public class PngChunkOutputStream extends DataOutputStream
{
	Bitmap32Manager m_manager;

	boolean m_bIdatWritten;
	int m_nFrameSequence;

	PngCRC32 m_crc = new PngCRC32();

	/**
	 * Constructor for this PngChunkOutputStream.
	 *
	 * @param os The {@link OutputStream} to write to.
	 */
	public PngChunkOutputStream(OutputStream os)
	{
		super(os);
	}

	/**
	 * Writes a byte to the output stream with CRC update.
	 *
	 * @param b The byte to write in the lower 8 bits.
	 * @throws IOException In case of IO problems.
	 */
	void writeByte_crc(int b) throws IOException
	{
		write(b);
		m_crc.update(b);
	}

	/**
	 * Writes a short to the output stream with CRC update.
	 *
	 * @param n The short to write is in the lower 16 bits.
	 * @throws IOException In case of IO problems.
	 */
	void writeShort_crc(int n) throws IOException
	{
		writeShort(n);
		m_crc.updateShort(n);
	}

	/**
	 * Writes an int to the output stream with CRC update.
	 *
	 * @param n The int to write.
	 * @throws IOException In case of IO problems.
	 */
	void writeInt_crc(int n) throws IOException
	{
		writeInt(n);
		m_crc.updateInt(n);
	}

	/**
	 * Writes an array of bytes with CRC update.
	 *
	 * @param ab The byte array to write.
	 * @throws IOException In case of IO problems.
	 */
	void write_crc(byte[] ab) throws IOException
	{
		write_crc(ab, 0, ab.length);
	}

	/**
	 * Writes an array of bytes with CRC update.
	 *
	 * @param ab The byte array to write.
	 * @param nOffs The offset in the receiving array.
	 * @param nLen The length to write.
	 * @throws IOException In case of IO problems.
	 */
	void write_crc(byte[] ab, int nOffs, int nLen) throws IOException
	{
		write(ab, nOffs, nLen);
		m_crc.update(ab, nOffs, nLen);
	}

	/**
	 * Calculates a prediction of steps to save a Bitmap32Sequence.
	 *
	 * @param sequence A {@link Bitmap32Sequence} object.
	 * @return An int containing the calculated prediction.
	 */
	public static int calcStepsForSave(Bitmap32Sequence sequence)
	{
		// TODO: Poor implementation of progress prediction.
		// But how to predict the sizes without blowing up the memory consumption?

		int nSteps = 2;			// Signature and header

		PngAnimationType animType = sequence.getAnimationType();
		switch (animType)
		{
		case NONE:				// IDAT only
			nSteps++;
			break;

		case SKIPFIRST:	// acTL + IDAT + n * (fcTL + fdAT)
			nSteps += 1 + 1 + sequence.getFramesCount() * 2;
			break;

		default:
		case ANIMATED:		// acTL + n * (fcTL + (IDAT | fdAT))
			nSteps += 1 + sequence.getFramesCount() * 2;
			break;
		}

		nSteps += sequence.getTextChunksCount();

		if (sequence.getPalette() != null)
		{
			nSteps++;
		}

		if (sequence.getTransparency() != null)
		{
			nSteps++;
		}

		// TODO: Further chunks types

		nSteps++;				// IEND

		return nSteps;
	}

	/**
	 * Writes a Bitmap32Sequence to the output stream.
	 *
	 * @param sequence A {@link Bitmap32Sequence} object.
     * @param progress A {@link Progress} object to update the visual progress indicator.
	 * @throws IOException In case of IO problems.
	 * @throws DataFormatException In case of data format problems.
	 */
	public void write(Bitmap32Sequence sequence, Progress<?> progress) throws IOException, DataFormatException
	{
		m_manager = new Bitmap32Manager(sequence);
		m_bIdatWritten = false;

		progress.updateProgress(0);

		write(PngConstants.PNG_SIGNATURE);
		progress.addProgress(1);

		write_IHDR(sequence.getHeader());
		progress.addProgress(1);

		for (int n = 0, nTexts = sequence.getTextChunksCount(); n < nTexts; n++)
		{
			write_text(sequence.getTextChunk(n));
			progress.addProgress(1);
		}

		PngPalette palette = sequence.getPalette();
		if (palette != null)
		{
			write_PLTE(palette);
			progress.addProgress(1);
		}

		PngTransparency transparency = sequence.getTransparency();
		if (transparency != null)
		{
			write_tRNS(transparency);
			progress.addProgress(1);
		}

		if (sequence.isAnimated())
		{
			write_acTL(sequence.getAnimationControl());
			progress.addProgress(1);
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream(PngConstants.BUFFER_32K);
		PngAnimationType animType = sequence.getAnimationType();

		switch (animType)
		{
		case NONE:
		case SKIPFIRST:
			m_manager.deflateBitmap(bos, sequence.getDefaultBitmap());
			writeDataChunks(bos.toByteArray());
			progress.addProgress(1);
			//...
		default: break;
		}

		if (animType != PngAnimationType.NONE) // Just to be sure
		{
			m_nFrameSequence = 0;

			for (int nFrame = 0, nFrames = sequence.getFramesCount(); nFrame < nFrames; nFrame++)
			{
				Bitmap32 bitmap = sequence.getFrame(nFrame);
				PngFrameControl fcTL = bitmap.getFrameControl();

				write_fcTL(fcTL);
				progress.addProgress(1);

				bos.reset();
				m_manager.deflateBitmap(bos, bitmap);
				writeDataChunks(bos.toByteArray());
				progress.addProgress(1);
			}
		}

		write_IEND();

		flush();

		progress.addProgress(1);
	}

	/**
	 * Writes the already deflated bitmap data in blocks of 32 kBytes
	 * as IDAT or fdAT chunks.
	 *
	 * @param abData A byte array of the deflated bitmap.
	 * @throws IOException In case of IO problems.
	 */
	void writeDataChunks(byte[] abData) throws IOException
	{
		for (int n = 0, nLen = abData.length; n < nLen; n += PngConstants.BUFFER_32K)
		{
			int nSize = Math.min(nLen - n, PngConstants.BUFFER_32K);

			if (!m_bIdatWritten)
			{
				write_IDAT(abData, n, nSize);
			}
			else
			{
				write_fdAT(abData, n, nSize);
			}
		}

		m_bIdatWritten = true;
	}

	/**
	 * Writes the initial header chunk (IHDR).
	 *
	 * @param header The {@link PngHeader} object.
	 * @throws IOException In case of IO problems.
	 */
	void write_IHDR(PngHeader header) throws IOException
	{
		//System.out.println("write IHDR len=" + PngConstants.LENGTH_IHDR);

		m_crc.reset();

		writeInt(PngConstants.LENGTH_IHDR);
		writeInt_crc(PngConstants.IHDR);
		writeInt_crc(header.getWidth());
		writeInt_crc(header.getHeight());
		writeByte_crc(header.getBitDepth());
		writeByte_crc(header.getColorType().getType());
		writeByte_crc(header.getCompressionMethod());
		writeByte_crc(header.getFilterMethod());
		writeByte_crc(header.getInterlaceMethod());
		writeInt((int)m_crc.getValue());
	}

	/**
	 * Writes the palette chunk (PLTE).
	 *
	 * @param palette The {@link PngPalette} object.
	 * @throws IOException In case of IO problems.
	 */
	void write_PLTE(PngPalette palette) throws IOException
	{
		byte[] abRGB = palette.getBytes();
		int nLen = abRGB.length;

		//System.out.println("write PLTE len=" + nLen);

		m_crc.reset();

		writeInt(nLen);
		writeInt_crc(PngConstants.PLTE);
		write_crc(abRGB, 0, nLen);
		writeInt((int)m_crc.getValue());
	}

	/**
	 * Writes the transparency chunk (tRNS).
	 *
	 * @param transparency The {@link PngTransparency} object.
	 * @throws IOException In case of IO problems.
	 */
	void write_tRNS(PngTransparency transparency) throws IOException
	{
		byte[] abtRNS = transparency.getBytes();
		int nLen = abtRNS.length;

		//System.out.println("write tRNS len=" + nLen);

		m_crc.reset();

		writeInt(nLen);
		writeInt_crc(PngConstants.tRNS);
		write_crc(abtRNS, 0, nLen);
		writeInt((int)m_crc.getValue());
	}

	/**
	 * Writes an image data chunk (IDAT).
	 *
	 * @param ab An array of bytes.
	 * @param nOffs The offset to start at.
	 * @param nLen The length to write.
	 * @throws IOException In case of IO problems.
	 */
	void write_IDAT(byte[] ab, int nOffs, int nLen) throws IOException
	{
		//System.out.println("write IDAT len=" + nLen);

		m_crc.reset();

		writeInt(nLen);
		writeInt_crc(PngConstants.IDAT);
		write_crc(ab, nOffs, nLen);
		writeInt((int)m_crc.getValue());
	}

	/**
	 * Writes a frame data chunk (fdAT).
	 *
	 * @param ab An array of bytes.
	 * @param nOffs The offset to start at.
	 * @param nLen The length to write.
	 * @throws IOException In case of IO problems.
	 */
	void write_fdAT(byte[] ab, int nOffs, int nLen) throws IOException
	{
		//System.out.println("write fDAT len=" + nLen);

		m_crc.reset();

		writeInt(nLen + 4); // + 4 for the additional sequence number
		writeInt_crc(PngConstants.fdAT);
		writeInt_crc(m_nFrameSequence);
		write_crc(ab, nOffs, nLen);
		writeInt((int)m_crc.getValue());

		m_nFrameSequence++;
	}

	/**
	 * Writes the animation control chunk (acTL).
	 *
	 * @param acTL The {@link PngAnimationControl} object.
	 * @throws IOException In case of IO problems.
	 */
	void write_acTL(PngAnimationControl acTL) throws IOException
	{
		//System.out.println("write acTL len=" + PngConstants.LENGTH_acTL);

		m_crc.reset();

		writeInt(PngConstants.LENGTH_acTL);
		writeInt_crc(PngConstants.acTL);
		writeInt_crc(acTL.getNumFrames());
		writeInt_crc(acTL.getNumPlays());
		writeInt((int)m_crc.getValue());
	}

	/**
	 * Writes a frame control chunk (fcTL).
	 *
	 * @param fcTL A {@link PngFrameControl} object.
	 * @throws IOException In case of IO problems.
	 */
	void write_fcTL(PngFrameControl fcTL) throws IOException
	{
		//System.out.println("write fcTL len=" + PngConstants.LENGTH_fcTL);

		m_crc.reset();

		writeInt(PngConstants.LENGTH_fcTL);
		writeInt_crc(PngConstants.fcTL);
		writeInt_crc(m_nFrameSequence);
		writeInt_crc(fcTL.getWidth());
		writeInt_crc(fcTL.getHeight());
		writeInt_crc(fcTL.getXOffset());
		writeInt_crc(fcTL.getYOffset());
		writeShort_crc(fcTL.getDelayNum());
		writeShort_crc(fcTL.getDelayDen());
		writeByte_crc(fcTL.getDisposeOp());
		writeByte_crc(fcTL.getBlendOp());
		writeInt((int)m_crc.getValue());

		m_nFrameSequence++;
	}

	/**
	 * Writes a text chunk. Either a tEXt, zTXt or iTXt.
	 *
	 * @param text A {@link PngText} object.
	 * @throws IOException In case of IO problems.
	 * @throws DataFormatException In case of data format problems.
	 */
	void write_text(PngText text) throws IOException, DataFormatException
	{
		String str = text.getKeyword();
		if (str == null || str == "")
		{
			str = "null";
		}

		if (str.length() > 79)
		{
			str = str.substring(0, 79);
		}

		byte[] abKeyword = str.getBytes(StandardCharsets.ISO_8859_1),
				abText = null, abDeflated = null;

		int nLen = abKeyword.length + 1, // Keyword is common to all.
			nChunkType = text.getChunkType();

		m_crc.reset();

		switch (nChunkType)
		{
		case PngConstants.tEXt:
			str = text.getText();
			if (str != null && str.length() > 0)
			{
				if (str.length() > PngConstants.BUFFER_32K - 80)
				{
					str = str.substring(0, PngConstants.BUFFER_32K - 80);
				}

				abText = str.getBytes(StandardCharsets.ISO_8859_1);
				nLen += abText.length;
			}

			//System.out.println("write tEXt len=" + nLen);

			writeInt(nLen);
			writeInt_crc(nChunkType);
			write_crc(abKeyword);
			writeByte_crc(0); // Null separator
			if (abText != null)
			{
				write_crc(abText);
			}

			writeInt((int)m_crc.getValue());
			break;

		case PngConstants.zTXt:

			// Compression method
			nLen++;

			// Text deflation if any
			str = text.getText();
			if (str != null && str.length() > 0)
			{
				abText = str.getBytes(StandardCharsets.ISO_8859_1);
				abDeflated = Util.deflate(abText, 0, abText.length);
				nLen += abDeflated.length;
			}

			//System.out.println("write zTXt len=" + nLen);

			writeInt(nLen);
			writeInt_crc(nChunkType);
			write_crc(abKeyword);
			writeByte_crc(0); // Null separator
			writeByte_crc(text.getCompressionMethod());
			if (abDeflated != null)
			{
				write_crc(abDeflated);
			}

			writeInt((int)m_crc.getValue());
			break;

		case PngConstants.iTXt:

			// Compression flag
			nLen++;

			// Compression method
			nLen++;

			byte[] abLanguageTag = null,
					abTranslatedKeyword = null;

			// Language tag
			str = text.getLanguageTag();
			if (str != null && str.length() > 0)
			{
				abLanguageTag = str.getBytes(StandardCharsets.ISO_8859_1);
				nLen += abLanguageTag.length;
			}

			nLen++; // Null separator

			// Translated keyword
			str = text.getTranslatedKeyword();
			if (str != null && str.length() > 0)
			{
				abTranslatedKeyword = str.getBytes(StandardCharsets.UTF_8);
				nLen += abTranslatedKeyword.length;
			}

			nLen++; // Null separator

			str = text.getText();
			if (str != null && str.length() > 0)
			{
				abText = str.getBytes(StandardCharsets.UTF_8);

				if (text.getCompressionFlag() > 0)
				{
					abDeflated = Util.deflate(abText, 0, abText.length);
					nLen += abDeflated.length;
				}
				else
				{
					nLen += abText.length;
				}
			}

			//System.out.println("write iTXt len=" + nLen);

			writeInt(nLen);
			writeInt_crc(nChunkType);
			write_crc(abKeyword);
			writeByte_crc(0); // Zero separator
			writeByte_crc(text.getCompressionFlag());
			writeByte_crc(text.getCompressionMethod());
			if (abLanguageTag != null)
			{
				write_crc(abLanguageTag);
			}
			writeByte_crc(0); // Zero separator
			if (abTranslatedKeyword != null)
			{
				write_crc(abTranslatedKeyword);
			}
			writeByte_crc(0); // Zero separator
			if (abDeflated != null)
			{
				write_crc(abDeflated);
			}
			else if (abText != null)
			{
				write_crc(abText);
			}

			writeInt((int)m_crc.getValue());
			break;
		}
	}

	/**
	 * Writes the end chunk (IEND).
	 *
	 * @throws IOException In case of IO problems.
	 */
	void write_IEND() throws IOException
	{
		//System.out.println("write IEND len=" + PngConstants.LENGTH_IEND);

		m_crc.reset();

		writeInt(PngConstants.LENGTH_IEND);
		writeInt_crc(PngConstants.IEND);
		writeInt((int)m_crc.getValue());
	}

	@Override
	public void close() throws IOException
	{
		flush();
		super.close();
	}
}
