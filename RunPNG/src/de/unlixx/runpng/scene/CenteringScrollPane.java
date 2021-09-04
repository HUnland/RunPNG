package de.unlixx.runpng.scene;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Implementation based on a ScrollPane as a main view. It centers the viewport
 * item when it is smaller as the main view size.
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
public class CenteringScrollPane extends ScrollPane
{
	protected ImageCanvas m_scrollee;
	protected Canvas m_background;
	protected StackPane m_container;
	protected Rectangle m_rectCutFrame;

	protected double m_dBackgroundWidth;
	protected double m_dBackgroundHeight;

	/**
	 * Constructor with an initial view element.
	 *
	 * @param scrollee An object of type {@link ImageCanvas}.
	 */
	public CenteringScrollPane(ImageCanvas scrollee)
	{
		setBackground(Background.EMPTY);

		m_dBackgroundWidth = scrollee.getWidth();
		m_dBackgroundHeight = scrollee.getHeight();

		m_container = new StackPane();
		setContent(m_container);

		m_rectCutFrame = new Rectangle();
		m_rectCutFrame.setFill(Color.TRANSPARENT);
		m_rectCutFrame.setStroke(Color.YELLOW);
		m_rectCutFrame.getStrokeDashArray().addAll(3d);
		m_rectCutFrame.setStrokeWidth(1);
		m_rectCutFrame.setVisible(false);
		m_container.getChildren().add(m_rectCutFrame);

		if (scrollee != null)
		{
			setScrollee(scrollee);
		}

		viewportBoundsProperty().addListener(new ChangeListener<Bounds>()
		{
			@Override
			public void changed(ObservableValue<? extends Bounds> observableValue, Bounds boundsOld, Bounds boundsNew)
			{
				updateContent(boundsNew);
			}
		});

		updateContent(viewportBoundsProperty().get());
	}

	/**
	 * Sets the cutframe - a dotted rectangle - visible or invisible.
	 *
	 * @param bVisible True to show the cutframe. E. g. in case the currently shown
	 * image is larger than the application dimensions.
	 */
	public void setCutFrameVisible(boolean bVisible)
	{
		m_rectCutFrame.setVisible(bVisible);
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

	/**
	 * Sets the scale of the embedded {@link ImageCanvas}.
	 *
	 * @param dScale A double containing the scale. Where 1.0 means 100%.
	 */
	public void setScrolleeScale(double dScale)
	{
		if (m_scrollee != null && dScale != m_scrollee.getScale())
		{
			m_scrollee.setScale(dScale);

			updateContent(viewportBoundsProperty().get());
		}
	}

	/**
	 * Sets the background size to the application dimensions.
	 *
	 * @param dBackgroundWidth The desired background width.
	 * @param dBackgroundHeight The desired background height.
	 */
	public void setBackgroundSize(double dBackgroundWidth, double dBackgroundHeight)
	{
		m_dBackgroundWidth = dBackgroundWidth;
		m_dBackgroundHeight = dBackgroundHeight;

		updateContent(viewportBoundsProperty().get());
	}

	/**
	 * Updates the sizes of all embedded elements.
	 *
	 * @param boundsVP A {@link Bounds} object.
	 */
	protected void updateContent(Bounds boundsVP)
	{
		if (m_scrollee != null)
		{
			Bounds bounds = m_scrollee.getBoundsInParent();
			double dScrolleeScale = m_scrollee.getScale();

			if (m_background != null)
			{
				m_background.setWidth(m_dBackgroundWidth * dScrolleeScale);
				m_background.setHeight(m_dBackgroundHeight * dScrolleeScale);
			}

			m_rectCutFrame.setWidth(m_dBackgroundWidth * dScrolleeScale);
			m_rectCutFrame.setHeight(m_dBackgroundHeight * dScrolleeScale);

			if ((int)bounds.getWidth() > (int)m_rectCutFrame.getWidth()
				|| (int)bounds.getHeight() > (int)m_rectCutFrame.getHeight())
			{
				m_rectCutFrame.setVisible(true);
			}
			else
			{
				m_rectCutFrame.setVisible(false);
				m_rectCutFrame.setWidth(1);
				m_rectCutFrame.setHeight(1);
			}

			layout();

			if (bounds.getWidth() <= boundsVP.getWidth())
			{
				setHbarPolicy(ScrollBarPolicy.NEVER);
				m_container.setPrefWidth(boundsVP.getWidth() - 1);
			}
			else
			{
				setHbarPolicy(ScrollBarPolicy.ALWAYS);
				m_container.setPrefWidth(bounds.getWidth());

				setHvalue(.5);
			}

			if (bounds.getHeight() <= boundsVP.getHeight())
			{
				setVbarPolicy(ScrollBarPolicy.NEVER);
				m_container.setPrefHeight(boundsVP.getHeight() - 1);
			}
			else
			{
				setVbarPolicy(ScrollBarPolicy.ALWAYS);
				m_container.setPrefHeight(bounds.getHeight());

				setVvalue(.5);
			}
		}
		else
		{
			setHbarPolicy(ScrollBarPolicy.NEVER);
			setVbarPolicy(ScrollBarPolicy.NEVER);
		}
	}

	/**
	 * Sets another ImageCanvas as scrollee.
	 *
	 * @param scrollee An object of type {@link ImageCanvas}.
	 */
	public void setScrollee(ImageCanvas scrollee)
	{
		if (m_scrollee != null)
		{
			m_container.getChildren().remove(m_scrollee);
		}

		m_scrollee = scrollee;

		m_container.getChildren().add(m_background == null ? 0 : 1, m_scrollee);

		m_scrollee.boundsInLocalProperty().addListener(new ChangeListener<Bounds>()
		{
			@Override
			public void changed(ObservableValue<? extends Bounds> observableValue, Bounds boundsOld, Bounds boundsNew)
			{
				updateContent(viewportBoundsProperty().get());
			}
		});
	}

	/**
	 * Gets the ImageCanvas currently set.
	 *
	 * @return A {@link ImageCanvas}.
	 * Or null if none has been set.
	 */
	public ImageCanvas getScrollee()
	{
		return m_scrollee;
	}

	/**
	 * Sets another background node.
	 *
	 * @param background An object of type {@link Canvas}.
	 */
	public void setBackgroundNode(Canvas background)
	{
		if (m_background != null)
		{
			m_container.getChildren().remove(m_background);
		}

		m_background = background;
		if (m_background != null)
		{
			m_container.getChildren().add(0, background);
		}
	}
}
