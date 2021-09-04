package de.unlixx.runpng.scene.settings;

import java.util.ArrayList;
import java.util.List;

import de.unlixx.runpng.App;
import de.unlixx.runpng.AppFramesView;
import de.unlixx.runpng.bitmap.Bitmap32Analyzer;
import de.unlixx.runpng.bitmap.Bitmap32Optimizer;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.FramePane;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.event.ValueEvent;
import de.unlixx.runpng.util.undo.UndoEvent;
import de.unlixx.runpng.util.undo.Undoable;
import de.unlixx.runpng.util.undo.UndoableCollection;
import de.unlixx.runpng.util.undo.UndoableIntegerSpinner;
import de.unlixx.runpng.util.undo.UndoableToggleGroup;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Implementation of a FileSettingsPane as part of the tabbed side bar.
 * This pane allows settings of the file specific variables like animation type,
 * loop count, size, interlacing or palette.
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
public class FileSettingsPane extends AbstractSettingsPane
{
	/**
	 * This enum depicts a sizechange
	 * with one of NONE, CROP or SCALE.
	 */
	protected static enum SIZECHANGE
	{
		NONE,
		CROP,
		SCALE
	};

	protected static class SIZE
	{
		SIZECHANGE m_sizeChange;
		int m_nWidth;
		int m_nHeight;
	}

	protected List<SIZE> m_stackSizeChanges = new ArrayList<>();

	protected final Tab m_tab;

	protected int m_nImageWidth;
	protected int m_nImageHeight;
	protected int m_nNumOfLoops;
	protected int m_nNumOfFrames;

	protected SIZECHANGE m_sizeChange = SIZECHANGE.NONE;
	protected SIZECHANGE m_sizeChangeApplied = SIZECHANGE.NONE;

	protected PngAnimationType m_animationType = PngAnimationType.NONE;

	protected EventHandler<ActionEvent> m_animationControlHandler;
	protected EventHandler<ActionEvent> m_animationTypeHandler;

	protected ToggleGroup m_togglesAnimationTypes;
	protected RadioButton m_radioNotAnimated;
	protected RadioButton m_radioAnimated;
	protected RadioButton m_radioAnimatedSkipFirst;

	protected TextField m_fieldNumOfFrames;

	protected CheckBox m_checkInfinite;
	protected Spinner<Integer> m_spinnerNumOfLoops;
	protected HBox m_hboxNumOfLoops;


	protected Spinner<Integer> m_spinnerImageWidth;
	protected Spinner<Integer> m_spinnerImageHeight;


	protected ToggleGroup m_togglesSizeChange;
	protected RadioButton m_radioSizeDontChange;
	protected RadioButton m_radioSizeClip;
	protected RadioButton m_radioSizeScaleToFit;

	protected Button m_buttonApplySize;

	protected CheckBox m_checkInterlaced;

	protected CheckBox m_checkPaletteOptimization;
	protected Button m_buttonPaletteAnalyze;

