package de.unlixx.runpng.png.chunks;

import de.unlixx.runpng.png.PngColorType;

/**
 * Implementation of the png header chunk (IHDR). This must be the first chunk
 * of the png file and follows directly the png signature.
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
 * @see <a href="https://www.w3.org/TR/PNG/#11IHDR">https://www.w3.org/TR/PNG/#11IHDR</a>
 */
public class PngHeader
{
	final int m_nWidth;
	final int m_nHeight;
	final int m_nScanlineStride;
	final PngColorType m_colorType;
	final int m_nBitDepth;
	final int m_nCompressionMethod;
	final int m_nFilterMethod;
	final int m_nInterlaceMethod;
	final int m_nBitsPerPixel;

	/**
	 * Constructor of this PngHeader class.
	 *
	 * @param nWidth The width of the whole picture.
	 * @param nHeight The height of the whole picture.
	 * @param nBitDepth The number of bits per sample or per palette index (not per pixel).
	 * One of 1, 2, 4, 8, 16. Depends on the color type.
	 * @param colorType A {@link PngColorType} enum type.
	 * created from the color type byte.
	 * @param nCompressionMethod The compression method. Actually only compression method 0
	 * (deflate/inflate compression with a sliding window of at most 32768 bytes) is internationally
	 * standardized for png.
	 * @param nFilterMethod The filter method to support compression. Actually only filter method 0
	 * (adaptive filtering with five basic filter types) is internationally standardized for png.
	 * @param nInterlaceMethod 0 = not interlaced, 1 = interlaced. Actually only interlace method Adam7
	 * is internationally standardized for png.
	 */
	public PngHeader(int nWidth, int nHeight, int nBitDepth, PngColorType colorType, int nCompressionMethod, int nFilterMethod, int nInterlaceMethod)
	{
		m_nWidth = nWidth;
		m_nHeight = nHeight;
		m_nBitDepth = nBitDepth;
		m_colorType = colorType;
		m_nCompressionMethod = nCompressionMethod;
		m_nFilterMethod = nFilterMethod;
		m_nInterlaceMethod = nInterlaceMethod;
		m_nBitsPerPixel = nBitDepth * colorType.getComponentsPerPixel();
		m_nScanlineStride = colorType.calcScanlineStride(nWidth, nBitDepth);
	}

	/**
	 * Gets the scanline stride (bytes per scanline).
	 *
	 * @return An int containing the scanline stride.
	 */
	public int getScanlineStride()
	{
		return m_nScanlineStride;
	}

	/**
	 * Gets the width of the whole picture.
	 *
	 * @return An int containing the picture width.
	 */
	public int getWidth()
	{
		return m_nWidth;
	}

	/**
	 * Gets the height of the whole picture.
	 *
	 * @return An int containing the picture height.
	 */
	public int getHeight()
	{
		return m_nHeight;
	}

	/**
	 * Gets the number of bits per sample or per palette index (not per pixel).
	 * One of 1, 2, 4, 8, 16. Depends on the color type.
	 *
	 * @return An int containing the bit depth.
	 */
	public int getBitDepth()
	{
		return m_nBitDepth;
	}

	/**
	 * Gets the number of the bits per pixel.
	 *
	 * @return An int containing the number of bits per pixel.
	 */
	public int getBitsPerPixel()
	{
		return (byte)m_nBitsPerPixel;
	}

	/**
	 * Gets a {@link PngColorType} enum type,
	 * created from the color type byte.
	 *
	 * @return A PngColorType object.
	 */
	public PngColorType getColorType()
	{
		return m_colorType;
	}

	/**
	 * Gets the compression method. Actually only compression method 0
	 * (deflate/inflate compression with a sliding window of at most 32768 bytes) is internationally
	 * standardized for png.
	 *
	 * @return An int containing the method of compression.
	 */
	public int getCompressionMethod()
	{
		return m_nCompressionMethod;
	}

	/**
	 * Gets the filter method to support compression. Actually only filter method 0
	 * (adaptive filtering with five basic filter types) is internationally standardized for png.
	 *
	 * @return An int containing the method of filtering.
	 */
	public int getFilterMethod()
	{
		return m_nFilterMethod;
	}

	/**
	 * Gets the interlace method. 0 = not interlaced, 1 = interlaced. Actually only interlace method Adam7
	 * is internationally standardized for png.
	 *
	 * @return An int containing the interlace method.
	 */
	public int getInterlaceMethod()
	{
		return m_nInterlaceMethod;
	}
}
