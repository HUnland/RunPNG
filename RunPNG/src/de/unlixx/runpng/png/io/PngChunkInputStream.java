package de.unlixx.runpng.png.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;

import de.unlixx.runpng.bitmap.Bitmap32Manager;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngCRC32;
import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.PngConstants;
import de.unlixx.runpng.png.chunks.PngAnimationControl;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.png.chunks.PngHeader;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.png.chunks.PngText;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.Progress;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * PngChunkInputStream based on DataInputStream. This class reads
 * a chunked png file (animated or not) from an input stream.
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
public class PngChunkInputStream extends DataInputStream
{
	Bitmap32Manager m_manager;

	int m_nIdatCount = 0;
	int m_nFrameSequenceExpected = 0;
	PngAnimationType m_animationType = PngAnimationType.NONE;

	PngCRC32 m_crc = new PngCRC32();

	boolean m_bGotHeader;

	byte[] m_abBuffer = new byte[PngConstants.BUFFER_32K];

	// Collects the deflated IDAT or fdAT chunks
	final ByteArrayOutputStream m_bosDataLingering = new ByteArrayOutputStream();
	int m_nChunkTypeLingering; // IDAT or fdAT

	/**
	 * Constructor for this PngChunkInputStream.
	 *
	 * @param is An {@link InputStream} to read from.
	 */
	public PngChunkInputStream(InputStream is)
	{
		super(is);
	}

	/**
	 * Reads a byte from input stream with CRC update.
	 *
	 * @return A byte from input stream.
	 * @throws IOException In case of IO problems.
	 */
	byte readByte_crc() throws IOException
	{
		byte b = readByte();
		m_crc.update(b);
		return b;
	}

	/**
	 * Reads an unsigned short from input stream with CRC update.
	 *
	 * @return An unsigned short from input stream in the lower 16 bits.
	 * @throws IOException In case of IO problems.
	 */
	int readUnsignedShort_crc() throws IOException
	{
		int n = readUnsignedShort();
		m_crc.updateShort(n);
		return n;
	}

	/**
	 * Reads an int from input stream with CRC update.
	 *
	 * @return An int from input stream.
	 * @throws IOException In case of IO problems.
	 */
	int readInt_crc() throws IOException
	{
		int n = readInt();
		m_crc.updateInt(n);
		return n;
	}

	/**
	 * Reads an array of bytes with CRC update.
	 *
	 * @param ab The receiving byte array.
	 * @return An int containing the number of bytes read.
	 * @throws IOException In case of IO problems.
	 */
	int read_crc(byte ab[]) throws IOException
	{
		int nRead = read(ab);
		m_crc.update(ab);
		return nRead;
    }

    /**
	 * Reads an array of bytes with CRC update.
     *
	 * @param ab The receiving byte array.
     * @param nOffs The offset in the receiving array.
     * @param nLen The length to read.
	 * @return An int containing the number of bytes read.
	 * @throws IOException In case of IO problems.
     */
    public int read_crc(byte[] ab, int nOffs, int nLen) throws IOException
    {
        int nRead = read(ab, nOffs, nLen);
        m_crc.update(ab, nOffs, nRead);
        return nRead;
    }

	/**
	 * Reads an array of bytes with CRC update.
	 *
	 * @param ab The receiving byte array.
	 * @throws IOException In case of IO problems.
	 */
    public void readFully_crc(byte[] ab) throws IOException
    {
        readFully_crc(ab, 0, ab.length);
    }

    /**
	 * Reads an array of bytes with CRC update.
     *
	 * @param ab The receiving byte array.
     * @param nOffs The offset in the receiving array.
     * @param nLen The length to read.
	 * @throws IOException In case of IO problems.
     */
    public void readFully_crc(byte[] ab, int nOffs, int nLen) throws IOException
    {
        readFully(ab, nOffs, nLen);
        m_crc.update(ab, nOffs, nLen);
    }

    /**
     * Reads a Bitmap32Sequence from the input stream.
     *
     * @param progress A {@link Progress} object to update the visual progress indicator.
     * @return A decompressed but still optimized {@link Bitmap32Sequence} object.
	 * @throws IOException In case of an IO problem.
	 * @throws DataFormatException In case of problems with the data format.
     */
	public Bitmap32Sequence read(Progress<?> progress) throws IOException, DataFormatException
	{
		m_manager = new Bitmap32Manager();

		readSignature();
		progress.addProgress(PngConstants.PNG_SIGNATURE.length);

		boolean bFinished = false;
		while (!bFinished)
		{
			final int nLen = readInt(),
				nChunkType = readInt();

			bFinished = chunkWedge(nChunkType, nLen);

			progress.addProgress(4 + nLen + 4); // Chunk type length + chunk length + checksum length
		}

		return m_manager.getSequence();
	}