	/**
	 * Constructor of this FileSettingsPane.
	 *
	 * @param tab The {@link Tab} which this pane is content of.
	 */
	public FileSettingsPane(Tab tab)
	{
		m_tab = tab;

		Label label;

		m_bAdjusting = true;

		//setGridLinesVisible(true);

		BorderStroke stroke = new BorderStroke(Color.TRANSPARENT,  BorderStrokeStyle.SOLID, null, new BorderWidths(Util.SPACING));
		setBorder(new Border(stroke));

		label = createSectionLabel("label.animation");
		add(label, 0, 0, 2, 1);
		setValignment(label, VPos.BASELINE);

		m_togglesAnimationTypes = new ToggleGroup();
		m_togglesAnimationTypes.selectedToggleProperty().addListener((obs, toggleOld, toggleNew) -> handleToggleAnimationTypes(toggleOld, toggleNew));

		m_radioNotAnimated = createRadioButton("label.animated.none", "tooltip.animated.none",
				m_togglesAnimationTypes, PngAnimationType.NONE);
		add(m_radioNotAnimated, 0, 1, 4, 1);

		m_radioAnimated = createRadioButton("label.animated", "tooltip.animated",
				m_togglesAnimationTypes, PngAnimationType.ANIMATED);
		add(m_radioAnimated, 0, 2, 4, 1);

		m_radioAnimatedSkipFirst = createRadioButton("label.animated.skipfirst", "tooltip.animated.skipfirst",
				m_togglesAnimationTypes, PngAnimationType.SKIPFIRST);
		add(m_radioAnimatedSkipFirst, 0, 3, 4, 1);

		label = createRowLabel("label.numofframes");
		add(label, 0, 4, 1, 1);

		m_fieldNumOfFrames = createPositiveIntegerTextField(0, 4, 0, 999999999, Pos.CENTER_LEFT,
				"tooltip.desirednumofframes", action -> { } );
		m_fieldNumOfFrames.setDisable(true);
		add(m_fieldNumOfFrames, 2, 4, 1, 1);

		m_checkInfinite = createCheckBox("label.infiniteloop", "tooltip.infiniteloop", value -> handleCheckInfinite());
		m_checkInfinite.setSelected(true);
		add(m_checkInfinite, 0, 5, 2, 1);

		m_spinnerNumOfLoops = createIntegerSpinner(0, 0, 99999, 1, Pos.CENTER_RIGHT,
				"tooltip.desiredloops", action -> handleFieldNumOfLoops(action));
		add(m_spinnerNumOfLoops, 2, 5, 1, 1);

		label = createRowLabel("label.loops");
		add(label, 3, 5, 1, 1);

		label = createSectionLabel("label.imagesize");
		add(label, 0, 7, 2, 1);
		setValignment(label, VPos.BASELINE);

		label = createRowLabel("label.width");
		add(label, 0, 8, 1, 1);

		m_spinnerImageWidth = createIntegerSpinner(App.DEFAULT_IMAGE_WIDTH,
				App.MIN_IMAGE_WIDTH, App.MAX_IMAGE_WIDTH, 1, Pos.CENTER_RIGHT,
				"tooltip.desiredwidth", value -> handleWidthValueChange(value));
		add(m_spinnerImageWidth, 2, 8, 1, 1);

		label = new Label("px");
		add(label, 3, 8, 1, 1);

		label = createRowLabel("label.height");
		add(label, 0, 9, 1, 1);

		m_spinnerImageHeight = createIntegerSpinner(App.DEFAULT_IMAGE_HEIGHT,
				App.MIN_IMAGE_HEIGHT, App.MAX_IMAGE_HEIGHT, 1, Pos.CENTER_RIGHT,
				"tooltip.desiredheight", value -> handleHeightValueChange(value));
		add(m_spinnerImageHeight, 2, 9, 1, 1);

		label = new Label("px");
		add(label, 3, 9, 1, 1);

		m_togglesSizeChange = new ToggleGroup();
		m_togglesSizeChange.selectedToggleProperty().addListener((obs, toggleOld, toggleNew) -> handleToggleSizeChange(toggleOld, toggleNew));

		m_radioSizeDontChange = createRadioButton("label.imagesize.dontchange", "tooltip.imagesize.dontchange",
				m_togglesSizeChange, SIZECHANGE.NONE);
		add(m_radioSizeDontChange, 0, 11, 4, 1);

		m_radioSizeClip = createRadioButton("label.imagesize.croporpad", "tooltip.imagesize.croporpad",
				m_togglesSizeChange, SIZECHANGE.CROP);
		add(m_radioSizeClip, 0, 12, 4, 1);

		m_radioSizeScaleToFit = createRadioButton("label.imagesize.scaleorpad", "tooltip.imagesize.scaleorpad",
				m_togglesSizeChange, SIZECHANGE.SCALE);
		add(m_radioSizeScaleToFit, 0, 13, 4, 1);

		m_buttonApplySize = createTextButton("label.applysize", "tooltip.applysize", action -> handleSizeChange());
		add(m_buttonApplySize, 0, 15, 3, 1);

		m_togglesAnimationTypes.selectToggle(m_radioNotAnimated);
		m_togglesSizeChange.selectToggle(m_radioSizeDontChange);

		label = createSectionLabel("label.filesettings");
		add(label, 0, 17, 3, 1);
		setValignment(label, VPos.BASELINE);

		m_checkInterlaced = createCheckBox("label.interlaced", "tooltip.interlaced", value -> handleCheckInterlaced());
		add(m_checkInterlaced, 0, 19, 3, 1);

		m_checkPaletteOptimization = createCheckBox("label.palette.optimize", "tooltip.palette.optimize", action -> handleCheckPalette());
		add(m_checkPaletteOptimization, 0, 20, 3, 1);

		m_buttonPaletteAnalyze = createTextButton("label.analysis", "tooltip.palette.analysis", action -> handlePaletteAnalyze());
		add(m_buttonPaletteAnalyze, 3, 20, 1, 1);

		m_bAdjusting = false;
	}

