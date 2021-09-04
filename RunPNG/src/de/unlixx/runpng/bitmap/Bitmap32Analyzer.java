package de.unlixx.runpng.bitmap;

import java.util.HashMap;
import java.util.Map;

import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.chunks.PngPalette;
import de.unlixx.runpng.util.ARGB;
import de.unlixx.runpng.util.Loc;

/**
 * Analyzer for {@link Bitmap32}
 * and {@link Bitmap32Sequence}.
 * It counts colors by their kind and collects distinct colors. Additionally it
 * creates a suggestion object for color type.
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
public class Bitmap32Analyzer
{
	long m_lGrey;
	long m_lNonGrey;
	long m_lTransparent;
	long m_lTransparentNonZero;
	long m_lTranslucent;
	long m_lOpaque;
	long m_lTotal;

	HashMap<Integer, ARGB> m_mapColors = new HashMap<>();

	/**
	 * Constructor for this Bitmap32Analyzer
	 */
	public Bitmap32Analyzer()
	{
	}

	/**
	 * Starts analysis of a single Bitmap32.
	 *
	 * @param bitmap A {@link Bitmap32} object.
	 */
	public void analyze(Bitmap32 bitmap)
	{
		reset();

		_analyze(bitmap);
	}

	/**
	 * Internal analysis method.
	 *
	 * @param bitmap A {@link Bitmap32} object.
	 */
	void _analyze(Bitmap32 bitmap)
	{
		int[] anPixels = bitmap.getPixels();

		for (int nARGB : anPixels)
		{
			final int nA = (nARGB >>> 24) & 0xff,
					nR = (nARGB >>> 16) & 0xff,
					nG = (nARGB >>> 8) & 0xff,
					nB = nARGB & 0xff;

			switch (nA)
			{
			case 0: m_lTransparent++; break;
			case 0xff: m_lOpaque++; break;
			default: m_lTranslucent++; break;
			}

			if (nA == 0)
			{
				if (nARGB != 0)
				{
					m_lTransparentNonZero++;
				}
			}
			else
			{
				if (nR == nG && nG == nB)
				{
					m_lGrey++;
				}
				else
				{
					m_lNonGrey++;
				}
			}

			ARGB argb = m_mapColors.get(nARGB);
			if (argb != null)
			{
				argb.incFrequency();
			}
			else
			{
				m_mapColors.put(nARGB, new ARGB(nARGB, 1));
			}

			m_lTotal++;
		}
	}

	/**
	 * Analyzes a complete Bitmap32Sequence in a loop.
	 *
	 * @param sequence A {@link Bitmap32Sequence} object.
	 */
	public void analyze(Bitmap32Sequence sequence)
	{
		reset();

		Bitmap32[] aBitmaps = sequence.getBitmaps();

		for (Bitmap32 bitmap : aBitmaps)
		{
			_analyze(bitmap);
		}

		// TODO: remove
		boolean bPrint = false;
		if (bPrint)
		{
			printColors();
		}
	}

	// TODO: remove
	public void printColors()
	{
		System.out.println("\nBitmap32Analyzer Color List length = " + m_mapColors.size());
		System.out.println("============================================================");

		int n = 0;
		for (int nARGB : m_mapColors.keySet())
		{
			int nA = ((nARGB >>> 24) & 0xff),
				nR = ((nARGB >>> 16) & 0xff),
				nG = ((nARGB >>> 8) & 0xff),
				nB = (nARGB & 0xff),
				nFreq = m_mapColors.get(nARGB).getFrequency();

			System.out.println(String.format("idx: % 5d, alpha: %02x, red: %02x, green: %02x, blue: %02x, freq: %d", n, nA, nR, nG, nB, nFreq));
			n++;
		}
	}

	/**
	 * Gets the count of detected grey pixels.
	 *
	 * @return A long integer containing the count of grey pixels.
	 */
	public long getGreyCount()
	{
		return m_lGrey;
	}

	/**
	 * Gets the count of detected non grey pixels.
	 *
	 * @return A long integer containing the count of non grey pixels.
	 */
	public long getNonGreyCount()
	{
		return m_lNonGrey;
	}

	/**
	 * Gets the count of somewhat bogus transparent pixels. This means
	 * pixels with alpha = 0 but RGB != 0.
	 *
	 * @return A long integer containing the count of transparent but colored pixels.
	 */
	public long getTransparentNonZeroCount()
	{
		return m_lTransparentNonZero;
	}

	/**
	 * Gets the count of detected fully transparent pixels.
	 *
	 * @return A long integer containing the count of fully transparent pixels.
	 */
	public long getTransparentCount()
	{
		return m_lTransparent;
	}

	/**
	 * Gets the count of detected translucent pixels.
	 *
	 * @return A long integer containing the count of translucent pixels.
	 */
	public long getTranslucentCount()
	{
		return m_lTranslucent;
	}

	/**
	 * Gets the total count of pixels.
	 *
	 * @return A long integer containing the total count of pixels.
	 */
	public long getTotalCount()
	{
		return m_lTotal;
	}

	/**
	 * Gets the count of detected fully opaque pixels.
	 *
	 * @return A long integer containing the count of fully opaque pixels.
	 */
	public long getOpaqueCount()
	{
		return m_lOpaque;
	}

	/**
	 * Gets the count of detected distinct colors.
	 *
	 * @return An int containing the count of distinct colors.
	 */
	public int getDistinctColorCount()
	{
		return m_mapColors.size();
	}

	/**
	 * Gets the map of distinct colors.
	 *
	 * @return A map of distinct colors as the keys and {@link ARGB} objects as values.
	 */
	public Map<Integer, ARGB> getDistinctColorMap()
	{
		return new HashMap<Integer, ARGB>(m_mapColors);
	}

	/**
	 * Gets an unsorted array of ARGB distinct colors.
	 *
	 * @return An array of {@link ARGB} objects.
	 */
	public ARGB[] getDistinctColors()
	{
		ARGB[] aARGB = new ARGB[m_mapColors.size()];
		return m_mapColors.values().toArray(aARGB);
	}

	/**
	 * Resets this Bitmap32Analyzer for reuse.
	 */
	protected void reset()
	{
		m_lGrey = 0;
		m_lNonGrey = 0;
		m_lTransparent = 0;
		m_lTransparentNonZero = 0;
		m_lTranslucent = 0;
		m_lOpaque = 0;
		m_lTotal = 0;

		m_mapColors.clear();
	}

	/**
	 * Creates a localized analysis text to show to the user.
	 *
	 * @return A string containing the analysis text.
	 */
	public String getAnalysisText()
	{
		StringBuffer sb = new StringBuffer(
				Loc.getString("message.analyze.summary",
				m_lNonGrey,
				m_lGrey,
				m_lTranslucent,
				m_lTransparent,
				m_lTotal,
				m_mapColors.size()))
				.append("\n")
				.append(Loc.getString("message.analyze.wouldsaveas")).append(" ");

		Suggestion suggestion = getSuggestion();
		PngColorType colorType = suggestion.getColorType();
		switch (colorType)
		{
		case GREYSCALE: sb.append(Loc.getString("message.analyze.greyscale")); break;
		case TRUECOLOR: sb.append(Loc.getString("message.analyze.truecolor")); break;
		case INDEXED: sb.append(Loc.getString("message.analyze.indexedcolor")); break;
		case GREYSCALE_ALPHA: sb.append(Loc.getString("message.analyze.greyscalealpha")); break;
		case TRUECOLOR_ALPHA: sb.append(Loc.getString("message.analyze.truecoloralpha")); break;
		}

		sb.append(" ");

		if (colorType == PngColorType.INDEXED &&
				(m_lTransparent > 0 || m_lTranslucent > 0))
		{
			sb.append(Loc.getString("message.analyze.with.trnstable"));
		}
		else if (suggestion.needstRNS())
		{
			int ntRNS = suggestion.gettRNSColor();
			if (ntRNS >= 0)
			{
				sb.append(Loc.getString("message.analyze.with.trnssample", ntRNS));
			}
		}

		sb.append(".\n\n");

		return sb.toString();
	}

	/**
	 * Creates a Suggestion object.
	 *
	 * @return A Suggestion object.
	 */
	public Suggestion getSuggestion()
	{
		return new Suggestion();
	}

	/**
	 * This is an object with a suggestion how to save the
	 * analyzed bitmap or sequence.
	 */
	public class Suggestion
	{
		int m_nBitDepth;
		PngPalette m_palette;
		boolean m_btRNS;
		int m_ntRNSColor = -1;
		PngColorType m_colorType;

		/**
		 * Constructor of this Suggestion object.
		 * The suggestion will be decided herein.
		 */
		Suggestion()
		{
			m_nBitDepth = 8; // TODO: Guess what

			int nSize = m_mapColors.size();
			if (nSize > 0 && nSize <= 256)
			{
				int n = 0, anPalette[] = new int[nSize];
				for (int nARGB : m_mapColors.keySet())
				{
					anPalette[n++] = nARGB;
				}

				m_palette = new PngPalette(anPalette);
				m_colorType = PngColorType.INDEXED;
				m_btRNS = m_lTransparent > 0 || m_lTranslucent > 0;
			}
			else
			{
				if (m_lGrey > 0 && m_lNonGrey == 0)
				{
					if (m_lTransparent == 0 && m_lTranslucent == 0)
					{
						m_colorType = PngColorType.GREYSCALE;
					}
					else if (m_lTransparent > 0 && m_lTranslucent == 0)
					{
						m_colorType = PngColorType.GREYSCALE;
						m_btRNS = true;
					}
					else
					{
						m_colorType = PngColorType.GREYSCALE_ALPHA;
					}
				}
				else
				{
					if (m_lTransparent == 0 && m_lTranslucent == 0)
					{
						m_colorType = PngColorType.TRUECOLOR;
					}
					else if (m_lTransparent > 0 && m_lTranslucent == 0)
					{
						m_colorType = PngColorType.TRUECOLOR;
						m_btRNS = true;
					}
					else
					{
						m_colorType = PngColorType.TRUECOLOR_ALPHA;
					}
				}
			}

			if (m_btRNS && (m_colorType == PngColorType.GREYSCALE || m_colorType == PngColorType.TRUECOLOR))
			{
				for (int n = 0; n < 0x00ffffff; n++)
				{
					if (!m_mapColors.containsKey(n))
					{
						m_ntRNSColor = n;
						break;
					}
				}
			}
		}

		/**
		 * Gets the suggested bit depth.
		 *
		 * @return An int containing the suggested bit depth.
		 */
		public int getBitDepth()
		{
			return m_nBitDepth;
		}

		/**
		 * Gets the suggested palette in case of indexed color type.
		 *
		 * @return A suggested {@link PngPalette} object.
		 */
		public PngPalette getPalette()
		{
			return m_palette;
		}

		/**
		 * Tells whether the bitmap or sequence needs a tRNS chunk to save.
		 *
		 * @return True, if the bitmap or sequence needs a tRNS chunk to save.
		 */
		public boolean needstRNS()
		{
			return m_btRNS;
		}

		/**
		 * Gets the suggested tRNS color sample.
		 *
		 * @return An int containing the suggested tRNS color sample.
		 * Or -1 if not applicable.
		 */
		public int gettRNSColor()
		{
			return m_ntRNSColor;
		}

		/**
		 * Gets the suggested color type.
		 *
		 * @return A {@link PngColorType} enum type.
		 */
		public PngColorType getColorType()
		{
			return m_colorType;
		}
	}
}
