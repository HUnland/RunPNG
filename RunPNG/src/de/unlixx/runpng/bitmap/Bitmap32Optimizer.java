package de.unlixx.runpng.bitmap;

import java.util.Arrays;
import java.util.HashMap;

import de.unlixx.runpng.bitmap.Bitmap32Analyzer.Suggestion;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.png.chunks.PngHeader;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.png.chunks.PngTransparency;
import de.unlixx.runpng.util.ARGB;
import de.unlixx.runpng.util.ImageUtil;

/**
 * Statically used Bitmap32Optimizer to create and to reconstruct delta frames. In case of optimize
 * this class creates delta frames with reduced bitmap information. In case of deoptimze this class
 * reconstructs the previously reduced frames.
 * In addition this optimizer has methods to reduce color range for palette use.
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
public class Bitmap32Optimizer
{
	/**
	 * Private constructor to prevent errenous instantiation.
	 */
	private Bitmap32Optimizer() {}

	/**
	 * This method starts the optimization of consecutive frames.
	 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>
	 *
	 * @param sequence A {@link Bitmap32Sequence} object.
	 */
	public static void optimize(Bitmap32Sequence sequence)
	{
		// For testing purposes
		//sequence.setOptimized(true);

		if (sequence.isOptimized())
		{
			return;
		}

		if (sequence.getAnimationType() != PngAnimationType.NONE && sequence.getFramesCount() > 0)
		{
			Bitmap32 bitmapRef = sequence.getFrame(0).clone();

			for (int nFrame = 0, nFrames = sequence.getFramesCount(); nFrame < nFrames; nFrame++)
			{
				Bitmap32 bitmapDiff = sequence.getFrame(nFrame);

				/*
				if (nFrame > 0)
				{
					new CompareBitmapsDialog(nFrame, bitmapRef, bitmapDiff, bitmapDiff.getFrameControl()).showAndWait();
				}
				*/

				Bitmap32 bitmapNext = nFrame < nFrames - 1 ? sequence.getFrame(nFrame + 1) : null,
						bitmapOpt = optimize(nFrame == 0, bitmapRef, bitmapDiff, bitmapNext);

				if (bitmapOpt != null)
				{
					sequence.replaceFrame(nFrame, bitmapOpt);
				}
				else if (nFrame > 0)
				{
					// bitmapOpt == null indicates no changes to the previous frame
					Bitmap32 bitmapPrev = sequence.getFrame(nFrame - 1);
					PngFrameControl fcTLPrev = bitmapPrev.getFrameControl(),
							fcTLDiff = bitmapDiff.getFrameControl();
					PngDelayFraction fractionPrev = fcTLPrev.getDelayFraction(),
							fractionDiff = fcTLDiff.getDelayFraction();
					fractionPrev.setMilliseconds(fractionPrev.getDelayMillis() + fractionDiff.getDelayMillis());
					sequence.removeFrame(nFrame--);
					nFrames--;
				}
			}
		}

		sequence.setOptimized(true);
	}

	/**
	 * Internally used method to detect the delta between reference, difference and possible next frame.
	 *
	 * @param bFirst True, if it is the first frame (reference frame).
	 * @param bitmapRef A Bitmap32 as reference frame.
	 * @param bitmapDiff A Bitmap32 as difference frame.
	 * @param bitmapNext The next Bitmap32 in the sequence for dispose op prediction.
	 * @return An optimized Bitmap32 with probably reduced size and reduced content.
	 */
	static Bitmap32 optimize(boolean bFirst, Bitmap32 bitmapRef, Bitmap32 bitmapDiff, Bitmap32 bitmapNext)
	{
		final int anRef[] = bitmapRef.getPixels(),
			anDiff[] = bitmapDiff.getPixels(),
			anNext[] = bitmapNext != null ? bitmapNext.getPixels() : null,
			nWidth = bitmapRef.getWidth(),
			nHeight = bitmapRef.getHeight();

		//System.out.println("Rect of change: x = " + nOptX1 + ", y = " + nOptY1 + ", width = " + nOptWidth + ", height = " + nOptHeight);

		if (!bFirst)
		{
			final DiffRect rc = calcDiffRect(anRef, anDiff, nWidth, nHeight);
			if (rc.nW == 0 || rc.nH == 0)
			{
				return null;
			}

			final int nBlendOp = (rc.nI > 0 && rc.nL == 0) ? PngFrameControl.BLEND_OP_OVER : PngFrameControl.BLEND_OP_SOURCE,
				anOpt[] = new int[rc.nW * rc.nH];

			for (int nY = 0; nY < rc.nH; nY++)
			{
				for (int nX = 0; nX < rc.nW; nX++)
				{
					final int nIdxDest = nY * rc.nW + nX,
						nIdxSrc = (nY + rc.nY) * nWidth + nX + rc.nX;

					anOpt[nIdxDest] = anDiff[nIdxSrc];

					// TODO: Review. This should work. Or shouldn't it?
					/*
					if (nBlendOp == PngFrameControl.BLEND_OP_SOURCE)
					{
						anOpt[nIdxDest] = anDiff[nIdxSrc];
					}
					else
					{
						anOpt[nIdxDest] = anDiff[nIdxSrc] != anRef[nIdxSrc] ? anDiff[nIdxSrc] : 0;
					}
					*/
				}
			}

			int nDisposeOp;

			if (anNext != null)
			{
				// Find the dispose op by smallest rectangle needed for the next frame

				// Calc previous op
				DiffRect rcPrev = calcDiffRect(anRef, anNext, nWidth, nHeight);
				int nPrevSq = rcPrev.nW * rcPrev.nH;

				nDisposeOp = PngFrameControl.DISPOSE_OP_PREVIOUS;

				// Calc with background op
				int[] anBkgnd = applyRect(anRef, nWidth, nHeight, null, rc.nX, rc.nY, rc.nW, rc.nH, nBlendOp);
				DiffRect rcBkgnd = calcDiffRect(anRef, anNext, nWidth, nHeight);
				int nBkgndSq = rcBkgnd.nW * rcBkgnd.nH;

				if (nBkgndSq < nPrevSq)
				{
					nDisposeOp = PngFrameControl.DISPOSE_OP_BACKGROUND;
				}

				// Calc with optimized diff
				applyRect(anRef, nWidth, nHeight, anOpt, rc.nX, rc.nY, rc.nW, rc.nH, nBlendOp);
				DiffRect rcNone = calcDiffRect(anRef, anNext, nWidth, nHeight);
				int nNoneSq = rcNone.nW * rcNone.nH;

				if (nNoneSq < Math.min(nPrevSq, nBkgndSq))
				{
					nDisposeOp = PngFrameControl.DISPOSE_OP_NONE;
				}
				else if (nDisposeOp == PngFrameControl.DISPOSE_OP_PREVIOUS)
				{
					applyRect(anRef, nWidth, nHeight, anBkgnd, rc.nX, rc.nY, rc.nW, rc.nH, nBlendOp);
				}
				else if (nDisposeOp == PngFrameControl.DISPOSE_OP_BACKGROUND)
				{
					applyRect(anRef, nWidth, nHeight, null, rc.nX, rc.nY, rc.nW, rc.nH, nBlendOp);
				}
			}
			else
			{
				nDisposeOp = PngFrameControl.DISPOSE_OP_NONE;
			}

			PngFrameControl fcTLDiff = bitmapDiff.getFrameControl(),
					fcTLOpt = new PngFrameControl(rc.nW, rc.nH, rc.nX, rc.nY,
												fcTLDiff.getDelayNum(), fcTLDiff.getDelayDen(),
												nDisposeOp, nBlendOp);

			return new Bitmap32(fcTLOpt, anOpt);
		}
		else // if (bitmapNext == null)
		{
			bitmapDiff.getFrameControl().setDisposeOp(PngFrameControl.DISPOSE_OP_NONE);
		}

		return bitmapDiff;
	}

	/**
	 * Internally used method to try several dispose operations in order to get the
	 * smallest delta rectangle.
	 *
	 * @param anLower The lower pixel array.
	 * @param nLowerWidth The width of the lower pixel array.
	 * @param nLowerHeight The height of the lower pixel array.
	 * @param anUpper The upper pixel array. May be null in some cases.
	 * @param nUpperX The horizontal offset of the upper pixel array.
	 * @param nUpperY The vertical offset of the upper pixel array.
	 * @param nUpperWidth The width of the upper pixel array.
	 * @param nUpperHeight The height of the upper pixel array.
	 * @param nBlendOp Currently not used.
	 * @return A resulting pixel array in upper rectangle size and position.
	 */
	static int[] applyRect(final int[] anLower, final int nLowerWidth, final int nLowerHeight,
			final int[] anUpper, final int nUpperX, final int nUpperY, final int nUpperWidth, final int nUpperHeight,
			int nBlendOp)
	{
		final int anBkp[] = new int[nUpperWidth * nUpperHeight];

		for (int nY = 0; nY < nUpperHeight; nY++)
		{
			if (anUpper == null)
			{
				for (int nX = 0; nX < nUpperWidth; nX++)
				{
					final int nSrcIdx = nY * nUpperWidth + nX,
						nDestIdx = (nY + nUpperY) * nLowerWidth + nX + nUpperX;
					anBkp[nSrcIdx] = anLower[nDestIdx];
					anLower[nDestIdx] = 0;
				}
			}
			else
			{
				for (int nX = 0; nX < nUpperWidth; nX++)
				{
					final int nSrcIdx = nY * nUpperWidth + nX,
						nDestIdx = (nY + nUpperY) * nLowerWidth + nX + nUpperX;
					anBkp[nSrcIdx] = anLower[nDestIdx];
					anLower[nDestIdx] = anUpper[nSrcIdx];
				}
			}
		}

		return anBkp;
	}

	/**
	 * Little helper class to keep results from calculation.
	 */
	static class DiffRect
	{
		int nX, nY, nW, nH, nI, nL;
		// nI -> identical pixels inside of "drawn" rect
		// nL -> lower opaque pixels while the upper are not fully opaque
	}

	/**
	 * Internally used method to calculate a difference rectangle from actual reference
	 * frame (lower with diff applied) and the next frame (upper)
	 *
	 * @param anLower The lower pixel array.
	 * @param anUpper The upper pixel array.
	 * @param nWidth The initial width.
	 * @param nHeight The initial height.
	 * @return A DiffRect object with the results.
	 */
	static DiffRect calcDiffRect(final int[] anLower, final int[] anUpper, final int nWidth, final int nHeight)
	{
		int	nX1 = nWidth,
			nX2 = 0,
			nY1 = nHeight,
			nY2 = 0,
			nI = 0,
			nL = 0;

		for (int nY = 0; nY < nHeight; nY++)
		{
			if (anLower == null)
			{
				for (int nX = 0; nX < nWidth; nX++)
				{
					final int nIdx = nY * nWidth + nX,
						nPxU = anUpper[nIdx],
						nAU = (nPxU >>> 24) & 0xff;

					if (0 != nPxU)
					{
						nX1 = Math.min(nX1, nX);
						nX2 = Math.max(nX2, nX + 1);
						nY1 = Math.min(nY1, nY);
						nY2 = Math.max(nY2, nY + 1);
					}
					else if (nAU > 0)
					{
						nI++;
					}
				}
			}
			else
			{
				for (int nX = 0; nX < nWidth; nX++)
				{
					final int nIdx = nY * nWidth + nX,
						nPxL = anLower[nIdx],
						nPxU = anUpper[nIdx],
						nAL = (nPxL >>> 24) & 0xff,
						nAU = (nPxU >>> 24) & 0xff;

					if (nPxL != nPxU)
					{
						nX1 = Math.min(nX1, nX);
						nX2 = Math.max(nX2, nX + 1);
						nY1 = Math.min(nY1, nY);
						nY2 = Math.max(nY2, nY + 1);

						if (nAL > 0 && nAU < 0xff)
						{
							nL++;
						}
					}
					else if (nAU > 0)
					{
						nI++;
					}
				}
			}
		}

		final DiffRect rc = new DiffRect();
		rc.nW = Math.max(0, nX2 - nX1);
		rc.nH = Math.max(0, nY2 - nY1);
		// In case of empty, just to be clean
		rc.nX = rc.nW > 0 ? nX1 : 0;
		rc.nY = rc.nH > 0 ? nY1 : 0;
		rc.nI = nI;
		rc.nL = nL;

		return rc;
	}

	// TODO: Not really needed anymore.
	static double bufferDiffInPercent(final int[] anRef, final int[] anDiff)
	{
		if (anRef.length == anDiff.length)
		{
			double dDiff = 0;
			final int nLen = anRef.length;

			for (int n = 0; n < nLen; n++)
			{
				if (anRef[n] != anDiff[n])
				{
					dDiff++;
				}
			}

			return dDiff * 100 / nLen;
		}

		// Should not happen
		System.err.println("ARGB32Optimizer.bufferDiffInPercent: Non equal buffer size!");
		return 100;
	}

	/**
	 * Optimizes a Bitmap32Sequence for alternative color type if possible.
	 * This is an in-place operation and the given sequence should only be
	 * used for saving after.
	 *
	 * @param sequence A {@link Bitmap32Sequence} object.
	 * @return True, if the color type has been changed.
	 */
	public static boolean optimizeColorType(final Bitmap32Sequence sequence)
	{
		Bitmap32Analyzer analyzer = new Bitmap32Analyzer();
		analyzer.analyze(sequence);

		Suggestion suggestion = analyzer.getSuggestion();
		PngColorType colorType = suggestion.getColorType();

		PngHeader header = sequence.getHeader();

		if (colorType != header.getColorType())
		{
			int nBitDepth = suggestion.getBitDepth();
			PngPalette palette = suggestion.getPalette();
			int ntRNSColor = suggestion.gettRNSColor();
			PngTransparency tRNS;

			PngHeader headerOld = sequence.getHeader(),
					headerNew = new PngHeader(headerOld.getWidth(), headerOld.getHeight(), nBitDepth,
									colorType, headerOld.getCompressionMethod(), headerOld.getFilterMethod(), headerOld.getInterlaceMethod());
			sequence.setHeader(headerNew);

			switch (colorType)
			{
			case GREYSCALE:
			case TRUECOLOR: tRNS = ntRNSColor >= 0 ? new PngTransparency(colorType, nBitDepth, ntRNSColor) : null; break;
			case INDEXED: tRNS = palette.spawnTransparency(); break;
			default: tRNS = null; break;
			}

			sequence.setPalette(palette);
			sequence.setTransparency(tRNS);

			return true;
		}

		return false;
	}

	/**
	 * Optimizes a Bitmap32Sequence for color palette use. This is an in-place operation.
	 *
	 * @param sequence A {@link Bitmap32Sequence} object.
	 * @return True, if the bitmaps were changed. False, if there was no change needed.
	 */
	public static boolean optimizeForPalette(final Bitmap32Sequence sequence)
	{
		Bitmap32Analyzer analyzer = new Bitmap32Analyzer();
		analyzer.analyze(sequence);

		final int nMax = PngPalette.MAX_SIZE;

		if (analyzer.getDistinctColorCount() <= nMax)
		{
			return false;
		}

		System.out.println("Start");
		long lStart = System.currentTimeMillis();

		ARGB[] aARGB = analyzer.getDistinctColors();

		Bitmap32Quantizer quantizer = new Bitmap32Quantizer(aARGB);
		aARGB = quantizer.rollup(nMax);

		HashMap<Integer, Integer> mapReloc = new HashMap<Integer, Integer>();
		for (ARGB argb : aARGB)
		{
			int nReloc = argb.getRelocation();
			if (nReloc >= 0)
			{
				mapReloc.put(argb.argb, nReloc);
			}
		}

		Bitmap32[] aBitmaps = sequence.getBitmaps();

		for (Bitmap32 bitmap : aBitmaps)
		{
			int[] anARGB = bitmap.getPixels();
			for (int n = 0, nLen = anARGB.length; n < nLen; n++)
			{
				int nReloc = mapReloc.getOrDefault(anARGB[n], -1);
				if (nReloc >= 0)
				{
					anARGB[n] = aARGB[nReloc].argb;
				}
			}
		}

		System.out.println("optimizeForPalette: " + (System.currentTimeMillis() - lStart) + " ms");

		return true;
	}

	/**
	 * Deoptimizes an optimized Bitmap32Sequence in order to reconstruct
	 * the original pictures.
	 *
	 * @param sequence A {@link Bitmap32Sequence} object.
	 */
	public static void deoptimize(final Bitmap32Sequence sequence)
	{
		// For testing purposes
		//sequence.setOptimized(false);

		if (!sequence.isOptimized()
			|| sequence.getAnimationType() == PngAnimationType.NONE
			|| sequence.getFramesCount() == 0)
		{
			sequence.setOptimized(false);
			return;
		}

		Bitmap32 bitmapRef = sequence.getFrame(0).clone();

		for (int nFrame = 0, nFrames = sequence.getFramesCount(); nFrame < nFrames; nFrame++)
		{
			Bitmap32 bitmapDiff = sequence.getFrame(nFrame);
			Bitmap32 bitmapDeopt = apply(nFrame == 0, bitmapRef, bitmapDiff);

			sequence.replaceFrame(nFrame, bitmapDeopt);
		}

		sequence.setOptimized(false);
	}

	/**
	 * Internally used method to recreate the delta frames.
	 *
	 * @param bFirst True, if it is the first frame (reference frame).
	 * @param bitmapRef A Bitmap32 as reference frame.
	 * @param bitmapDiff A Bitmap32 as difference frame (delta).
	 * @return A deoptimized and reconstructed Bitmap32.
	 */
	static Bitmap32 apply(boolean bFirst, Bitmap32 bitmapRef, Bitmap32 bitmapDiff)
	{
		final PngFrameControl fcTLDiff = bitmapDiff.getFrameControl();
		final int anRef[] = bitmapRef.getPixels(),
			anDiff[] = bitmapDiff.getPixels(),
			nBlendOp = fcTLDiff.getBlendOp(),
			nOffsX = fcTLDiff.getXOffset(),
			nOffsY = fcTLDiff.getYOffset(),
			nWidth = fcTLDiff.getWidth(),
			nHeight = fcTLDiff.getHeight(),
			nScanlineStride = bitmapRef.m_nWidth;

		Bitmap32 bitmapResult;
		int anRefCopy[] = null;

		if (!bFirst)
		{
			if (fcTLDiff.getDisposeOp() == PngFrameControl.DISPOSE_OP_PREVIOUS)
			{
				// TODO: Smaller rectangle?
				anRefCopy = Arrays.copyOf(anRef, anRef.length);
			}

			switch (nBlendOp)
			{
			case PngFrameControl.BLEND_OP_SOURCE:
				for (int nY = 0; nY < nHeight; nY++)
				{
					for (int nX = 0; nX < nWidth; nX++)
					{
						final int nSrc = nY * nWidth + nX,
							nDest = (nY + nOffsY) * nScanlineStride + nX + nOffsX;

						anRef[nDest] = anDiff[nSrc];
					}
				}
				break;

			case PngFrameControl.BLEND_OP_OVER:
				for (int nY = 0; nY < nHeight; nY++)
				{
					for (int nX = 0; nX < nWidth; nX++)
					{
						final int nSrc = nY * nWidth + nX,
							nDest = (nY + nOffsY) * nScanlineStride + nX + nOffsX,
							nDiff = anDiff[nSrc],
							nDiffA = (nDiff >>> 24) & 0xff,
							nRef = anRef[nDest],
							nRefA = (nRef >>> 24) & 0xff;

						// Similar to https://www.w3.org/TR/PNG/#13Alpha-channel-processing , but without gamma for now
						if (nRefA == 0)
						{
							// Background pixel is fully transparent -> overwrite with whatever the diff pixel is.
							anRef[nDest] = nDiff;
						}
						else if (nDiffA > 0)
						{
							anRef[nDest] = ImageUtil.blendPixel(nRef, nDiff);
						}
					}
				}
				break;
			}

			bitmapResult = bitmapRef.clone();
			PngDelayFraction fractionResult = bitmapResult.getFrameControl().getDelayFraction();
			fractionResult.setDelayNum(fcTLDiff.getDelayNum());
			fractionResult.setDelayDen(fcTLDiff.getDelayDen());
		}
		else
		{
			bitmapResult = bitmapDiff.clone();
		}

		switch (fcTLDiff.getDisposeOp())
		{
		case PngFrameControl.DISPOSE_OP_NONE:
			// Do nothing
			break;

		case PngFrameControl.DISPOSE_OP_BACKGROUND:
			for (int nY = 0; nY < nHeight; nY++)
			{
				for (int nX = 0; nX < nWidth; nX++)
				{
					anRef[(nY + nOffsY) * nScanlineStride + nX + nOffsX] = 0;
				}
			}
			break;

		case PngFrameControl.DISPOSE_OP_PREVIOUS:
			if (bFirst)
			{
				Arrays.fill(anRef, 0);
			}
			else
			{
				System.arraycopy(anRefCopy, 0, anRef, 0, anRef.length);
			}
			break;
		}

		return bitmapResult;
	}
}
