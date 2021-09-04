package de.unlixx.runpng.scene;

import de.unlixx.runpng.scene.settings.AbstractSettingsPane.ScaleApplied;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Implementation of a ThumbnailView with multiple levels.
 * <table summary="">
 * <tr><td>Checkerboard</td><td>Shows a checkerboard pattern in case of transparencies.</td></tr>
 * <tr><td>Background</td><td>Shows an opaque to transparent plain colored background.</td></tr>
 * <tr><td>Image</td><td>Shows the image if one is loaded or it is transparent.</td></tr>
 * <tr><td>Cutframe</td><td>Shows a cutframe if the logical size of the loaded image is larger than the application settings.</td></tr>
 * </table>
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
public class ThumbnailView extends StackPane
{
	protected CheckerboardCanvas m_checkerboard;
	protected Canvas m_canvasBgnd;
	protected Color m_colorBgnd = Color.TRANSPARENT;
	protected Canvas m_canvasImage;
	protected boolean m_bInvalidated;
	protected Pane m_paneRect;
	protected Rectangle m_rectCutFrame;

	protected Image m_image;

	protected boolean m_bHorz;
	protected double m_dSize;

	protected boolean m_bAppSizeRelated;
	protected double m_dAppWidth;
	protected double m_dAppHeight;

	protected ScaleApplied m_applied;

	/**
	 * Constructor of this ThumbnailView with fix size for horizontal
	 * or vertical direction.
	 *
	 * @param bHorz If true then this view has a fix size horizontal size.
	 * @param dSize The fix size horizontally or vertically.
	 */
	public ThumbnailView(boolean bHorz, double dSize)
	{
		this(bHorz, dSize, -1, -1);
	}

	/**
	 * Constructor of this ThumbnailView with fix size for horizontal
	 * or vertical direction. This shows the loaded image relative to the
	 * settings of the application size if both application dimensions are set.
	 * If the loaded image is larger than the application settings then a
	 * dotted cutframe is shown.
	 *
	 * @param bHorz If true then this view has a fix size horizontal size.
	 * @param dSize The fix size horizontally or vertically.
	 * @param dAppWidth The width from the application settings.
	 * @param dAppHeight The height from the application settings.
	 */
	public ThumbnailView(boolean bHorz, double dSize, double dAppWidth, double dAppHeight)
	{
		m_dAppWidth = dAppWidth;
		m_dAppHeight = dAppHeight;

		m_bHorz = bHorz;
		m_dSize = dSize;

		m_applied = ScaleApplied.NONE;

		if (dAppWidth > 0 && dAppHeight > 0)
		{
			m_bAppSizeRelated = true;
		}

		m_checkerboard = new CheckerboardCanvas(0, 0, 5);
		m_canvasBgnd = new Canvas();

		m_canvasImage = new Canvas(0, 0);

		m_canvasImage.boundsInLocalProperty().addListener(new ChangeListener<Bounds>()
		{
			@Override
			public void changed(ObservableValue<? extends Bounds> observableValue, Bounds boundsOld, Bounds boundsNew)
			{
				double dWidth = m_canvasImage.getWidth(),
					dHeight = m_canvasImage.getHeight();

				m_checkerboard.setWidth(dWidth);
				m_checkerboard.setHeight(dHeight);

				m_canvasBgnd.setWidth(dWidth);
				m_canvasBgnd.setHeight(dHeight);

				invalidate();
			}
		});

		m_canvasImage.setWidth(m_dSize);
		m_canvasImage.setHeight(m_dSize);

		m_paneRect = new Pane();
		m_rectCutFrame = new Rectangle();
		m_rectCutFrame.setFill(Color.TRANSPARENT);
		m_rectCutFrame.setStroke(Color.YELLOW);
		m_rectCutFrame.getStrokeDashArray().addAll(3d);
		m_rectCutFrame.setStrokeWidth(1);
		m_rectCutFrame.setVisible(false);
		m_paneRect.getChildren().add(m_rectCutFrame);

		getChildren().addAll(m_checkerboard, m_canvasBgnd, m_canvasImage, m_paneRect);
	}

