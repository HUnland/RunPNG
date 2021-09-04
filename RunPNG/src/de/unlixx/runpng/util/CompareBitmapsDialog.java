package de.unlixx.runpng.util;

import de.unlixx.runpng.App;
import de.unlixx.runpng.bitmap.Bitmap32;
import de.unlixx.runpng.png.chunks.PngFrameControl;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

/**
 * Just for testing purposes.
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
 *
 */
public class CompareBitmapsDialog extends Dialog<Boolean>
{
	protected BorderPane m_rootPane;
	protected HBox m_centerPane;

	public CompareBitmapsDialog(int nNumber, Bitmap32 bitmapLeft, Bitmap32 bitmapRight, PngFrameControl fcTL)
	{
		Image imageLeft = null, imageRight = null;

		if (bitmapLeft != null)
		{
			imageLeft = ImageUtil.imageFromBitmap(bitmapLeft);
		}

		if (bitmapRight != null)
		{
			imageRight = ImageUtil.imageFromBitmap(bitmapRight);
		}

		initialize(nNumber, imageLeft, imageRight, fcTL);
	}

	public CompareBitmapsDialog(int nNumber, Image imageLeft, Image imageRight, PngFrameControl fcTL)
	{
		initialize(nNumber, imageLeft, imageRight, fcTL);
	}

	protected void initialize(int nNumber, Image imageLeft, Image imageRight, PngFrameControl fcTL)
	{
		setTitle("Compare Bitmaps #" + nNumber);

		initModality(Modality.APPLICATION_MODAL);

		m_rootPane = new BorderPane();

		VBox vboxLabels = new VBox();
		vboxLabels.setSpacing(Util.SPACING);

		String strLabel;

		strLabel = "Dispose Op: ";
		switch (fcTL.getDisposeOp())
		{
		case PngFrameControl.DISPOSE_OP_NONE:
			strLabel += "NONE";
			break;

		case PngFrameControl.DISPOSE_OP_BACKGROUND:
			strLabel += "BACKGROUND";
			break;

		case PngFrameControl.DISPOSE_OP_PREVIOUS:
			strLabel += "PREVIOUS";
			break;
		}

		strLabel += ", Blend Op: ";
		switch (fcTL.getBlendOp())
		{
		case PngFrameControl.BLEND_OP_SOURCE:
			strLabel += "SOURCE";
			break;

		case PngFrameControl.BLEND_OP_OVER:
			strLabel += "OVER";
			break;
		}

		Label label = new Label(strLabel);
		vboxLabels.getChildren().add(label);

		strLabel = "x = " + fcTL.getXOffset() + ", y = " + fcTL.getYOffset() + ", width = " + fcTL.getWidth() + ", height = " + fcTL.getHeight()
			+ ", blend op = " + (fcTL.getBlendOp() == PngFrameControl.BLEND_OP_SOURCE ? "source" : "over");
		label = new Label(strLabel);
		vboxLabels.getChildren().add(label);

		m_rootPane.setTop(vboxLabels);

		m_centerPane = new HBox();
		m_centerPane.setSpacing(Util.SPACING);
		m_centerPane.setAlignment(Pos.CENTER);

		if (imageLeft != null)
		{
			ImageView viewLeft = new ImageView(imageLeft);
			m_centerPane.getChildren().add(viewLeft);
		}

		if (imageRight != null)
		{
			ImageView viewRight = new ImageView(imageRight);
			m_centerPane.getChildren().add(viewRight);
		}

		m_rootPane.setCenter(m_centerPane);

		ButtonType buttonTypeOk = new ButtonType("Next >", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);

		DialogPane dialogPane = getDialogPane();
		dialogPane.setContent(m_rootPane);

		initOwner(App.getMainApp().getMainWindow());
	}
}
