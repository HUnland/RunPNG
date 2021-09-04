package de.unlixx.runpng.scene.effects;

import de.unlixx.runpng.scene.ThumbnailView;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Util;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MaskPane extends AbstractEffectPane
{
	public MaskPane(EventHandler<EffectAction> handlerEffectAction)
	{
		super(handlerEffectAction);

		setId("effect.mask");

		//setGridLinesVisible(true);

		m_bAdjusting = true;

		Label label;

		label = createRowLabel("label.opacity");
		add(label, 0, 0, 1, 1);

		m_sliderOpacity = new Slider(0, 100, 100);
		add(m_sliderOpacity, 1, 0, 2, 1);
		m_sliderOpacity.setTooltip(Util.createTooltip("tooltip.slider.opacity"));
		m_sliderOpacity.setOnMouseDragged(event -> { handleOpacityChange(false); });
		m_sliderOpacity.setOnMouseReleased(event -> { handleOpacityChange(true); });
		m_sliderOpacity.setOnKeyReleased(event -> { handleOpacityChange(true); });

		m_labelSlider = new Label("100%");
		add(m_labelSlider, 3, 0, 1, 1);

		label = createRowLabel("label.mask.pic");
		add(label, 0, 2, 1, 1);

		m_buttonPicLoad = createImageButton("mask.pic.open", "tooltip.mask.load", "icons/32x32/file.open.png", action -> { handlePicLoad(); });
		add(m_buttonPicLoad, 1, 2, 1, 1);

		m_buttonPicRemove = createImageButton("mask.pic.remove", "tooltip.mask.remove", "icons/32x32/trashbin.png", action -> { handlePicRemove(); });
		add(m_buttonPicRemove, 2, 2, 1, 1);

		m_vboxImageScale = new VBox();
		m_vboxImageScale.setAlignment(Pos.TOP_RIGHT);
		m_vboxImageScale.setSpacing(Util.SPACING);
		add(m_vboxImageScale, 0, 3, 1, 1);

		m_buttonScaleSym = createImageButton("mask.pic.scale.both", "tooltip.scale.sym", "icons/32x32/scale.symmetrical.png", action -> { handleScaleSymmetrical(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleSym);

		m_buttonScaleBoth = createImageButton("mask.pic.scale.both", "tooltip.scale.both", "icons/32x32/scale.both.png", action -> { handleScaleBoth(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleBoth);

		m_buttonScaleHorz = createImageButton("mask.pic.scale.horz", "tooltip.scale.horz", "icons/32x32/scale.horz.png", action -> { handleScaleHorz(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleHorz);

		m_buttonScaleVert = createImageButton("mask.pic.scale.vert", "tooltip.scale.vert", "icons/32x32/scale.vert.png", action -> { handleScaleVert(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleVert);

		m_buttonRevert = createImageButton("mask.pic.scale.vert", "tooltip.revert", "icons/32x32/revert.png", action -> { handleRevert(); });
		m_vboxImageScale.getChildren().add(m_buttonRevert);

		m_thumbnail = new ThumbnailView(true, THUMBNAIL_WIDTH, getAppWidth(), getAppHeight());
		m_thumbnail.setBackgroundColor(Color.WHITE);
		add(m_thumbnail, 1, 3, 3, 1);
		m_thumbnail.setAlignment(Pos.TOP_LEFT);

		m_checkSkipFirst = createCheckBox("label.skipfirst", "tooltip.effect.skipfirst", action -> { handleSkipFirst(); });
		add(m_checkSkipFirst, 0, 4, 2, 1);

		updateUI();

		m_bAdjusting = false;
	}

	@Override
	protected void handleOpacityChange(boolean bComplete)
	{
		long lVal = Math.round(m_sliderOpacity.getValue());
		m_labelSlider.setText("" + lVal + "%");

		m_colorNew = new Color(lVal / 100d, lVal / 100d, lVal / 100d, 1d);

		m_thumbnail.setBackgroundColor(m_colorNew);

		if (bComplete)
		{
			handleApply("action.opacitychange");
		}
	}

	@Override
	public void reset()
	{
		super.reset();
		m_sliderOpacity.setValue(100);
		m_labelSlider.setText("100%");
		m_thumbnail.setBackgroundColor(Color.WHITE);
	}

	@Override
	public Image applyEffect(int nFrame, Image image)
	{
		if (!(nFrame == 0 && m_bSkipFirst))
		{
			return ImageUtil.imageApplyMask(image, m_thumbnail.getBackgroundColor(), m_thumbnail.getViewImage());
		}

		return image;
	}
}
