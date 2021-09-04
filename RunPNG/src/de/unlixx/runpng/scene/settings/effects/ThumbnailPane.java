package de.unlixx.runpng.scene.settings.effects;

import de.unlixx.runpng.AppFileManager;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.ThumbnailView;
import de.unlixx.runpng.scene.settings.AbstractSettingsPane;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.event.ApplyEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Implementation of a ThumbnailPane with control buttons to manipulate
 * the scale of the viewed image. This pane will be used in the effect and
 * generator panes.
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
public class ThumbnailPane extends AbstractSettingsPane
{
	protected final static int THUMBNAIL_WIDTH = 200;
	protected final static int THUMBNAIL_HEIGHT = 200;

	protected EventHandler<ApplyEvent> m_handlerApply;

	protected Button m_buttonPicLoad;
	protected Button m_buttonPicRemove;

	protected VBox m_vboxImageScale;
	protected ToggleButton m_buttonScaleSym;
	protected ToggleButton m_buttonScaleBoth;
	protected ToggleButton m_buttonScaleHorz;
	protected ToggleButton m_buttonScaleVert;
	protected ToggleButton m_buttonScaleNone;
	protected ToggleGroup m_togglesApplied;

	protected ScaleApplied m_applied = ScaleApplied.NONE;
	protected ScaleApplied m_appliedNew = ScaleApplied.NONE;

	protected ThumbnailView m_thumbnail;
	protected Image m_imageNew;
	protected Image m_imageScaled;

	/**
	 * Constructor of this ThumbnailPane.
	 *
	 * @param strLabelId A string containing a localizable label id.
	 * @param handlerApply An {@link EventHandler}
	 * to inform about user chosen scaling.
	 */
	public ThumbnailPane(String strLabelId, EventHandler<ApplyEvent> handlerApply)
	{
		m_bAdjusting = true;

		m_handlerApply = handlerApply;

		Label label;

		label = createRowLabel("label." + strLabelId + ".pic");
		add(label, 0, 0, 1, 1);

		m_buttonPicLoad = createImageButton("pic.open", "tooltip." + strLabelId + ".load", "icons/32x32/file.open.png", action -> handlePicLoad());
		add(m_buttonPicLoad, 1, 0, 1, 1);

		m_buttonPicRemove = createImageButton("pic.remove", "tooltip." + strLabelId + ".remove", "icons/32x32/trashbin.png", action -> handlePicRemove());
		add(m_buttonPicRemove, 2, 0, 1, 1);

		m_vboxImageScale = new VBox();
		m_vboxImageScale.setAlignment(Pos.TOP_RIGHT);
		m_vboxImageScale.setSpacing(Util.SPACING);
		add(m_vboxImageScale, 0, 1, 1, 1);

		m_togglesApplied = new ToggleGroup();

		m_buttonScaleSym = createImageToggleButton("float.pic.scale.both", "tooltip.scale.sym", "icons/32x32/scale.sym.png", null);
		m_buttonScaleSym.setUserData(ScaleApplied.SYM);
		m_buttonScaleSym.setToggleGroup(m_togglesApplied);

		m_buttonScaleBoth = createImageToggleButton("float.pic.scale.both", "tooltip.scale.both", "icons/32x32/scale.both.png", null);
		m_buttonScaleBoth.setUserData(ScaleApplied.BOTH);
		m_buttonScaleBoth.setToggleGroup(m_togglesApplied);

		m_buttonScaleHorz = createImageToggleButton("float.pic.scale.horz", "tooltip.scale.horz", "icons/32x32/scale.horz.png", null);
		m_buttonScaleHorz.setUserData(ScaleApplied.HORZ);
		m_buttonScaleHorz.setToggleGroup(m_togglesApplied);

		m_buttonScaleVert = createImageToggleButton("float.pic.scale.vert", "tooltip.scale.vert", "icons/32x32/scale.vert.png", null);
		m_buttonScaleVert.setUserData(ScaleApplied.VERT);
		m_buttonScaleVert.setToggleGroup(m_togglesApplied);

		m_buttonScaleNone = createImageToggleButton("float.pic.scale.vert", "tooltip.revert", "icons/32x32/revert.png", null);
		m_buttonScaleNone.setUserData(ScaleApplied.NONE);
		m_buttonScaleNone.setToggleGroup(m_togglesApplied);

		m_togglesApplied.selectToggle(m_buttonScaleNone);
		m_togglesApplied.selectedToggleProperty().addListener((toggle, toggleOld, toggleNew) -> handleAppliedToggle(toggleOld, toggleNew));

		m_vboxImageScale.getChildren().addAll(m_buttonScaleSym, m_buttonScaleBoth, m_buttonScaleHorz, m_buttonScaleVert, m_buttonScaleNone);

		m_thumbnail = new ThumbnailView(true, THUMBNAIL_WIDTH, getAppWidth(), getAppHeight());
		add(m_thumbnail, 1, 1, 4, 1);
		m_thumbnail.setAlignment(Pos.TOP_LEFT);

		final Tooltip tooltip = Util.createTooltip("tooltip.thumbnailpane");
		tooltip.setOnShowing(value ->
		{
			Image image = m_thumbnail.getImage();
			String strId = tooltip.getId(),
					strText = Loc.getString(strId, (int)m_thumbnail.getAppWidth(), (int)m_thumbnail.getAppHeight(),
							image != null ? (int)image.getWidth() : 0, image != null ? (int)image.getHeight() : 0);
			tooltip.setText(strText);
		});
		Tooltip.install(m_thumbnail, tooltip);

		updateUI();

		m_bAdjusting = false;
	}

