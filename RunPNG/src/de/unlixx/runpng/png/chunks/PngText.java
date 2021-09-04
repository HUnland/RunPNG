package de.unlixx.runpng.png.chunks;

import de.unlixx.runpng.png.PngConstants;

/**
 * Container for a png text chunk (tEXt, zTXt, iTXt). These chunks are starting always with
 * a keyword of length 1 - 79. All other textual members may contain text or may be null.
 * In case of compression: Only the text itself shall be compressed. All other textual members
 * must remain uncompressed.
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
 * @see <a href="https://www.w3.org/TR/PNG/#11tEXt">https://www.w3.org/TR/PNG/#11tEXt</a>
 * @see <a href="https://www.w3.org/TR/PNG/#11zTXt">https://www.w3.org/TR/PNG/#11zTXt</a>
 * @see <a href="https://www.w3.org/TR/PNG/#11iTXt">https://www.w3.org/TR/PNG/#11iTXt</a>
 * @see <a href="http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html">http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html</a>
 */
public class PngText
{
	final int m_nChunkType;
	final String m_strKeyword;
	final int m_nCompressionFlag;
	final int m_nCompressionMethod;
	final String m_strLanguageTag;
	final String m_strTranslatedKeyword;
	final String m_strText;

	/**
	 * Constructor for chunk type tEXt. Where keyword and text must have character set ISO 8859-1.
	 *
	 * @param strKeyword A string of length 1 - 79. This must not be null.
	 * @param strText A string containing a text. In case of tEXt it is limited to 32688 (max. chunk length - 80).
	 */
	public PngText(String strKeyword, String strText)
	{
		this(PngConstants.tEXt, strKeyword, 0, 0, null, null, strText);
	}

	/**
	 * Constructor for chunk type zTXt. Where keyword and text must have character set ISO 8859-1.
	 *
	 * @param strKeyword A string of length 1 - 79. This must not be null.
	 * @param nCompressionMethod An int containing the compression method. Actually only compression
	 * method 0 (deflate/inflate compression) is internationally standardized for png.
	 * @param strText A string containing a text. A limitation is not specified, but the length
	 * of the compressed text should not exceed 32687 (max. chunk length - 81).
	 */
	public PngText(String strKeyword, int nCompressionMethod, String strText)
	{
		this(PngConstants.zTXt, strKeyword, 1, nCompressionMethod, null, null, strText);
	}

	/**
	 * Constructor for chunk type iTXt. Where keyword and language tag must have character set ISO 8859-1,
	 * while the translated keyword and the text must be coded in character set UTF-8.
	 *
	 * @param strKeyword A string of length 1 - 79. This must not be null.
	 * @param nCompressionFlag An int containing 0 (uncompressed) or 1 (compressed).
	 * @param nCompressionMethod An int containing the compression method. Actually only compression
	 * method 0 (deflate/inflate compression) is internationally standardized for png.
	 * @param strLanguageTag A string containing a language code according RFC 3066. Or null, if not specified.
	 * @param strTranslatedKeyword The language specific translated keyword. Or null, if not specified.
	 * @param strText A string containing a text. A limitation is not specified, but the overall length of
	 * the whole chunk (compressed or not) should not exceed 32768 bytes (max. chunk length).
	 *
	 * @see <a href="https://www.ietf.org/rfc/rfc3066.txt">https://www.ietf.org/rfc/rfc3066.txt</a>
	 */
	public PngText(String strKeyword, int nCompressionFlag, int nCompressionMethod, String strLanguageTag, String strTranslatedKeyword, String strText)
	{
		this(PngConstants.iTXt, strKeyword, nCompressionFlag, nCompressionMethod, strLanguageTag, strTranslatedKeyword, strText);
	}

	/**
	 * Constructor for chunk type tEXt, zTXt, iTXt.
	 *
	 * @param nChunkType An int containing one of PngConstants.tEXt, PngConstants.zTXt or PngConstants.iTXt.
	 * @param strKeyword A string of length 1 - 79. This must not be null.
	 * @param nCompressionFlag An int containing 0 (uncompressed) or 1 (compressed).
	 * @param nCompressionMethod An int containing the compression method. Actually only compression
	 * method 0 (deflate/inflate compression) is internationally standardized for png.
	 * @param strLanguageTag A string containing a language code according RFC 3066. Or null, if not specified.
	 * @param strTranslatedKeyword The language specific translated keyword. Or null, if not specified.
	 * @param strText A string containing a text. In case of tEXt it is limited to 32688 (max. chunk length - 80).
	 * In other cases the overall length of the whole chunk (compressed or not) should not exceed 32768 bytes (max. chunk length).
	 *
	 * @see <a href="https://www.ietf.org/rfc/rfc3066.txt">https://www.ietf.org/rfc/rfc3066.txt</a>
	 */
	PngText(int nChunkType, String strKeyword, int nCompressionFlag, int nCompressionMethod, String strLanguageTag, String strTranslatedKeyword, String strText)
	{
		m_nChunkType = nChunkType;
		m_strKeyword = strKeyword;
		m_nCompressionFlag = nCompressionFlag;
		m_nCompressionMethod = nCompressionMethod;
		m_strLanguageTag = strLanguageTag;
		m_strTranslatedKeyword = strTranslatedKeyword;
		m_strText = strText;
	}

	/**
	 * Gets the chunk type.
	 *
	 * @return An int containing one of PngConstants.tEXt, PngConstants.zTXt or PngConstants.iTXt.
	 */
	public int getChunkType()
	{
		return m_nChunkType;
	}

	/**
	 * Gets the keyword of this text chunk.
	 *
	 * @return A string of length 1 - 79. This must not be null.
	 */
	public String getKeyword()
	{
		return m_strKeyword;
	}

	/**
	 * Gets the compression flag.
	 *
	 * @return An int containing 0 (uncompressed) or 1 (compressed).
	 */
	public int getCompressionFlag()
	{
		return m_nCompressionFlag;
	}

	/**
	 * Gets the compression method.
	 *
	 * @return An int containing the compression method. Actually only compression
	 * method 0 (deflate/inflate compression) is internationally standardized for png.
	 */
	public int getCompressionMethod()
	{
		return m_nCompressionMethod;
	}

	/**
	 * Gets the language tag of this text chunk.
	 *
	 * @return A string containing a language code according RFC 3066. Or null, if not specified.
	 *
	 * @see <a href="https://www.ietf.org/rfc/rfc3066.txt">https://www.ietf.org/rfc/rfc3066.txt</a>
	 */
	public String getLanguageTag()
	{
		return m_strLanguageTag;
	}

	/**
	 * Gets the translated keyword of this text chunk.
	 *
	 * @return The language specific translated keyword. Or null, if not specified.
	 */
	public String getTranslatedKeyword()
	{
		return m_strTranslatedKeyword;
	}

	/**
	 * Gets the text content of this text chunk.
	 *
	 * @return A string containing the text. Or null, if not specified.
	 */
	public String getText()
	{
		return m_strText;
	}
}
