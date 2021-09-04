package de.unlixx.runpng.util;

/**
 * Immutable version number (major.minor)
 *
 * @author Hans-Josef Unland
 */

/**
 * Immutable version number (major.minor)
 *
 * @author H. Unland (https://github.com/HUnland)
 *
   <!--
   Copyright 2021 H. Unland (https://github.com/HUnland)<br>
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
public class Version
{
	final int m_nMajor;
	final int m_nMinor;

	/**
	 * Constructor of a Version object.
	 *
	 * @param nMajor An int containing the major value.
	 * @param nMinor An int containing the minor value.
	 */
	public Version(int nMajor, int nMinor)
	{
		m_nMajor = nMajor;
		m_nMinor = nMinor;
	}

	/**
	 * Gets the major number.
	 *
	 * @return The major number as an int.
	 */
	public int getMajor()
	{
		return m_nMajor;
	}

	/**
	 * Gets the minor number.
	 *
	 * @return The minor number as an int.
	 */
	public int getMinor()
	{
		return m_nMajor;
	}

	/**
	 * Compares this Version object with another one.
	 *
	 * @param version A Version object to compare with this object.
	 * @return -1 if this is below, 0 if this is equal and 1 if this is above
	 * the given version object.
	 */
	public int compareTo(Version version)
	{
		if (version != null)
		{
			if (m_nMajor != version.m_nMajor)
			{
				return m_nMajor < version.m_nMajor ? -1 : 1;
			}

			if (m_nMinor != version.m_nMinor)
			{
				return m_nMinor < version.m_nMinor ? -1 : 1;
			}

			return 0;
		}

		return 1;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Version)
		{
			return ((Version)o).m_nMajor == m_nMajor && ((Version)o).m_nMinor == m_nMinor;
		}

		return false;
	}

	/**
	 * Gets a Version object from a string.
	 *
	 * @param str A string containing a version number (major.minor).
	 * @return An instance of Version.
	 */
	public static Version valueOf(String str)
	{
		int nMajor = -1,
			nMinor = 0;

		if (str != null)
		{
			int nPos = str.indexOf('.');
			if (nPos > 0)
			{
				nMajor = Util.toInt(str.substring(0, nPos), -1);
				nMinor = Util.toInt(str.substring(nPos + 1), 0);
			}
		}

		return new Version(nMajor, nMinor);
	}

	/**
	 * Checks whether this Version has a valid content.
	 *
	 * @return true if this Version object is valid.
	 */
	public boolean isValid()
	{
		return m_nMajor >= 0;
	}

	@Override
	public String toString()
	{
		return "" + m_nMajor + "." + m_nMinor;
	}
}