	/**
	 * Handles a user click of the palette check box.
	 */
	protected void handleCheckPalette()
	{
		Undoable<?> undo = new Undoable<CheckBox>(m_checkPaletteOptimization, "label.palette.optimize")
		{
			final boolean m_bSelected = m_checkPaletteOptimization.isSelected();

			@Override
			public void undoAction()
			{
				m_checkPaletteOptimization.setSelected(!m_bSelected);
				handlePaletteOptimization();
			}

			@Override
			public void redoAction()
			{
				m_checkPaletteOptimization.setSelected(m_bSelected);
				handlePaletteOptimization();
			}
		};

		handlePaletteOptimization();

		getApp().addUndo(undo);
	}

	/**
	 * Handler to apply or revoke a palette optimization.
	 */
	protected void handlePaletteOptimization()
	{
		getApp().getEffectSettings().applyEffects();

		if (m_checkPaletteOptimization.isSelected())
		{
			AppFramesView frames = getApp().getFramesView();
			Bitmap32Sequence sequence = frames.createSequence(false, false, false);
			if (Bitmap32Optimizer.optimizeForPalette(sequence))
			{
				frames.applyViewsSequence(sequence);
			}
		}
	}

	/**
	 * Runs an analysis of the current image sequence for possible palette use
	 * and shows the user a box with the result.
	 */
	protected void handlePaletteAnalyze()
	{
		Bitmap32Sequence sequence = getApp().getFramesView().createSequence(false, false, false);

		Bitmap32Analyzer analyzer = new Bitmap32Analyzer();
		analyzer.analyze(sequence);
		Util.showInformation("title.analyze", "message.placeholder.string", analyzer.getAnalysisText());
	}

	/**
	 * Sets the interlace method.
	 *
	 * @param nInterlaceMethod Either 0 for none or != 0 for Adam7.
	 */
	public void setInterlaceMethod(int nInterlaceMethod)
	{
		m_checkInterlaced.setSelected(nInterlaceMethod != 0);
	}

	/**
	 * Gets the interlace method.
	 *
	 * @return Either 0 for none or 1 for Adam7.
	 */
	public int getInterlaceMethod()
	{
		return m_checkInterlaced.isSelected() ? 1 : 0;
	}

	/**
	 * Handles a user click of the interlaced check box.
	 */
	protected void handleCheckInterlaced()
	{
		if (isUserAction())
		{
			Undoable<CheckBox> undo = new Undoable<CheckBox>(m_checkInfinite, "label.interlaced")
			{
				final boolean m_bSelected = m_checkInterlaced.isSelected();

				@Override
				public void undoAction()
				{
					m_checkInterlaced.setSelected(!m_bSelected);
				}

				@Override
				public void redoAction()
				{
					m_checkInterlaced.setSelected(m_bSelected);
				}
			};

			getApp().addUndo(undo);
		}
	}

