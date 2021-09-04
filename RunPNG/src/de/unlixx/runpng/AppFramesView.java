package de.unlixx.runpng;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Element;

import de.unlixx.runpng.bitmap.Bitmap32;
import de.unlixx.runpng.bitmap.Bitmap32Optimizer;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngColorType;
import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.png.chunks.PngAnimationControl;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.png.chunks.PngHeader;
import de.unlixx.runpng.scene.FramePane;
import de.unlixx.runpng.scene.FramesBox;
import de.unlixx.runpng.scene.settings.FileSettingsPane;
import de.unlixx.runpng.scene.settings.FrameSettingsPane;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.PivotSorter;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.event.ValueEvent;
import de.unlixx.runpng.util.undo.Undoable;
import de.unlixx.runpng.util.undo.UndoableCollection;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Implements an AppFramesView in order to show and manage a sequence
 * of images. This class derives from {@link ScrollPane}
 * and creates a {@link FramesBox} to
 * store the images in {@link FramePane} containers.
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
public class AppFramesView extends ScrollPane
{
	final protected App m_app = App.getMainApp();
	final protected FramesBox m_framesBox;
	final protected ContextMenu m_contextMenu;

	/**
	 * Constructor of this AppFramesView. Creates the {@link FramesBox}
	 * and sets it as the content of this class.
	 */
	public AppFramesView()
	{
		setBackground(Background.EMPTY);

		BorderStroke stroke;

		stroke = new BorderStroke(Color.LIGHTGREY,  BorderStrokeStyle.SOLID, null, new BorderWidths(2, 0, 0, 0));
		setBorder(new Border(stroke));

		m_framesBox = new FramesBox(this, Util.SPACING);
		stroke = new BorderStroke(Color.TRANSPARENT,  BorderStrokeStyle.SOLID, null, new BorderWidths(Util.SPACING / 2, Util.SPACING, Util.SPACING / 2, Util.SPACING));
		m_framesBox.setBorder(new Border(stroke));

		setContent(m_framesBox);
		setMinViewportHeight(120);

		m_framesBox.setOnFramesChange(action ->
		{
			m_app.getFileSettings().setNumberOfFrames(m_framesBox.getFramesCount());
		});

		m_contextMenu = m_app.getMenuTool().readContextMenu("menusandbars.xml", "contextmenu.frame",
				action -> handleContextMenu(action), event -> validateContextMenu(event));
	}

	/**
	 * Gets the FramesBox which is content of this class.
	 *
	 * @return The {@link FramesBox} content.
	 */
	public FramesBox getFramesBox()
	{
		return m_framesBox;
	}

	/**
	 * Sets the adjusting flag in order to don't start actions caused by
	 * adjusting operations.
	 *
	 * @param bAdjusting True if the following operations shall be ignored
	 * by change listeners and similar.
	 */
	public void setAdjusting(boolean bAdjusting)
	{
		m_framesBox.setAdjusting(bAdjusting);
	}

	/**
	 * Removes all FramePanes from the {@link FramesBox} content.
	 */
	public void clear()
	{
		m_framesBox.clear();
	}

	/**
	 * Called by the application in case of an app image size change. It dispatches the
	 * new sizes to all FramePanes.
	 *
	 * @param nWidth The new image width.
	 * @param nHeight The new image height.
	 */
	void appSizeChanged(int nWidth, int nHeight)
	{
		for (int n = 0, nLen = getFramesCount(); n < nLen; n++)
		{
			FramePane pane = getFrame(n);
			pane.appSizeChanged(nWidth, nHeight);
		}
	}

	/**
	 * Handler for context menu action events.
	 *
	 * @param action An {@link ActionEvent} object.
	 */
	void handleContextMenu(ActionEvent action)
	{
		Object o = action.getSource();
		if (o instanceof MenuItem)
		{
			MenuItem item = (MenuItem)o;
			String strId = item.getId();

			Node node = item.getParentPopup().getOwnerNode();
			if (node instanceof FramePane)
			{
				FramePane pane = (FramePane)node;
				AppFileManager filemanager = m_app.getFileManager();
				int nIdx = pane.getIndex();

				switch (strId)
				{
				case "menu.file.saveas":
					String strNameProposed = filemanager.getFilename();

					int nDigits = String.valueOf(m_framesBox.getFramesCount() - 1).length();
					strNameProposed = String.format(strNameProposed + " #%0" + nDigits + "d.png", pane.getIndex());

					filemanager.onSavePngFileAs(pane.getViewImage(), strNameProposed);
					break;

				case "menu.file.addimage":
					addPngFile(nIdx);
					break;

				case "menu.edit.duplicate":
					duplicateFrame(pane);
					break;

				case "menu.edit.delete":
					removeFrame(pane);
					break;
				}
			}
		}
	}

