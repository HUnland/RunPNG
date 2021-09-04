package de.unlixx.runpng.scene.effects;

import de.unlixx.runpng.AppFileManager;
import de.unlixx.runpng.scene.AbstractSettingsPane;
import de.unlixx.runpng.scene.ThumbnailView;
import de.unlixx.runpng.util.ImageUtil;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public abstract class AbstractThumbnailPane extends AbstractSettingsPane
{
	protected final static int THUMBNAIL_WIDTH = 200;
	protected final static int THUMBNAIL_HEIGHT = 200;

	protected Button m_buttonPicLoad;
	protected Button m_buttonPicRemove;
	protected VBox m_vboxImageScale;
	protected Button m_buttonScaleSym;
	protected Button m_buttonScaleBoth;
	protected Button m_buttonScaleHorz;
	protected Button m_buttonScaleVert;
	protected Button m_buttonRevert;

	protected SCALEAPPLIED m_applied = SCALEAPPLIED.NONE;
	protected SCALEAPPLIED m_appliedNew = SCALEAPPLIED.NONE;

	protected ThumbnailView m_thumbnail;
	protected Image m_image;

	public AbstractThumbnailPane()
	{
	}

	protected boolean scaleAlreadyApplied(SCALEAPPLIED apply)
	{
		return apply == m_applied;
	}

	protected void handleScaleSymmetrical()
	{
		Image image = m_thumbnail.getBaseImage();
		int nAppWidth = getAppWidth(),
			nAppHeight = getAppHeight();

		if (!scaleAlreadyApplied(SCALEAPPLIED.SCALESYM) && image != null && (int)image.getWidth() != nAppWidth && (int)image.getHeight() != nAppHeight)
		{
			m_image = m_thumbnail.getViewImage();
			m_thumbnail.setViewImage(ImageUtil.imageScaleSymmetrical(image, getAppWidth(), getAppHeight(), ImageUtil.SCALETYPE.BOTH));

			m_appliedNew = SCALEAPPLIED.SCALESYM;

			handleApply("action.scalesym");
		}
	}

	protected void handleScaleBoth()
	{
		Image image = m_thumbnail.getBaseImage();
		int nAppWidth = getAppWidth(),
			nAppHeight = getAppHeight();

		if (!scaleAlreadyApplied(SCALEAPPLIED.SCALEBOTH) && image != null && ((int)image.getWidth() != nAppWidth || (int)image.getHeight() != nAppHeight))
		{
			m_image = m_thumbnail.getViewImage();
			m_thumbnail.setViewImage(ImageUtil.imageScaleAsymmetrical(image, getAppWidth(), getAppHeight()));

			m_appliedNew = SCALEAPPLIED.SCALEBOTH;

			handleApply("action.scaleboth");
		}
	}

	protected void handleScaleHorz()
	{
		Image image = m_thumbnail.getBaseImage();
		int nAppWidth = getAppWidth();

		if (!scaleAlreadyApplied(SCALEAPPLIED.SCALEHORZ) && image != null && (int)image.getWidth() != nAppWidth)
		{
			m_image = m_thumbnail.getViewImage();
			m_thumbnail.setViewImage(ImageUtil.imageScaleAsymmetrical(image, getAppWidth(), image.getHeight()));

			m_appliedNew = SCALEAPPLIED.SCALEHORZ;

			handleApply("action.scalehorz");
		}
	}

	protected void handleScaleVert()
	{
		Image image = m_thumbnail.getBaseImage();
		int nAppHeight = getAppHeight();

		if (!scaleAlreadyApplied(SCALEAPPLIED.SCALEVERT) && image != null && (int)image.getHeight() != nAppHeight)
		{
			m_image = m_thumbnail.getViewImage();
			m_thumbnail.setViewImage(ImageUtil.imageScaleAsymmetrical(image, image.getWidth(), nAppHeight));

			m_appliedNew = SCALEAPPLIED.SCALEVERT;

			handleApply("action.scalevert");
		}
	}

	protected void handleRevert()
	{
		if (m_applied != SCALEAPPLIED.NONE)
		{
			m_image = m_thumbnail.getViewImage();
			m_thumbnail.setViewImage(m_thumbnail.getBaseImage());

			m_appliedNew = SCALEAPPLIED.NONE;

			handleApply("action.revert");
		}
	}

	protected void handlePicLoad()
	{
		AppFileManager manager = getApp().getFileManager();
		manager.onSingleImageOpen(value ->
		{
			m_thumbnail.setBaseImage(value.getValue());

			m_applied = SCALEAPPLIED.NONE;

			handleApply("action.picload");
		});
	}

	protected void handlePicRemove()
	{
		m_image = m_thumbnail.getViewImage();
		m_thumbnail.setBaseImage(null);

		m_applied = SCALEAPPLIED.NONE;

		handleApply("action.picremove");
	}

	protected void updateUI()
	{
		if (m_thumbnail != null)
		{
			Image image = m_thumbnail.getBaseImage();

			m_vboxImageScale.setDisable(image == null);
			m_buttonPicRemove.setDisable(image == null);

			if (image != null)
			{
				m_buttonRevert.setDisable(m_applied == SCALEAPPLIED.NONE);
			}
		}
	}

	public void reset()
	{
		if (m_thumbnail != null)
		{
			m_thumbnail.setBackgroundColor(Color.TRANSPARENT);

			m_thumbnail.setBaseImage(null);

			m_applied = SCALEAPPLIED.NONE;
			m_appliedNew = SCALEAPPLIED.NONE;
		}
	}

	protected abstract void handleApply(String strUndoId);
}