	/**
	 * Handles a change of the desired image width.
	 *
	 * @param value A {@link ValueEvent} object.
	 */
	protected void handleWidthValueChange(ValueEvent<Integer> value)
	{
		m_nImageWidth = value.getValue();
		/*
		int nWidthNew = value.getValue();

		if (m_nImageWidth != nWidthNew)
		{
			if (isUserAction())
			{
				UndoableIntegerSpinner undo = new UndoableIntegerSpinner(m_spinnerImageWidth, m_nImageWidth, nWidthNew, "label.width");
				getApp().addUndo(undo);
			}

			m_nImageWidth = nWidthNew;
		}
		*/
	}

	/**
	 * Handles a change of the desired image height.
	 *
	 * @param value A {@link ValueEvent} object.
	 */
	protected void handleHeightValueChange(ValueEvent<Integer> value)
	{
		m_nImageHeight = value.getValue();
		/*
		int nHeightNew = value.getValue();

		if (m_nImageHeight != nHeightNew)
		{
			if (isUserAction())
			{
				UndoableIntegerSpinner undo = new UndoableIntegerSpinner(m_spinnerImageHeight, m_nImageHeight, nHeightNew, "label.height");
				getApp().addUndo(undo);
			}

			m_nImageHeight = nHeightNew;
		}
		*/
	}

	/**
	 * Handles a change of the size change type by user interaction.
	 *
	 * @param toggleOld The old {@link Toggle} selected.
	 * @param toggleNew The new {@link Toggle} selected.
	 */
	protected void handleToggleSizeChange(Toggle toggleOld, Toggle toggleNew)
	{
		m_sizeChange = (SIZECHANGE)toggleNew.getUserData();
		/*
		SIZECHANGE sizeChange = (SIZECHANGE)toggleNew.getUserData();
		//m_buttonApplySize.setDisable(sizeChange == SIZECHANGE.NONE);

		if (m_sizeChange != sizeChange)
		{
			if (isUserAction())
			{
				UndoableToggleGroup undo = new UndoableToggleGroup(m_togglesAnimationTypes, toggleOld, toggleNew, ((RadioButton)toggleNew).getId());
				getApp().addUndo(undo);
			}

			m_sizeChange = sizeChange;
		}
		*/
	}

	/**
	 * Applies the recently applied size to a FramePane.
	 *
	 * @param pane A {@link FramePane} object.
	 */
	protected Image applyFrameSize(FramePane pane)
	{
		Image imageBase = pane.getBaseImage(),
				imageView = imageBase;

		for (SIZE size : m_stackSizeChanges)
		{
			switch (size.m_sizeChange)
			{
			default:
			case NONE: break;

			case CROP:
				imageView = ImageUtil.imageCropCentered(imageView, size.m_nWidth, size.m_nHeight);
				break;

			case SCALE:
				imageView = ImageUtil.imageScaleCentered(imageView, size.m_nWidth, size.m_nHeight, ImageUtil.SCALEMODE.BOTH, true);
				break;
			}
		}

		pane.setViewImage(imageView);

		return imageView;
	}

	/**
	 * Applies the recently applied size to all FramePanes.
	 */
	protected void applyFrameSizes()
	{
		AppFramesView frames = getApp().getFramesView();

		for (int n = 0, nFrames = frames.getFramesCount(); n < nFrames; n++)
		{
			applyFrameSize(frames.getFrame(n));
		}
	}

