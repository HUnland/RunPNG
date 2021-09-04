package de.unlixx.runpng.scene;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 *
 * @author Hans-Josef Unland
 *
 */
public class ThumbnailCanvas extends Canvas
{
	protected Image m_image;
	protected double m_dSize;
	protected boolean m_bHorz;

	public ThumbnailCanvas(boolean bHorz, double dSize)
	{
		super(dSize, dSize);

		m_bHorz = bHorz;
		m_dSize = dSize;
	}

	public void setImage(Image image)
	{
		m_image = image;

		setSize(m_dSize);
	}

	public Image getImage()
	{
		return m_image;
	}

	public boolean isHorizontal()
	{
		return m_bHorz;
	}

	public void setSize(double dSize)
	{
		m_dSize = dSize;

		if (m_image == null)
		{
			setWidth(dSize);
			setHeight(dSize);
		}
		else
		{
			double dRel = m_image.getWidth() / m_image.getHeight();

			if (m_bHorz)
			{
				setWidth(dSize);
				setHeight(dSize / dRel);
			}
			else
			{
				setWidth(dSize * dRel);
				setHeight(dSize);
			}
		}

		redraw();
	}

	protected void redraw()
	{
		double dWidth = getWidth(),
				dHeight = getHeight();

		GraphicsContext gc = getGraphicsContext2D();

		Color clr = Color.TRANSPARENT;
		gc.setFill(clr);
		gc.clearRect(0, 0, dWidth, dHeight);

		if (dWidth > 0 && dHeight > 0 && m_image != null)
		{
			gc.drawImage(m_image, 0, 0, m_image.getWidth(), m_image.getHeight(), 0, 0, dWidth, dHeight);
		}
	}
}
