package de.unlixx.runpng.util;

import java.util.Arrays;

import de.unlixx.runpng.bitmap.Bitmap32;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Static utility methods to handle bitmaps and image objects.
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
public class ImageUtil
{
	private ImageUtil() { }

	/**
	 * Converts the values of a Color object into an int value.
	 *
	 * @param color The given {@link Color} object.
	 * @return An int ARGB value of the color.
	 */
	public static int getIntARGBColor(final Color color)
	{
		return ((int)(color.getOpacity() * 255)) << 24
				| ((int)(color.getRed() * 255)) << 16
				| ((int)(color.getGreen() * 255)) << 8
				| ((int)(color.getBlue() * 255));
	}

	/**
	 * Creates a Color object by the given ARGB integers.
	 * All int values must be in the unsigned byte range (0 - 255).
	 *
	 * @param nA An int containing the alpha value.
	 * @param nR An int containing the red part.
	 * @param nG An int containing the green part.
	 * @param nB An int containing the blue part.
	 * @return The resulting {@link Color} object.
	 */
	public static Color getColor(final int nA, final int nR, final int nG, final int nB)
	{
		return new Color(nR / 255d, nG / 255d, nB / 255d, nA / 255d);
	}

	/**
	 * Creates a Color object by a single integer.
	 * All four bytes of this int are used in ARGB order.
	 *
	 * @param nARGB An int containing the whole color information.
	 * @return The resulting {@link Color} object.
	 */
	public static Color getColor(final int nARGB)
	{
		return new Color(((nARGB >> 16) & 0xff)  / 255d, ((nARGB >> 8) & 0xff) / 255d, (nARGB & 0xff) / 255d, ((nARGB >> 24) & 0xff) / 255d);
	}

	/**
	 * Creates a greyscale in the range 0 - 255 from the given RGB, where 0 is black
	 * and 255 is white. A probably contained alpha value will be ignored.
	 *
	 * @param nRGB An int containing an RGB color information.
	 * @return A greyscale value in the range 0 - 255.
	 */
	public static int linearGrey(final int nRGB)
	{
		return (((nRGB >> 16) & 0xff) + ((nRGB >> 8) & 0xff) + (nRGB & 0xff)) / 3;
	}

	/**
	 * Blends an upper pixel over a given lower pixel according the opacity (alpha)
	 * of lower and upper pixel.
	 *
	 * @param nLower An int containing the ARGB color information of the lower pixel.
	 * @param nUpper An int containing the ARGB color information of the upper pixel.
	 * @return The resulting ARGB color value.
	 */
	public static int blendPixel(final int nLower, final int nUpper)
	{
		double dUA = (double)((nUpper & 0xff000000) >>> 24) / 255,
			dLA = (double)((nLower & 0xff000000) >>> 24) / 255 * (1 - dUA),
			dDiv = dUA + dLA,
			dMU = dUA / dDiv,
			dML = dLA / dDiv;

		int nLR = (nLower & 0x00ff0000) >> 16,
			nUR = (nUpper & 0x00ff0000) >> 16,
			nLG = (nLower & 0x0000ff00) >> 8,
			nUG = (nUpper & 0x0000ff00) >> 8,
			nLB = (nLower & 0x000000ff),
			nUB = (nUpper & 0x000000ff),
			nA = (int)((dLA + dUA) * 255),
			nR = (int)(dML * nLR + dMU * nUR),
			nG = (int)(dML * nLG + dMU * nUG),
			nB = (int)(dML * nLB + dMU * nUB);

		return nA << 24 | nR << 16 | nG << 8 | nB;
	}

