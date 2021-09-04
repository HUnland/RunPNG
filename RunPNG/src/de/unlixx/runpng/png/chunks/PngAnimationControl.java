package de.unlixx.runpng.png.chunks;

/**
 * Implements the animation control chunk (acTL). This chunk can appear 0 or 1 time in a png file.
 * If it exists then it must appear before the first IDAT chunk. And if so then the png file is at least
 * potentially animated. If none exists then we can consider the png file as not animated with just
 * one bitmap.
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
 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>
 */
public class PngAnimationControl
{
	int m_nNumFrames;
	int m_nNumPlays;

	/**
	 * Constructor for PngAnimationControl. It contains just two int values.
	 * The num_frames value contains the number of frames (bitmaps). Which means
	 * the number of animated frames, but the png file may contain one more bitmap
	 * at the begin which is not animated.
	 * The second value is num_plays. This depicts how many loops the software shall play.
	 * If it is 0 then it must repeat infinitely.
	 *
	 * @param nNumFrames An int which receives the num_frames value.
	 * @param nNumPlays An int which receives the num_plays value.
	 */
	public PngAnimationControl(int nNumFrames, int nNumPlays)
	{
		m_nNumFrames = nNumFrames;
		m_nNumPlays = nNumPlays;
	}

	/**
	 * Checks whether the png shall loop infinitely.
	 *
	 * @return True if the png shall loop infinitely.
	 */
	public boolean loopsInfinitely()
	{
		return 0 == m_nNumPlays;
	}

	/**
	 * Sets another num_plays value.
	 *
	 * @param bNumPlays An int with the new num_plays value.
	 */
	public void setNumPlays(int bNumPlays)
	{
		m_nNumPlays = bNumPlays;
	}

	/**
	 * Gets the num_plays value.
	 *
	 * @return An int with the num_plays value.
	 */
	public int getNumPlays()
	{
		return m_nNumPlays;
	}

	/**
	 * Sets another num_frames value.
	 *
	 * @param nNumFrames An int with the new num_frames value.
	 */
	public void setNumFrames(int nNumFrames)
	{
		m_nNumFrames = nNumFrames;
	}

	/**
	 * Gets the num_frames value.
	 *
	 * @return An int with the num_frames value.
	 */
	public int getNumFrames()
	{
		return m_nNumFrames;
	}
}