	/**
	 * Handles a user click on a scale toggle button.
	 *
	 * @param toggleOld The old {@link Toggle}.
	 * @param toggleNew The new {@link Toggle}.
	 */
	protected void handleAppliedToggle(Toggle toggleOld, Toggle toggleNew)
	{
		if (toggleNew == null)
		{
			toggleOld.setSelected(true);
		}
		else
		{
			switch((ScaleApplied)toggleNew.getUserData())
			{
			case SYM: handleScaleSym(); break;
			case BOTH: handleScaleBoth(); break;
			case HORZ: handleScaleHorz(); break;
			case VERT: handleScaleVert(); break;
			default: handleScaleNone(); break;
			}
		}
	}

	/**
	 * Checks whether a scale has already been applied.
	 * @param apply A {@link ScaleApplied} enumerated value.
	 * @return True if this scale has already been applied.
	 */
	protected boolean scaleAlreadyApplied(ScaleApplied apply)
	{
		return apply == m_applied;
	}

	/**
	 * Handles a click on the symmetrical scale toggle button.
	 */
	protected void handleScaleSym()
	{
		Image image = m_thumbnail.getImage();
		double dAppWidth = m_thumbnail.getAppWidth(),
			dAppHeight = m_thumbnail.getAppHeight();

		if (!scaleAlreadyApplied(ScaleApplied.SYM) && image != null && image.getWidth() != dAppWidth && image.getHeight() != dAppHeight)
		{
			m_appliedNew = ScaleApplied.SYM;
			m_thumbnail.setScaleApplied(m_appliedNew);

			m_imageScaled = null;

			handleApply("action.scalesym");
		}
	}

	/**
	 * Handles a click on the asymmetrical scale toggle button for both directions.
	 */
	protected void handleScaleBoth()
	{
		Image image = m_thumbnail.getImage();
		double dAppWidth = m_thumbnail.getAppWidth(),
				dAppHeight = m_thumbnail.getAppHeight();

		if (!scaleAlreadyApplied(ScaleApplied.BOTH) && image != null && image.getWidth() != dAppWidth && image.getHeight() != dAppHeight)
		{
			m_appliedNew = ScaleApplied.BOTH;
			m_thumbnail.setScaleApplied(m_appliedNew);

			m_imageScaled = null;

			handleApply("action.scaleboth");
		}
	}