	/**
	 * Applies possibly lingering bitmap data to the manager.
	 *
	 * @throws DataFormatException  In case of problems with the data format.
	 */
	void applyLingeringData() throws DataFormatException
	{
		if (m_bosDataLingering.size() > 0)
		{
			m_manager.applyDeflatedBitmap(m_nChunkTypeLingering, m_bosDataLingering.toByteArray());

			m_bosDataLingering.reset();
			m_nChunkTypeLingering = 0;
		}
	}

	/**
	 * Chunk wedge to distribute the input stream to the methods according the chunk type.
	 *
	 * @param nChunkType The chunk type from input.
	 * @param nLen The chunk length from input.
	 * @return True if it reached the IEND chunk.
	 * @throws IOException In case of an IO problem.
	 * @throws DataFormatException In case of problems with the data format.
	 */
	boolean chunkWedge(int nChunkType, int nLen) throws IOException, DataFormatException
	{
		//System.out.println("Chunk wedge " + PngConstants.chunkTypeName(nChunkType) + " length = " + nLen);

		if (nLen < 0)
		{
			throw new EOFException(Loc.getString("exception.unexpected.end", "chunk wedge"));
		}

		if (nLen > 0xfffff)
		{
			throw new Failure("failure.wrong.chunklength");
		}

		if (!m_bGotHeader && nChunkType != PngConstants.IHDR)
		{
			throw new Failure("failure.chunkbeforeheader");
		}

		int nCRCCalc;

		switch (nChunkType)
		{
		case PngConstants.IHDR:
			nCRCCalc = read_IHDR(nLen);
			break;

		case PngConstants.acTL:
			nCRCCalc = read_acTL(nLen);
			break;

		case PngConstants.fcTL:
			applyLingeringData();
			nCRCCalc = read_fcTL(nLen);
			break;

		case PngConstants.IDAT:
			nCRCCalc = read_IDAT(nLen);
			break;

		case PngConstants.fdAT:
			nCRCCalc = read_fdAT(nLen);
			break;

		case PngConstants.bKGD:
			nCRCCalc = read_bKGD(nLen);
			break;

		case PngConstants.gAMA:
			nCRCCalc = read_gAMA(nLen);
			break;

		case PngConstants.tRNS:
			nCRCCalc = read_tRNS(nLen);
			break;

		case PngConstants.PLTE:
			nCRCCalc = read_PLTE(nLen);
			break;

		case PngConstants.tEXt:
		case PngConstants.zTXt:
		case PngConstants.iTXt:
			nCRCCalc = read_text(nChunkType, nLen);
			break;

		case PngConstants.IEND:
			applyLingeringData();
			nCRCCalc = PngConstants.CHECKSUM_IEND; // Does never change

			// Nothing further to read
			// TODO: But what the hell is after IEND? Approx. 40 bytes left over sometimes.
			break;

		default:
			nCRCCalc = skipIgnoredChunk(nChunkType, nLen);
			break;
		}

		int nCRCRead = readInt();
		handleChunkCRC(nChunkType, nCRCCalc, nCRCRead);
		return nChunkType == PngConstants.IEND;
	}

	/**
	 * Checks the calculated checksum against the checksum read from file.
	 *
	 * @param nChunkType The chunk type read.
	 * @param nCRC The calculated checksum.
	 * @param nCRCRead The checksum read from file.
	 */
	void handleChunkCRC(int nChunkType, int nCRC, int nCRCRead)
	{
		if (nCRC != nCRCRead)
		{
			throw new Failure("failure.checksum.error.chunk", PngConstants.chunkTypeName(nChunkType), nCRCRead, nCRC);
		}
	}

	/**
	 * Reads the unique png signature from file.
	 *
	 * @throws IOException In case of IO problems.
	 */
	void readSignature() throws IOException
	{
		//System.out.println("Read signature length = 8");

		for (int n = 0; n < PngConstants.PNG_SIGNATURE.length; n++)
		{
			int nByte = read();
			if (nByte != (PngConstants.PNG_SIGNATURE[n] & 0xff))
			{
				throw new Failure("failure.wrong.signature");
			}
		}
	}