	/**
	 * Invoked from apply size button.
	 */
	protected void handleSizeChange()
	{
		final App app = getApp();

		final int nWidthNew = m_nImageWidth = m_spinnerImageWidth.getValue(),
				nHeightNew = m_nImageHeight = m_spinnerImageHeight.getValue(),
				nWidthOld = app.getImageWidth(),
				nHeightOld = app.getImageHeight();

		if (m_sizeChangeApplied != m_sizeChange || nWidthNew != nWidthOld || nHeightNew != nHeightOld)
		{
			final SIZECHANGE sizeChangeAppliedOld = m_sizeChangeApplied,
					sizeChangeAppliedNew = m_sizeChange;

			final List<SIZE> stackSizeChangesOld = m_stackSizeChanges;

			m_sizeChangeApplied = m_sizeChange;

			Undoable<FileSettingsPane> undo = new Undoable<FileSettingsPane>(this, "label.applysize")
			{
				@Override
				public void undoAction()
				{
					m_bAdjusting = true;

					m_sizeChangeApplied = sizeChangeAppliedOld;
					m_stackSizeChanges = stackSizeChangesOld;

					m_nImageWidth = nWidthOld;
					m_spinnerImageWidth.getValueFactory().setValue(nWidthOld);

					m_nImageHeight = nHeightOld;
					m_spinnerImageHeight.getValueFactory().setValue(nHeightOld);

					applyFrameSizes();

					app.setAppSize(nWidthOld, nHeightOld);

					m_bAdjusting = false;
				}

				@Override
				public void redoAction()
				{
					SIZE size;

					m_bAdjusting = true;

					m_sizeChangeApplied = sizeChangeAppliedNew;

					switch (m_sizeChangeApplied)
					{
					default:
					case NONE:
						m_stackSizeChanges = new ArrayList<>();
						break;

					case CROP:
					case SCALE:
						m_stackSizeChanges = new ArrayList<>(m_stackSizeChanges);
						size = new SIZE();
						size.m_sizeChange = sizeChangeAppliedNew;
						size.m_nWidth = nWidthNew;
						size.m_nHeight = nHeightNew;
						m_stackSizeChanges.add(size);
						break;
					}

					m_nImageWidth = nWidthNew;
					m_spinnerImageWidth.getValueFactory().setValue(nWidthNew);

					m_nImageHeight = nHeightNew;
					m_spinnerImageHeight.getValueFactory().setValue(nHeightNew);

					applyFrameSizes();

					app.setAppSize(nWidthNew, nHeightNew);

					m_bAdjusting = false;
				}
			};

			undo.redoAction();

			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					FramePane pane = app.getFramesView().getSelectedFrame();
					if (pane != null)
					{
						pane.onSelection();
					}

					for (Toggle toggle : m_togglesSizeChange.getToggles())
					{
						if (m_sizeChangeApplied.equals(toggle.getUserData()))
						{
							m_togglesSizeChange.selectToggle(toggle);
							break;
						}
					}
				}
			};

			run.run();

			undo.setRunFinally(run);

