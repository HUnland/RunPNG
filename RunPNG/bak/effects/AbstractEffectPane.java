package de.unlixx.runpng.scene.effects;

import de.unlixx.runpng.util.undo.UndoableRun;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public abstract class AbstractEffectPane extends AbstractThumbnailPane
{
	protected ColorPicker m_pickerPlain;
	protected Slider m_sliderOpacity;
	protected Label m_labelSlider;

	protected Color m_color = Color.TRANSPARENT;
	protected Color m_colorNew = Color.TRANSPARENT;

	protected CheckBox m_checkSkipFirst;

	protected boolean m_bSkipFirst;
	protected boolean m_bSkipFirstNew;

	protected final EventHandler<EffectAction> m_handlerEffectAction;

	public AbstractEffectPane(EventHandler<EffectAction> handlerEffectAction)
	{
		m_handlerEffectAction = handlerEffectAction;
	}

	protected void handleSkipFirst()
	{
		if (m_checkSkipFirst.isSelected() != m_bSkipFirst)
		{
			m_bSkipFirstNew = m_checkSkipFirst.isSelected();

			handleApply("action.effect.skipfirst");
		}
	}

	protected void handleColorChange()
	{
		m_colorNew = m_pickerPlain.getValue();
		double dOpacity = m_colorNew.getOpacity() * 100d;
		m_sliderOpacity.setValue(dOpacity);
		m_labelSlider.setText("" + Math.round(dOpacity) + "%");

		m_thumbnail.setBackgroundColor(m_colorNew);

		handleApply("action.colorchange");
	}

	protected void handleOpacityChange(boolean bComplete)
	{
		long lVal = Math.round(m_sliderOpacity.getValue());
		m_labelSlider.setText("" + lVal + "%");

		Color color = m_pickerPlain.getValue();
		m_colorNew = new Color(color.getRed(), color.getGreen(), color.getBlue(), lVal / 100d);
		m_pickerPlain.setValue(m_colorNew);

		m_thumbnail.setBackgroundColor(color);

		if (bComplete)
		{
			handleApply("action.opacitychange");
		}
	}

	@Override
	protected void handleApply(String strUndoId)
	{
		if (isUserAction())
		{
			final Color colorOld = m_color,
					colorNew = m_colorNew;

			final Image imageOld = m_image,
					imageNew = m_thumbnail != null ? m_thumbnail.getViewImage() : null;

			final SCALEAPPLIED appliedOld = m_applied,
					appliedNew = m_appliedNew;

			final boolean bSkipFirstOld = m_bSkipFirst,
					bSkipFirstNew = m_bSkipFirstNew;

			if (!colorOld.equals(colorNew)
				|| (imageOld == null && imageNew != null)
				|| (imageOld != null && !imageOld.equals(imageNew))
				|| bSkipFirstOld != bSkipFirstNew)
			{
				Runnable redo;

				UndoableRun undo = new UndoableRun(this, new Runnable()
				{
					@Override
					public void run()
					{
						if (m_pickerPlain != null)
						{
							m_color = colorOld;
							m_pickerPlain.setValue(colorOld);
						}

						if (m_thumbnail != null)
						{
							if (m_pickerPlain != null)
							{
								m_thumbnail.setBackgroundColor(colorOld);
							}

							m_image = imageOld;
							m_thumbnail.setViewImage(imageOld);
						}

						m_applied = appliedOld;

						if (m_checkSkipFirst != null)
						{
							m_bSkipFirst = bSkipFirstOld;
							m_checkSkipFirst.setSelected(bSkipFirstOld);
						}

						m_handlerEffectAction.handle(new EffectAction(AbstractEffectPane.this));

						updateUI();
					}
				}, redo = new Runnable()
				{
					@Override
					public void run()
					{
						if (m_pickerPlain != null)
						{
							m_color = colorNew;
							m_pickerPlain.setValue(colorNew);
						}

						if (m_thumbnail != null)
						{
							if (m_pickerPlain != null)
							{
								m_thumbnail.setBackgroundColor(colorNew);
							}

							m_image = imageNew;
							m_thumbnail.setViewImage(imageNew);
						}

						m_applied = appliedNew;

						if (m_checkSkipFirst != null)
						{
							m_bSkipFirst = bSkipFirstNew;
							m_checkSkipFirst.setSelected(bSkipFirstNew);
						}

						m_handlerEffectAction.handle(new EffectAction(AbstractEffectPane.this));

						updateUI();
					}
				}, getId(), strUndoId);

				redo.run();

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

		if (m_color != null)
		{
			m_color = Color.TRANSPARENT;
		}

		if (m_sliderOpacity != null)
		{
			m_sliderOpacity.setValue(0);

			if (m_labelSlider != null)
			{
				m_labelSlider.setText("0%");
			}
		}

		super.reset();

		updateUI();
	}

	public abstract Image applyEffect(int nFrame, Image image);

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		m_thumbnail.appSizeChanged(nAppWidth, nAppHeight);
	}
}
