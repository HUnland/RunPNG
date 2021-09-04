package de.unlixx.runpng.scene.settings.effects;

import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.util.ImageUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Implementation of a foreground effect for the main image sequence.
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
public class ForegroundPane extends AbstractEffectPane
{
	/**
	 * Constructor for this effect pane.
	 *
	 * @param handlerEffectAction An {@link EventHandler}
	 * to inform about user actions in this pane.
	 */
	public ForegroundPane(EventHandler<ActionEvent> handlerEffectAction)
	{
		super(handlerEffectAction);

		setId("effect.foreground");

		//setGridLinesVisible(true);

		m_bAdjusting = true;

		Label label;

		label = createRowLabel("label.foreground.color");
		add(label, 0, 0, 1, 1);

		m_pickerPlain = createColorPicker(Color.TRANSPARENT, "tooltip.foreground.color", action -> handleColorChange());
		add(m_pickerPlain, 1, 0, 3, 1);

		label = createRowLabel("label.opacity");
		add(label, 0, 1, 1, 1);

		m_sliderOpacity = createSlider(0, 100, 0, "tooltip.slider.opacity", event -> handleOpacityChange(event));
		add(m_sliderOpacity, 1, 1, 2, 1);

		m_labelSlider = new Label("0%");
		add(m_labelSlider, 3, 1, 1, 1);

		m_thumbnailPane = new ThumbnailPane("foreground", this);
		add(m_thumbnailPane, 0, 4, 5, 1);

		m_checkSkipFirst = createCheckBox("label.skipfirst", "tooltip.effect.skipfirst", action -> handleSkipFirst());
		add(m_checkSkipFirst, 0, 5, 2, 1);

		m_bAdjusting = false;
	}

	@Override
	public Image applyEffect(int nFrame, Image image)
	{
		if ((m_pickerPlain.getValue().getOpacity() == 0 && m_thumbnailPane.getViewImage() == null) || (nFrame == 0 && m_bSkipFirst))
		{
			return image;
		}

		return ImageUtil.imageApplyForeground(image, m_pickerPlain.getValue(), m_thumbnailPane.getScaledViewImage());
	}

	@Override
	public void collectProject(PngProject project)
	{
		if (m_thumbnailPane.getViewImage() != null
			|| m_thumbnailPane.getBackgroundColor().getOpacity() > 0)
		{
			collectProject(project, getId());
		}
	}

	@Override
	public void applyProject(PngProject project)
	{
		applyProject(project, getId());
	}
}
