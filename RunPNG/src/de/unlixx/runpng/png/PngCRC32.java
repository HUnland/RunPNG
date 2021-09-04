package de.unlixx.runpng.png;

import java.util.zip.CRC32;

/**
 * Convenience extension of the {@link CRC32} checksum calculator.
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
public class PngCRC32 extends CRC32
{
	/**
	 * Updates the checksum with the four bytes of an int. MSB first.
	 *
	 * @param n The int to use for update.
	 */
	public void updateInt(int n)
	{
		update((n >>> 24) & 0xff);
		update((n >> 16) & 0xff);
		update((n >> 8) & 0xff);
		update(n & 0xff);
	}

	/**
	 * Updates the checksum with the two bytes oa a short. MSB first.
	 *
	 * @param n The int to use for update. Only the last 16 bits are used.
	 */
	public void updateShort(int n)
	{
		update((n >> 8) & 0xff);
		update(n & 0xff);
	}
}
