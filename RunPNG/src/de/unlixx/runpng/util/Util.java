package de.unlixx.runpng.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.unlixx.runpng.App;
import de.unlixx.runpng.util.exceptions.Failure;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Static utility clas.
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
public class Util
{
	/**
	 * The common spacing for GUI elements.
	 */
	public static final double SPACING = 6;

	/**
	 * The maximum with of a {@link Tooltip}.
	 */
	public static final double TOOLTIP_MAX_WIDTH = 300;

	/**
	 * Contains the default bold font, derived from default font.
	 */
	public static final Font DEFAULT_BOLD_FONT;

	/**
	 * Contains the default title font, derived from default font.
	 */
	public static final Font DEFAULT_TITLE_FONT;

	/**
	 * Holds the information about a possibly newer version, if one detected.
	 */
	static Version m_versionDetected;

	/**
	 * Keeps the common DocumentBuilder instance.
	 */
	static DocumentBuilder m_documentBuilder;

	static
	{
		Font fontDefault = Font.getDefault();

		DEFAULT_BOLD_FONT = Font.font(fontDefault.getName(), FontWeight.BOLD, fontDefault.getSize());
		DEFAULT_TITLE_FONT = Font.font(fontDefault.getName(), FontWeight.BOLD, fontDefault.getSize() + 1);

		try
		{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			m_documentBuilder = documentBuilderFactory.newDocumentBuilder();
			m_documentBuilder.setEntityResolver(new EntityResolver()
			{
				@Override
				public InputSource resolveEntity(String strPublicId, String strSystemId) throws SAXException, IOException
				{
					int n = strSystemId.lastIndexOf("/");
					if (n >= 0)
					{
						strSystemId = strSystemId.substring(n + 1);
					}

					InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(App.RESOURCE_PATH + strSystemId);
					return new InputSource(is);
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Private constructor to prevent erreneous instatiation.
	 */
	private Util() { }

	/**
	 * Delay for test purposes. Don't use in normal app.
	 *
	 * @param nMillis The count of milliseconds to sleep.
	 */
	public static void sleep(int nMillis)
	{
		try
		{
			Thread.sleep(nMillis);
		}
		catch (InterruptedException e) {}
	}

	/**
	 * Converts a string to an int.
	 *
	 * @param strInt The string to convert.
	 * @param nDefault A default value.
	 * @return An int value. Or the default value it the string can't be converted.
	 */
	public static int toInt(String strInt, int nDefault)
	{
		try
		{
			return Integer.valueOf(strInt);
		}
		catch (Exception e) { }

		return nDefault;
	}

	/**
	 * Converts a string to a float.
	 *
	 * @param strFloat The string to convert.
	 * @param fDefault A default value.
	 * @return A float value. Or the default value it the string can't be converted.
	 */
	public static float toFloat(String strFloat, float fDefault)
	{
		try
		{
			return Float.valueOf(strFloat);
		}
		catch (NumberFormatException e) { }

		return fDefault;
	}

	/**
	 * Opens a file with a path relative to the package root and
	 * returns its contents in a string.
	 *
	 * @param strRes Filename and path relative to the package root.
	 * @return A string containing the file content.
	 */
	public static String readResourceAsString(String strRes)
	{
		InputStream is = Util.class.getClassLoader().getResourceAsStream(strRes);

		if (is != null)
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String strLine;
				StringBuffer sb = new StringBuffer();

				while ((strLine = reader.readLine()) != null)
				{
					sb.append(strLine);
				}

				reader.close();

				return sb.toString();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Gets the common DocumentBuilder instance.
	 *
	 * @return A {@link DocumentBuilder} object.
	 */
	public static DocumentBuilder getDocumentBuilder()
	{
		return m_documentBuilder;
	}

	/**
	 * Checks for the internet availability.
	 *
	 * @return true if the test server is reachable.
	 */
	public static boolean netAvailable()
	{
		try
		{
			InetAddress addr = InetAddress.getByName("github.com");
			return addr.isReachable(3000);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			//e.printStackTrace();
		}

		return false;
	}

	/**
	 * Starts a lookup for a newer version at the server.
	 * And if so then it shows a message.
	 */
	public static void checkForUpdate()
	{
		Task<Version> task = new Task<Version>()
		{
			@Override
			protected Version call() throws Exception
			{
				Version version = null;

				if (netAvailable())
				{
					try
					{
						URL url = new URL(Loc.getString("link.version.test"));
						URLConnection conn = url.openConnection();
						conn.setUseCaches(false);

						InputStream is = conn.getInputStream();
						byte[] ab = new byte[16];
						int nRead = is.read(ab);
						if (nRead > 0)
						{
							String str = new String(ab, 0, nRead).trim();
							version = Version.valueOf(str);
						}

						is.close();
					}
					catch (IOException e)
					{
						//e.printStackTrace();
					}
				}

				return version;
			}
		};

		task.setOnSucceeded(value ->
		{
			if (value != null)
			{
				m_versionDetected = task.getValue();

				if (m_versionDetected != null && App.APP_VERSION.compareTo(m_versionDetected) < 0)
				{
					showUpdate(m_versionDetected);
				}
			}
		});

		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Inflates (uncompresses) a given byte array.
	 *
	 * @param ab The byte array to inflate.
	 * @param nOffs An int containing the offset where to start inflating in the byte array.
	 * @param nLen An int containing the length of bytes to inflate.
	 * @return A byte array containing the inflated data.
	 * @throws DataFormatException In case of problems with the data format.
	 */
	public static byte[] inflate(byte[] ab, int nOffs, int nLen) throws DataFormatException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] abBuff = new byte[4096];

		Inflater inflater = new Inflater();
		inflater.setInput(ab, nOffs, nLen);

		do
		{
			int nInflated = inflater.inflate(abBuff);
			if (nInflated == 0)
			{
				inflater.end();
				return bos.toByteArray();
			}
			else
			{
				bos.write(abBuff, 0, nInflated);
			}
		}
		while (true);
	}

	/**
	 * Deflates (compresses) a given byte array.
	 *
	 * @param ab The byte array to inflate.
	 * @param nOffs An int containing the offset where to start inflating in the byte array.
	 * @param nLen An int containing the length of bytes to inflate.
	 * @return A byte array containing the inflated data.
	 * @throws DataFormatException In case of problems with the data format.
	 */
	public static byte[] deflate(byte[] ab, int nOffs, int nLen) throws DataFormatException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] abBuff = new byte[4096];

		Deflater deflater = new Deflater(9);
		deflater.setInput(ab, nOffs, nLen);
		deflater.finish();

		do
		{
			int nDeflated = deflater.deflate(abBuff);
			if (nDeflated == 0)
			{
				deflater.end();
				return bos.toByteArray();
			}
			else
			{
				bos.write(abBuff, 0, nDeflated);
			}
		}
		while (true);
	}

	/**
	 * Removes the extension from a given files name.
	 *
	 * @param file The file which contains the filename.
	 * @return A string containing the bare filename without extension.
	 */
	public static String filenameWithoutExt(File file)
	{
		return filenameWithoutExt(file.getName());
	}

	/**
	 * Removes the extension from a given filename string.
	 *
	 * @param strName The string which contains the filename.
	 * @return A string containing the bare filename without extension.
	 */
	public static String filenameWithoutExt(String strName)
	{
		int n = strName.lastIndexOf('.');
		if (n > 0)
		{
			strName = strName.substring(0, n);
		}

		return strName;
	}

	/**
	 * Iterates down the parents stack of node to find a parent of the given class.
	 *
	 * @param child The child node to search it's parents.
	 * @param clazz The parent class type wanted.
	 * @return A parent node of the given class. Or null, if not found.
	 */
	public static Node getParentNodeOfClass(Node child, Class<?> clazz)
	{
		Node node = child;
		do
		{
			node = node.getParent();
		}
		while (node != null && !clazz.isAssignableFrom(node.getClass()));

		return node;
	}

	/**
	 * Creates a tooltip which will be automatically updated on locale change.
	 *
	 * @param strId A string corresponding with an id in the resource files.
	 * @return A new {@link Tooltip} object.
	 */
	public static Tooltip createTooltip(String strId)
	{
		Tooltip tooltip = new Tooltip();
		tooltip.setId(strId);
		tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
		tooltip.setWrapText(true);
		Loc.addIdTextObject(tooltip);
		return tooltip;
	}

	/**
	 * Creates an instant tooltip for short term in the current language.
	 *
	 * @param strId A string corresponding with an id in the resource files.
	 * @param args A variable arguments list.
	 * @return A new {@link Tooltip} object.
	 */
	public static Tooltip createInstantTooltip(String strId, Object... args)
	{
		Tooltip tooltip = new Tooltip();
		tooltip.setText(Loc.getString(strId, args));
		tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
		tooltip.setWrapText(true);
		return tooltip;
	}

	/**
	 * Prepares an alert dialog to be app-modal and owned by the main app window.
	 *
	 * @param alert The {@link Alert} object.
	 */
	protected static void prepareForApp(Alert alert)
	{
		Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(App.getAppIcon());

		alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(App.getMainApp().getMainWindow());
	}

	/**
	 * Shows an update dialog. Presenting current and new version.
	 *
	 * @param versionNew An object of type {@link Version}
	 */
	public static void showUpdate(Version versionNew)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(Loc.getString("title.newerversion.available"));
		alert.setHeaderText("RunPNG");

		ImageView view = new ImageView(App.getAppIcon());
		view.setFitWidth(32);
		view.setPreserveRatio(true);
		alert.setGraphic(view);

		Text text = new Text(Loc.getString("message.newerversion.available", App.APP_VERSION.toString(), versionNew.toString()));
		Hyperlink link = new Hyperlink(Loc.getString("label.download"));
		link.setTooltip(createInstantTooltip("tooltip.download.from", Loc.getString("link.version.downloadpage")));
		link.setFont(DEFAULT_BOLD_FONT);
		link.setOnAction(value ->
		{
			HostServices hostServices = App.getMainApp().getHostServices();
			String strURI = Loc.getString("link.version.downloadpage");
			hostServices.showDocument(strURI);
		});

		VBox vbox = new VBox(SPACING, text, link);
		alert.getDialogPane().setContent(vbox);
		alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		alert.getDialogPane().getButtonTypes().remove(ButtonType.OK);

		prepareForApp(alert);
		alert.showAndWait();
	}

	/**
	 * Shows an "About" dialog.
	 */
	public static void showHelpAbout()
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(Loc.getString("title.help.about"));
		alert.setHeaderText("RunPNG");

		ImageView view = new ImageView(App.getAppIcon());
		view.setFitWidth(32);
		view.setPreserveRatio(true);
		alert.setGraphic(view);

		Text textAbout = new Text(Loc.getString("message.help.about", App.APP_VERSION.toString()));

		VBox vbox = new VBox(SPACING, textAbout);

		if (App.APP_VERSION.compareTo(m_versionDetected) < 0)
		{
			Text textUpdate = new Text(Loc.getString("message.newerversion.available", App.APP_VERSION.toString(), m_versionDetected.toString()));
			Hyperlink link = new Hyperlink(Loc.getString("label.download"));
			link.setTooltip(createInstantTooltip("tooltip.download.from", Loc.getString("link.version.downloadpage")));
			link.setFont(DEFAULT_BOLD_FONT);
			link.setOnAction(value ->
			{
				HostServices hostServices = App.getMainApp().getHostServices();
				String strURI = Loc.getString("link.version.downloadpage");
				hostServices.showDocument(strURI);
			});

			vbox.getChildren().addAll(new Separator(), textUpdate, link);
		}

		alert.getDialogPane().setContent(vbox);
		alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		alert.getDialogPane().getButtonTypes().remove(ButtonType.OK);

		prepareForApp(alert);
		alert.showAndWait();
	}

	/**
	 * Shows an information dialog with a message. Title and message will be taken from the language resources
	 * and probably filled with additional arguments.
	 *
	 * @param strTitleId A string containing a title id for lookup in the language resources.
	 * @param strMessageId A string containing an id for lookup in the language resources.
	 * @param args A variable arguments list with items to put into the message string.
	 */
	public static void showInformation(String strTitleId, String strMessageId, Object... args)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(Loc.getString(strTitleId));
		alert.setHeaderText(null);

		alert.setContentText(Loc.getString(strMessageId, args));

		prepareForApp(alert);
		alert.showAndWait();
	}

