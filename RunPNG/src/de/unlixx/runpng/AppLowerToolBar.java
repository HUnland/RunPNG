package de.unlixx.runpng;

import java.util.Locale;

import de.unlixx.runpng.scene.FadeOutProgressBar;
import de.unlixx.runpng.scene.Spacer;
import de.unlixx.runpng.util.LabeledValue;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.LocaleListCell;
import de.unlixx.runpng.util.Util;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;

/**
 * Implementaion of the AppLowerToolBar. This contains actually a zoom combo box,
 * a progress bar and a language combo box.
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
class AppLowerToolBar extends ToolBar
{
	final App m_app = App.getMainApp();
	final ProgressBar m_progressbar;
	final ComboBox<LabeledValue<Integer>> m_comboZoom;
	final ComboBox<Locale> m_comboLang;

	final int[] m_anZooms = new int[]{ 500, 400, 300, 200, 150, 100, 75, 50, 25, 10 };

	/**
	 * Constructor of this AppLowerToolBar.
	 */
	AppLowerToolBar()
	{
		Label label;

		label = new Label();
		label.setId("label.zoom");
		Loc.addIdTextObject(label);
		label.setAlignment(Pos.CENTER);

		m_comboZoom = new ComboBox<>();
		m_comboZoom.setTooltip(Util.createTooltip("tooltip.zoom"));
		ObservableList<LabeledValue<Integer>> list = m_comboZoom.getItems();

		for (int n : m_anZooms)
		{
			list.add(new LabeledValue<Integer>("" + n + "%", n));
		}
		m_comboZoom.setValue(new LabeledValue<Integer>(null, 100));

		m_comboZoom.setOnAction(action ->
		{
			int nScale = m_comboZoom.getValue().getValue();
			m_app.getCenterView().setScrolleeScale(nScale / 100d);
		});

		getItems().addAll(label, m_comboZoom, new Spacer());

		label = new Label();
		label.setId("label.progress");
		Loc.addIdTextObject(label);
		label.setAlignment(Pos.CENTER_RIGHT);

		m_progressbar = new FadeOutProgressBar();
		m_progressbar.setTooltip(Util.createTooltip("tooltip.progressbar"));
		m_progressbar.setProgress(0);

		getItems().addAll(label, m_progressbar, new Spacer(Orientation.HORIZONTAL));

		label = new Label();
		label.setId("label.lang");
		Loc.addIdTextObject(label);
		label.setAlignment(Pos.CENTER_RIGHT);

		m_comboLang = new ComboBox<>();
		m_comboLang.setTooltip(Util.createTooltip("tooltip.lang"));
		m_comboLang.setCellFactory(cell -> new LocaleListCell());
		m_comboLang.setButtonCell(new LocaleListCell());

		ObservableList<Locale> listLang = m_comboLang.getItems();

		for (Locale locale : Loc.getLocalesList())
		{
			listLang.add(locale);
		}

		m_comboLang.setValue(Loc.getListedLocale(Locale.getDefault()));
		m_comboLang.setOnAction(action ->
		{
			Loc.localeChanged(m_comboLang.getValue());
			m_app.updateTitle();
			m_app.m_toolbarMain.validate(); // Tooltips
		});
		Loc.addChangeListener((localeOld, localeNew) -> m_comboLang.setValue(localeNew));

		getItems().addAll(label, m_comboLang);
	}

	/**
	 * External access to the zoom value.
	 *
	 * @param nZoom An int containing the zoom value. Which shall be one of
	 * 500, 400, 300, 200, 150, 100, 75, 50, 25, 10. Where 100 means 100%.
	 */
	public void setZoom(int nZoom)
	{
		m_comboZoom.setValue(new LabeledValue<Integer>(null, nZoom));
		m_app.getCenterView().setScrolleeScale(nZoom / 100d);
	}
}