			if (isUserAction())
			{
				app.addUndo(undo);
			}
		}
	}

	/**
	 * Gets the enumeration type of the chosen size change.
	 *
	 * @return One of the {@link SIZECHANGE} enumeration types.
	 */
	public SIZECHANGE getSizeChangeType()
	{
		return m_sizeChange;
	}

	/**
	 * Handles a change of the animation type by user interaction.
	 *
	 * @param toggleOld The old {@link Toggle} selected.
	 * @param toggleNew The new {@link Toggle} selected.
	 */
	protected void handleToggleAnimationTypes(Toggle toggleOld, Toggle toggleNew)
	{
		Toggle toggle = m_togglesAnimationTypes.getSelectedToggle();
		PngAnimationType pngAnimationType = (PngAnimationType)toggle.getUserData();
		String strUndo = "";

		switch (pngAnimationType)
		{
		case NONE:
			strUndo = "label.animated.none";
			m_checkInfinite.setDisable(true);
			m_spinnerNumOfLoops.setDisable(true);
			break;

		case ANIMATED:
			strUndo = "label.animated";
			m_checkInfinite.setDisable(false);
			m_spinnerNumOfLoops.setDisable(false);
			break;

		case SKIPFIRST:
			strUndo = "label.animated.skipfirst";
			m_checkInfinite.setDisable(false);
			m_spinnerNumOfLoops.setDisable(false);
		}

		if (pngAnimationType != m_animationType)
		{
			if (isUserAction())
			{
				UndoableToggleGroup undo = new UndoableToggleGroup(m_togglesAnimationTypes, toggleOld, toggleNew, strUndo);
				getApp().addUndo(undo);
			}

			m_animationType = pngAnimationType;

			if (m_animationTypeHandler != null)
			{
				ActionEvent event = new ActionEvent(m_togglesAnimationTypes, ActionEvent.NULL_SOURCE_TARGET);
				m_animationTypeHandler.handle(event);
			}
		}
	}

	/**
	 * Handles a user click of the infinite loop check box.
	 */
	protected void handleCheckInfinite()
	{
		UndoableCollection coll = new UndoableCollection("label.infiniteloop");

		final boolean bSelected = m_checkInfinite.isSelected();

		coll.add(new Undoable<CheckBox>(m_checkInfinite, "")
		{
			@Override
			public void undoAction()
			{
				m_checkInfinite.setSelected(!bSelected);
			}

			@Override
			public void redoAction()
			{
				m_checkInfinite.setSelected(bSelected);
			}
		});

		if (bSelected)
		{
			if (m_nNumOfLoops != 0)
			{
				UndoableIntegerSpinner undoText = new UndoableIntegerSpinner(m_spinnerNumOfLoops, m_spinnerNumOfLoops.getValue(), 0, "");
				coll.add(undoText);

				m_spinnerNumOfLoops.getValueFactory().setValue(0);
				m_nNumOfLoops = 0;

				if (isUserAction() && m_animationControlHandler != null)
				{
					ActionEvent event = new ActionEvent(m_spinnerNumOfLoops, ActionEvent.NULL_SOURCE_TARGET);
					m_animationControlHandler.handle(event);
				}
			}
		}

		if (isUserAction())
		{
			coll.setRunFinally(new Runnable()
			{
				@Override
				public void run()
				{
					m_spinnerNumOfLoops.setDisable(m_animationType == PngAnimationType.NONE);
				}
			});
			getApp().addUndo(coll);
		}
	}

	/**
	 * Handles a user change of the number of loops spinner.
	 *
	 * @param value A {@link ValueEvent} object.
	 */
	protected void handleFieldNumOfLoops(ValueEvent<Integer> value)
	{
		m_checkInfinite.setSelected(value.getValue() == 0);

		int nNumOfLoops = value.getValue();
		if (nNumOfLoops != m_nNumOfLoops)
		{
			int nOld = m_nNumOfLoops;
			m_nNumOfLoops = nNumOfLoops;

			if (isUserAction())
			{
				UndoableIntegerSpinner undo = new UndoableIntegerSpinner(m_spinnerNumOfLoops, nOld, nNumOfLoops, "label.loops");
				getApp().addUndo(undo);

				if (m_animationControlHandler != null)
				{
					ActionEvent event = new ActionEvent(m_spinnerNumOfLoops, ActionEvent.NULL_SOURCE_TARGET);
					m_animationControlHandler.handle(event);
				}
			}
		}
	}

	/**
	 * Sets an event handler for the change of the animation type.
	 *
	 * @param handler An {@link EventHandler}.
	 */
	public void setOnAnimationType(EventHandler<ActionEvent> handler)
	{
		m_animationTypeHandler = handler;
	}

	/**
	 * Sets the animation type.
	 *
	 * @param animationType A {@link PngAnimationType} enumeration value.
	 */
	public void setAnimationType(PngAnimationType animationType)
	{
		m_bAdjusting = true;

		m_animationType = animationType;

		switch (m_animationType)
		{
		case NONE: m_radioNotAnimated.setSelected(true); break;
		case ANIMATED: m_radioAnimated.setSelected(true); break;
		case SKIPFIRST: m_radioAnimatedSkipFirst.setSelected(true); break;
		}

		m_bAdjusting = false;
	}

	/**
	 * Gets the animation type.
	 *
	 * @return A {@link PngAnimationType} enumeration value.
	 */
	public PngAnimationType getAnimationType()
	{
		return m_animationType;
	}

	/**
	 * Sets the number of frames value.
	 *
	 * @param nNumOfFrames An int containing the number of frames.
	 */
	public void setNumberOfFrames(int nNumOfFrames)
	{
		m_fieldNumOfFrames.setText("" + nNumOfFrames);
		m_nNumOfFrames = nNumOfFrames;
	}

	/**
	 * Gets the number of frames value.
	 *
	 * @return An int containing the number of frames.
	 */
	public int getNumberOfFrames()
	{
		return m_nNumOfFrames;
	}

	/**
	 * Sets the number of loops value.
	 *
	 * @param nNumOfLoops An int containing the number of loops.
	 */
	public void setNumberOfLoops(int nNumOfLoops)
	{
		m_bAdjusting = true;

		m_checkInfinite.setSelected(nNumOfLoops == 0);
		m_spinnerNumOfLoops.getValueFactory().setValue(nNumOfLoops);
		m_spinnerNumOfLoops.setDisable(nNumOfLoops == 0 || m_animationType == PngAnimationType.NONE);
		m_nNumOfLoops = nNumOfLoops;

		m_bAdjusting = false;
	}

	/**
	 * Gets the number of loops value.
	 *
	 * @return An int containing the number of loops.
	 */
	public int getNumberOfLoops()
	{
		return m_nNumOfLoops;
	}

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		if (!m_bAdjusting)
		{
			m_bAdjusting = true;

			m_nImageWidth = nAppWidth;
			m_spinnerImageWidth.getValueFactory().setValue(m_nImageWidth);

			m_nImageHeight = nAppHeight;
			m_spinnerImageHeight.getValueFactory().setValue(m_nImageHeight);

			m_bAdjusting = false;
		}
	}

	/**
	 * Gets the image width value.
	 *
	 * @return An in containing the image width.
	 */
	public int getImageWidth()
	{
		return m_nImageWidth;
	}

	/**
	 * Gets the image height value.
	 *
	 * @return An in containing the image height.
	 */
	public int getImageHeight()
	{
		return m_nImageHeight;
	}

	@Override
	public void reset()
	{
		m_bAdjusting = true;

		m_togglesAnimationTypes.selectToggle(m_radioNotAnimated);
		m_animationType = PngAnimationType.NONE;

		m_fieldNumOfFrames.setText("1");
		m_nNumOfFrames = 1;

		m_checkInfinite.setSelected(true);
		m_spinnerNumOfLoops.getValueFactory().setValue(0);
		m_spinnerNumOfLoops.setDisable(true);
		m_nNumOfLoops = 0;

		m_nImageWidth = getApp().getImageWidth();
		m_spinnerImageWidth.getValueFactory().setValue(m_nImageWidth);

		m_nImageHeight = getApp().getImageHeight();
		m_spinnerImageHeight.getValueFactory().setValue(m_nImageHeight);

		m_togglesSizeChange.selectToggle(m_radioSizeDontChange);
		m_sizeChange = m_sizeChangeApplied = SIZECHANGE.NONE;
		m_stackSizeChanges = new ArrayList<>();

		m_checkInterlaced.setSelected(false);
		m_checkPaletteOptimization.setSelected(false);

		m_bAdjusting = false;
	}

	/**
	 * Handler to do an update after an undo or a redo action.
	 *
	 * @param objSource The source object which was involved in the action.
	 */
	protected void updateAfterXxdo(Object objSource)
	{
		if (m_spinnerNumOfLoops.equals(objSource))
		{
			m_nNumOfLoops = m_spinnerNumOfLoops.getValue();

			if (m_animationControlHandler != null)
			{
				ActionEvent action = new ActionEvent(m_spinnerNumOfLoops, ActionEvent.NULL_SOURCE_TARGET);
				m_animationControlHandler.handle(action);
			}
		}
		else if (m_togglesAnimationTypes.equals(objSource))
		{
			if (m_animationTypeHandler != null)
			{
				ActionEvent action = new ActionEvent(m_togglesAnimationTypes, ActionEvent.NULL_SOURCE_TARGET);
				m_animationTypeHandler.handle(action);
			}
		}
	}

	@Override
	public void undoableUndone(UndoEvent event)
	{
		updateAfterXxdo(event.getSource());
	}

	@Override
	public void undoableRedone(UndoEvent event)
	{
		updateAfterXxdo(event.getSource());
	}

	@Override
	public void collectProject(PngProject project)
	{
		project.setMetaValue("filesettings", "animationtype", m_animationType.toString());
		project.setMetaValue("filesettings", "numofframes", m_nNumOfFrames);
		project.setMetaValue("filesettings", "numofloops", m_nNumOfLoops);
		project.setMetaValue("filesettings", "imagewidth", m_nImageWidth);
		project.setMetaValue("filesettings", "imageheight", m_nImageHeight);
		project.setMetaValue("filesettings", "sizechangeapplied", m_sizeChangeApplied.toString());

		int n = 0;
		for (SIZE size : m_stackSizeChanges)
		{
			project.setMetaValue("filesettings", "stack.sizechange." + n, size.m_sizeChange.toString());
			project.setMetaValue("filesettings", "stack.sizechange.width." + n, size.m_nWidth);
			project.setMetaValue("filesettings", "stack.sizechange.height." + n, size.m_nHeight);
			n++;
		}

		project.setMetaValue("filesettings", "interlaced", "" + m_checkInterlaced.isSelected());
		project.setMetaValue("filesettings", "paletteoptimization", "" + m_checkPaletteOptimization.isSelected());
	}

	@Override
	public void applyProject(PngProject project)
	{
		m_bAdjusting = true;

		try
		{
			m_animationType = PngAnimationType.valueOf(project.getMetaValue("filesettings", "animationtype", m_animationType.toString()));
			for (Toggle toggle : m_togglesAnimationTypes.getToggles())
			{
				if (m_animationType.equals(toggle.getUserData()))
				{
					m_togglesAnimationTypes.selectToggle(toggle);
					break;
				}
			}

			m_nNumOfFrames = project.getMetaValueInt("filesettings", "numofframes", m_nNumOfFrames);
			m_fieldNumOfFrames.setText("" + m_nNumOfFrames);

			m_nNumOfLoops = project.getMetaValueInt("filesettings", "numofloops", m_nNumOfLoops);
			m_spinnerNumOfLoops.getValueFactory().setValue(m_nNumOfLoops);
			m_checkInfinite.setSelected(m_nNumOfLoops == 0);

			m_nImageWidth = project.getMetaValueInt("filesettings", "imagewidth", m_nImageWidth);
			m_spinnerImageWidth.getValueFactory().setValue(m_nImageWidth);

			m_nImageHeight = project.getMetaValueInt("filesettings", "imageheight", m_nImageHeight);
			m_spinnerImageHeight.getValueFactory().setValue(m_nImageHeight);

			String str;

			str = project.getMetaValue("filesettings", "interlaced", "" + m_checkInterlaced.isSelected());
			m_checkInterlaced.setSelected("true".equals(str));

			str = project.getMetaValue("filesettings", "paletteoptimization", "" + m_checkPaletteOptimization.isSelected());
			m_checkPaletteOptimization.setSelected("true".equals(str));

			m_sizeChangeApplied = SIZECHANGE.valueOf(project.getMetaValue("filesettings", "sizechangeapplied", m_sizeChangeApplied.toString()));
			for (Toggle toggle : m_togglesSizeChange.getToggles())
			{
				if (m_sizeChangeApplied.equals(toggle.getUserData()))
				{
					m_togglesSizeChange.selectToggle(toggle);
					break;
				}
			}

			for (int n = 0; n < 1024; n++)
			{
				str = project.getMetaValue("filesettings", "stack.sizechange." + n, "");
				if (str == "")
				{
					break;
				}

				SIZE size = new SIZE();
				size.m_sizeChange = SIZECHANGE.valueOf(str);
				size.m_nWidth = project.getMetaValueInt("filesettings", "stack.sizechange.width." + n, -1);
				size.m_nHeight = project.getMetaValueInt("filesettings", "stack.sizechange.height." + n, -1);

				m_stackSizeChanges.add(size);
			}

			if (m_stackSizeChanges.size() > 0)
			{
				applyFrameSizes();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			m_bAdjusting = false;
		}
	}
}