	/**
	 * Shows a Yes-Cancel dialog with a message. Title and message will be taken from the language resources
	 * and probably filled with arguments from the arguments list.
	 *
	 * @param strTitleId A string containing a title id for lookup in the language resources.
	 * @param strMessageId A string containing an id for lookup in the language resources.
	 * @param args A variable arguments list with items to put into the message string.
	 * @return An enumeration value of {@link ButtonData}
	 * with the data of the button type the user pressed.
	 */
	public static ButtonData showYesCancel(String strTitleId, String strMessageId, Object... args)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(Loc.getString(strTitleId));
		alert.setHeaderText(null);

		alert.setContentText(Loc.getString(strMessageId, args));

		ButtonType buttonYes = new ButtonType(Loc.getString("label.yes"), ButtonData.YES),
				buttonCancel = new ButtonType(Loc.getString("label.cancel"), ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonYes, buttonCancel);

		prepareForApp(alert);
		Optional<ButtonType> answer = alert.showAndWait();
		if (!answer.isPresent())
		{
			return ButtonData.CANCEL_CLOSE;
		}

		return answer.get().getButtonData();
	}

	/**
	 * Shows a Yes-No-Cancel dialog with a message. Title and message will be taken from the language resources
	 * and probably filled with arguments from the arguments list.
	 *
	 * @param strTitleId A string containing a title id for lookup in the language resources.
	 * @param strMessageId A string containing an id for lookup in the language resources.
	 * @param args A variable arguments list with items to put into the message string.
	 * @return An enumeration value of {@link ButtonData}
	 * with the data of the button type the user pressed.
	 */
	public static ButtonData showYesNoCancel(String strTitleId, String strMessageId, Object... args)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(Loc.getString(strTitleId));
		alert.setHeaderText(null);

		alert.setContentText(Loc.getString(strMessageId, args));

		ButtonType buttonYes = new ButtonType(Loc.getString("label.yes"), ButtonData.YES),
				buttonNo = new ButtonType(Loc.getString("label.no"), ButtonData.NO),
				buttonCancel = new ButtonType(Loc.getString("label.cancel"), ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);

		prepareForApp(alert);
		Optional<ButtonType> answer = alert.showAndWait();
		if (!answer.isPresent())
		{
			return ButtonData.CANCEL_CLOSE;
		}

		return answer.get().getButtonData();
	}

	/**
	 * Shows an error dialog with a message. Title and message will be taken from the language resources
	 * and probably filled with additional arguments.
	 *
	 * @param strTitleId A string containing a title id for lookup in the language resources.
	 * @param strMessageId A string containing an id for lookup in the language resources.
	 * @param args A variable arguments list with items to put into the message string.
	 */
	public static void showError(String strTitleId, String strMessageId, Object... args)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(Loc.getString(strTitleId));
		alert.setHeaderText(null);

		alert.setContentText(Loc.getString(strMessageId, args));

		ButtonType buttonOk = new ButtonType(Loc.getString("label.ok"), ButtonData.OK_DONE);
		alert.getButtonTypes().setAll(buttonOk);

		prepareForApp(alert);
		alert.showAndWait();
	}

	/**
	 * Invokes an error dialog on behalf of an IOException.
	 *
	 * @param e An {@link IOException}.
	 */
	public static void showError(IOException e)
	{
		e.printStackTrace();

		showError("title.file.error", "message.error.fileio", e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
	}

	/**
	 * Invokes an error dialog on behalf of a Failure.
	 *
	 * @param f An {@link Failure}.
	 */
	public static void showError(Failure f)
	{
		f.printStackTrace();

		showError("failure.title", "failure.text", f.getMessage());
	}

	/**
	 * Invokes an error dialog on behalf of a Throwable.
	 *
	 * @param t A generic {@link Throwable}.
	 */
	public static void showError(Throwable t)
	{
		if (t instanceof IOException)
		{
			showError((IOException)t);
			return;
		}

		if (t instanceof Failure)
		{
			showError((Failure)t);
			return;
		}

		t.printStackTrace();

		showError("title.png.error", "message.error.pngio", t.getClass().getSimpleName() + ": " + t.getLocalizedMessage());
	}
}