	/**
	 * Handles a click on the horizontal scale toggle button.
	 */
	protected void handleScaleHorz()
	{
		Image image = m_thumbnail.getImage();
		double dAppWidth = m_thumbnail.getAppWidth();

		if (!scaleAlreadyApplied(ScaleApplied.HORZ) && image != null && image.getWidth() != dAppWidth)
		{
			m_appliedNew = ScaleApplied.HORZ;
			m_thumbnail.setScaleApplied(m_appliedNew);

			m_imageScaled = null;

			handleApply("action.scalehorz");
		}
	}

	/**
	 * Handles a click on the vertical scale toggle button.
	 */
	protected void handleScaleVert()
	{
		Image image = m_thumbnail.getImage();
		double dAppHeight = m_thumbnail.getAppHeight();

		if (!scaleAlreadyApplied(ScaleApplied.VERT) && image != null && image.getHeight() != dAppHeight)
		{
			m_appliedNew = ScaleApplied.VERT;
			m_thumbnail.setScaleApplied(m_appliedNew);

			m_imageScaled = null;

			handleApply("action.scalevert");
		}
	}

	/**
	 * Handles a click on the non scale toggle button.
	 */
	protected void handleScaleNone()
	{
		if (m_applied != ScaleApplied.NONE)
		{
			m_appliedNew = ScaleApplied.NONE;
			m_thumbnail.setScaleApplied(m_appliedNew);

			m_imageScaled = null;

			handleApply("action.revert");
		}
	}

	/**
	 * Handles a click on the load button. This invokes the file open dialog.
	 */
	protected void handlePicLoad()
	{
		AppFileManager manager = getApp().getFileManager();
		manager.onSingleImageOpen(value ->
		{
			m_appliedNew = ScaleApplied.NONE;
			m_thumbnail.setScaleApplied(m_appliedNew);

			m_imageScaled = null;
			m_imageNew = value.getValue();

			handleApply("action.picload");
		});
	}

	/**
	 * Handles a click on the remove button. This removes the currently loaded image.
	 */
	protected void handlePicRemove()
	{
		m_appliedNew = ScaleApplied.NONE;
		m_thumbnail.setScaleApplied(m_appliedNew);

		m_imageScaled = null;
		m_imageNew = null;

		handleApply("action.picremove");
	}

	/**
	 * This method updates the availability of the scaling and remove buttons.
	 */
	protected void updateUI()
	{
		Image image = m_thumbnail.getImage();

		m_vboxImageScale.setDisable(image == null);
		m_buttonPicRemove.setDisable(image == null);
	}

	/**
	 * Gets the image from the embedded {@link ThumbnailView}.
	 *
	 * @return The current {@link Image} of the ThumbnailView.
	 * Or null if none loaded.
	 */
	public Image getViewImage()
	{
		return m_thumbnail.getImage();
	}

	/**
	 * Gets the new loaded image. This is called by handleApply() methods in reaction
	 * for a load event.
	 *
	 * @return The new loaded {@link Image}.
	 */
	public Image getNewImage()
	{
		return m_imageNew;
	}

	/**
	 * Sets another image in the embedded {@link ThumbnailView}.
	 *
	 * @param image The image {@link Image} for the ThumbnailView.
	 */
	public void setViewImage(Image image)
	{
		m_imageNew = image;
		m_thumbnail.setImage(image);
		updateUI();
	}

