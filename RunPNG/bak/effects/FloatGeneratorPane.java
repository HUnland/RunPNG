package de.unlixx.runpng.scene.effects;

import de.unlixx.runpng.App;
import de.unlixx.runpng.bitmap.Bitmap32;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngAnimationType;
import de.unlixx.runpng.png.PngDelayFraction;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import de.unlixx.runpng.scene.ThumbnailView;
import de.unlixx.runpng.util.ImageUtil;
import de.unlixx.runpng.util.Util;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FloatGeneratorPane extends AbstractThumbnailPane
{
	protected ToggleButton m_buttonRight;
	protected ToggleButton m_buttonUp;
	protected ToggleButton m_buttonLeft;
	protected ToggleButton m_buttonDown;
	protected ToggleGroup m_togglesDir;

	protected DIRECTION m_dir;

	protected Spinner<Integer> m_spinnerImageWidth;
	protected int m_nWidth;

	protected Spinner<Integer> m_spinnerImageHeight;
	protected int m_nHeight;

	protected Spinner<Integer> m_spinnerNumOfFrames;
	protected int m_nNumOfFrames;

	protected Spinner<Integer> m_spinnerMillis;
	protected int m_nMillis;

	protected Button m_generateSequence;

	public FloatGeneratorPane()
	{
		setId("effect.generator.float");

		//setGridLinesVisible(true);

		m_bAdjusting = true;

		Label label;

		label = createRowLabel("label.float.pic");
		add(label, 0, 0, 1, 1);

		m_buttonPicLoad = createImageButton("float.pic.open", "tooltip.float.load", "icons/32x32/file.open.png", action -> { handlePicLoad(); });
		add(m_buttonPicLoad, 1, 0, 1, 1);

		m_buttonPicRemove = createImageButton("float.pic.remove", "tooltip.float.remove", "icons/32x32/trashbin.png", action -> { handlePicRemove(); });
		add(m_buttonPicRemove, 2, 0, 1, 1);

		m_vboxImageScale = new VBox();
		m_vboxImageScale.setAlignment(Pos.TOP_RIGHT);
		m_vboxImageScale.setSpacing(Util.SPACING);
		add(m_vboxImageScale, 0, 1, 1, 1);

		m_buttonScaleSym = createImageButton("float.pic.scale.both", "tooltip.scale.sym", "icons/32x32/scale.symmetrical.png", action -> { handleScaleSymmetrical(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleSym);

		m_buttonScaleBoth = createImageButton("float.pic.scale.both", "tooltip.scale.both", "icons/32x32/scale.both.png", action -> { handleScaleBoth(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleBoth);

		m_buttonScaleHorz = createImageButton("float.pic.scale.horz", "tooltip.scale.horz", "icons/32x32/scale.horz.png", action -> { handleScaleHorz(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleHorz);

		m_buttonScaleVert = createImageButton("float.pic.scale.vert", "tooltip.scale.vert", "icons/32x32/scale.vert.png", action -> { handleScaleVert(); });
		m_vboxImageScale.getChildren().add(m_buttonScaleVert);

		m_buttonRevert = createImageButton("float.pic.scale.vert", "tooltip.revert", "icons/32x32/revert.png", action -> { handleRevert(); });
		m_vboxImageScale.getChildren().add(m_buttonRevert);

		m_thumbnail = new ThumbnailView(true, THUMBNAIL_WIDTH, getAppWidth(), getAppHeight());
		add(m_thumbnail, 1, 1, 4, 1);
		m_thumbnail.setAlignment(Pos.TOP_LEFT);

		label = createRowLabel("label.float.dir");
		label.setAlignment(Pos.CENTER_LEFT);
		add(label, 0, 2, 1, 2);

		m_togglesDir = new ToggleGroup();

		m_buttonRight = createImageToggleButton("float.dir.right", "tooltip.float.dir.right", "icons/32x32/dir.right.png", null);
		m_buttonRight.setUserData(DIRECTION.RIGHT);
		m_buttonRight.setToggleGroup(m_togglesDir);
		add(m_buttonRight, 3, 2, 1, 2);

		m_buttonUp = createImageToggleButton("float.dir.up", "tooltip.float.dir.up", "icons/32x32/dir.up.png", null);
		m_buttonUp.setUserData(DIRECTION.UP);
		m_buttonUp.setToggleGroup(m_togglesDir);
		add(m_buttonUp, 2, 2, 1, 1);

		m_buttonLeft = createImageToggleButton("float.dir.left", "tooltip.float.dir.left", "icons/32x32/dir.left.png", null);
		m_buttonLeft.setUserData(DIRECTION.LEFT);
		m_buttonLeft.setToggleGroup(m_togglesDir);
		add(m_buttonLeft, 1, 2, 1, 2);

		m_buttonDown = createImageToggleButton("float.dir.down", "tooltip.float.dir.down", "icons/32x32/dir.down.png", null);
		m_buttonDown.setUserData(DIRECTION.DOWN);
		m_buttonDown.setToggleGroup(m_togglesDir);
		add(m_buttonDown, 2, 3, 1, 1);

		m_togglesDir.selectToggle(m_buttonDown);
		m_dir = DIRECTION.DOWN;

		m_togglesDir.selectedToggleProperty().addListener((toggle, toggleOld, toggleNew) -> { handleDirToggle(toggleOld, toggleNew); });

		label = createRowLabel("label.widthxheight");
		add(label, 0, 4, 1, 1);

		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setSpacing(Util.SPACING);

		m_spinnerImageWidth = createIntegerSpinner(AppMain.DEFAULT_IMAGE_WIDTH,
				AppMain.MIN_IMAGE_WIDTH, AppMain.MAX_IMAGE_WIDTH, 1, Pos.CENTER_RIGHT,
				"tooltip.desiredwidth", action -> { handleImageWidth(action); });

		m_spinnerImageHeight = createIntegerSpinner(AppMain.DEFAULT_IMAGE_HEIGHT,
				AppMain.MIN_IMAGE_HEIGHT, AppMain.MAX_IMAGE_HEIGHT, 1, Pos.CENTER_RIGHT,
				"tooltip.desiredheight", action -> { handleImageHeight(action); });

		hbox.getChildren().addAll(m_spinnerImageWidth, new Label("x"), m_spinnerImageHeight, new Label("px"));
		add(hbox, 1, 4, 4, 1);

		label = createRowLabel("label.numofframes");
		add(label, 0, 5, 1, 1);
		setValignment(label, VPos.CENTER);

		m_spinnerNumOfFrames = createIntegerSpinner(1, 1, 1000, 1, Pos.CENTER_RIGHT,
				"tooltip.desirednumofframes", action -> { handleNumOfFrames(action); } );
		add(m_spinnerNumOfFrames, 1, 5, 3, 1);
		m_nNumOfFrames = 1;

		label = createRowLabel("label.milliseconds");
		add(label, 0, 6, 1, 1);
		setValignment(label, VPos.CENTER);

		m_spinnerMillis = createIntegerSpinner(100, 0, 65535000, 10, Pos.CENTER_RIGHT,
				"tooltip.fraction.millis", action -> { handleFieldMillis(action); });
		add(m_spinnerMillis, 1, 6, 3, 1);
		m_nMillis = 100;

		label = new Label("ms");
		add(label, 4, 6, 1, 1);
		setValignment(label, VPos.CENTER);

		m_generateSequence = createTextButton("label.generate.sequence", "tooltip.generate.sequence", action -> { generateSequence(); } );
		add(m_generateSequence, 0, 7, 1, 1);

		updateUI();

		m_bAdjusting = false;
	}

	protected void handleDirToggle(Toggle toggleOld, Toggle toggleNew)
	{
		if (toggleNew == null)
		{
			toggleOld.setSelected(true);
		}
		else
		{
			m_dir = (DIRECTION)toggleNew.getUserData();
		}
	}

	protected void handleImageWidth(ActionEvent action)
	{

	}

	protected void handleImageHeight(ActionEvent action)
	{

	}

	protected void handleNumOfFrames(ActionEvent action)
	{
		m_nNumOfFrames = m_spinnerNumOfFrames.getValue();
	}

	protected void handleFieldMillis(ActionEvent action)
	{
		m_nMillis = m_spinnerMillis.getValue();
	}

	protected void generateSequence()
	{
		Bitmap32 bitmap = ImageUtil.bitmapFromImage(m_thumbnail.getViewImage()),
				bitmapNew;

		PngDelayFraction fraction = new PngDelayFraction(m_nMillis);
		PngFrameControl control = new PngFrameControl(bitmap.getWidth(), bitmap.getHeight(), 0, 0, fraction.getNumerator(), fraction.getDenominator());
		bitmap.setFrameControl(control);

		Bitmap32Sequence sequence = new Bitmap32Sequence(bitmap, false, PngAnimationType.KEEP_DEFAULT);

		switch (m_dir)
		{
		case DOWN:
		case UP:
			double dLines = bitmap.getHeight() / (double)m_nNumOfFrames,
				dOffsY = dLines;

			for (int n = 1; n < m_nNumOfFrames; n++)
			{
				switch (m_dir)
				{
				case DOWN:
					bitmapNew = ImageUtil.bitmapShiftDown(bitmap, (int)dOffsY);
					break;

				default:
					bitmapNew = ImageUtil.bitmapShiftUp(bitmap, (int)dOffsY);
					break;
				}

				bitmapNew.setFrameControl(control);
				sequence.addFrame(bitmapNew);
				dOffsY += dLines;
			}
			break;

		case RIGHT:
		case LEFT:
			double dCols = bitmap.getWidth() / (double)m_nNumOfFrames,
				dOffsX = dCols;

			for (int n = 1; n < m_nNumOfFrames; n++)
			{
				switch (m_dir)
				{
				case RIGHT:
					bitmapNew = ImageUtil.bitmapShiftRight(bitmap, (int)dOffsX);
					break;

				default:
					bitmapNew = ImageUtil.bitmapShiftLeft(bitmap, (int)dOffsX);
					break;
				}

				bitmapNew.setFrameControl(control);
				sequence.addFrame(bitmapNew);
				dOffsX += dCols;
			}
			break;
		}

		getApp().getFramesView().applyBitmapSequence(sequence, true);
	}

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		m_spinnerImageWidth.getValueFactory().setValue(nAppWidth);
		m_nWidth = nAppWidth;

		m_spinnerImageHeight.getValueFactory().setValue(nAppHeight);
		m_nHeight = nAppHeight;

		m_thumbnail.appSizeChanged(m_nWidth, m_nHeight);
	}

	@Override
	protected void handleApply(String strUndoId)
	{
		// TODO: Undo
		updateUI();
	}
}