	/**
	 * Shifts a bitmap horizontally by a given column amount. The resulting overflow
	 * will be inserted at the other side.
	 *
	 * @param bitmap A {@link Bitmap32} object with content to shift.
	 * @param nCols An int containing the amount of columns to shift. A positive value
	 * causes a shift to the right side. A negative value vice versa. If a 0 is given or the
	 * shifting is &gt;= the width then the bitmap simply gets copied.
	 * @return A new Bitmap32 object, shifted by the given amount of columns.
	 */
	public static Bitmap32 bitmapShiftHorz(final Bitmap32 bitmap, final int nCols)
	{
		final int nWidth = bitmap.getWidth(),
				nHeight = bitmap.getHeight(),
				anARGB32[] = bitmap.getPixels(),
				nArrLen = anARGB32.length,
				anARGB32New[] = new int[nArrLen],
				nShift = Math.max(1, Math.min(Math.abs(nCols), nWidth));

		if (nCols != 0 && nShift < nWidth)
		{
			for (int nRow = 0; nRow < nHeight; nRow++)
			{
				final int nOffsX = nRow * nWidth;

				if (nCols > 0)
				{
					System.arraycopy(anARGB32, nOffsX, anARGB32New, nOffsX + nShift, nWidth - nShift);
					System.arraycopy(anARGB32, nOffsX + nWidth - nShift, anARGB32New, nOffsX, nShift);
				}
				else
				{
					System.arraycopy(anARGB32, nOffsX + nShift, anARGB32New, nOffsX, nWidth - nShift);
					System.arraycopy(anARGB32, nOffsX, anARGB32New, nOffsX + nWidth - nShift, nShift);
				}
			}
		}
		else
		{
			System.arraycopy(anARGB32, 0, anARGB32New, 0, nArrLen);
		}

		return new Bitmap32(anARGB32New, nWidth, nHeight);
	}

	/**
	 * Shifts a bitmap vertically by a given line amount. The resulting overflow
	 * will be inserted at the other side.
	 *
	 * @param bitmap A {@link Bitmap32} object with content to shift.
	 * @param nLines An int containing the amount of lines to shift. A positive value
	 * causes a shift down. A negative value vice versa. If a 0 is given or the
	 * shifting is &gt;= the width then the bitmap simply gets copied.
	 * @return A new Bitmap32 object, shifted by the given amount of lines.
	 */
	public static Bitmap32 bitmapShiftVert(final Bitmap32 bitmap, final int nLines)
	{
		final int nWidth = bitmap.getWidth(),
				nHeight = bitmap.getHeight(),
				anARGB32[] = bitmap.getPixels(),
				nArrLen = anARGB32.length,
				anARGB32New[] = new int[nArrLen],
				nShift = Math.max(1, Math.min(Math.abs(nLines), nHeight)),
				nBlock = nShift * nWidth;

		if (nLines != 0 && nShift < nHeight)
		{
			if (nLines > 0)
			{
				System.arraycopy(anARGB32, 0, anARGB32New, nBlock, nArrLen - nBlock);
				System.arraycopy(anARGB32, nArrLen - nBlock, anARGB32New, 0, nBlock);
			}
			else
			{
				System.arraycopy(anARGB32, nBlock, anARGB32New, 0, nArrLen - nBlock);
				System.arraycopy(anARGB32, 0, anARGB32New, nArrLen - nBlock, nBlock);
			}
		}
		else
		{
			System.arraycopy(anARGB32, 0, anARGB32New, 0, nArrLen);
		}

		return new Bitmap32(anARGB32New, nWidth, nHeight);
	}


	/**
	 * Crops a bitmap while keeping it centered. The resulting bitmap will be filled with
	 * a given ARGB color before the cropped bitmap is copied over the destination bitmap.
	 *
	 * @param bitmap The source {@link Bitmap32} object to copy and crop.
	 * @param nDestWidth The desired width of the new bitmap.
	 * @param nDestHeight The desired height of the new bitmap.
	 * @param nARGBFill An ARGB color value for the fill before copy operation.
	 * @return A new Bitmap32 object, cropped to the given dimensions and filled where needed.
	 */
	public static Bitmap32 bitmapCropCentered(final Bitmap32 bitmap, final int nDestWidth, final int nDestHeight, final int nARGBFill)
	{
		final int nSrcWidth = bitmap.getWidth(),
			nSrcHeight = bitmap.getHeight(),
			nOffsX = (nDestWidth - nSrcWidth) / 2,
			nOffsY = (nDestHeight - nSrcHeight) / 2;

		final int[] anARGB32New = new int[nDestWidth * nDestHeight],
				anARGB32 = bitmap.getPixels();

		Arrays.fill(anARGB32New, nARGBFill);

		for (int nY = 0; nY < nDestHeight; nY++)
		{
			for (int nX = 0; nX < nDestWidth; nX++)
			{
				final int nSrcX = nX - nOffsX,
						nSrcY = nY - nOffsY;

				if (nSrcX >= 0 && nSrcX < nSrcWidth && nSrcY >= 0 && nSrcY < nSrcHeight)
				{
					anARGB32New[nY * nDestWidth + nX] = anARGB32[nSrcY * nSrcWidth + nSrcX];
				}
			}
		}

		return new Bitmap32(anARGB32New, nDestWidth, nDestHeight);
	}

