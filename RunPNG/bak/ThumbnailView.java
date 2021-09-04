package de.unlixx.runpng.scene;

import de.unlixx.runpng.util.ImageUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ThumbnailView extends StackPane
{
	CheckerboardCanvas m_checkerboard;
	Canvas m_canvasBgnd;
	Color m_colorBgnd = Color.TRANSPARENT;
	ImageView m_view;
	Pane m_paneRect;
	Rectangle m_rectCutFrame;

	Image m_imageBase;

	double m_dFixWidth;
	double m_dFixHeight;

	boolean m_bAppSizeRelated;
	double m_dAppWidth;
	double m_dAppHeight;

	public ThumbnailView(double dFixWidth, double dFixHeight)
	{
		this(dFixWidth, dFixHeight, -1, -1);
	}

	public ThumbnailView(double dFixWidth, double dFixHeight, double dAppWidth, double dAppHeight)
	{
		m_dAppWidth = dAppWidth;
		m_dAppHeight = dAppHeight;

		m_dFixWidth = dFixWidth;
		m_dFixHeight = dFixHeight;

		if (dAppWidth > 0 && dAppHeight > 0)
		{
			m_bAppSizeRelated = true;
		}

		m_checkerboard = new CheckerboardCanvas(0, 0, 5);
		m_canvasBgnd = new Canvas();
		m_view = new ImageView();

		if (m_bAppSizeRelated)
		{
			m_view.setFitWidth(m_dFixWidth > 0 ? m_dFixWidth : m_dFixHeight);
			m_view.setFitHeight(m_dFixHeight > 0 ? m_dFixHeight : m_dFixWidth);
		}
		else
		{
			m_view.setFitWidth(dFixWidth);
			m_view.setFitHeight(dFixHeight);
			m_view.setPreserveRatio(true);
		}

		m_view.boundsInLocalProperty().addListener(new ChangeListener<Bounds>()
		{
			@Override
			public void changed(ObservableValue<? extends Bounds> observableValue, Bounds boundsOld, Bounds boundsNew)
			{
				double dWidth = boundsNew.getWidth(),
					dHeight = boundsNew.getHeight();

				m_checkerboard.setWidth(Math.floor(dWidth) - 1);
				m_checkerboard.setHeight(Math.floor(dHeight) - 1);

				m_canvasBgnd.setWidth(dWidth);
				m_canvasBgnd.setHeight(dHeight);
				repaintBgnd();
			}
		});

		m_paneRect = new Pane();

		m_rectCutFrame = new Rectangle();
		m_rectCutFrame.setFill(Color.TRANSPARENT);
		m_rectCutFrame.setStroke(Color.YELLOW);
		m_rectCutFrame.getStrokeDashArray().addAll(3d);
		m_rectCutFrame.setStrokeWidth(1);
		m_rectCutFrame.setVisible(false);
		m_paneRect.getChildren().add(m_rectCutFrame);

		getChildren().addAll(m_checkerboard, m_canvasBgnd, m_view, m_paneRect);
	}

	private void repaintBgnd()
	{
		GraphicsContext gc = m_canvasBgnd.getGraphicsContext2D();

		double dWidth = m_canvasBgnd.getWidth(),
				dHeight = m_canvasBgnd.getHeight();

		gc.clearRect(0, 0, dWidth, dHeight);

		gc.setFill(m_colorBgnd);
		gc.fillRect(0, 0, dWidth, dHeight);
	}

	public void setBackgroundColor(Color color)
	{
		m_colorBgnd = color == null ? Color.TRANSPARENT : color;
		repaintBgnd();
	}

	public Color getBackgroundColor()
	{
		return m_colorBgnd;
	}

	public void setBaseImage(Image image)
	{
		m_rectCutFrame.setVisible(false);
		m_imageBase = image;
		m_view.setImage(image);
	}

	public void setViewImage(Image image)
	{
		m_view.setImage(image);

		if (m_bAppSizeRelated)
		{
			appSizeChanged(m_dAppWidth, m_dAppHeight); // Cheap trick
		}
	}

	public void appSizeChanged(double dAppWidth, double dAppHeight)
	{
		m_rectCutFrame.setVisible(false);
		m_rectCutFrame.setX(0);
		m_rectCutFrame.setY(0);
		m_rectCutFrame.setWidth(1);
		m_rectCutFrame.setHeight(1);

		if (!m_bAppSizeRelated)
		{
			return;
		}

		m_dAppWidth = dAppWidth;
		m_dAppHeight = dAppHeight;

		Image image = m_view.getImage();

		double dBaseWidth,
				dBaseHeight;

		if (image != null)
		{
			dBaseWidth = image.getWidth();
			dBaseHeight = image.getHeight();

			if (dBaseWidth < dAppWidth || dBaseHeight < dAppHeight)
			{
				image = ImageUtil.imageCropCentered(image, Math.max(dAppWidth, dBaseWidth), Math.max(dAppHeight, dBaseHeight));
				m_view.setImage(image);
			}
		}
		else
		{
			dBaseWidth = dAppWidth;
			dBaseHeight = dAppHeight;
		}

		if (dBaseWidth > dAppWidth || dBaseHeight > dAppHeight)
		{
			Bounds bounds = m_view.getBoundsInLocal();

			double dFitWidth = m_view.getFitWidth(),
					dFitHeight = m_view.getFitHeight(),
					dWidth,
					dHeight,
					dScale = 1d;

			if (image != null)
			{
				dWidth = image.getWidth();
				dHeight = image.getHeight();
			}
			else
			{
				dWidth = dAppWidth;
				dHeight = dAppHeight;
			}

			if (dFitWidth > 0)
			{
				dScale = dFitWidth / dWidth;
			}
			else if (dFitHeight > 0)
			{
				dScale = dFitHeight / dHeight;
			}

			m_rectCutFrame.setX((bounds.getWidth() - dAppWidth * dScale) / 2);
			m_rectCutFrame.setWidth(dAppWidth * dScale);
			m_rectCutFrame.setY((bounds.getHeight() - dAppHeight * dScale) / 2);
			m_rectCutFrame.setHeight(dAppHeight * dScale);

			m_rectCutFrame.setVisible(true);
		}
	}

	public boolean hasCutFrame()
	{
		return m_rectCutFrame.isVisible();
	}

	public Image getViewImage()
	{
		return m_view.getImage();
	}

	public Image getBaseImage()
	{
		return m_imageBase;
	}

	public void setFitWidth(double dWidth)
	{
		m_view.setFitWidth(dWidth);
	}

	public void setFitHeight(double dHeight)
	{
		m_view.setFitWidth(dHeight);
	}
}
