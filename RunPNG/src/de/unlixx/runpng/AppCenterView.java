package de.unlixx.runpng;

import de.unlixx.runpng.scene.CenteringScrollPane;
import de.unlixx.runpng.scene.CheckerboardCanvas;
import de.unlixx.runpng.scene.ImageCanvas;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;

/**
 * Implementation of the application center view.
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
class AppCenterView extends CenteringScrollPane
{
	/**
	 * Package private constructor with an initial ImageCanvas.
	 * A {@link CheckerboardCanvas}
	 * will be implicitly set as the background.
	 *
	 * @param scrollee An object of type {@link ImageCanvas}.
	 */
	AppCenterView(ImageCanvas scrollee)
	{
		super(scrollee);

		setBackgroundNode(new CheckerboardCanvas());

		BorderStroke stroke = new BorderStroke(Color.LIGHTGREY,  BorderStrokeStyle.SOLID, null, new BorderWidths(0, 2, 0, 0));
		setBorder(new Border(stroke));
	}
}
