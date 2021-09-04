package de.unlixx.runpng.bitmap;

import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * This is the abstract super class of the scanline classes provided
 * for the five color types. It is also the only place where scanline
 * objects can be obtained.
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
public abstract class Scanline32
{
	final PngColorType m_colorType;
	final int m_nBitDepth;
	int m_nScanlineStride;
	Bitmap32 m_bitmap;
	PngPalette m_palette;
	PngTransparency m_transparency;

	/**
	 * Package private constructor of this abstract class.
	 *
	 * @param colorType A {@link PngColorType} enum type.
	 * @param nBitDepth An int containing the bit depth of the color.
	 * @param bitmap A {@link Bitmap32} object.
	 */
	Scanline32(PngColorType colorType, int nBitDepth, Bitmap32 bitmap)
	{
		m_colorType = colorType;
		m_nBitDepth = nBitDepth;
		m_bitmap = bitmap;
		m_nScanlineStride = m_colorType.calcScanlineStride(bitmap.getWidth(), m_nBitDepth);
	}

	/**
	 * Replaces the current bitmap with another one. Recalculates the scanline stride.
	 *
	 * @param bitmap A {@link Bitmap32} object.
	 */
	public void setBitmap(Bitmap32 bitmap)
	{
		m_bitmap = bitmap;
		m_nScanlineStride = m_colorType.calcScanlineStride(bitmap.getWidth(), m_nBitDepth);
	}

	/**
	 * Gets the current bitmap.
	 *
	 * @return A {@link Bitmap32} object.
	 */
	public Bitmap32 getBitmap()
	{
		return m_bitmap;
	}

	/**
	 * Sets the palette chunk (PLTE) of this scanline object in case of indexed color type.
	 *
	 * @param palette A {@link PngPalette} object.
	 */
	public void setPalette(PngPalette palette)
	{
		m_palette = palette;
	}

	/**
	 * Gets the palette chunk (PLTE) of this scanline object in case of indexed color type.
	 *
	 * @return A {@link PngPalette} object.
	 * Or null if not applicable.
	 */
	public PngPalette getPalette()
	{
		return m_palette;
	}

	/**
	 * Gets the scanline stride which is calculated by bytes per width + 1 for the filter byte.
	 *
	 * @return An int containing the scanline stride.
	 */
	public int getScanlineStride()
	{
		return m_nScanlineStride;
	}

	/**
	 * Gets the raw width in pixels of the current bitmap.
	 *
	 * @return An int containing the raw width.
	 */
	public int getBitmapWidth()
	{
		return m_bitmap.getWidth();
	}

	/**
	 * Gets the height of the current bitmap in scanlines.
	 *
	 * @return An int containing the height.
	 */
	public int getBitmapHeight()
	{
		return m_bitmap.getHeight();
	}

	/**
	 * Writes a scanline to the current bitmap.
	 *
	 * @param abSrc A source byte array containing the color/greyscale components to write.
	 * @param nSrcIdx An int containing the start index in the source byte array.
	 * @param nOffsX An int containing the offset where to start writing.
	 * @param nStepX An int containing the step width per pixel.
	 * @param nLine An int containing the line number to write.
	 */
	public abstract void write(byte[] abSrc, int nSrcIdx, int nOffsX, int nStepX, int nLine);

	/**
	 * Reads a scanline from the current bitmap.
	 *
	 * @param abDest A destination byte array which receives the color/greyscale components.
	 * @param nDestIdx An int containing the start index in the destination byte array.
	 * @param nOffsX An int containing the offset where to start reading.
	 * @param nStepX An int containing the step width per pixel.
	 * @param nLine An int containing the line number to read.
	 */
	public abstract void read(byte[] abDest, int nDestIdx, int nOffsX, int nStepX, int nLine);

	/**
	 * Sets the transparency chunk (tRNS) for the color types 0, 2 and 3. In case of indexed color (type 3) this
	 * is a table in conjunction with the {@link PngPalette} chunk (PLTE).
	 * In case of truecolor (type 2) and greyscale (type 0) this is a color sample with a byte length of 6 or 2.
	 *
	 * @param trans A {@link PngTransparency} object.
	 */
	public abstract void setTransparency(PngTransparency trans);

	/**
	 * Creates a scanline object according color type and bit depth and sets an initial bitmap to read from or write to.
	 *
	 * @param colorType A {@link PngColorType} enum type.
	 * @param nBitDepth An int containing the bit depth.
	 * @param bitmap A {@link Bitmap32} object.
	 * @return A Scanline32 object derived from this class.
	 */
	public static Scanline32 getScanlineFor(PngColorType colorType, int nBitDepth, Bitmap32 bitmap)
	{
		switch (colorType)
		{
		case TRUECOLOR:
		case TRUECOLOR_ALPHA:
			return new Scanline32TrueColor(colorType, nBitDepth, bitmap);

		case INDEXED:
			return new Scanline32Indexed(colorType, nBitDepth, bitmap);

		case GREYSCALE:
		case GREYSCALE_ALPHA:
			switch (nBitDepth)
			{
			case 1:
			case 2:
				return new Scanline32Indexed(colorType, nBitDepth, bitmap);

			case 4:
				if (colorType.hasAlpha())
				{
					return new Scanline32Greyscale(colorType, nBitDepth, bitmap);
				}
				return new Scanline32Indexed(colorType, nBitDepth, bitmap);

			case 8:
			case 16:
				return new Scanline32Greyscale(colorType, nBitDepth, bitmap);

			default:
				throw new Failure("failure.wrong.bitdepth");
			}

		default:
			throw new Failure("failure.unsupported.colortype", colorType.getType());
		}
	}
}
