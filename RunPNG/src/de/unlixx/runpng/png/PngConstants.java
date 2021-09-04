package de.unlixx.runpng.png;

/**
 * Collection of some constants for use in png handling.
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
 * @see <a href="https://www.w3.org/QA/Tips/png-gif">https://www.w3.org/QA/Tips/png-gif</a>
 * @see <a href="http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html">http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html</a>
 * @see <a href="https://wiki.mozilla.org/APNG_Specification">https://wiki.mozilla.org/APNG_Specification</a>
 *
 */
public class PngConstants
{
	public static final byte[] PNG_SIGNATURE = new byte[] { (byte)0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };

	/**
	 * Combined chunk types (PNG, APNG) as their numerical equivalent
	 *
	 * @see <a href="https://www.w3.org/TR/PNG/#4Concepts.FormatTypes">https://www.w3.org/TR/PNG/#4Concepts.FormatTypes</a>
	 * @see <a href="https://wiki.mozilla.org/APNG_Specification">https://wiki.mozilla.org/APNG_Specification</a>
	 */
	public static final int IHDR = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R'; // 1229472850
	public static final int PLTE = 'P' << 24 | 'L' << 16 | 'T' << 8 | 'E'; // 1347179589
	public static final int IDAT = 'I' << 24 | 'D' << 16 | 'A' << 8 | 'T'; // 1229209940
	public static final int IEND = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D'; // 1229278788
	public static final int gAMA = 'g' << 24 | 'A' << 16 | 'M' << 8 | 'A'; // 1732332865
	public static final int bKGD = 'b' << 24 | 'K' << 16 | 'G' << 8 | 'D'; // 1649100612
	public static final int tRNS = 't' << 24 | 'R' << 16 | 'N' << 8 | 'S'; // 1951551059
	public static final int acTL = 'a' << 24 | 'c' << 16 | 'T' << 8 | 'L'; // 1633899596
	public static final int fcTL = 'f' << 24 | 'c' << 16 | 'T' << 8 | 'L'; // 1717785676
	public static final int fdAT = 'f' << 24 | 'd' << 16 | 'A' << 8 | 'T'; // 1717846356
	public static final int cHRM = 'c' << 24 | 'H' << 16 | 'R' << 8 | 'M'; // 1665684045
	public static final int iCCP = 'i' << 24 | 'C' << 16 | 'C' << 8 | 'P'; // 1766015824
	public static final int sBIT = 's' << 24 | 'B' << 16 | 'I' << 8 | 'T'; // 1933723988
	public static final int sRGB = 's' << 24 | 'R' << 16 | 'G' << 8 | 'B'; // 1934772034
	public static final int hIST = 'h' << 24 | 'I' << 16 | 'S' << 8 | 'T'; // 1749635924
	public static final int pHYs = 'p' << 24 | 'H' << 16 | 'Y' << 8 | 's'; // 1883789683
	public static final int sPLT = 's' << 24 | 'P' << 16 | 'L' << 8 | 'T'; // 1934642260
	public static final int tIME = 't' << 24 | 'I' << 16 | 'M' << 8 | 'E'; // 1950960965
	public static final int iTXt = 'i' << 24 | 'T' << 16 | 'X' << 8 | 't'; // 1767135348
	public static final int tEXt = 't' << 24 | 'E' << 16 | 'X' << 8 | 't'; // 1950701684
	public static final int zTXt = 'z' << 24 | 'T' << 16 | 'X' << 8 | 't'; // 2052348020

	/**
	 * Common "big" buffer size. E. g. for maximum deflated IDAT or fdAT blocks.
	 */
	public static final int BUFFER_32K = 0x8000;

	/**
	 *  Static lengths of frequently used chunk types
	 */
	public static final int LENGTH_IHDR = 13;
	public static final int LENGTH_acTL = 8;
	public static final int LENGTH_fcTL = 26;
	public static final int LENGTH_IEND = 0; // :-)

	/**
	 * CRC32 checksum of the IEND chunk.
	 */
	public static final int CHECKSUM_IEND = 0xae426082;

	/**
	 * Decodes a chunk type as a string
	 *
	 * @param nChunkType The chunk type.
	 * @return The string equivalent of the chunk type
	 */
	public static String chunkTypeName(int nChunkType)
	{
		char ac[] = new char[4];
		ac[0] = (char)((nChunkType >>> 24) & 0xff);
		ac[1] = (char)((nChunkType >>> 16) & 0xff);
		ac[2] = (char)((nChunkType >>> 8) & 0xff);
		ac[3] = (char)(nChunkType & 0xff);

		// Prevent nonsense
		for (char c : ac)
		{
			if (!(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z'))
			{
				return "???";
			}
		}

		return new String(ac, 0, 4);
	}
}
