package de.unlixx.runpng.scene.settings.effects;

import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.SliderEvent;
import de.unlixx.runpng.util.ImageUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Implementation of a mask effect for the main image sequence.
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
public class MaskPane extends AbstractEffectPane
{
	/**
	 * Constructor for this effect pane.
	 *
	 * @param handlerEffectAction An {@link EventHandler}
	 * to inform about user actions in this pane.
	 */
	public MaskPane(EventHandler<ActionEvent> handlerEffectAction)
	{
		super(handlerEffectAction);

		setId("effect.mask");

		//setGridLinesVisible(true);

		m_bAdjusting = true;

		Label label;

		label = createRowLabel("label.opacity");
		add(label, 0, 0, 1, 1);

		m_sliderOpacity = createSlider(0, 100, 0, "tooltip.slider.opacity", event -> handleOpacityChange(event));
		add(m_sliderOpacity, 1, 0, 2, 1);

		m_labelSlider = new Label("100%");
		add(m_labelSlider, 3, 0, 1, 1);

		m_thumbnailPane = new ThumbnailPane("mask", this);
		add(m_thumbnailPane, 0, 1, 5, 1);

		m_checkSkipFirst = createCheckBox("label.skipfirst", "tooltip.effect.skipfirst", action -> handleSkipFirst());
		add(m_checkSkipFirst, 0, 2, 2, 1);

		reset();

		m_bAdjusting = false;
	}

	@Override
	protected void setBackgroundColor(Color color)
	{
		int nARGB = ImageUtil.getIntARGBColor(color),
				nGrey = ImageUtil.linearGrey(nARGB);

		m_color = m_colorNew = ImageUtil.getColor(0xff, nGrey, nGrey, nGrey);

		double dVal = nGrey / 2.55d;
		m_sliderOpacity.setValue(dVal);
		m_labelSlider.setText("" + Math.round(dVal) + "%");

		m_thumbnailPane.setBackgroundColor(m_color);
	}

	@Override
	protected double percentOpacity(Color color)
	{
		return color.getRed() * 100d; // It is grey, so it can be any color. But not the alpha part.
	}

	@Override
	protected void handleOpacityChange(SliderEvent event)
	{
		long lVal = Math.round(event.getValue());
		m_labelSlider.setText("" + lVal + "%");

		m_colorNew = new Color(lVal / 100d, lVal / 100d, lVal / 100d, 1d);

		m_thumbnailPane.setBackgroundColor(m_colorNew);

		if (event.getCommit())
		{
			handleApply("action.opacitychange");
		}
	}

	@Override
	public void reset()
	{
		super.reset();
		setBackgroundColor(Color.WHITE);
	}

	@Override
	public Image applyEffect(int nFrame, Image image)
	{
		if (!(nFrame == 0 && m_bSkipFirst))
		{
			Image imageMask = m_thumbnailPane.getScaledViewImage();
			Color color = m_thumbnailPane.getBackgroundColor();

			if (imageMask != null || !Color.WHITE.equals(color))
			{
				return ImageUtil.imageApplyMask(image, color, imageMask);
			}
		}

		return image;
	}

	@Override
	public void collectProject(PngProject project)
	{
		if (m_thumbnailPane.getViewImage() != null
			|| !Color.WHITE.equals(m_thumbnailPane.getBackgroundColor()))
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
