package de.unlixx.runpng.scene.settings;

import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.event.ValueEvent;
import de.unlixx.runpng.util.undo.Undoable;
import de.unlixx.runpng.util.undo.UndoableIntegerSpinner;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Implementation of a FrameSettingsPane as part of the tabbed side bar.
 * This pane allows the setting of a frame specific delay time either
 * as a fraction or as milliseconds.
 * Additionally the settings can be spread to all frames of the image
 * sequence by user click.
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
public class FrameSettingsPane extends AbstractSettingsPane
{
	protected final Tab m_tab;

	protected HBox m_hboxFraction;
	protected HBox m_hboxMillis;

	protected int m_nFrameIndex;

	protected Spinner<Integer> m_spinnerNumerator;
	protected Spinner<Integer> m_spinnerDenominator;
	protected Spinner<Integer> m_spinnerMillis;

	protected Button m_buttonSpreadDelay;

	protected PngDelayFraction m_fractionDelay = new PngDelayFraction(0, 100);

	protected EventHandler<ActionEvent> m_handlerOnDelayFraction;

	/**
	 * Constructor of this FrameSettingsPane.
	 *
	 * @param tab The {@link Tab} which this pane is content of.
	 */
	public FrameSettingsPane(Tab tab)
	{
		m_tab = tab;

		Label label;

		m_bAdjusting = true;

		//setGridLinesVisible(true);

		BorderStroke stroke = new BorderStroke(Color.TRANSPARENT,  BorderStrokeStyle.SOLID, null, new BorderWidths(Util.SPACING));
		setBorder(new Border(stroke));

		label = createSectionLabel("label.delaystiming");
		add(label, 0, 0, 2, 1);
		setValignment(label, VPos.BASELINE);

		label = createRowLabel("label.fraction");
		add(label, 0, 2, 1, 1);
		setValignment(label, VPos.CENTER);

		m_hboxFraction = new HBox(6);
		m_hboxFraction.setAlignment(Pos.BASELINE_LEFT);

		m_spinnerNumerator = createIntegerSpinner(1, 0, 65535, 1, Pos.CENTER_RIGHT,
				"tooltip.fraction.numerator", action -> handleFieldNumerator(action));
		m_spinnerNumerator.valueProperty().addListener((obs, nOld, nNew) -> handleFractionValueChange(obs, nOld, nNew));
		m_hboxFraction.getChildren().add(m_spinnerNumerator);

		label = new Label("/");
		m_hboxFraction.getChildren().add(label);

		m_spinnerDenominator = createIntegerSpinner(10, 1, 1000, 10, Pos.CENTER_RIGHT,
				"tooltip.fraction.denominator", action -> handleFieldDenominator(action));
		m_spinnerDenominator.valueProperty().addListener((obs, nOld, nNew) -> handleFractionValueChange(obs, nOld, nNew));
		m_hboxFraction.getChildren().add(m_spinnerDenominator);

		label = new Label("s");
		m_hboxFraction.getChildren().add(label);

		add(m_hboxFraction, 1, 2, 3, 1);
		setValignment(m_hboxFraction, VPos.CENTER);

		label = createRowLabel("label.milliseconds");
		add(label, 0, 3, 1, 1);
		setValignment(label, VPos.CENTER);

		m_hboxMillis = new HBox(6);
		m_hboxMillis.setAlignment(Pos.BASELINE_LEFT);

		m_spinnerMillis = createIntegerSpinner(100, 0, 65535000, 10, Pos.CENTER_RIGHT,
				"tooltip.fraction.millis", action -> handleFieldMillis(action));
		m_spinnerMillis.valueProperty().addListener((obs, nOld, nNew) -> handleFractionValueChange(obs, nOld, nNew));
		m_hboxMillis.getChildren().add(m_spinnerMillis);

		label = new Label("ms");
		m_hboxMillis.getChildren().add(label);

		add(m_hboxMillis, 1, 3, 3, 1);
		setValignment(m_hboxMillis, VPos.CENTER);

		m_buttonSpreadDelay = createTextButton("label.spreaddelaytoallframes", "tooltip.fraction.spreaddelaytoallframes", null);
		add(m_buttonSpreadDelay, 0, 5, 2, 1);

		Loc.addChangeListener((localeOld, localeNew) -> updateTitle());

		m_bAdjusting = false;
	}

	/**
	 * Updates the tab title in case of frame selection change or a locale change.
	 */
	protected void updateTitle()
	{
		if (m_tab != null)
		{
			String strId = m_tab.getId(),
					strTitle = Loc.getString(strId) + " #" + m_nFrameIndex;
			m_tab.setText(strTitle);
		}
	}

	/**
	 * Sets the index of the currently selected frame.
	 *
	 * @param nIndex An int containing the index.
	 */
	public void setFrameIndex(int nIndex)
	{
		m_nFrameIndex = nIndex;
		updateTitle();
	}

	/**
	 * Sets an event handler for a user change of one of the delay fields.
	 *
	 * @param handler An {@link EventHandler}.
	 */
	public void setOnDelayFraction(EventHandler<ActionEvent> handler)
	{
		m_handlerOnDelayFraction = handler;
	}