	/**
	 * Validates menu items if needed.
	 *
	 * @param event An {@link Event} object.
	 */
	void validateContextMenu(Event event)
	{
		Object o = event.getSource();
		if (o instanceof MenuItem)
		{
			MenuItem item = (MenuItem)o;
			String strId = item.getId();

			switch (strId)
			{
			case "menu.edit.delete":
				item.setDisable(m_framesBox.getFramesCount() < 2);
				break;
			}
		}
	}

	/**
	 * Adds a png file after the given index. This can be a single image
	 * or a whole image sequence.
	 *
	 * @param nAfterIndex The index after the png file will be inserted.
	 */
	public void addPngFile(final int nAfterIndex)
	{
		AppFileManager filemanager = m_app.getFileManager();

		File[] afiles = filemanager.fileOpenDialog("title.file.open", false,
				new ExtensionFilter(Loc.getString("filter.file.pngapng"), "*.png", "*.apng"));

		if (afiles != null)
		{
			filemanager.openPngFiles(afiles, new EventHandler<ValueEvent<Bitmap32Sequence>>()
			{
				@Override
				public void handle(ValueEvent<Bitmap32Sequence> event)
				{
					Bitmap32Sequence sequence = event.getValue();
					UndoableCollection coll = new UndoableCollection("menu.file.addimage");
					applyBitmapSequence(sequence, true, nAfterIndex + 1, coll);
					setSelectedIndex(nAfterIndex + 1);
					m_app.addUndo(coll);
				}
			});
		}
	}

	/**
	 * Removes a single FramePane from the {@link FramesBox}.
	 *
	 * @param pane A {@link FramePane} container object.
	 */
	public void removeFrame(final FramePane pane)
	{
		final int nNumber = pane.getIndex();
		m_framesBox.removeFrame(nNumber);

		Undoable<FramePane> undo = new Undoable<FramePane>(pane, "menu.edit.delete")
		{
			@Override
			public void undoAction()
			{
				m_framesBox.addFrame(pane, nNumber);
			}

			@Override
			public void redoAction()
			{
				m_framesBox.removeFrame(nNumber);
			}
		};

		m_app.addUndo(undo);
	}

	/**
	 * Duplicates the given FramePane and inserts it directly after the original.
	 *
	 * @param pane A {@link FramePane} object to duplicate.
	 * @return The new created FramePane.
	 */
	public FramePane duplicateFrame(final FramePane pane)
	{
		final int nIdx = pane.getIndex() + 1;

		// TODO: Clone or another constructor
		final FramePane paneNew = createFrame(pane.getBaseImage(), nIdx, pane.getDelayFraction().clone(), null);
		paneNew.setViewImage(pane.getViewImage());

		m_framesBox.scrollIntoView(paneNew);
		pane.setSelected(false);
		m_framesBox.setSelectedIndex(nIdx);

		Undoable<FramePane> undo = new Undoable<FramePane>(pane, "menu.edit.duplicate")
		{
			@Override
			public void undoAction()
			{
				m_framesBox.removeFrame(nIdx);
			}

			@Override
			public void redoAction()
			{
				m_framesBox.addFrame(paneNew, nIdx);
				m_framesBox.scrollIntoView(paneNew);
			}
		};

		m_app.addUndo(undo);

		return paneNew;
	}

