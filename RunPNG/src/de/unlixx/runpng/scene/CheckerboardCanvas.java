package de.unlixx.runpng.scene;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Implementation of a CheckerboardCanvas as a background for transparent images.
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
public class CheckerboardCanvas extends Canvas
{
	protected double m_dSquareLen;

	/**
	 * Constructor for an initially sizeless CheckerboardCanvas.
	 * The squares side length will be set to 10 px.
	 */
	public CheckerboardCanvas()
	{
		this(0, 0, 10);
	}

	/**
	 * Constructor for a CheckerboardCanvas with adjustable sizes.
	 *
	 * @param dWidth The initial width of the canvas.
	 * @param dHeight The initial height of the canvas.
	 * @param dSquareLen The squares side length.
	 */
	public CheckerboardCanvas(double dWidth, double dHeight, double dSquareLen)
	{
		super(dWidth, dHeight);

		m_dSquareLen = dSquareLen;

		InvalidationListener listener = new InvalidationListener()
		{
			boolean m_bInvalidated;

		    @Override
		    public void invalidated(Observable o)
		    {
		    	if (!m_bInvalidated)
		    	{
		    		m_bInvalidated = true;

		    		Platform.runLater(new Runnable()
					{
						@Override
						public void run()
						{
				    		redraw();
				    		m_bInvalidated = false;
						}
					});
		    	}
		    }
		};

		widthProperty().addListener(listener);
		heightProperty().addListener(listener);
	}

	/**
	 * Redraws the canvas as needed. This will automatically be called
	 * in case of a size change.
	 */
	protected void redraw()
	{
		double dWidth = getWidth(),
			dHeight = getHeight();

		if (dWidth > 0 && dHeight > 0)
		{
			GraphicsContext gc = getGraphicsContext2D();

			Color clr = Color.GREY;
			gc.setFill(clr);
			gc.fillRect(0, 0, dWidth, dHeight);

			clr = clr.brighter();
			gc.setFill(clr);

			for (double dV = 0; dV < dHeight; dV += m_dSquareLen)
			{
				double dOffs = ((dV / m_dSquareLen) % 2) * m_dSquareLen;

				for (double dH = 0; dH < dWidth; dH += m_dSquareLen * 2)
				{
					gc.fillRect(dH + dOffs, dV, Math.min(m_dSquareLen, dWidth - (dH + dOffs)),
							Math.min(m_dSquareLen, dHeight - dV));
				}
			}
		}
	}
}
