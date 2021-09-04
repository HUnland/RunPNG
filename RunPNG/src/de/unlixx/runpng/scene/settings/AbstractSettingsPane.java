package de.unlixx.runpng.scene.settings;

import de.unlixx.runpng.App;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.SliderEvent;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.PositiveIntegerFilter;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.event.ValueEvent;
import de.unlixx.runpng.util.undo.UndoEvent;
import de.unlixx.runpng.util.undo.UndoListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**
 * AbstractSettingsPane derives from GridPane with some
 * handy generic methods.
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
public abstract class AbstractSettingsPane extends GridPane implements UndoListener
{
	public enum ScaleApplied
	{
		NONE,
		SYM,
		BOTH,
		HORZ,
		VERT
	}

	protected boolean m_bAdjusting;

	/**
	 * Constructor of this AbstractSettingsPane.
	 */
	protected AbstractSettingsPane()
	{
		getApp().addUndoListener(this);

		setHgap(Util.SPACING);
		setVgap(Util.SPACING);
	}

	/**
	 * Gets the main application.
	 *
	 * @return An {@link App} object.
	 */
	protected static App getApp()
	{
		return App.getMainApp();
	}

	/**
	 * Gets the current application width.
	 *
	 * @return An int containing the application width.
	 */
	protected static int getAppWidth()
	{
		return getApp().getImageWidth();
	}

	/**
	 * Gets the current application height.
	 *
	 * @return An int containing the application height.
	 */
	protected static int getAppHeight()
	{
		return getApp().getImageHeight();
	}

	/**
	 * Check if the current action comes from the user or the software.
	 *
	 * @return True if the action comes from the user.
	 */
	protected boolean isUserAction()
	{
		return !m_bAdjusting && !getApp().isUndoManagerActive();
	}

	/**
	 * Creates a common row label.
	 *
	 * @param strId A string with a localizable label id.
	 * @return A {@link Label} object.
	 */
	protected Label createRowLabel(String strId)
	{
		Label label = new Label();
		label.setId(strId);
		Loc.addIdTextObject(label);

		return label;
	}

	/**
	 * Creates a section label with a bold title font.
	 *
	 * @param strId A string with a localizable label id.
	 * @return A {@link Label} object.
	 */
	protected Label createSectionLabel(String strId)
	{
		Label label = new Label();
		label.setId(strId);
		label.setFont(Util.DEFAULT_TITLE_FONT);
		setValignment(label, VPos.BASELINE);
		Loc.addIdTextObject(label);

		return label;
	}

	/**
	 * Creates a RadioButton object and adds it to a toggle group.
	 *
	 * @param strId A string with a localizable label id.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param toggles A {@link ToggleGroup} object.
	 * @param objData An optional object to be set in the user data.
	 * @return A {@link RadioButton} object.
	 */
	protected RadioButton createRadioButton(String strId, String strTooltipId, ToggleGroup toggles, Object objData)
	{
		RadioButton radio = new RadioButton();
		radio.setId(strId);
		radio.setUserData(objData);
		radio.setTooltip(Util.createTooltip(strTooltipId));
		radio.setToggleGroup(toggles);
		Loc.addIdTextObject(radio);

		return radio;
	}

	/**
	 * Creates a CheckBox object.
	 *
	 * @param strId A string with a localizable label id.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param handler A {@link EventHandler} object to handle changes of the CheckBox.
	 * @return A {@link CheckBox} object.
	 */
	protected CheckBox createCheckBox(String strId, String strTooltipId, EventHandler<ActionEvent> handler)
	{
		CheckBox check = new CheckBox();
		check.setId(strId);
		check.setTooltip(Util.createTooltip(strTooltipId));
		setValignment(check, VPos.CENTER);


		if (handler != null)
		{
			check.setOnAction(handler);
		}

		Loc.addIdTextObject(check);

		return check;
	}