	/**
	 * Sets another applied scale type.
	 *
	 * @param applied An {@link ScaleApplied} enumeration type.
	 */
	public void setScaleApplied(ScaleApplied applied)
	{
		m_applied = applied;
		recalculateSizes();
	}

	/**
	 * Gets the current scale type applied.
	 *
	 * @return An {@link ScaleApplied} enumeration type.
	 */
	public ScaleApplied getScaleApplied()
	{
		return m_applied;
	}

	/**
	 * Invalidation call in case of a size, scale or image change.
	 */
	protected void invalidate()
	{
		if (!m_bInvalidated)
		{
			m_bInvalidated = true;

			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					repaintBgnd();
					repaintImage();

					m_bInvalidated = false;
				}
			});
		}
	}

	/**
	 * Repaints the current image in case of an invalidation call.
	 */
	protected void repaintBgnd()
	{
		double dWidth = m_canvasBgnd.getWidth(),
				dHeight = m_canvasBgnd.getHeight();

		if (dWidth > 0 && dHeight > 0)
		{
			GraphicsContext gc = m_canvasBgnd.getGraphicsContext2D();

			gc.clearRect(0, 0, dWidth, dHeight);

			gc.setFill(m_colorBgnd);
			gc.fillRect(0, 0, dWidth, dHeight);
		}
	}

	/**
	 * Repaints the current image in case of an invalidation call.
	 */
	protected void repaintImage()
	{
		double dWidth = m_canvasImage.getWidth(),
				dHeight = m_canvasImage.getHeight();

		if (dWidth > 0 && dHeight > 0)
		{
			GraphicsContext gc = m_canvasImage.getGraphicsContext2D();

			Color clr = Color.TRANSPARENT;
			gc.setFill(clr);
			gc.clearRect(0, 0, dWidth, dHeight);

			double dImageWidth,
					dImageHeight;

			if (m_image != null
				&& (dImageWidth = m_image.getWidth()) > 0
				&& (dImageHeight = m_image.getHeight()) > 0)
			{
				if (m_bAppSizeRelated)
				{
					double dDestWidth,
						dDestHeight,
						dScale = m_bHorz ? dImageWidth / Math.max(m_dAppWidth, dImageWidth)
									: dImageHeight / Math.max(m_dAppHeight, dImageHeight),
						dRel = dImageWidth / dImageHeight;

					switch (m_applied)
					{
					case SYM:
						dDestWidth = dWidth;
						dDestHeight = dDestWidth / dRel;

						if (dDestHeight > dHeight)
						{
							dDestHeight = dHeight;
							dDestWidth = dDestHeight * dRel;
						}
						break;

					case BOTH:
						dDestWidth = dWidth;
						dDestHeight = dHeight;
						break;

					case HORZ:
						dDestWidth = dWidth;
						if (dImageHeight > m_dAppHeight)
						{
							dDestHeight = dHeight;
							break;
						}
						dDestHeight = m_bHorz ? dHeight * dScale : dHeight * dScale / dRel;
						break;

					case VERT:
						dDestHeight = dHeight;
						if (dImageWidth > m_dAppWidth)
						{
							dDestWidth = dWidth;
							break;
						}
						dDestWidth = m_bHorz ? dWidth * dScale : dWidth * dScale * dRel;
						break;

					default:
						dDestWidth = m_bHorz ? m_dSize * dScale : m_dSize * dRel * dScale;
						dDestHeight = m_bHorz ? m_dSize / dRel * dScale : m_dSize * dScale;
						break;
					}

					//System.out.printf("repaintImage: dDestWidth = %.02f, dDestHeight = %.02f, dScale=%.02f\n", dDestWidth, dDestHeight, dScale);

					gc.drawImage(m_image, 0, 0, dImageWidth, dImageHeight, (dWidth - dDestWidth) / 2, (dHeight - dDestHeight) / 2, dDestWidth, dDestHeight);
				}
				else
				{
					gc.drawImage(m_image, 0, 0, dImageWidth, dImageHeight, 0, 0, dWidth, dHeight);
				}
			}
		}
	}

	/**
	 * Checks whether this ThumbnailView has a fix horizontal size.
	 *
	 * @return True if this ThumbnailView has a fix horizontal size.
	 * Afterwards it is vertically fix.
	 */
	public boolean isHorizontal()
	{
		return m_bHorz;
	}

	/**
	 * Sets the background color of this ThumbnailView.
	 *
	 * @param color A {@link Color} object.
	 */
	public void setBackgroundColor(Color color)
	{
		m_colorBgnd = color == null ? Color.TRANSPARENT : color;
		repaintBgnd();
	}

	/**
	 * Gets the background color of this ThumbnailView.
	 *
	 * @return A {@link Color} object.
	 */
	public Color getBackgroundColor()
	{
		return m_colorBgnd;
	}

	/**
	 * Sets the image of this ThumbnailView.
	 *
	 * @param image An {@link Image} object.
	 */
	public void setImage(Image image)
	{
		m_rectCutFrame.setVisible(false);

		m_image = image;
		m_applied = ScaleApplied.NONE;

		if (m_bAppSizeRelated)
		{
			recalculateSizes();
			return;
		}

		if (m_image == null
			|| m_image.getWidth() == 0
			|| m_image.getHeight() == 0)
		{
			m_canvasImage.setWidth(m_dSize);
			m_canvasImage.setHeight(m_dSize);
		}
		else
		{
			double dRel = m_image.getWidth() / m_image.getHeight();

			if (m_bHorz)
			{
				m_canvasImage.setWidth(m_dSize);
				m_canvasImage.setHeight(m_dSize / dRel);
			}
			else
			{
				m_canvasImage.setWidth(m_dSize * dRel);
				m_canvasImage.setHeight(m_dSize);
			}
		}

		invalidate();
	}

	/**
	 * Gets the current image of this ThumbnailView.
	 *
	 * @return An {@link Image} object.
	 * Or null if none ist set.
	 */
	public Image getImage()
	{
		return m_image;
	}

	/**
	 * Called in the case of changed application size settings. If this ThumbnailView
	 * has been constructed with application dimensions then this causes a recalculation
	 * of the sizes and relations. If not then this invokation has no visible effect.
	 *
	 * @param dAppWidth The new application image width.
	 * @param dAppHeight The new application image height.
	 */
	public void appSizeChanged(double dAppWidth, double dAppHeight)
	{
		m_dAppWidth = dAppWidth;
		m_dAppHeight = dAppHeight;

		if (m_bAppSizeRelated)
		{
			recalculateSizes();
		}
	}

	/**
	 * Gets the application width.
	 *
	 * @return a double with the application width if available. Afterwards it returns -1.
	 */
	public double getAppWidth()
	{
		return m_dAppWidth;
	}

	/**
	 * Gets the application height.
	 *
	 * @return a double with the application height if available. Afterwards it returns -1.
	 */
	public double getAppHeight()
	{
		return m_dAppHeight;
	}

	/**
	 * Checks whether this view is application size related.
	 *
	 * @return True if this view is application size related.
	 */
	public boolean isAppSizeRelated()
	{
		return m_bAppSizeRelated;
	}

	/**
	 * Recalculates the sizes and relations if this ThumbnailView has
	 * been constructed with application width and height.
	 * Afterwards it does not very much.
	 */
	public void recalculateSizes()
	{
		m_rectCutFrame.setVisible(false);

		if (!m_bAppSizeRelated)
		{
			return;
		}

		if (m_image == null
			|| m_image.getWidth() == 0
			|| m_image.getHeight() == 0)
		{
			double dRel = m_dAppWidth / m_dAppHeight;

			if (m_bHorz)
			{
				m_canvasImage.setWidth(m_dSize);
				m_canvasImage.setHeight(m_dSize / dRel);
			}
			else
			{
				m_canvasImage.setWidth(m_dSize * dRel);
				m_canvasImage.setHeight(m_dSize);
			}

			invalidate();
			return;
		}

		double dImageWidth = m_image.getWidth(),
				dImageHeight = m_image.getHeight(),
				dBaseWidth,
				dBaseHeight;

		switch (m_applied)
		{
		case SYM:
		case BOTH:
			dBaseWidth = m_dAppWidth;
			dBaseHeight = m_dAppHeight;
			break;

		case HORZ:
			dBaseWidth = m_dAppWidth;
			dBaseHeight = Math.max(m_dAppHeight, dImageHeight);
			break;

		case VERT:
			dBaseWidth = Math.max(m_dAppWidth, dImageWidth);
			dBaseHeight = m_dAppHeight;
			break;

		default:
			dBaseWidth = Math.max(m_dAppWidth, dImageWidth);
			dBaseHeight = Math.max(m_dAppHeight, dImageHeight);
			break;
		}

		double dRel = dBaseWidth / dBaseHeight,
			dWidth = m_bHorz ? m_dSize : m_dSize * dRel,
			dHeight = m_bHorz ? m_dSize / dRel : m_dSize;

		m_canvasImage.setWidth(dWidth);
		m_canvasImage.setHeight(dHeight);

		//System.out.printf("revalidateSizes: dWidth = %.02f, dHeight = %.02f\n", dWidth, dHeight);
		//System.out.printf("revalidateSizes: dBaseWidth = %.02f, dBaseHeight = %.02f\n", dBaseWidth, dBaseHeight);

		try
		{
			if (dBaseWidth > m_dAppWidth || dBaseHeight > m_dAppHeight)
			{
				double dRectWidth, dRectHeight;

				dRel = m_dAppWidth / m_dAppHeight;

				switch (m_applied)
				{
				case SYM:
				case BOTH:
					// Should not happen normally
					System.err.println("revalidateSizes: Should not happen?");
					return;

				case HORZ:
					dRectWidth = dWidth;
					dRectHeight = m_bHorz ? m_dSize / dBaseWidth * m_dAppWidth / dRel : m_dSize / dBaseHeight * m_dAppHeight;
					break;

				case VERT:
					dRectWidth = m_bHorz ? m_dSize / dBaseWidth * m_dAppWidth : m_dSize / dBaseHeight * m_dAppHeight * dRel;
					dRectHeight = dHeight;
					break;

				default:
					dRectWidth = m_bHorz ? m_dSize / dBaseWidth * m_dAppWidth : m_dSize / dBaseHeight * m_dAppHeight * dRel;
					dRectHeight = m_bHorz ? m_dSize / dBaseWidth * m_dAppWidth / dRel : m_dSize / dBaseHeight * m_dAppHeight;
					break;
				}

				//System.out.printf("revalidateSizes2: dRectWidth = %.02f, dRectHeight = %.02f, dRel = %.02f\n", dRectWidth, dRectHeight, dRel);

				m_rectCutFrame.setX((dWidth - dRectWidth) / 2);
				m_rectCutFrame.setWidth(dRectWidth);
				m_rectCutFrame.setY((dHeight - dRectHeight) / 2);
				m_rectCutFrame.setHeight(dRectHeight);

				m_rectCutFrame.setVisible(true);
			}
		}
		finally
		{
			invalidate();
		}
	}

	/**
	 * Checks whether the cutframe is shown.
	 *
	 * @return True if the cutframe is visible.
	 */
	public boolean isCutFrameVisible()
	{
		return m_rectCutFrame.isVisible();
	}
}
