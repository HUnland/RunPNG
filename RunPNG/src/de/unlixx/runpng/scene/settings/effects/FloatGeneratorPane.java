package de.unlixx.runpng.scene.settings.effects;

import java.util.List;

import org.w3c.dom.Element;

import de.unlixx.runpng.App;
import de.unlixx.runpng.AppFramesView;
import de.unlixx.runpng.bitmap.Bitmap32;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.scene.settings.AbstractSettingsPane;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Progress;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.event.ApplyEvent;
import de.unlixx.runpng.util.event.ValueEvent;
import de.unlixx.runpng.util.undo.Undoable;
import de.unlixx.runpng.util.undo.UndoableIntegerSpinner;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

/**
 * Implementation of a float generator to create a new main image sequence.
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
public class FloatGeneratorPane extends AbstractSettingsPane
{
	protected enum Direction
	{
		RIGHT,
		UP,
		LEFT,
		DOWN
	}

	protected ThumbnailPane m_thumbnailPane;

	protected ToggleButton m_buttonRight;
	protected ToggleButton m_buttonUp;
	protected ToggleButton m_buttonLeft;
	protected ToggleButton m_buttonDown;
	protected ToggleGroup m_togglesDir;

	protected Direction m_dir;

	protected Spinner<Integer> m_spinnerImageWidth;
	protected int m_nWidth;

	protected Spinner<Integer> m_spinnerImageHeight;
	protected int m_nHeight;

	protected Spinner<Integer> m_spinnerNumOfFrames;
	protected int m_nNumOfFrames;

	protected Spinner<Integer> m_spinnerMillis;
	protected int m_nMillis;

	protected Button m_generateSequence;

	/**
	 * Constructor for ths generator.
	 */
	public FloatGeneratorPane()
	{
		setId("generator.float");

		//setGridLinesVisible(true);

		m_bAdjusting = true;

		Label label;

		m_thumbnailPane = new ThumbnailPane("float", apply -> handleApply(apply));
		add(m_thumbnailPane, 0, 0, 5, 1);

		label = createRowLabel("label.float.dir");
		label.setAlignment(Pos.CENTER_LEFT);
		add(label, 0, 1, 1, 2);

		m_togglesDir = new ToggleGroup();

		m_buttonRight = createImageToggleButton("float.dir.right", "tooltip.float.dir.right", "icons/32x32/dir.right.png", null);
		m_buttonRight.setUserData(Direction.RIGHT);
		m_buttonRight.setToggleGroup(m_togglesDir);
		add(m_buttonRight, 3, 1, 1, 2);

		m_buttonUp = createImageToggleButton("float.dir.up", "tooltip.float.dir.up", "icons/32x32/dir.up.png", null);
		m_buttonUp.setUserData(Direction.UP);
		m_buttonUp.setToggleGroup(m_togglesDir);
		add(m_buttonUp, 2, 1, 1, 1);

		m_buttonLeft = createImageToggleButton("float.dir.left", "tooltip.float.dir.left", "icons/32x32/dir.left.png", null);
		m_buttonLeft.setUserData(Direction.LEFT);
		m_buttonLeft.setToggleGroup(m_togglesDir);
		add(m_buttonLeft, 1, 1, 1, 2);

		m_buttonDown = createImageToggleButton("float.dir.down", "tooltip.float.dir.down", "icons/32x32/dir.down.png", null);
		m_buttonDown.setUserData(Direction.DOWN);
		m_buttonDown.setToggleGroup(m_togglesDir);
		add(m_buttonDown, 2, 2, 1, 1);

		m_togglesDir.selectToggle(m_buttonDown);
		m_dir = Direction.DOWN;

		m_togglesDir.selectedToggleProperty().addListener((toggle, toggleOld, toggleNew) -> handleDirToggle(toggleOld, toggleNew));

		label = createRowLabel("label.widthxheight");
		add(label, 0, 3, 1, 1);

		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setSpacing(Util.SPACING);

		m_spinnerImageWidth = createIntegerSpinner(App.DEFAULT_IMAGE_WIDTH,
				App.MIN_IMAGE_WIDTH, App.MAX_IMAGE_WIDTH, 1, Pos.CENTER_RIGHT,
				"tooltip.desiredwidth", action -> handleImageWidth(action));

		m_spinnerImageHeight = createIntegerSpinner(App.DEFAULT_IMAGE_HEIGHT,
				App.MIN_IMAGE_HEIGHT, App.MAX_IMAGE_HEIGHT, 1, Pos.CENTER_RIGHT,
				"tooltip.desiredheight", action -> handleImageHeight(action));

		hbox.getChildren().addAll(m_spinnerImageWidth, new Label("x"), m_spinnerImageHeight, new Label("px"));
		add(hbox, 1, 3, 4, 1);

		label = createRowLabel("label.numofframes");
		add(label, 0, 4, 1, 1);
		setValignment(label, VPos.CENTER);

		m_spinnerNumOfFrames = createIntegerSpinner(1, 1, 1000, 1, Pos.CENTER_RIGHT,
				"tooltip.desirednumofframes", action -> handleNumOfFrames(action));
		add(m_spinnerNumOfFrames, 1, 4, 3, 1);
		m_nNumOfFrames = 1;

		label = createRowLabel("label.delaystiming");
		add(label, 0, 5, 1, 1);
		setValignment(label, VPos.CENTER);

		m_spinnerMillis = createIntegerSpinner(100, 0, 65535000, 10, Pos.CENTER_RIGHT,
				"tooltip.fraction.millis", action -> handleFieldMillis(action));
		add(m_spinnerMillis, 1, 5, 3, 1);
		m_nMillis = 100;

		label = new Label("ms");
		add(label, 4, 5, 1, 1);
		setValignment(label, VPos.CENTER);

		m_generateSequence = createTextButton("label.generate.sequence", "tooltip.generate.sequence", action -> generateSequence());
		add(m_generateSequence, 0, 7, 1, 1);

		m_bAdjusting = false;
	}

	/**
	 * Handles a user click on a direction toggle button.
	 *
	 * @param toggleOld The old {@link Toggle}.
	 * @param toggleNew The new {@link Toggle}.
	 */
	protected void handleDirToggle(Toggle toggleOld, Toggle toggleNew)
	{
		if (toggleNew == null || toggleOld == toggleNew)
		{
			toggleOld.setSelected(true);
		}
		else
		{
			m_dir = (Direction)toggleNew.getUserData();
		}
	}

	/**
	 * Handles a user change of the image width. Creates an undoable.
	 *
	 * @param value A {@link ValueEvent} with the new width.
	 */
	protected void handleImageWidth(ValueEvent<Integer> value)
	{
		if (m_nWidth != value.getValue())
		{
			UndoableIntegerSpinner undo = new UndoableIntegerSpinner(m_spinnerImageWidth, m_nWidth, value.getValue(), "action.widthchange");

			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					m_nWidth = m_spinnerImageWidth.getValue();
					m_thumbnailPane.appSizeChanged(m_nWidth, m_nHeight);
				}
			};

			run.run();
			undo.setRunFinally(run);
			getApp().addUndo(undo);
		}
	}

	/**
	 * Handles a user change of the image height. Creates an undoable.
	 *
	 * @param value A {@link ValueEvent} with the new height.
	 */
	protected void handleImageHeight(ValueEvent<Integer> value)
	{
		if (m_nHeight != value.getValue())
		{
			UndoableIntegerSpinner undo = new UndoableIntegerSpinner(m_spinnerImageHeight, m_nHeight, value.getValue(), "action.heightchange");

			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					m_nHeight = m_spinnerImageHeight.getValue();
					m_thumbnailPane.appSizeChanged(m_nWidth, m_nHeight);
				}
			};

			run.run();
			undo.setRunFinally(run);
			getApp().addUndo(undo);
		}
	}

	/**
	 * Handles a user change of the number of frames. Creates an undoable.
	 *
	 * @param value A {@link ValueEvent} with the new
	 * number of frames.
	 */
	protected void handleNumOfFrames(ValueEvent<Integer> value)
	{
		if (m_nNumOfFrames != value.getValue())
		{
			UndoableIntegerSpinner undo = new UndoableIntegerSpinner(m_spinnerNumOfFrames, m_nNumOfFrames, value.getValue(), "action.numofframeschange");

			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					m_nNumOfFrames = m_spinnerNumOfFrames.getValue();
				}
			};

			run.run();
			undo.setRunFinally(run);
			getApp().addUndo(undo);
		}
	}

	/**
	 * Handles a user change of the delay time. Creates an undoable.
	 *
	 * @param value A {@link ValueEvent} with the new
	 * delay time in milliseconds.
	 */
	protected void handleFieldMillis(ValueEvent<Integer> value)
	{
		if (m_nMillis != value.getValue())
		{
			UndoableIntegerSpinner undo = new UndoableIntegerSpinner(m_spinnerMillis, m_nMillis, value.getValue(), "action.millischange");

			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					m_nMillis = m_spinnerMillis.getValue();
				}
			};

			run.run();
			undo.setRunFinally(run);
			getApp().addUndo(undo);
		}
	}

	/**
	 * Generates a new image sequence according the user settings and applies it to the frames view.
	 */
	protected void generateSequence()
	{
		AppFramesView framesView = getApp().getFramesView();

		Bitmap32Sequence sequenceOld = framesView.createSequence(true, true, true);

		final Image image = m_thumbnailPane.getScaledViewImage();
		final Bitmap32 bitmap = ImageUtil.bitmapFromImage(image);

		final PngDelayFraction fraction = new PngDelayFraction(m_nMillis);
		final PngFrameControl fcTL = new PngFrameControl(bitmap.getWidth(), bitmap.getHeight(), 0, 0, fraction.getDelayNum(), fraction.getDelayDen());
		bitmap.setFrameControl(fcTL);

		framesView.clear();
		framesView.createFrame(image, -1, fraction, null);
		framesView.setSelectedIndex(0);

		Bitmap32Sequence sequenceNew = new Bitmap32Sequence(bitmap, false, m_nNumOfFrames > 1 ? PngAnimationType.ANIMATED : PngAnimationType.NONE);
		getApp().setAppSize(m_nWidth, m_nHeight);

		Progress<Bitmap32> progress = new Progress<Bitmap32>(getApp().getProgressBar(), m_nNumOfFrames)
		{
			@Override
			protected Bitmap32 call() throws Exception
			{
				updateProgress(1);

				Bitmap32 bitmapNew;

				switch (m_dir)
				{
				case DOWN:
				case UP:
					double dLines = bitmap.getHeight() / (double)m_nNumOfFrames,
						dOffsY = dLines;

					for (int n = 1; n < m_nNumOfFrames; n++)
					{
						bitmapNew = ImageUtil.bitmapShiftVert(bitmap, m_dir == Direction.DOWN ? (int)Math.round(dOffsY) : -(int)Math.round(dOffsY));

						bitmapNew.setFrameControl(fcTL);
						sequenceNew.addFrame(bitmapNew);
						dOffsY += dLines;

						addProgress(1);

						clearAcknowledge();
						updateValue(bitmapNew);

						while (!getAcknowledge())
						{
							Thread.sleep(10);
						}
					}
					break;

				case RIGHT:
				case LEFT:
					double dCols = bitmap.getWidth() / (double)m_nNumOfFrames,
						dOffsX = dCols;

					for (int n = 1; n < m_nNumOfFrames; n++)
					{
						bitmapNew = ImageUtil.bitmapShiftHorz(bitmap, m_dir == Direction.RIGHT ? (int)Math.round(dOffsX) : -(int)Math.round(dOffsX));

						bitmapNew.setFrameControl(fcTL);
						sequenceNew.addFrame(bitmapNew);
						dOffsX += dCols;

						addProgress(1);

						clearAcknowledge();
						updateValue(bitmapNew);

						while (!getAcknowledge())
						{
							Thread.sleep(10);
						}
					}
					break;
				}

				updateProgress(m_nNumOfFrames, m_nNumOfFrames);

				return null;
			}
		};

		getApp().setWaitCursor();

		progress.valueProperty().addListener(change ->
		{
			Bitmap32 bitmapNew = progress.getValue();
			if (bitmapNew != null)
			{
				try
				{
					framesView.createFrame(ImageUtil.imageFromBitmap(bitmapNew), -1, fraction, null);
				}
				finally
				{
					progress.acknowledge();
				}
			}
		});

		progress.setOnFailed(value ->
		{
			getApp().setDefaultCursor();
			getApp().getFramesView().applyBitmapSequence(sequenceOld, true);

			Throwable t = progress.getException();
			if (t != null)
			{
				Util.showError(t);
			}
		});

		progress.setOnSucceeded(value ->
		{
			getApp().setDefaultCursor();
			getApp().setAnimationType(m_nNumOfFrames > 1 ? PngAnimationType.ANIMATED : PngAnimationType.NONE);

			final int nWidthOld = m_nWidth,
					nHeightOld = m_nHeight;

			Undoable<FloatGeneratorPane> undo = new Undoable<FloatGeneratorPane>(this, "effect.generator.float")
			{
				@Override
				public void undoAction()
				{
					getApp().getFramesView().applyBitmapSequence(sequenceOld, true);
					appSizeChanged(nWidthOld, nHeightOld);
				}

				@Override
				public void redoAction()
				{
					getApp().getFramesView().applyBitmapSequence(sequenceNew, true);
				}
			};

			getApp().addUndo(undo);
		});

		Thread thread = new Thread(progress);
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		m_spinnerImageWidth.getValueFactory().setValue(nAppWidth);
		m_nWidth = nAppWidth;

		m_spinnerImageHeight.getValueFactory().setValue(nAppHeight);
		m_nHeight = nAppHeight;

		m_thumbnailPane.appSizeChanged(m_nWidth, m_nHeight);
	}

	/**
	 * Handles the apply of changes. Creates Undoables for undo and redo.
	 *
	 * @param apply An {@link ApplyEvent} object.
	 * and redo buttons and menu items.
	 */
	public void handleApply(ApplyEvent apply)
	{
		if (m_thumbnailPane == apply.getSource())
		{
			final Image imageOld = m_thumbnailPane.getViewImage(),
					imageNew = m_thumbnailPane.getNewImage();
			final ScaleApplied appliedOld = m_thumbnailPane.getScaleApplied(),
					appliedNew = m_thumbnailPane.getScaleAppliedNew();

			final int nWidthOld = m_nWidth,
					nHeightOld = m_nHeight,
					nWidthNew = imageOld != imageNew && imageNew != null ? (int)imageNew.getWidth() : nWidthOld,
					nHeightNew = imageOld != imageNew && imageNew != null ? (int)imageNew.getHeight() : nHeightOld;

			Undoable<FloatGeneratorPane> undo = new Undoable<FloatGeneratorPane>(this, "effect.generator.float")
			{
				@Override
				public void undoAction()
				{
					if (imageOld != imageNew)
					{
						m_thumbnailPane.setViewImage(imageOld);
					}

					if (appliedOld != appliedNew)
					{
						m_thumbnailPane.setScaleApplied(appliedOld);
					}

					if (nWidthOld != nWidthNew || nHeightOld != nHeightNew)
					{
						appSizeChanged(nWidthOld, nHeightOld);
					}
				}

				@Override
				public void redoAction()
				{
					if (imageOld != imageNew)
					{
						m_thumbnailPane.setViewImage(imageNew);
					}

					if (appliedOld != appliedNew)
					{
						m_thumbnailPane.setScaleApplied(appliedNew);
					}

					if (nWidthOld != nWidthNew || nHeightOld != nHeightNew)
					{
						appSizeChanged(nWidthNew, nHeightNew);
					}
				}
			};

			undo.redoAction();
			getApp().addUndo(undo);
		}
	}

	@Override
	public void reset()
	{
		m_spinnerImageWidth.getValueFactory().setValue(App.DEFAULT_IMAGE_WIDTH);
		m_nWidth = App.DEFAULT_IMAGE_WIDTH;

		m_spinnerImageHeight.getValueFactory().setValue(App.DEFAULT_IMAGE_HEIGHT);
		m_nHeight = App.DEFAULT_IMAGE_HEIGHT;

		m_spinnerNumOfFrames.getValueFactory().setValue(1);
		m_nNumOfFrames = 1;

		m_spinnerMillis.getValueFactory().setValue(100);
		m_nMillis = 100;

		m_togglesDir.selectToggle(m_buttonDown);
		m_dir = Direction.DOWN;

		m_thumbnailPane.reset();
	}

	@Override
	public void collectProject(PngProject project)
	{
		String strId = getId();

		Image image = m_thumbnailPane.getViewImage();
		ScaleApplied applied = m_thumbnailPane.getScaleApplied();
		int nIndex = 0;

		if (image != null)
		{
			String strName = getId() + "/float." + nIndex + ".png";

			project.addFileDescription("png", strId, strName, nIndex, "Float generator");
			project.setMetaValue(strId, "scale", applied.toString());
			project.setMetaValue(strId, "direction", m_dir.toString());
			project.setMetaValue(strId, "width", "" + m_spinnerImageWidth.getValue());
			project.setMetaValue(strId, "height", "" + m_spinnerImageHeight.getValue());
			project.setMetaValue(strId, "frames", "" + m_spinnerNumOfFrames.getValue());
			project.setMetaValue(strId, "delay", "" + m_spinnerMillis.getValue());

			Bitmap32 bitmap = ImageUtil.bitmapFromImage(image);
			Bitmap32Sequence sequence = new Bitmap32Sequence(bitmap, false, PngAnimationType.NONE);
			project.addNamedSequence(strName, sequence);
		}
	}

	@Override
	public void applyProject(PngProject project)
	{
		String strId = getId();

		List<Element> files = project.getMetaFilesByUsage(strId); // Exactly one entry actually (0.1)
		for (int n = 0, nLen = files.size(); n < nLen; n++)
		{
			Element file = files.get(n);
			String strType = file.getAttribute("type");
			if ("png".equals(strType))
			{
				String strName = file.getAttribute("name");

				Bitmap32Sequence sequence = project.getNamedSequence(strName);
				if (sequence != null)
				{
					Image image = ImageUtil.imageFromBitmap(sequence.getDefaultBitmap());
					m_thumbnailPane.setViewImage(image);

					try
					{
						m_thumbnailPane.setScaleApplied(ScaleApplied.valueOf(project.getMetaValue(strId, "scale", "NONE")));
					}
					catch (Exception e) {}
				}
				else
				{
					Util.showError("title.project.error", "message.error.missinginternalfileinproject", strName);
				}

				m_spinnerImageWidth.getValueFactory().setValue(project.getMetaValueInt(strId, "width", App.DEFAULT_IMAGE_WIDTH));
				m_spinnerImageHeight.getValueFactory().setValue(project.getMetaValueInt(strId, "height", App.DEFAULT_IMAGE_HEIGHT));
				m_spinnerNumOfFrames.getValueFactory().setValue(project.getMetaValueInt(strId, "frames", 1));
				m_spinnerMillis.getValueFactory().setValue(project.getMetaValueInt(strId, "delay", 100));

				String strDir = project.getMetaValue(strId, "direction", "DOWN");
				try
				{
					m_dir = Direction.valueOf(strDir);
					for (Toggle toggle : m_togglesDir.getToggles())
					{
						if (m_dir.equals(toggle.getUserData()))
						{
							m_togglesDir.selectToggle(toggle);
							break;
						}
					}
				}
				catch (Exception e) {}
			}
		}
	}
}
