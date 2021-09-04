package de.unlixx.runpng.scene;

import de.unlixx.runpng.util.Util;
import javafx.geometry.Orientation;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Handy little spacer class to keep things in GUI appart.
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
public class Spacer extends Pane
{
	/**
	 * Constructor for symmetrical standard spacing.
	 */
	public Spacer()
	{
		this(Util.SPACING, Util.SPACING);
	}

	/**
	 * Constructor for symmetrical spacing.
	 *
	 * @param dSize A double containing the horizontal and vertical spacing.
	 */
	public Spacer(double dSize)
	{
		this(dSize, dSize);
	}

	/**
	 * Constructor for asymmetrical spacing.
	 *
	 * @param dWidth A double containing the horizontal spacing.
	 * @param dHeight A double containing the vertical spacing.
	 */
	public Spacer(double dWidth, double dHeight)
	{
		setPrefSize(dWidth, dHeight);
	}

	/**
	 * Constructor for spring spacing. Either horizontal or vertical.
	 * It takes up all the remaining space that is available in the selected orientation.
	 *
	 * @param orientation An {@link Orientation} enumeration type.
	 */
	public Spacer(Orientation orientation)
	{
		switch (orientation)
		{
		case HORIZONTAL: HBox.setHgrow(this, Priority.SOMETIMES); break;
		case VERTICAL: VBox.setVgrow(this, Priority.SOMETIMES); break;
		default: // In case of null. TODO: Test it.
			HBox.setHgrow(this, Priority.SOMETIMES);
			VBox.setVgrow(this, Priority.SOMETIMES);
			break;
		}
	}
}
