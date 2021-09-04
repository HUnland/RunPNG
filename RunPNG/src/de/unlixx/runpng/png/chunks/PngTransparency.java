package de.unlixx.runpng.png.chunks;

import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * Implements the transparency chunk (tRNS) for the color types 0, 2 and 3. This chunk can appear 0 or 1 time
 * in a png file. In case of indexed color (type 3) this is a table in conjunction with the
 * {@link PngPalette} chunk (PLTE).
 * In case of truecolor (type 2) and greyscale (type 0) this is a color sample with a byte length of 6 or 2.
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
 * @see <a href="https://www.w3.org/TR/PNG/#11tRNS">https://www.w3.org/TR/PNG/#11tRNS</a>
 * @see <a href="http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html">http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html</a>
 * <pre>There seems to be an error in the byte order of colors at w3.org. RBG instead of RGB.</pre>
 */
public class PngTransparency
{
	PngColorType m_colorType;
	int m_nBitDepth;
	long m_ltRNS = -1;
	byte[] m_abtRNS;

	/**
	 * Constructor for use in conjunction with a {@link PngPalette}.
	 *
	 * @param colorType A {@link PngColorType} enum type.
	 * @param nBitDepth An int containing the bit depth.
	 * @param abtRNS A byte array containing alpha values for use in conjunction with a color palette.
	 * This byte array length may be shorter than but must not exceed the color count of the palette.
	 */
	public PngTransparency(PngColorType colorType, int nBitDepth, byte[] abtRNS)
	{
		m_colorType = colorType;
		m_nBitDepth = nBitDepth;
		m_abtRNS = abtRNS;

		switch (colorType)
		{
		case GREYSCALE:
			if (abtRNS.length != 2)
			{
				throw new Failure("failure.wrong.transparency.length", 2, abtRNS.length);
			}

			switch (m_nBitDepth)
			{
			case 8: m_ltRNS = (abtRNS[0] & 0xff); break;
			case 16: m_ltRNS = (abtRNS[0] & 0xff) << 8 | (abtRNS[1] & 0xff); break;
			default: break;
			}
			break;

		case TRUECOLOR:
			if (abtRNS.length != 6)
			{
				throw new Failure("failure.wrong.transparency.length", 6, abtRNS.length);
			}

			switch (m_nBitDepth)
			{
			case 8: m_ltRNS = (abtRNS[0] & 0xff) << 16 | (abtRNS[2] & 0xff) << 8 | (abtRNS[4] & 0xff); break;
			case 16: m_ltRNS = (abtRNS[0] & 0xff) << 40 | (abtRNS[1] & 0xff) << 32 | (abtRNS[2] & 0xff) << 24
											| (abtRNS[3] & 0xff) << 16 | (abtRNS[4] & 0xff) << 8 | (abtRNS[5] & 0xff); break;
			default: break;
			}
			break;

		case INDEXED:
			break;

		case GREYSCALE_ALPHA:
		case TRUECOLOR_ALPHA:
			throw new Failure("failure.wrong.transparency", colorType.getType());
		default:
			break;
		}

	}

	/**
	 * Constructor for use with greyscale (type 0) and truecolor (type 2).
	 * It takes a color sample with a color treated as transparent in the bitmap.
	 *
	 * @param colorType A {@link PngColorType} enum type.
	 * @param nBitDepth An int containing the bit depth.
	 * @param ntRNS An int containing the color sample treated as transparent.
	 */
	public PngTransparency(PngColorType colorType, int nBitDepth, int ntRNS)
	{
		m_colorType = colorType;
		m_nBitDepth = nBitDepth;
		m_ltRNS = ntRNS;

		switch (colorType)
		{
		case GREYSCALE:
			m_abtRNS = new byte[2];
			m_abtRNS[0] = (byte)(ntRNS & 0xff);
			break;

		case TRUECOLOR:
			m_abtRNS = new byte[6];
			m_abtRNS[0] = (byte)((ntRNS >>> 16) & 0xff);
			m_abtRNS[2] = (byte)((ntRNS >>> 8) & 0xff);
			m_abtRNS[4] = (byte)(ntRNS & 0xff);
			break;

		case INDEXED: // Just kidding :-)
			m_abtRNS = new byte[4];
			m_abtRNS[0] = (byte)((ntRNS >>> 24) & 0xff);
			m_abtRNS[1] = (byte)((ntRNS >>> 16) & 0xff);
			m_abtRNS[2] = (byte)((ntRNS >>> 8) & 0xff);
			m_abtRNS[3] = (byte)(ntRNS & 0xff);
			break;

		case GREYSCALE_ALPHA:
		case TRUECOLOR_ALPHA:
			throw new Failure("failure.wrong.transparency", colorType.getType());
		default:
			m_abtRNS = null;
			break;
		}
	}

	/**
	 * Gets a tRNS sample color as a long integer value.
	 *
	 * @return A long integer containing an appropriate sample color (in the 2 or 6 lower bytes).
	 */
	public long gettRNS()
	{
		return m_ltRNS;
	}

	/**
	 * Gets a 2 or 6 bytes long array for greyscale or truecolor.
	 * Or a variable length byte array for indexed color.
	 *
	 * @return An array of bytes according the colortype.
	 */
	public byte[] getBytes()
	{
		return m_abtRNS;
	}
}
