package de.unlixx.runpng.scene;

import de.unlixx.runpng.App;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Scaling ImageCanvas for the {@link de.unlixx.runpng.scene.CenteringScrollPane CenteringScrollPane}
 * of the application.
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
public class ImageCanvas extends Canvas
{
	protected Image m_image;
	protected double m_dScale = 1;

	/**
	 * Constructor of ImageCanvas with an initial image.
	 *
	 * @param image An {@link Image} object.
	 */
	public ImageCanvas(Image image)
	{
		this(image.getWidth(), image.getHeight());

		m_image = image;
	}

	/**
	 * Contructor of ImageCanvas with initial size.
	 *
	 * @param dWidth A double containg the initial width.
	 * @param dHeight A double containg the initial width.
	 */
	public ImageCanvas(double dWidth, double dHeight)
	{
		super(dWidth, dHeight);
	}

	/**
	 * Sets the image of this ImageCanvas.
	 *
	 * @param image An {@link Image} object.
	 */
	public void setImage(Image image)
	{
		setWidth(image.getWidth() * m_dScale);
		setHeight(image.getHeight() * m_dScale);

		m_image = image;
		redraw();
	}

	/**
	 * Gets the current image of this ImageCanvas.
	 *
	 * @return An {@link Image} object.
	 * Or null if none has been set.
	 */
	public Image getImage()
	{
		return m_image;
	}

	/**
	 * Sets the scale of this ImageCanvas.
	 *
	 * @param dScale A double containing the scale. Where 1.0 means 100%.
	 */
	public void setScale(double dScale)
	{
		m_dScale = dScale;

		setWidth(m_image.getWidth() * dScale);
		setHeight(m_image.getHeight() * dScale);

		redraw();
	}

	/**
	 * Gets the scale of this ImageCanvas.
	 *
	 * @return A double containing the scale. Where 1.0 means 100%.
	 */
	public double getScale()
	{
		return m_dScale;
	}

	/**
	 * Removes the current Image and sets width and height to application size.
	 */
	public void clearImage()
	{
		m_image = null;
		setWidth(App.getMainApp().getImageWidth() * m_dScale);
		setHeight(App.getMainApp().getImageHeight() * m_dScale);
		redraw();
	}

	/**
	 * Redraws this ImageCanvas.
	 */
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
