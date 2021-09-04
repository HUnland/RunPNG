package de.unlixx.runpng.png;

/**
 * Three possible types of animation. These values are not officially
 * specified and not stored in a png file. Rather than they are either
 * detected while reading a file or decided by the user.
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
public enum PngAnimationType
{
	NONE,
	ANIMATED,
	SKIPFIRST
}
