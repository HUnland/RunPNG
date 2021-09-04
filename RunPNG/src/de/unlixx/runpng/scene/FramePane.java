package de.unlixx.runpng.scene;

import de.unlixx.runpng.App;
import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.Util;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * The FramePane class is a container for a frame image which it shows in an embedded
 * {@link ThumbnailView}. It shows also a
 * label with it's current index and the number of milliseconds delay for the
 * frame image.
 * FramePane implements the {@link Toggle} interface
 * and shows a colored border when selected. If it is unselected then the border is grey.
 * It provides a tooltip with information about index, delay, width and height
 * of the image.
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
public class FramePane extends VBox implements Toggle
{
	static final int THUMBNAIL_HEIGHT = 100;

	protected final static Border m_borderNormal = new Border(new BorderStroke(Color.LIGHTGREY,  BorderStrokeStyle.SOLID, null, new BorderWidths(3)));
	protected final static Border m_borderSelected = new Border(new BorderStroke(Color.YELLOW,  BorderStrokeStyle.SOLID, null, new BorderWidths(3)));

	protected ThumbnailView m_thumbnail;
	protected Label m_label;
	protected int m_nIndex;
	protected PngDelayFraction m_fraction;

	protected Image m_imageBase;

	protected EventHandler<ActionEvent> m_handlerSelection;

	protected final SimpleBooleanProperty m_selectedProperty = new SimpleBooleanProperty(false);
	protected final SimpleObjectProperty<ToggleGroup> m_toggleGroupProperty = new SimpleObjectProperty<ToggleGroup>();

	/**
	 * The constructor of this FramePane. It creates a {@link ThumbnailView}
	 * with a fix height while preserving the image ratio. This means that the width is variable.
	 * The given image here is not necessarily what the embedded ThumbnailView gets to show. This
	 * depends on the effects applied later.
	 * <pre>
	 * See also: {@link #setViewImage(Image image)}
	 * </pre>
	 *
	 * @param image The {@link Image} to show initially.
	 * @param fraction The {@link PngDelayFraction}
	 * for the current image.
	 */
	public FramePane(Image image, PngDelayFraction fraction)
	{
		super();

		App app = App.getMainApp();

		setBorder(m_borderNormal);

		setSpacing(0);
		setAlignment(Pos.BOTTOM_CENTER);

		// TODO: CSS

		m_nIndex = -1;
		m_fraction = fraction;

		m_thumbnail = new ThumbnailView(false, THUMBNAIL_HEIGHT, app.getImageWidth(), app.getImageHeight());
		getChildren().add(m_thumbnail);

		m_label = new Label();
		getChildren().add(m_label);

		m_selectedProperty.addListener(change ->
		{
			if (m_selectedProperty.get())
			{
				setBorder(m_borderSelected);
			}
			else
			{
				setBorder(m_borderNormal);
			}
		});

		addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
		{
			if (event.isPrimaryButtonDown())
			{
				if (m_toggleGroupProperty != null)
				{
					ToggleGroup toggles = m_toggleGroupProperty.getValue();
					if (toggles != null)
					{
						toggles.selectToggle(this);
						onSelection();
					}
				}
			}
		});

		final Tooltip tooltip = Util.createTooltip("tooltip.framepane");
		tooltip.setOnShowing(value ->
		{
			Image imageView = m_thumbnail.getImage();

			String strId = tooltip.getId(),
					strText = Loc.getString(strId, getIndex(), getDelayFraction().getDelayMillis(), (int)imageView.getWidth(), (int)imageView.getHeight());
			tooltip.setText(strText);
		});
		Tooltip.install(this, tooltip);

