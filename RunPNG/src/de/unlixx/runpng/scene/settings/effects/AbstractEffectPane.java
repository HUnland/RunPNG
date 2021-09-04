package de.unlixx.runpng.scene.settings.effects;

import java.util.List;

import org.w3c.dom.Element;

import de.unlixx.runpng.bitmap.Bitmap32;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.SliderEvent;
import de.unlixx.runpng.scene.settings.AbstractSettingsPane;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.event.ApplyEvent;
import de.unlixx.runpng.util.undo.Undoable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Abstract base class for some effect panes.
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
public abstract class AbstractEffectPane extends AbstractSettingsPane implements EventHandler<ApplyEvent>
{
	protected ColorPicker m_pickerPlain;
	protected Slider m_sliderOpacity;
	protected Label m_labelSlider;

	protected Color m_color = Color.TRANSPARENT;
	protected Color m_colorNew = Color.TRANSPARENT;

	protected ThumbnailPane m_thumbnailPane;

	protected CheckBox m_checkSkipFirst;

	protected boolean m_bSkipFirst;
	protected boolean m_bSkipFirstNew;

	protected final EventHandler<ActionEvent> m_handlerEffectAction;

	/**
	 * Constructor for this abstract class.
	 *
	 * @param handlerEffectAction An {@link EventHandler}
	 * to inform about user actions in this pane.
	 */
	protected AbstractEffectPane(EventHandler<ActionEvent> handlerEffectAction)
	{
		m_handlerEffectAction = handlerEffectAction;
	}

	/**
	 * Invoked when a user clicks the skip first checkbox.
	 */
	protected void handleSkipFirst()
	{
		if (m_checkSkipFirst.isSelected() != m_bSkipFirst)
		{
			m_bSkipFirstNew = m_checkSkipFirst.isSelected();

			handleApply("action.effect.skipfirst");
		}
	}

	/**
	 * Sets the background color of the effect image, updates the color picker
	 * and the opacity slider according the given color.
	 *
	 * @param color A {@link Color} object.
	 */
	protected void setBackgroundColor(Color color)
	{
		m_color = m_colorNew = color;

		m_pickerPlain.setValue(color);
		double dOpacity = color.getOpacity() * 100d;
		m_sliderOpacity.setValue(dOpacity);
		m_labelSlider.setText("" + Math.round(dOpacity) + "%");

		m_thumbnailPane.setBackgroundColor(color);
	}

	/**
	 * Invoked when a user changes the color of the color picker.
	 */
	protected void handleColorChange()
	{
		m_colorNew = m_pickerPlain.getValue();
		double dOpacity = m_colorNew.getOpacity() * 100d;
		m_sliderOpacity.setValue(dOpacity);
		m_labelSlider.setText("" + Math.round(dOpacity) + "%");

		m_thumbnailPane.setBackgroundColor(m_colorNew);

		handleApply("action.colorchange");
	}

	/**
	 * Invoked when a user moves the opacity slider.
	 *
	 * @param event A {@link SliderEvent} object.
	 */
	protected void handleOpacityChange(SliderEvent event)
	{
		long lVal = Math.round(event.getValue());

		if (m_labelSlider != null)
		{
			m_labelSlider.setText("" + lVal + "%");
		}

		if (m_pickerPlain != null)
		{
			Color color = m_pickerPlain.getValue();
			m_colorNew = new Color(color.getRed(), color.getGreen(), color.getBlue(), lVal / 100d);
			m_pickerPlain.setValue(m_colorNew);

			m_thumbnailPane.setBackgroundColor(color);
		}

		if (event.getCommit())
		{
			handleApply("action.opacitychange");
		}
	}

	/**
	 * Calculates the opacity value in percent.
	 *
	 * @param color A {@link Color} object with the opacity value.
	 * @return The percentage as a double.
	 */
	protected double percentOpacity(Color color)
	{
		return color.getOpacity() * 100d;
	}