	/**
	 * Creates a new FramePane and inserts it at the given index.
	 *
	 * @param image The {@link Image} object for the new FramePane.
	 * @param nIdx An integer containing the index where the new FramePane will be inserted.
	 * If &lt; 0 then it will be added at the end.
	 * @param fraction The {@link PngDelayFraction} object,
	 * needed to show and host the delay time.
	 * @param coll The {@link UndoableCollection} to list and manage
	 * an {@link Undoable} for this FramePane creation.
	 * If the collection is null then no undo is possible.
	 * @return The new created {@link FramePane} object.
	 */
	public FramePane createFrame(Image image, int nIdx, PngDelayFraction fraction, UndoableCollection coll)
	{
		final FramePane pane = new FramePane(image, fraction);

		if (nIdx < 0)
		{
			nIdx = m_framesBox.getFramesCount();
		}

		m_framesBox.addFrame(pane, nIdx);

		m_app.getEffectSettings().applyEffects(pane);

		final int nFrameIdx = nIdx;

		if (coll != null)
		{
			Undoable<FramePane> undo = new Undoable<FramePane>(pane, "")
			{
				@Override
				public void undoAction()
				{
					m_framesBox.removeFrame(nFrameIdx);
				}

				@Override
				public void redoAction()
				{
					m_framesBox.addFrame(pane, nFrameIdx);
				}
			};

			coll.add(undo);
		}

		pane.setOnContextMenuRequested(event ->
		{
			pane.setSelected(true);
			m_contextMenu.show((FramePane)event.getSource(), event.getScreenX(), event.getScreenY());
		});

		pane.setOnSelection(action ->
		{
			m_framesBox.scrollIntoView(pane);
			m_app.setCenterImage(pane);
		});

		return pane;
	}

	/**
	 * Gets the current number of FramePanes managed.
	 *
	 * @return An int containing the number of FramePanes.
	 */
	public int getFramesCount()
	{
		return m_framesBox.getFramesCount();
	}

	/**
	 * Gets the currently selected index.
	 *
	 * @return An int containing the index of the selected FramePane.
	 * Or -1 if none is selected.
	 */
	public int getSelectedIndex()
	{
		return m_framesBox.getSelectedIndex();
	}

	/**
	 * Sets the currently selection index.
	 *
	 * @param nIdx An int containing the index of the selected FramePane.
	 * @return A {@link FramePane} object.
	 * Or null if the index is out of bounds.
	 */
	public FramePane setSelectedIndex(int nIdx)
	{
		return m_framesBox.setSelectedIndex(nIdx);
	}

	/**
	 * Gets a FramePane at the given index.
	 *
	 * @param nIdx The index of the pane to get.
	 * @return A {@link FramePane} object.
	 */
	public FramePane getFrame(int nIdx)
	{
		return m_framesBox.getFrame(nIdx);
	}

	/**
	 * Gets the currently selected FramePane.
	 *
	 * @return A {@link FramePane} object.
	 * Or null if none is selected.
	 */
	public FramePane getSelectedFrame()
	{
		return m_framesBox.getSelectedFrame();
	}

	/**
	 * Scrolls the given FramePane into the visible part of the viewport.
	 * If the FramePane is already completely shown in the viewport then
	 * this method does nothing.
	 *
	 * @param pane A {@link FramePane} object.
	 */
	public void scrollIntoView(FramePane pane)
	{
		m_framesBox.scrollIntoView(pane);
	}

	/**
	 * Creates a Bitmap32Sequence from a given single Image object.
	 *
	 * @param image An {@link Image} object.
	 * @return The {@link Bitmap32Sequence} object.
	 */
	public Bitmap32Sequence createSequence(Image image)
	{
		Bitmap32 bitmap = ImageUtil.bitmapFromImage(image);
		Bitmap32Sequence sequence = new Bitmap32Sequence(bitmap, true, PngAnimationType.NONE);
		return sequence;
	}