	/**
	 * Creates a TextField which allows only positive integer values.
	 *
	 * @param nDefault An int containing the start value.
	 * @param nPrefColumns The number of preferred columns.
	 * @param nMin An int containing the minimum value.
	 * @param nMax An int containing the maximum value.
	 * @param alignment A {@link Pos} enum type to position the number in the field.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param handler An {@link EventHandler} for handling user actions.
	 * @return A {@link TextField} object.
	 */
	protected TextField createPositiveIntegerTextField(int nDefault, int nPrefColumns, int nMin, int nMax,
			Pos alignment, String strTooltipId, EventHandler<ActionEvent> handler)
	{
		final TextField field = new TextField();
		field.setPrefColumnCount(nPrefColumns);
		field.setText("" + nDefault);
		field.setAlignment(alignment);
		field.setOnKeyPressed(event -> { if (handler != null && event.getCode() == KeyCode.ENTER) handler.handle(new ActionEvent(field, field)); });
		field.textProperty().addListener(new PositiveIntegerFilter(field, nMin, nMax));
		field.focusedProperty().addListener((obs, bOld, bNew) ->
		{
			if (bNew)
			{
				Platform.runLater(field::selectAll);
			}
			else if (handler != null)
			{
				handler.handle(new ActionEvent(field, field));
			}
		});

		field.setTooltip(Util.createTooltip(strTooltipId));

		return field;
	}

	/**
	 * Creates a Slider object.
	 *
	 * @param dMin A double containing the minimum value.
	 * @param dMax A double containing the maximum value.
	 * @param dValue A double containing the initial value.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param handler An {@link EventHandler} for handling user actions. In that case
	 * a special {@link SliderEvent} will be sent to the handler.
	 * @return A {@link Slider} object.
	 */
	protected Slider createSlider(double dMin, double dMax, double dValue, String strTooltipId, EventHandler<SliderEvent> handler)
	{
		Slider slider = new Slider(dMin, dMax, dValue);
		slider.setTooltip(Util.createTooltip(strTooltipId));
		slider.setOnMouseDragged(event -> { if (handler != null) handler.handle(new SliderEvent(slider, slider.getValue(), false)); });
		slider.setOnMouseReleased(event -> { if (handler != null) handler.handle(new SliderEvent(slider, slider.getValue(), true)); });
		slider.setOnKeyReleased(event -> { if (handler != null) handler.handle(new SliderEvent(slider, slider.getValue(), true)); });
		return slider;
	}

	/**
	 * Creates an element of type Spinner for positive integer values.
	 *
	 * @param nDefault An int containing the start value.
	 * @param nMin An int containing the minimum value.
	 * @param nMax An int containing the maximum value.
	 * @param nStepWidth The step width to increase or decrease the value if the user clicks an arrow button.
	 * @param alignment A {@link Pos} enum type to position the number in the field.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param handler An {@link EventHandler} for handling user actions.
	 * @return A {@link Spinner} object.
	 */
	protected Spinner<Integer> createIntegerSpinner(int nDefault, int nMin, int nMax, int nStepWidth,
			Pos alignment, String strTooltipId, EventHandler<ValueEvent<Integer>> handler)
	{
		Spinner<Integer> spinner = new Spinner<Integer>(nMin, nMax, nDefault, nStepWidth);
		spinner.setEditable(true);

		final TextField field = spinner.getEditor();
		field.setAlignment(alignment);
		//field.textProperty().addListener(new PositiveIntegerFilter(spinner.editorProperty().get(), nMin, nMax));
		field.setPrefColumnCount((int)Math.log10(nMax) + 2);

		field.focusedProperty().addListener((obs, bOld, bNew) ->
		{
			if (bNew)
			{
				Platform.runLater(field::selectAll);
			}
			else
			{
				spinner.getValueFactory().setValue(Util.toInt(field.getText(), 0));

				if (handler != null)
				{
					handler.handle(new ValueEvent<Integer>(spinner.getValue()));
				}
			}
		});

		field.setOnKeyPressed(event ->
		{
			if (event.getCode() == KeyCode.ENTER)
			{
				spinner.getValueFactory().setValue(Util.toInt(field.getText(), 0));

				if (handler != null)
				{
					handler.handle(new ValueEvent<Integer>(spinner.getValue()));
				}
			}
		});

		Tooltip tooltip = Util.createTooltip(strTooltipId);
		spinner.setTooltip(tooltip);
		field.setTooltip(tooltip);

		return spinner;
	}