	/**
	 * Passes a change of one of the delay fields to the registered handler.
	 *
	 * @param undoable An {@link Undoable}
	 * which the handler must add to it's own undoables collection.
	 */
	protected void handleDelayHandler(Undoable<?> undoable)
	{
		if (isUserAction())
		{
			if (m_handlerOnDelayFraction != null)
			{
				ActionEvent event = new ActionEvent(undoable, ActionEvent.NULL_SOURCE_TARGET);
				m_handlerOnDelayFraction.handle(event);
			}
		}
	}

	/**
	 * Handles a change of one of the delay fields.
	 *
	 * @param obs An {@link Observable} object.
	 * @param nOld The old value.
	 * @param nNew The new value.
	 */
	protected void handleFractionValueChange(Observable obs, int nOld, int nNew)
	{
		if (!m_bAdjusting)
		{
			m_bAdjusting = true;

			PngDelayFraction fractionTemp = m_fractionDelay.clone();

			if (m_spinnerNumerator.valueProperty() == obs)
			{
				fractionTemp.setDelayNum(nNew);
				m_spinnerDenominator.getValueFactory().setValue(fractionTemp.getDelayDen());
				m_spinnerMillis.getValueFactory().setValue(fractionTemp.getDelayMillis());
			}
			else if (m_spinnerDenominator.valueProperty() == obs)
			{
				fractionTemp.setDelayDen(nNew);
				m_spinnerNumerator.getValueFactory().setValue(fractionTemp.getDelayNum());
				m_spinnerMillis.getValueFactory().setValue(fractionTemp.getDelayMillis());
			}
			else if (m_spinnerMillis.valueProperty() == obs)
			{
				fractionTemp.setMilliseconds(nNew);
				m_spinnerNumerator.getValueFactory().setValue(fractionTemp.getDelayNum());
				m_spinnerDenominator.getValueFactory().setValue(fractionTemp.getDelayDen());
			}

			m_bAdjusting = false;
		}
	}

	/**
	 * Handles a user change of the numerator spinner.
	 *
	 * @param value An object of type {@link ValueEvent}.
	 */
	protected void handleFieldNumerator(ValueEvent<Integer> value)
	{
		if (isUserAction())
		{
			int nNumeratorNew = value.getValue(),
				nNumeratorOld = m_fractionDelay.getDelayNum();
			if (nNumeratorNew != nNumeratorOld)
			{
				m_fractionDelay.setDelayNum(nNumeratorNew);
				handleDelayHandler(new UndoableIntegerSpinner(m_spinnerNumerator, nNumeratorOld, nNumeratorNew, "label.numerator"));
			}
		}
	}

	/**
	 * Handles a user change of the denominator spinner.
	 *
	 * @param value An object of type {@link ValueEvent}.
	 */
	protected void handleFieldDenominator(ValueEvent<Integer> value)
	{
		if (isUserAction())
		{
			int nDenominatorNew = value.getValue(),
				nDenominatorOld = m_fractionDelay.getDelayDen();
			if (nDenominatorNew != nDenominatorOld)
			{
				m_fractionDelay.setDelayDen(nDenominatorNew);
				handleDelayHandler(new UndoableIntegerSpinner(m_spinnerDenominator, nDenominatorOld, nDenominatorNew, "label.denominator"));
			}
		}
	}

	/**
	 * Handles a user change of the milliseconds spinner.
	 *
	 * @param value An object of type {@link ValueEvent}.
	 */
	protected void handleFieldMillis(ValueEvent<Integer> value)
	{
		if (isUserAction())
		{
			int nMillisNew = value.getValue(),
				nMillisOld = m_fractionDelay.getDelayMillis();
			if (nMillisNew != nMillisOld)
			{
				m_fractionDelay.setMilliseconds(nMillisNew);
				handleDelayHandler(new UndoableIntegerSpinner(m_spinnerMillis, nMillisOld, nMillisNew, "label.milliseconds"));
			}
		}
	}

	/**
	 * Sets an event handler for a button click at the spread delay button.
	 *
	 * @param handler An {@link EventHandler}.
	 */
	public void setOnButtonSpreadDelay(EventHandler<ActionEvent> handler)
	{
		m_buttonSpreadDelay.setOnAction(handler);
	}

	/**
	 * Sets the given PngDelayFraction to this pane.
	 *
	 * @param fraction An object of type {@link PngDelayFraction}.
	 */
	public void setDelayFraction(PngDelayFraction fraction)
	{
		m_bAdjusting = true;

		m_spinnerNumerator.getValueFactory().setValue(fraction.getDelayNum());
		m_spinnerDenominator.getValueFactory().setValue(fraction.getDelayDen());
		m_spinnerMillis.getValueFactory().setValue(fraction.getDelayMillis());

		m_fractionDelay.setDelayNum(fraction.getDelayNum());
		m_fractionDelay.setDelayDen(fraction.getDelayDen());

		m_bAdjusting = false;
	}

	/**
	 * Creates and returns a new PngDelayFraction from the current settings.
	 *
	 * @return An object of type {@link PngDelayFraction}.
	 */
	public PngDelayFraction getDelayFraction()
	{
		return new PngDelayFraction(m_spinnerNumerator.getValue(), m_spinnerDenominator.getValue());
	}

	@Override
	public void reset()
	{
		setDelayFraction(new PngDelayFraction(0, 100));
	}

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		// Nothing to do here at the moment
	}

	@Override
	public void collectProject(PngProject project)
	{
		// Nothing to do here at the moment
	}

	@Override
	public void applyProject(PngProject project)
	{
		// Nothing to do here at the moment
	}
}