	/**
	 * Creates a Bitmap32Sequence from the stored base images or from the view images.
	 * <pre>
	 * See also {@link FramePane#FramePane(Image, PngDelayFraction)} regarding base and view images.
	 * </pre>
	 *
	 * @param bBase True if the Bitmap32Sequence has to be created from base images.
	 * @param bOptimize True if the Bitmap32Sequence shall be optimized for png saving.
	 * @param bAll True if all frames must be collected. Regardless of the animation type.
	 * @return The {@link Bitmap32Sequence} object.
	 */
	public Bitmap32Sequence createSequence(boolean bBase, boolean bOptimize, boolean bAll)
	{
		FileSettingsPane fileSettings = m_app.getFileSettings();

		PngAnimationType animType = fileSettings.getAnimationType();
		int nFrames = getFramesCount(),
			nWidth = m_app.getImageWidth(),
			nHeight = m_app.getImageHeight();

		PngHeader header = new PngHeader(nWidth, nHeight, 8, PngColorType.TRUECOLOR_ALPHA,
								0, 0, fileSettings.getInterlaceMethod());

		Bitmap32Sequence sequence = new Bitmap32Sequence(header, false);

		if (bAll || (nFrames > 1 && animType != PngAnimationType.NONE))
		{
			PngAnimationControl acTL = sequence.getAnimationControl();
			acTL.setNumFrames(bAll ? nFrames : (animType == PngAnimationType.ANIMATED ? nFrames : nFrames - 1));
			acTL.setNumPlays(fileSettings.getNumberOfLoops());
		}

		sequence.setAnimationType(animType);

		FramePane pane = getFrame(0);
		Bitmap32 bitmap = ImageUtil.bitmapFromImage(bBase ? pane.getBaseImage() : pane.getViewImage(), nWidth, nHeight);
		sequence.setDefaultBitmap(bitmap);
		//setDefaultBitmap() adds it now automatically to the frames if ANIMATED.

		if (bAll || (nFrames > 1 && animType != PngAnimationType.NONE))
		{
			PngDelayFraction fraction = pane.getDelayFraction();
			bitmap.setFrameControl(new PngFrameControl(nWidth, nHeight, 0, 0, fraction.getDelayNum(), fraction.getDelayDen()));

			for (int n = 1; n < nFrames; n++)
			{
				pane = getFrame(n);
				fraction = pane.getDelayFraction();

				bitmap = ImageUtil.bitmapFromImage(bBase ? pane.getBaseImage() : pane.getViewImage(), nWidth, nHeight);
				bitmap.setFrameControl(new PngFrameControl(nWidth, nHeight, 0, 0, fraction.getDelayNum(), fraction.getDelayDen()));

				sequence.addFrame(bitmap);
			}
		}

		if (bOptimize)
		{
			Bitmap32Optimizer.optimize(sequence);
		}

		return sequence;
	}

	/**
	 * Applies a Bitmap32Sequence to this view. Which can be the first sequence or
	 * an additional.
	 *
	 * @param sequence The {@link Bitmap32Sequence} object to apply.
	 * @param bFirst True if the apply shall start from scratch.
	 */
	public void applyBitmapSequence(Bitmap32Sequence sequence, boolean bFirst)
	{
		Bitmap32Optimizer.deoptimize(sequence);

		PngAnimationType animType = sequence.getAnimationType();

		if (bFirst)
		{
			m_app.clearImage();

			setAdjusting(true);
			clear();
			setAdjusting(false);

			PngHeader header = sequence.getHeader();
			m_app.setAppSize(header.getWidth(), header.getHeight());

			FileSettingsPane fileSettings = m_app.getFileSettings();
			fileSettings.setInterlaceMethod(header.getInterlaceMethod());
			fileSettings.setAnimationType(animType);
		}

		applyBitmapSequence(sequence, false, -1, null);

		if (bFirst)
		{
			setSelectedIndex(0);

			FrameSettingsPane frameSettings = m_app.getFrameSettings();
			frameSettings.setFrameIndex(0);

			if (animType == PngAnimationType.NONE
					|| animType == PngAnimationType.SKIPFIRST)
			{
				frameSettings.setDisable(true);
			}
			else
			{
				frameSettings.setDisable(false);
			}

			m_app.validateToolBars();
			m_app.updateTitle();
		}
	}