	/**
	 * Creates a Button with just a text label.
	 *
	 * @param strId A string with a localizable label id.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param handler An {@link EventHandler} for handling user actions.
	 * @return A {@link Button} object.
	 */
	protected Button createTextButton(String strId, String strTooltipId, EventHandler<ActionEvent> handler)
	{
		Button button = new Button();
		button.setId(strId);
		button.setTooltip(Util.createTooltip(strTooltipId));
		Loc.addIdTextObject(button);

		if (handler != null)
		{
			button.setOnAction(handler);
		}

		return button;
	}

	/**
	 * Creates a Button with an image from the app resurces.
	 *
	 * @param strId A string with an id. Just to identify.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param strImagePath The image path in the resources (e. g. icons/32x32/file.open.png).
	 * @param handler An {@link EventHandler} for handling user actions.
	 * @return A {@link Button} object.
	 */
	protected Button createImageButton(String strId, String strTooltipId, String strImagePath, EventHandler<ActionEvent> handler)
	{
		Button button = getApp().getMenuTool().createImageButton(strId, strTooltipId, strImagePath);

		if (handler != null)
		{
			button.setOnAction(handler);
		}

		return button;
	}

	/**
	 * Creates a ToggleButton with an image from the app resurces.
	 *
	 * @param strId A string with an id. Just to identify.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param strImagePath The image path in the resources directory (e. g. icons/32x32/dir.up.png).
	 * @param handler An {@link EventHandler} for handling user actions.
	 * @return A {@link ToggleButton} object.
	 */
	protected ToggleButton createImageToggleButton(String strId, String strTooltipId, String strImagePath, EventHandler<ActionEvent> handler)
	{
		ToggleButton button = getApp().getMenuTool().createImageToggleButton(strId, strTooltipId, strImagePath);

		if (handler != null)
		{
			button.setOnAction(handler);
		}

		return button;
	}

	/**
	 * Creates a ColorPicker element.
	 *
	 * @param color A {@link Color} object with the initial color.
	 * @param strTooltipId A string with a localizable tooltip id.
	 * @param handler An {@link EventHandler} for handling user actions.
	 * @return A {@link ColorPicker} object.
	 */
	protected ColorPicker createColorPicker(Color color, String strTooltipId, EventHandler<ActionEvent> handler)
	{
		ColorPicker picker = new ColorPicker(color);
		picker.setTooltip(Util.createTooltip(strTooltipId));

		if (handler != null)
		{
			picker.setOnAction(handler);
		}

		return picker;
	}

	/**
	 * This method will called in case the settings pane has to set it's values back to default.
	 * E. g. when the user clicks on file new.
	 */
	public abstract void reset();

	/**
	 * This method will be called to inform the settings pane that the application image size
	 * has been changed.
	 *
	 * @param nAppWidth An int containing the new width.
	 * @param nAppHeight An int containing the new height.
	 */
	public abstract void appSizeChanged(int nAppWidth, int nAppHeight);

	/**
	 * Called before a project file is written to collect all settings from the panes.
	 *
	 * @param project An object of type {@link PngProject}.
	 */
	public abstract void collectProject(PngProject project);

	/**
	 * Called after a project file has been opened to apply the data to the settings pane.
	 *
	 * @param project An object of type {@link PngProject}.
	 */
	public abstract void applyProject(PngProject project);

	@Override
	public void undoableAdded(UndoEvent event)
	{
	}

	@Override
	public void undoableUndone(UndoEvent event)
	{
	}

	@Override
	public void undoableRedone(UndoEvent event)
	{
	}
}