	/**
	 * Reads the initial header chunk (IHDR).
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_IHDR(int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(PngConstants.IHDR);

		PngHeader header = new PngHeader(
				readInt_crc(),		// Width
				readInt_crc(),		// Height
				readByte_crc(),		// Bit depth
				PngColorType.byType(readByte_crc()), // Color type
				readByte_crc(),		// Compression method
				readByte_crc(),		// Filter method
				readByte_crc());	// Interlace method

		m_bGotHeader = true;

		m_manager.setHeader(header);

		return (int)m_crc.getValue();
	}

	/**
	 * Reads the transparency chunk (tRNS).
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_tRNS(int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(PngConstants.tRNS);

		byte[] abTrans = new byte[nLen];
		readFully_crc(abTrans);

		PngHeader header = m_manager.getHeader();

		m_manager.setTransparency(new PngTransparency(header.getColorType(), header.getBitDepth(), abTrans));

		return (int)m_crc.getValue();
	}

	/**
	 * Reads the background chunk (bKGD).
	 * (not yet implemented)
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_bKGD(int nLen) throws IOException
	{
		// TODO: Implement
		return skipIgnoredChunk(PngConstants.bKGD, nLen);
	}

	/**
	 * Reads the gamma correction chunk (gAMA).
	 * (not yet implemented)
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_gAMA(int nLen) throws IOException
	{
		// TODO: Implement
		return skipIgnoredChunk(PngConstants.gAMA, nLen);
	}

	/**
	 * Reads the palette chunk (PLTE).
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_PLTE(int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(PngConstants.PLTE);

		byte[] abPLTE = new byte[nLen];
		readFully_crc(abPLTE);
		m_manager.setPalette(PngPalette.createPalette(abPLTE));

		return (int)m_crc.getValue();
	}

	/**
	 * Collects image data for the IDAT and fdAT chunk and puts it
	 * to the lingering data.
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int collectImageData(int nLen) throws IOException
	{
		while (nLen > 0)
		{
			int nRead = read_crc(m_abBuffer, 0, Math.min(nLen, m_abBuffer.length));
			if (nRead <= 0)
			{
				throw new EOFException(Loc.getString("exception.unexpected.end", "collectImageData"));
			}

			m_bosDataLingering.write(m_abBuffer, 0, nRead);
			nLen -= nRead;
		}

		return (int)m_crc.getValue();
	}

	/**
	 * Reads the image data chunk (IDAT).
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_IDAT(int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(PngConstants.IDAT);

       if (m_nIdatCount == 0 && m_nFrameSequenceExpected == 0)
        {
            m_animationType = PngAnimationType.NONE;
            m_manager.setAnimationType(m_animationType);
        }

        m_nIdatCount++;

		m_nChunkTypeLingering = PngConstants.IDAT;

		return collectImageData(nLen);
	}

	/**
	 * Reads the animation control chunk (acTL).
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_acTL(int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(PngConstants.acTL);

		PngAnimationControl animControl = new PngAnimationControl(
				readInt_crc(),	// Number of frames
				readInt_crc()	// Number of loops
			);

		m_manager.setAnimationControl(animControl);

		return (int)m_crc.getValue();
	}

	/**
	 * Reads the frame control chunk (fcTL).
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_fcTL(int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(PngConstants.fcTL);

		int nSequence = readInt_crc();

		//System.out.println("fcTL sequence: " + nSequence);

		if (nSequence != m_nFrameSequenceExpected)
		{
			throw new Failure("failure.wrong.framesequence");
		}

		m_nFrameSequenceExpected++;

		PngFrameControl fcTL = new PngFrameControl(
				readInt_crc(),	// Width
				readInt_crc(),	// Height
				readInt_crc(),	// Offset x
				readInt_crc(),	// Offset y
				readUnsignedShort_crc(),	// Delay numerator
				readUnsignedShort_crc(),	// Delay denominator
				readByte_crc(),	// Dispose operator
				readByte_crc()	// Blend operator
			);

		if (nSequence == 0)
		{
			if (m_nIdatCount == 0)
			{
				m_animationType = PngAnimationType.ANIMATED;
			}
			else
			{
				m_animationType = PngAnimationType.SKIPFIRST;
			}

			m_manager.setAnimationType(m_animationType);
		}

		m_manager.setFrameControl(fcTL);

		return (int)m_crc.getValue();
	}

	/**
	 * Reads the frame data chunk (fdAT).
	 *
	 * @param nLen The length of the chunk.
	 * @return The CRC32 checksum.
	 * @throws IOException In case of IO problems.
	 */
	int read_fdAT(int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(PngConstants.fdAT);

		int nSequence = readInt_crc();
		nLen -= 4; // Length of sequence number

		if (nSequence != m_nFrameSequenceExpected)
		{
			throw new Failure("failure.wrong.framesequence");
		}

		m_nFrameSequenceExpected++;

		m_nChunkTypeLingering = PngConstants.fdAT;

		return collectImageData(nLen);
	}