	/**
	 * Internally used method to add the bitmap Bitmap32Sequence(s).
	 *
	 * @param sequence The {@link Bitmap32Sequence} object to apply.
	 * @param bIgnoreAnim True if the animation control of the sequence shall be ignored.
	 * @param nAtIndex The index where to insert the sequence.
	 * @param coll The {@link UndoableCollection} to list and manage
	 * the {@link Undoable} object(s).
	 * @return An int containing the index where the given sequence has been inserted.
	 */
	int applyBitmapSequence(Bitmap32Sequence sequence, boolean bIgnoreAnim, int nAtIndex, UndoableCollection coll)
	{
		int nIndex = nAtIndex < 0 ? getFramesCount() : nAtIndex;
		nAtIndex = nIndex;

		setAdjusting(true);

		if (!bIgnoreAnim)
		{
			PngAnimationControl animControl = sequence.getAnimationControl();
			if (animControl != null)
			{
				m_app.getFileSettings().setNumberOfLoops(animControl.getNumPlays());
			}
			else
			{
				m_app.getFileSettings().setNumberOfLoops(0);
			}
		}

		Bitmap32[] aBitmaps = sequence.getBitmaps();

		for (Bitmap32 bitmap : aBitmaps)
		{
			PngDelayFraction delay;
			PngFrameControl fcTL = bitmap.getFrameControl();
			if (fcTL != null)
			{
				delay = fcTL.getDelayFraction();
			}
			else
			{
				delay = new PngDelayFraction(0, 0);
			}

			createFrame(ImageUtil.imageFromBitmap(bitmap), nIndex++, delay, coll);
		}

		setAdjusting(false);

		int nFrames = getFramesCount();
		m_app.getFileSettings().setNumberOfFrames(nFrames);

		return nAtIndex;
	}

	/**
	 * Applies a sequence of views for the existing frame panes. This method has been
	 * created for palette optimization and should not be used for other purposes.
	 *
	 * @param sequence The {@link Bitmap32Sequence} object
	 * containing the new view images.
	 */
	public void applyViewsSequence(Bitmap32Sequence sequence)
	{
		Bitmap32Optimizer.deoptimize(sequence); // Shouldn't be necessary. But who knows?

		Bitmap32[] aBitmaps = sequence.getBitmaps();

		int nFrame = 0;
		for (Bitmap32 bitmap : aBitmaps)
		{
			getFrame(nFrame++).setViewImage(ImageUtil.imageFromBitmap(bitmap));
		}
	}

	/**
	 * Collects the data of this view in the given PngProject object.
	 *
	 * @param project A {@link PngProject} object.
	 */
	public void collectProject(PngProject project)
	{
		String strDir = "sequence.0";

		for (int n = 0, nLen = getFramesCount(); n < nLen; n++)
		{
			FramePane pane = getFrame(n);
			String strName = strDir + "/frame." + n + ".png";

			project.addFileDescription("png", "sequence", strName, n, "Main sequence frame");

			Bitmap32Sequence sequence = createSequence(pane.getBaseImage());
			project.addNamedSequence(strName, sequence);

			PngDelayFraction fraction = pane.getDelayFraction();
			project.setMetaValue(strName, "delay.num", fraction.getDelayNum());
			project.setMetaValue(strName, "delay.den", fraction.getDelayDen());
		}
	}

	/**
	 * Applies data from a PngProject object.
	 *
	 * @param project A {@link PngProject} object.
	 */
	public void applyProject(PngProject project)
	{
		clear();

		List<Element> files = project.getMetaFilesByUsage("sequence");
		Element[] ael = new Element[files.size()];
		ael = files.toArray(ael);

		PivotSorter.sort(ael, new Comparator<Element>()
		{
			@Override
			public int compare(Element el1, Element el2)
			{
				int nIdx1 = Util.toInt(el1.getAttribute("index"), -1),
					nIdx2 = Util.toInt(el2.getAttribute("index"), -1);
				return nIdx1 < nIdx2 ? -1 : nIdx1 > nIdx2 ? 1 : 0;
			}
		});

		for (int n = 0, nLen = ael.length; n < nLen; n++)
		{
			String strName = ael[n].getAttribute("name");

			PngDelayFraction fraction = new PngDelayFraction(
					project.getMetaValueInt(strName, "delay.num", 1),
					project.getMetaValueInt(strName, "delay.den", 100));
			Bitmap32Sequence sequence = project.getNamedSequence(strName);
			createFrame(ImageUtil.imageFromBitmap(sequence.getDefaultBitmap()), n, fraction, null);
		}

		setSelectedIndex(0);
	}
}