	/**
	 * Handles the apply of changes. Creates Undoables for undo and redo.
	 *
	 * @param strUndoId A string containing a localizable id for the undo
	 * and redo buttons and menu items.
	 */
	protected void handleApply(String strUndoId)
	{
		if (isUserAction())
		{
			final Color colorOld = m_color,
					colorNew = m_colorNew;

			final Image imageOld = m_thumbnailPane != null ? m_thumbnailPane.getViewImage() : null,
					imageNew = m_thumbnailPane != null ? m_thumbnailPane.getNewImage() : null;

			final ScaleApplied appliedOld = m_thumbnailPane != null ? m_thumbnailPane.getScaleApplied() : ScaleApplied.NONE,
					appliedNew = m_thumbnailPane != null ? m_thumbnailPane.getScaleAppliedNew() : ScaleApplied.NONE;

			final boolean bSkipFirstOld = m_bSkipFirst,
					bSkipFirstNew = m_bSkipFirstNew;

			if (!colorOld.equals(colorNew)
				|| (imageOld == null && imageNew != null)
				|| (imageOld != null && !imageOld.equals(imageNew))
				|| appliedOld != appliedNew
				|| bSkipFirstOld != bSkipFirstNew)
			{
				Undoable<AbstractEffectPane> undo = new Undoable<AbstractEffectPane>(this, "action." + getId(), strUndoId)
				{
					@Override
					public void undoAction()
					{
						m_color = colorOld;

						if (m_pickerPlain != null)
						{
							m_pickerPlain.setValue(colorOld);
						}

						if (m_sliderOpacity != null)
						{
							double dPercent = percentOpacity(colorOld);
							m_sliderOpacity.setValue(dPercent);

							if (m_labelSlider != null)
							{
								m_labelSlider.setText("" + Math.round(dPercent) + "%");
							}
						}

						if (m_thumbnailPane != null)
						{
							m_thumbnailPane.setBackgroundColor(colorOld);

							if (imageOld != imageNew)
							{
								m_thumbnailPane.setViewImage(imageOld);
							}

							m_thumbnailPane.setScaleApplied(appliedOld);
						}

						if (m_checkSkipFirst != null)
						{
							m_bSkipFirst = bSkipFirstOld;
							m_checkSkipFirst.setSelected(bSkipFirstOld);
						}

						if (m_handlerEffectAction != null)
						{
							m_handlerEffectAction.handle(new ActionEvent(AbstractEffectPane.this, ActionEvent.NULL_SOURCE_TARGET));
						}

						m_thumbnailPane.updateUI();
					}

					@Override
					public void redoAction()
					{
						m_color = colorNew;

						if (m_pickerPlain != null)
						{
							m_pickerPlain.setValue(colorNew);
						}

						if (m_sliderOpacity != null)
						{
							double dPercent = percentOpacity(colorNew);
							m_sliderOpacity.setValue(dPercent);

							if (m_labelSlider != null)
							{
								m_labelSlider.setText("" + Math.round(dPercent) + "%");
							}
						}

						if (m_thumbnailPane != null)
						{
							m_thumbnailPane.setBackgroundColor(colorNew);

							if (imageOld != imageNew)
							{
								m_thumbnailPane.setViewImage(imageNew);
							}

							m_thumbnailPane.setScaleApplied(appliedNew);
						}

						if (m_checkSkipFirst != null)
						{
							m_bSkipFirst = bSkipFirstNew;
							m_checkSkipFirst.setSelected(bSkipFirstNew);
						}

						if (m_handlerEffectAction != null)
						{
							m_handlerEffectAction.handle(new ActionEvent(AbstractEffectPane.this, ActionEvent.NULL_SOURCE_TARGET));
						}

						m_thumbnailPane.updateUI();
					}
				};

				undo.redoAction();
				getApp().addUndo(undo);
			}
		}
	}

	@Override
	public void reset()
	{
		if (m_pickerPlain != null)
		{
			m_pickerPlain.setValue(Color.TRANSPARENT);
		}

		m_color = Color.TRANSPARENT;
		m_colorNew = Color.TRANSPARENT;

		if (m_sliderOpacity != null)
		{
			m_sliderOpacity.setValue(0);

			if (m_labelSlider != null)
			{
				m_labelSlider.setText("0%");
			}
		}

		m_thumbnailPane.reset();
	}

	/**
	 * This method will be called when the main image sequence needs an update with
	 * the currently chosen effects.
	 *
	 * @param nFrame An int containing the frame index.
	 * @param image The {@link Image} object which has to
	 * receive the effect.
	 * @return The changed or unchanged image. Depends on the settings in the implementation.
	 */
	public abstract Image applyEffect(int nFrame, Image image);

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		m_thumbnailPane.appSizeChanged(nAppWidth, nAppHeight);
	}

	@Override
	public void handle(ApplyEvent event)
	{
		if (m_thumbnailPane.equals(event.getSource()))
		{
			handleApply(event.getUndoId());
		}
	}

	/**
	 * Collects the data of this pane in the given PngProject object.
	 *
	 * @param project A {@link PngProject} object.
	 * @param strUsage A string containing the usage information for the file description.
	 */
	public void collectProject(PngProject project, String strUsage)
	{
		Image image = m_thumbnailPane.getViewImage();
		ScaleApplied applied = m_thumbnailPane.getScaleApplied();
		Color color = m_thumbnailPane.getBackgroundColor();

		project.setMetaValue(strUsage, "background", String.format("argb:%08x", ImageUtil.getIntARGBColor(color)));

		if (image != null)
		{
			String strName = getId() + "/" + strUsage + ".0.png";

			project.addFileDescription("png", strUsage, strName, 0, "Common " + strUsage);
			project.setMetaValue(strUsage, "scale", applied.toString());

			Bitmap32 bitmap = ImageUtil.bitmapFromImage(image);
			Bitmap32Sequence sequence = new Bitmap32Sequence(bitmap, false, PngAnimationType.NONE);
			project.addNamedSequence(strName, sequence);
		}
	}

	/**
	 * Applies data from a PngProject object.
	 *
	 * @param project A {@link PngProject} object.
	 * @param strUsage A string containing the usage information for the file description.
	 */
	public void applyProject(PngProject project, String strUsage)
	{
		List<Element> files = project.getMetaFilesByUsage(strUsage); // Exactly one entry actually (0.1)
		for (int n = 0, nLen = files.size(); n < nLen; n++)
		{
			Element file = files.get(n);
			String strName = file.getAttribute("name");

			Bitmap32Sequence sequence = project.getNamedSequence(strName);
			if (sequence != null)
			{
				Image image = ImageUtil.imageFromBitmap(sequence.getDefaultBitmap());
				m_thumbnailPane.setViewImage(image);

				try
				{
					m_thumbnailPane.setScaleApplied(ScaleApplied.valueOf(project.getMetaValue(strUsage, "scale", "NONE")));
				}
				catch (Exception e) {}
			}
			else
			{
				Util.showError("title.project.error", "message.error.missinginternalfileinproject", strName);
			}
		}

		String strBackground = project.getMetaValue(strUsage, "background", "");
		if (strBackground.startsWith("argb:"))
		{
			try
			{
				Color color = ImageUtil.getColor(Integer.parseUnsignedInt(strBackground.substring(5), 16));
				setBackgroundColor(color);
			}
			catch (Exception e) {}
		}
	}
}