		m_imageBase = image;
		m_thumbnail.setImage(image);
	}

	/**
	 * Called by the application in case of an app image size change. It dispatches the
	 * new sizes to the thumbnail.
	 *
	 * @param nAppWidth The new image width.
	 * @param nAppHeight The new image height.
	 */
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		m_thumbnail.appSizeChanged(nAppWidth, nAppHeight);
	}

	/**
	 * Sets another base image of this pane. E. g. in the case the actual base image
	 * has been cut or scaled. If this base image has been set it gets also the view
	 * image until another view image is set.
	 *
	 * @param image The new base {@link Image}.
	 */
	public void setBaseImage(Image image)
	{
		m_imageBase = image;
		m_thumbnail.setImage(image);
	}

	/**
	 * Gets the current base image.
	 *
	 * @return The actual base {@link Image}.
	 */
	public Image getBaseImage()
	{
		return m_imageBase;
	}

	/**
	 * Sets the view image which is normally different from the base image.
	 *
	 * @param image An {@link Image} object.
	 */
	public void setViewImage(Image image)
	{
		m_thumbnail.setImage(image);

		if (isSelected())
		{
			onSelection(); // To refresh the center view
		}
	}

	/**
	 * Gets the current view image.
	 *
	 * @return An {@link Image} object.
	 */
	public Image getViewImage()
	{
		return m_thumbnail.getImage();
	}

	/**
	 * Sets an event handler for a selection event of this pane.
	 *
	 * @param handler An {@link EventHandler}.
	 */
	public void setOnSelection(EventHandler<ActionEvent> handler)
	{
		m_handlerSelection = handler;
	}

	/**
	 * Called to inform a selection handler that this pane has now been selected.
	 */
	public void onSelection()
	{
		if (m_handlerSelection != null)
		{
			ActionEvent action = new ActionEvent(this, ActionEvent.NULL_SOURCE_TARGET);
			m_handlerSelection.handle(action);
		}
	}

	/**
	 * Updates the label. This will be called when the index or the delay value changes.
	 */
	public void updateLabel()
	{
		m_label.setText("#" + m_nIndex + "   " + m_fraction.getDelayMillis() + " ms");
	}

	/**
	 * Sets the index value of this pane.
	 *
	 * @param nIndex An int with the index.
	 */
	public void setIndex(int nIndex)
	{
		if (m_nIndex != nIndex)
		{
			m_nIndex = nIndex;
			updateLabel();
		}
	}

	/**
	 * Gets the current index value of this pane.
	 *
	 * @return An int with the index.
	 */
	public int getIndex()
	{
		return m_nIndex;
	}

	/**
	 * Sets the current PngDelayFraction of this pane.
	 * The method stores a clone of the given PngDelayFraction to prevent shared instances.
	 *
	 * @param fraction An object of type {@link PngDelayFraction}
	 */
	public void setDelayFraction(PngDelayFraction fraction)
	{
		m_fraction = fraction.clone();
		updateLabel();
	}

	/**
	 * Gets the current PngDelayFraction of this pane.
	 * The method returns a clone of the stored PngDelayFraction to prevent shared instances.
	 *
	 * @return An object of type {@link PngDelayFraction}
	 */
	public PngDelayFraction getDelayFraction()
	{
		return m_fraction.clone();
	}

	/**
	 * Gets the current delay in milliseconds.
	 *
	 * @return An int containing the current delay in milliseconds.
	 */
	public int getDelayMillis()
	{
		return m_fraction.getDelayMillis();
	}

	@Override
	public ToggleGroup getToggleGroup()
	{
		return m_toggleGroupProperty.get();
	}

	@Override
	public boolean isSelected()
	{
		return m_selectedProperty.get();
	}

	@Override
	public BooleanProperty selectedProperty()
	{
		return m_selectedProperty;
	}

	@Override
	public void setSelected(boolean bSelected)
	{
		if (bSelected != m_selectedProperty.get())
		{
			m_selectedProperty.set(bSelected);

			if (bSelected)
			{
				ToggleGroup toggles = getToggleGroup();
				if (toggles != null)
				{
					toggles.selectToggle(FramePane.this);
				}

				onSelection();
			}
		}
	}

	@Override
	public void setToggleGroup(ToggleGroup toggles)
	{
		m_toggleGroupProperty.set(toggles);
	}

	@Override
	public ObjectProperty<ToggleGroup> toggleGroupProperty()
	{
		return m_toggleGroupProperty;
	}
}
