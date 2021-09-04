package de.unlixx.runpng.util;

import java.util.Locale;

import de.unlixx.runpng.App;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

/**
 * Specialized version of {@link ListCell} to show
 * the chosen locale in a combo box.
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
public class LocaleListCell extends ListCell<Locale>
{
	@Override
	protected void updateItem(Locale locale, boolean bEmpty)
	{
		super.updateItem(locale, bEmpty);

		setText(null);
		setGraphic(null);

		if (locale != null)
		{
			setText(locale.getDisplayLanguage(locale));

			ImageView view = new ImageView(App.RESOURCE_PATH + "icons/64x32/lang." + locale.getLanguage() + ".png");
			view.setFitHeight(MenuTool.MENU_ICONSIZE);
			view.setPreserveRatio(true);
			setGraphic(view);
		}
	}
}
