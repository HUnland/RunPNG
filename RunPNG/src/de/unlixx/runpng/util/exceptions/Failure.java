package de.unlixx.runpng.util.exceptions;

import de.unlixx.runpng.util.Loc;

/**
 * Common Failure based on RuntimeException.
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
public class Failure extends RuntimeException
{
	/**
	 * Constructor for this Failure object.
	 *
	 * @param strMessageId A message id connected to the language resources.
	 * @param args Variable arguments list with data for the message.
	 */
	public Failure(String strMessageId, Object... args)
	{
		super(Loc.getString(strMessageId, args));
	}
}