	/**
	 * Creates a scaled copy of the image from the embedded {@link ThumbnailView}
	 * according the applied scale.
	 *
	 * @return The scaled image {@link Image}.
	 */
	public Image getScaledViewImage()
	{
		if (m_imageScaled != null && m_applied == m_appliedNew)
		{
			return m_imageScaled;
		}

		Image image = m_thumbnail.getImage();
		if (image != null && image.getWidth() > 0 && image.getHeight() > 0)
		{
			int nAppWidth = (int)m_thumbnail.getAppWidth(),
				nAppHeight = (int)m_thumbnail.getAppHeight();

			switch (m_applied)
			{
			case SYM:
				m_imageScaled = ImageUtil.imageScaleCentered(image, nAppWidth, nAppHeight, ImageUtil.SCALEMODE.BOTH, true);
				break;

			case BOTH:
				m_imageScaled = ImageUtil.imageScaleCentered(image, nAppWidth, nAppHeight, ImageUtil.SCALEMODE.BOTH, false);
				break;

			case HORZ:
				m_imageScaled = ImageUtil.imageScaleCentered(image, nAppWidth, nAppHeight, ImageUtil.SCALEMODE.HORZ, false);
				break;

			case VERT:
				m_imageScaled = ImageUtil.imageScaleCentered(image, nAppWidth, nAppHeight, ImageUtil.SCALEMODE.VERT, false);
				break;

			default:
				m_imageScaled = image;
				break;
			}

			return m_imageScaled;
		}

		return m_imageScaled = null;
	}

	/**
	 * Sets the background color of the embedded {@link ThumbnailView}.
	 *
	 * @param color A {@link Color} object.
	 */
	public void setBackgroundColor(Color color)
	{
		m_thumbnail.setBackgroundColor(color);
	}

	/**
	 * Gets the background color of the embedded {@link ThumbnailView}.
	 *
	 * @return A {@link Color} object.
	 */
	public Color getBackgroundColor()
	{
		return m_thumbnail.getBackgroundColor();
	}

	/**
	 * Gets the current scale type applied. This will normaly be invoked by a handleApply() method
	 * in reation to an apply change.
	 *
	 * @return A {@link ScaleApplied} enumerated value.
	 */
	public ScaleApplied getScaleApplied()
	{
		return m_applied;
	}

	/**
	 * Gets the new scale type applied. This will normaly be invoked by a handleApply() method
	 * in reation to an apply change.
	 *
	 * @return A {@link ScaleApplied} enumerated value.
	 */
	public ScaleApplied getScaleAppliedNew()
	{
		return m_appliedNew;
	}

	/**
	 * Sets a new applied scale and informs the embedded
	 * {@link ThumbnailView} to do so.
	 *
	 * @param applied A {@link ScaleApplied} enum type.
	 */
	public void setScaleApplied(ScaleApplied applied)
	{
		m_applied = applied;
		//m_appliedNew = applied;

		switch(m_applied)
		{
		case SYM: m_togglesApplied.selectToggle(m_buttonScaleSym); break;
		case BOTH: m_togglesApplied.selectToggle(m_buttonScaleBoth); break;
		case HORZ: m_togglesApplied.selectToggle(m_buttonScaleHorz); break;
		case VERT: m_togglesApplied.selectToggle(m_buttonScaleVert); break;
		default: m_togglesApplied.selectToggle(m_buttonScaleNone); break;
		}

		m_thumbnail.setScaleApplied(applied);

		m_imageScaled = null;

		updateUI();
	}

	@Override
	public void reset()
	{
		m_thumbnail.setBackgroundColor(Color.TRANSPARENT);

		m_thumbnail.setImage(null);
		m_imageNew = null;
		m_imageScaled = null;

		m_togglesApplied.selectToggle(m_buttonScaleNone);

		updateUI();
	}

	/**
	 * Delegates the apply handling to the connected {@link EventHandler}.
	 *
	 * @param strUndoId A string containing a localizable id for the undo
	 * and redo buttons and menu items.
	 */
	protected void handleApply(String strUndoId)
	{
		if (m_handlerApply != null)
		{
			m_handlerApply.handle(new ApplyEvent(this, strUndoId));
		}
	}

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		m_thumbnail.appSizeChanged(nAppWidth, nAppHeight);
		m_imageScaled = null;
		updateUI();
	}

	@Override
	public void collectProject(PngProject project)
	{
		// Nothing to do actually
	}

	@Override
	public void applyProject(PngProject project)
	{
		// Nothing to do actually
	}
}