	/**
	 * Reads a string from a given byte array until it finds a zero byte.
	 *
	 * @param ab A byte array to read.
	 * @param nOffs An int containing the offset.
	 * @param sb A StringBuffer object to receive the string data.
	 * @param charset The {@link Charset} to use.
	 * @return An int containing the index after the zero byte.
	 */
	int getZeroTerminatedString(byte[] ab, int nOffs, StringBuffer sb, Charset charset)
	{
		int n = nOffs, nEnd = ab.length;
		for (; n < nEnd; n++)
		{
			if (ab[n] == 0)
			{
				break;
			}
		}

		sb.setLength(0);
		if (n > nOffs)
		{
			String str = new String(ab, nOffs, n, charset);
			sb.append(str);
		}

		return ++n;
	}

	/**
	 * Reads a text chunk.
	 *
	 * @param nChunkType An int containing one of PngConstants.tEXt, PngConstants.zTXt or PngConstants.iTXt.
	 * @param nLen An int containing the whole chunk length.
	 * @return An int containing the crc checksum.
	 * @throws IOException In case of IO problems.
	 * @throws DataFormatException In case of malformed compression.
	 *
	 * @see <a href="https://www.w3.org/TR/PNG/#11tEXt">https://www.w3.org/TR/PNG/#11tEXt</a>
	 * @see <a href="https://www.w3.org/TR/PNG/#11zTXt">https://www.w3.org/TR/PNG/#11zTXt</a>
	 * @see <a href="https://www.w3.org/TR/PNG/#11iTXt">https://www.w3.org/TR/PNG/#11iTXt</a>
	 * @see <a href="http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html">http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html</a>
	 * @see <a href="https://www.ietf.org/rfc/rfc3066.txt">https://www.ietf.org/rfc/rfc3066.txt</a>
	 */
	int read_text(int nChunkType, int nLen) throws IOException, DataFormatException
	{
		m_crc.reset();
		m_crc.updateInt(nChunkType);

		String strKeyword,
			strLanguageTag = null,
			strTranslatedKeyword = null,
			strText = null;

		PngText textChunk = null;

		int nCompressionFlag = 0,
			nCompressionMethod = 0;

		byte ab[] = new byte[nLen];
		readFully_crc(ab);

		StringBuffer sb = new StringBuffer();

		int n = getZeroTerminatedString(ab, 0, sb, StandardCharsets.ISO_8859_1);
		strKeyword = sb.toString(); // Better you have a length of 1 - 79!

		switch (nChunkType)
		{
		case PngConstants.tEXt:
			if (nLen - n > 0)
			{
				strText = new String(ab, n, nLen - n, StandardCharsets.ISO_8859_1);
			}
			textChunk = new PngText(strKeyword, strText);
			break;

		case PngConstants.zTXt:
			nCompressionMethod = ab[n++];

			if (nLen - n > 0)
			{
				ab = Util.inflate(ab, n, nLen - n);
				if (ab.length > 0)
				{
					strText = new String(ab, StandardCharsets.ISO_8859_1);
				}
			}
			textChunk = new PngText(strKeyword, nCompressionMethod, strText);
			break;

		case PngConstants.iTXt:
			nCompressionFlag = ab[n++];
			nCompressionMethod = ab[n++];

			if (nLen - n > 0)
			{
				n = getZeroTerminatedString(ab, n, sb, StandardCharsets.ISO_8859_1);
				if (sb.length() > 0)
				{
					strLanguageTag = sb.toString();
				}

				if (nLen - n > 0)
				{
					n = getZeroTerminatedString(ab, n, sb, StandardCharsets.UTF_8);
					if (sb.length() > 0)
					{
						strTranslatedKeyword = sb.toString();
					}
				}

				if (nLen - n > 0)
				{
					if (nCompressionFlag > 0)
					{
						ab = Util.inflate(ab, n, nLen - n);
						if (ab.length > 0)
						{
							strText = new String(ab, StandardCharsets.UTF_8);
						}
					}
					else
					{
						strText = new String(ab, n, nLen - n, StandardCharsets.UTF_8);
					}
				}
			}
			textChunk = new PngText(strKeyword, nCompressionFlag, nCompressionMethod, strLanguageTag, strTranslatedKeyword, strText);
			break;
		}

		if (textChunk != null)
		{
			m_manager.addTextChunk(textChunk);
		}

		return (int)m_crc.getValue();
	}

	/**
	 * Ignores a chunk.
	 *
	 * @param nChunkType An int containing the chunk type.
	 * @param nLen An int containing the whole chunk length.
	 * @return An int containing the crc checksum.
	 * @throws IOException In case of IO problems.
	 */
	int skipIgnoredChunk(int nChunkType, int nLen) throws IOException
	{
		m_crc.reset();
		m_crc.updateInt(nChunkType);

		byte[] ab = new byte[nLen];
		readFully_crc(ab);

		return (int)m_crc.getValue();
	}

	@Override
	public void close() throws IOException
	{
		super.close();
	}
}