	/**
	 * Creates an asymmetrically scaled image by the given width and height.
	 *
	 * @param image The source image object.
	 * @param dDestWidth The desired width of the new image.
	 * @param dDestHeight The desired height of the new image.
	 * @return A new scaled image object with the desired dimensions.
	 */
	public static WritableImage imageScaleAsymmetrical(final Image image, final double dDestWidth, final double dDestHeight)
	{
		final Canvas canvas = new Canvas(dDestWidth, dDestHeight);
		final GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, dDestWidth, dDestHeight);

		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		return canvas.snapshot(params, null);
	}

	/**
	 * Three possible scale modes for imageScaleCentered.
	 */
	public enum SCALEMODE
	{
		BOTH,
		HORZ,
		VERT
	}

	/**
	 * Scales an image symmetrically or asymetrically to the given dimensions while keeping it centered.
	 *
	 * @param image The source image object.
	 * @param dDestWidth The desired width of the new image.
	 * @param dDestHeight The desired height of the new image.
	 * @param scaleMode A mode which direction to scale
	 * @param bSym If true then the aspect ratio will be preserved while scaling.
	 * @return A new scaled image object with the desired dimensions.
	 */
	public static Image imageScaleCentered(final Image image, final double dDestWidth, final double dDestHeight, SCALEMODE scaleMode, boolean bSym)
	{
		double dSrcWidth = image.getWidth(),
				dSrcHeight = image.getHeight(),
				dScaleX = dDestWidth / dSrcWidth,
				dScaleY = dDestHeight / dSrcHeight,
				dScale, dGapX, dGapY;

		final Canvas canvas = new Canvas(dDestWidth, dDestHeight);
		final GraphicsContext gc = canvas.getGraphicsContext2D();

		if (bSym)
		{
			switch (scaleMode)
			{
			case HORZ: dScale = dScaleX; break;
			case VERT: dScale = dScaleY; break;
			default: dScale = Math.min(dScaleX, dScaleY);
			}

			dGapX = dDestWidth - dSrcWidth * dScale;
			dGapY = dDestHeight - dSrcHeight * dScale;
		}
		else
		{
			switch (scaleMode)
			{
			case HORZ:
				dGapX = 0;
				dGapY = dDestHeight - dSrcHeight;
				break;

			case VERT:
				dGapX = dDestWidth - dSrcWidth;
				dGapY = 0;
				break;

			default:
				dGapX = 0;
				dGapY = 0;
				break;
			}
		}

		gc.drawImage(image, 0, 0, dSrcWidth, dSrcHeight, dGapX / 2, dGapY / 2, dDestWidth - dGapX, dDestHeight - dGapY);

		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		return canvas.snapshot(params, null);
	}

	/**
	 * Crops an image while keeping it centered. The resulting image will be filled with
	 * transparency if the new dimensions are greater than the source image.
	 *
	 * @param image The source image to copy and crop.
	 * @param dDestWidth The desired width of the new image.
	 * @param dDestHeight The desired height of the new image.
	 * @return A new {@link Image} object, cropped to the given dimensions and filled where needed.
	 */
	public static Image imageCropCentered(final Image image, final double dDestWidth, final double dDestHeight)
	{
		double dSrcWidth = image.getWidth(),
				dSrcHeight = image.getHeight(),
				dOffsX = (dDestWidth - dSrcWidth) / 2,
				dOffsY = (dDestHeight - dSrcHeight) / 2;

		final Canvas canvas = new Canvas(dDestWidth, dDestHeight);
		final GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.drawImage(image, dOffsX, dOffsY, dSrcWidth, dSrcHeight);

		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		return canvas.snapshot(params, null);
	}

	/**
	 * Blends an ARGB color over a pixel array.
	 *
	 * @param anPixels The ARGB pixel array.
	 * @param color The ARGB color object to blend over.
	 */
	protected static void blendColorOver(final int[] anPixels, Color color)
	{
		final int nLen = anPixels.length,
				nColor = getIntARGBColor(color);

		for (int n = 0; n < nLen; n++)
		{
			if ((nColor & 0xff000000) == 0xff000000)
			{
				anPixels[n] = nColor;
			}
			else
			{
				anPixels[n] = blendPixel(anPixels[n], nColor);
			}
		}
	}

	/**
	 * Blends an upper pixel array over a given lower pixel array according the opacity (alpha)
	 * of lower and upper pixel. The lower pixel array contains the result.
	 *
	 * @param anLower An int array containing the ARGB color information of the lower pixels.
	 * @param anUpper An int array containing the ARGB color information of the upper pixels.
	 */
	protected static void blend(final int[] anLower, final int[] anUpper)
	{
		final int nLen = anLower.length;

	    for (int n = 0; n < nLen; n++)
	    {
	    	if ((anUpper[n] & 0xff000000) != 0)
	    	{
	    		anLower[n] = blendPixel(anLower[n], anUpper[n]);
	    	}
	    }
	}

	/**
	 * Blends an upper pixel array over a given lower pixel array according the opacity (alpha)
	 * of lower and upper pixel and according the given positions and dimensions. The lower pixel
	 * array contains the result.
	 *
	 * @param anLower An int array containing the ARGB color information of the lower pixels.
	 * @param nWidth The width of the destinated bitmap.
	 * @param nHeight The height of the destinated bitmap.
	 * @param anUpper An int array containing the ARGB color information of the upper pixels.
	 * @param nUpperOffsX The horizontal position of the source bitmap.
	 * @param nUpperOffsY The vertical position of the source bitmap.
	 * @param nUpperWidth The width of the source bitmap.
	 * @param nUpperHeight The height of the source bitmap.
	 */
	protected static void blend(final int[] anLower, final int nWidth, final int nHeight,
			final int[] anUpper, final int nUpperOffsX, final int nUpperOffsY, final int nUpperWidth, final int nUpperHeight)
	{
		final int nRowBegin = Math.max(0, nUpperOffsY),
				nRowEnd = Math.min(nHeight, nUpperOffsY + nUpperHeight),
				nColBegin = Math.max(0, nUpperOffsX),
				nColEnd = Math.min(nWidth, nUpperOffsX + nUpperWidth);

		for (int nRow = nRowBegin; nRow < nRowEnd; nRow++)
		{
			final int nUpperRowOffs = (nRow - nUpperOffsY) * nUpperWidth;

			for (int nCol = nColBegin; nCol < nColEnd; nCol++)
			{
				final int nLIdx = nRow * nWidth + nCol,
						nUpper = anUpper[nUpperRowOffs + nCol - nUpperOffsX];

				if ((anLower[nLIdx] & 0xff000000) == 0)
				{
					anLower[nLIdx] = nUpper; // Whatever it is
				}
				else //if ((nUpper & 0xff000000) != 0) // TODO?
				{
					anLower[nLIdx] = blendPixel(anLower[nLIdx], nUpper);
				}
			}
		}
	}

	/**
	 * Applies a foreground color and/or a foreground image to a given lower image. If the
	 * given foreground image size is not the same like the size of the lower image then
	 * the foreground image will be centered.
	 *
	 * @param image The source {@link Image} object to apply the foreground to.
	 * @param colorFgnd A {@link Color} object or null if not wanted.
	 * @param imageFgnd An {@link Image} object or null if not wanted.
	 * @return A copy of the given {@link Image} object with the foreground applied.
	 */
	public static Image imageApplyForeground(final Image image, final Color colorFgnd, final Image imageFgnd)
	{
		final Bitmap32 bitmapLower = bitmapFromImage(image),
				bitmapUpper = imageFgnd != null ? bitmapFromImage(imageFgnd) : null;

		final int nWidth = bitmapLower.getWidth(),
				nHeight = bitmapLower.getHeight();

		int[] anPixels = bitmapLower.getPixels();

		if (colorFgnd != null && colorFgnd.getOpacity() > 0)
		{
			blendColorOver(anPixels, colorFgnd);
		}

		if (bitmapUpper != null)
		{
			final int nFgndWidth = bitmapUpper.getWidth(),
					nFgndHeight = bitmapUpper.getHeight();

			blend(anPixels, nWidth, nHeight, bitmapUpper.getPixels(), (nWidth - nFgndWidth) / 2, (nHeight - nFgndHeight) / 2, nFgndWidth, nFgndHeight);
		}

		WritableImage imageResult = new WritableImage(nWidth, nHeight);
		PixelWriter pxwriter = imageResult.getPixelWriter();
		pxwriter.setPixels(0, 0, nWidth, nHeight,
				PixelFormat.getIntArgbInstance(), anPixels, 0, nWidth);

		return imageResult;
	}

	/**
	 * Applies a background color and/or a background image to a given upper image. If the
	 * given background image size is not the same like the size of the upper image then
	 * the background image will be centered.
	 *
	 * @param image The source {@link Image} object to apply the background to.
	 * @param colorBgnd A {@link Color} object or null if not wanted.
	 * @param imageBgnd An {@link Image} object or null if not wanted.
	 * @return A copy of the given {@link Image} object with the background applied.
	 */
	public static Image imageApplyBackground(final Image image, final Color colorBgnd, final Image imageBgnd)
	{
		final Bitmap32 bitmapUpper = bitmapFromImage(image),
				bitmapLower = imageBgnd != null ? bitmapFromImage(imageBgnd) : null;

		final int nWidth = bitmapUpper.getWidth(),
				nHeight = bitmapUpper.getHeight();

		int[] anUpper = bitmapUpper.getPixels(),
			anPixels = null;

		if (colorBgnd != null && colorBgnd.getOpacity() > 0)
		{
			anPixels = new int[anUpper.length];
			Arrays.fill(anPixels, getIntARGBColor(colorBgnd));
		}

		if (bitmapLower != null)
		{
			final int nBgndWidth = bitmapLower.getWidth(),
					nBgndHeight = bitmapLower.getHeight();

			if (anPixels == null)
			{
				anPixels = new int[anUpper.length];
			}

			blend(anPixels, nWidth, nHeight, bitmapLower.getPixels(), (nWidth - nBgndWidth) / 2, (nHeight - nBgndHeight) / 2, nBgndWidth, nBgndHeight);
		}

		if (anPixels == null)
		{
			anPixels = anUpper;
		}
		else
		{
			blend(anPixels, anUpper);
		}

		WritableImage imageResult = new WritableImage(nWidth, nHeight);
		PixelWriter pxwriter = imageResult.getPixelWriter();
		pxwriter.setPixels(0, 0, nWidth, nHeight,
				PixelFormat.getIntArgbInstance(), anPixels, 0, nWidth);

		return imageResult;
	}

	/**
	 * Applies a greyscale mask to a given pixel array. Where full white means opaque and full black
	 * means transparent. Values between black and white result in translucent pixels. Colors in the
	 * mask will be converted into greyscale. Alpha (opacity) values in the mask or in the background
	 * will be ignored.
	 *
	 * @param anPixels The ARGB pixel array.
	 * @param nWidth The supposed width of the bitmap.
	 * @param nHeight The supposed height of the bitmap.
	 * @param anMask An RGB mask pixel array.
	 * @param nMaskOffsX The horizontal offset of the mask.
	 * @param nMaskOffsY The vertical offset of the mask.
	 * @param nMaskWidth The supposed width of the mask.
	 * @param nMaskHeight The supposed height of the mask.
	 * @param nBgnd An RGB background color. Alpha (opacity) values will be ignored.
	 */
	protected static void mask(final int[] anPixels, final int nWidth, final int nHeight,
			final int[] anMask, final int nMaskOffsX, final int nMaskOffsY, final int nMaskWidth, final int nMaskHeight,
			int nBgnd)
	{
		double dMBgnd = linearGrey(nBgnd) / 255d;

		for (int nRow = 0; nRow < nHeight; nRow++)
		{
			final int nRowIdx = nRow * nWidth;

			if (nRow < nMaskOffsY || nRow >= nMaskOffsY + nMaskHeight)
			{
				for (int nCol = 0; nCol < nWidth; nCol++)
				{
					final int nIdx = nRowIdx + nCol,
							nA = ((anPixels[nIdx] >>> 24) & 0xff);

					anPixels[nIdx] = (anPixels[nIdx] & 0x00ffffff) | (((int)(Math.round(dMBgnd * nA)) << 24) & 0xff000000);
				}
			}
			else
			{
				final int nMaskRowOffs = (nRow - nMaskOffsY) * nMaskWidth;

				for (int nCol = 0; nCol < nWidth; nCol++)
				{
					final double dM = nCol >= nMaskOffsX && nCol < nMaskOffsX + nMaskWidth ?
							linearGrey(anMask[nMaskRowOffs + nCol - nMaskOffsX]) / 255d : dMBgnd;

					final int nIdx = nRow * nWidth + nCol,
							nA = ((anPixels[nIdx] >>> 24) & 0xff);

					anPixels[nIdx] = (anPixels[nIdx] & 0x00ffffff) | (((int)(Math.round(dM * nA)) << 24) & 0xff000000);
				}
			}
		}
	}

	/**
	 * Masks a pixel array with a greyscale background and a mask pixel array.
	 *
	 * @param anPixels The ARGB pixel array.
	 * @param nBgnd An RGB background color. Alpha (opacity) values will be ignored.
	 * @param anMask An RGB mask pixel array of the same length like the ARGB pixel array.
	 */
	protected static void mask(final int[] anPixels, final int nBgnd, final int[] anMask)
	{
		final int nLen = anPixels.length;
		double dMBgnd = linearGrey(nBgnd) / 255d;

		for (int n = 0; n < nLen; n++)
		{
			final double dM = anMask[n] != 0 ? linearGrey(anMask[n]) / 255d : dMBgnd;
			final int nA = ((anPixels[n] >>> 24) & 0xff);
			anPixels[n] = (anPixels[n] & 0x00ffffff) | (((int)(Math.round(dM * nA)) << 24) & 0xff000000);
		}
	}

	/**
	 * Masks a pixel array with a greyscale background.
	 *
	 * @param anPixels The ARGB pixel array.
	 * @param nBgnd An RGB background color. Alpha (opacity) values will be ignored.
	 */
	protected static void mask(final int[] anPixels, final int nBgnd)
	{
		final int nLen = anPixels.length;
		final double dMBgnd = linearGrey(nBgnd) / 255d;

		for (int n = 0; n < nLen; n++)
		{
			final int nA = ((anPixels[n] >>> 24) & 0xff);
			anPixels[n] = (anPixels[n] & 0x00ffffff) | (((int)(Math.round(dMBgnd * nA)) << 24) & 0xff000000);
		}
	}

	/**
	 * Applies a greyscale mask to a given image. Where full white means opaque and full black
	 * means transparent. Values between black and white result in translucent pixels. Colors in the
	 * mask will be converted into greyscale. Alpha (opacity) values in the mask or in the background
	 * will be ignored.
	 *
	 * @param image The source {@link Image} object to apply the mask to.
	 * @param colorBgnd An optional background {@link Color} object for smaller sized masks.
	 * If it is null then RGB white (#ffffff) is used.
	 * @param imageMask The mask {@link Image} object. Preferably in black and white or greyscale.
	 * @return The masked {@link Image} object.
	 */
	public static Image imageApplyMask(final Image image, Color colorBgnd, final Image imageMask)
	{
		final Bitmap32 bitmap = bitmapFromImage(image),
				bitmapMask = imageMask != null ? bitmapFromImage(imageMask) : null;

		final int nWidth = bitmap.getWidth(),
				nHeight = bitmap.getHeight(),
				nBgnd = colorBgnd != null ? getIntARGBColor(colorBgnd) : 0xffffff;

		final int[] anPixels = bitmap.getPixels();

		if (bitmapMask != null)
		{
			final int nMaskWidth = bitmapMask.getWidth(),
					nMaskHeight = bitmapMask.getHeight();

			final int[] anMask = bitmapMask.getPixels();

			if (nMaskWidth == nWidth && nMaskHeight == nHeight)
			{
				mask(anPixels, nBgnd, anMask);
			}
			else
			{
				mask(anPixels, nWidth, nHeight,
						anMask, (nWidth - nMaskWidth) / 2, (nHeight - nMaskHeight) / 2, nMaskWidth, nMaskHeight,
						nBgnd);
			}
		}
		else
		{
			mask(anPixels, nBgnd);
		}

		WritableImage imageResult = new WritableImage(nWidth, nHeight);
		PixelWriter pxwriter = imageResult.getPixelWriter();
		pxwriter.setPixels(0, 0, nWidth, nHeight,
				PixelFormat.getIntArgbInstance(), anPixels, 0, nWidth);

		return imageResult;
	}

	/**
	 * Creates a plain colored {@link WritableImage} of the given dimensions.
	 *
	 * @param dWidth The desired width.
	 * @param dHeight The desired height.
	 * @param colorBgnd The background color to use. If it is null then the default Color.BLACK
	 * is used by the system.
	 * @return A {@link WritableImage} object of the wanted size.
	 */
	public static WritableImage createPlainImage(double dWidth, double dHeight, Color colorBgnd)
	{
		final Canvas canvas = new Canvas(dWidth, dHeight);
		final GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.setFill(colorBgnd);
		gc.fillRect(0, 0, dWidth, dHeight);

		return canvas.snapshot(null, null);
	}

	/**
	 * Creates an {@link Image} object from
	 * the given {@link Bitmap32} object.
	 *
	 * @param bitmap The source bitmap object.
	 * @return An {@link Image} object of the same size.
	 */
	public static Image imageFromBitmap(Bitmap32 bitmap)
	{
		final int nWidth = bitmap.getWidth(),
			nHeight = bitmap.getHeight();

		WritableImage image = new WritableImage(nWidth, nHeight);
		PixelWriter pxwriter = image.getPixelWriter();
		pxwriter.setPixels(0, 0, nWidth, nHeight,
				PixelFormat.getIntArgbInstance(), bitmap.getPixels(), 0, nWidth);

		return image;
	}

	/**
	 * Creates a {@link Bitmap32} object from
	 * a given {@link Image} object.
	 *
	 * @param image The source image.
	 * @return A {@link Bitmap32} object of the same size.
	 */
	public static Bitmap32 bitmapFromImage(Image image)
	{
		final int nWidth = (int)image.getWidth(),
			nHeight = (int)image.getHeight(),
			an[] = new int[nWidth * nHeight];

		PixelReader pxreader = image.getPixelReader();
		pxreader.getPixels(0, 0, nWidth, nHeight, PixelFormat.getIntArgbInstance(), an, 0, nWidth);

		return new Bitmap32(an, nWidth, nHeight);
	}

	/**
	 * Creates a {@link Bitmap32} object from
	 * a given {@link Image} object and crops it centered to
	 * the given dimensions.
	 *
	 * @param image The source {@link Image} object.
	 * @param nNewWidth The desired new width.
	 * @param nNewHeight The desired new height.
	 * @return A {@link Bitmap32} object with new dimensions.
	 */
	public static Bitmap32 bitmapFromImage(Image image, int nNewWidth, int nNewHeight)
	{
		final int nWidth = (int)image.getWidth(),
			nHeight = (int)image.getHeight(),
			an[] = new int[nNewWidth * nNewHeight];

		if (nWidth != nNewWidth || nHeight != nNewHeight)
		{
			image = imageCropCentered(image, nNewWidth, nNewHeight);
		}

		PixelReader pxreader = image.getPixelReader();
		pxreader.getPixels(0, 0, nNewWidth, nNewHeight, PixelFormat.getIntArgbInstance(), an, 0, nNewWidth);

		return new Bitmap32(an, nNewWidth, nNewHeight);
	}
}
