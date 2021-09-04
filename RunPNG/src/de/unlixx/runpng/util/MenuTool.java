package de.unlixx.runpng.util;

import java.io.InputStream;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.unlixx.runpng.App;
import de.unlixx.runpng.scene.DualUseImageButton;
import de.unlixx.runpng.scene.DualUseMenuItem;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;

/**
 * MenuTool creates menubars, menus, contextmenus and toolbars from
 * XML resources and icons.
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
public class MenuTool
{
	public static final double MENU_ICONSIZE = 16;
	public static final double TOOLBAR_ICONSIZE = 20;

	/**
	 * Constructor for MenuTool.
	 *
	 */
	public MenuTool()
	{
	}

	/**
	 * Creates a single menu item from information in the given element.
	 *
	 * @param element An {@link Element} object containing information about the menu item to create.
	 * @param handlerAction An {@link EventHandler} to invoke in case of a menu item action.
	 * @return A {@link MenuItem} object.
	 */
	public MenuItem createMenuItem(Element element, EventHandler<ActionEvent> handlerAction)
	{
		MenuItem item = null;

		String strType = element.getTagName();
		if ("menuitem".equals(strType) || "dualusemenuitem".equals(strType))
		{
			item = "dualusemenuitem".equals(strType) ? new DualUseMenuItem() : new MenuItem();
			item.setId(element.getAttribute("id"));

			if (item instanceof DualUseMenuItem)
			{
				DualUseMenuItem dualuse = ((DualUseMenuItem)item);
				dualuse.setId1(element.getAttribute("id"));
				dualuse.setId2(element.getAttribute("id2"));
			}

			String strIcon = element.getAttribute("icon");
			if (strIcon != "")
			{
				ImageView view = new ImageView(App.RESOURCE_PATH + strIcon);
				view.setFitHeight(MENU_ICONSIZE);
				view.setPreserveRatio(true);
				item.setGraphic(view);

				if (item instanceof DualUseMenuItem)
				{
					DualUseMenuItem dualuse = ((DualUseMenuItem)item);
					dualuse.setNode1(view);

					strIcon = element.getAttribute("icon2");
					if (strIcon.length() > 0 && view != null)
					{
						view = new ImageView(App.RESOURCE_PATH + strIcon);
						view.setFitHeight(MENU_ICONSIZE);
						view.setPreserveRatio(true);
						dualuse.setNode2(view);
					}
				}
			}

			String strAccel = element.getAttribute("accel");
			if (strAccel != "")
			{
				item.setAccelerator(KeyCombination.keyCombination(strAccel));
			}

			item.setOnAction(handlerAction);

			Loc.addIdTextObject(item);
		}
		else if ("separator".equals(strType))
		{
			item = new SeparatorMenuItem();
		}
		else
		{
			System.out.println("Unexpected MenuItem of type: " + strType);
		}

		return item;
	}

	/**
	 * Creates a whole menu from information in the given element.
	 *
	 * @param element An {@link Element} object containing information about the menu to create.
	 * @param handlerAction An {@link EventHandler} to invoke in case of a menu item action.
	 * This will be passed to the menu items to create.
	 * @param handlerValidation An {@link EventHandler} to invoke in case of menu validation.
	 * @return A {@link Menu} object.
	 */
	public Menu createMenu(Element element,
			EventHandler<ActionEvent> handlerAction, EventHandler<Event> handlerValidation)
	{
		Menu menu = new Menu();
		menu.setId(element.getAttribute("id"));
		menu.setOnShowing(event ->
		{
			Object o = event.getSource();
			if (o instanceof Menu)
			{
				ObservableList<MenuItem> items = ((Menu)o).getItems();
				Iterator<MenuItem> it = items.iterator();

				while (it.hasNext())
				{
					MenuItem item = it.next();
					if (null != item.getId())
					{
						Event action = new ActionEvent(item, Event.NULL_SOURCE_TARGET);
						handlerValidation.handle(action);
					}
				}
			}
		});

		Loc.addIdTextObject(menu);

		NodeList nodes = element.getChildNodes();
		for (int n = 0, nLen = nodes.getLength(); n < nLen; n++)
		{
			if (nodes.item(n) instanceof Element)
			{
				MenuItem item = createMenuItem((Element)nodes.item(n), handlerAction);
				if (item != null)
				{
					menu.getItems().add(item);
				}
			}
		}

		return menu;
	}

	/**
	 * Creates a whole context menu from information in the given JSONObject.
	 *
	 * @param element An {@link Element} object containing information about the menu to create.
	 * @param handlerAction An {@link EventHandler} to invoke in case of a menu item action.
	 * This will be passed to the menu items to create.
	 * @param handlerValidation An {@link EventHandler} to invoke in case of context menu validation.
	 * @return A {@link ContextMenu} object.
	 */
	public ContextMenu createContextMenu(Element element,
			EventHandler<ActionEvent> handlerAction, EventHandler<Event> handlerValidation)
	{
		ContextMenu menu = new ContextMenu();
		menu.setId(element.getAttribute("id"));

		menu.setOnShowing(event ->
		{
			Object o = event.getSource();
			if (o instanceof ContextMenu)
			{
				ObservableList<MenuItem> items = ((ContextMenu)o).getItems();
				Iterator<MenuItem> it = items.iterator();

				while (it.hasNext())
				{
					MenuItem item = it.next();
					if (null != item.getId())
					{
						Event action = new ActionEvent(item, Event.NULL_SOURCE_TARGET);
						handlerValidation.handle(action);
					}
				}
			}
		});

		NodeList nodes = element.getChildNodes();
		for (int n = 0, nLen = nodes.getLength(); n < nLen; n++)
		{
			if (nodes.item(n) instanceof Element)
			{
				MenuItem item = createMenuItem((Element)nodes.item(n), handlerAction);
				if (item != null)
				{
					menu.getItems().add(item);
				}
			}
		}

		return menu;
	}

	/**
	 * Creates a context menu from XML file.
	 *
	 * @param strResName A string containing the name of the resource file.
	 * @param strSection A string containing the name of the section in the file.
	 * @param handlerAction An {@link EventHandler} to invoke in case of a menu item action.
	 * This will be passed to the menu items to create.
	 * @param handlerValidation An {@link EventHandler} to invoke in case of context menu validation.
	 * @return A {@link ContextMenu} object.
	 */
	public ContextMenu readContextMenu(String strResName, String strSection,
			EventHandler<ActionEvent> handlerAction, EventHandler<Event> handlerValidation)
	{
		ContextMenu menu = null;

		try
		{
			Document doc = openXMLDocument(strResName);
			if (doc != null)
			{
				Element elMenu = doc.getElementById(strSection);
				if (elMenu != null)
				{
					menu = createContextMenu(elMenu, handlerAction, handlerValidation);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return menu;
	}

	/**
	 * Creates a menu bar from XML file.
	 *
	 * @param menubar A {@link MenuBar} object to augment.
	 * @param strResName A string containing the name of the resource file.
	 * @param strSection A string containing the name of the section in the file.
	 * @param handlerAction An {@link EventHandler} to invoke in case of a menu item action.
	 * This will be passed to the menu items to create.
	 * @param handlerValidation An {@link EventHandler} to invoke in case of context menu validation.
	 */
	public void readMenuBar(MenuBar menubar, String strResName, String strSection,
			EventHandler<ActionEvent> handlerAction, EventHandler<Event> handlerValidation)
	{
		menubar.setId(strSection);

		try
		{
			Document doc = openXMLDocument(strResName);
			if (doc != null)
			{
				Element elMenuBar = doc.getElementById(strSection);
				if (elMenuBar != null)
				{
					NodeList nodes = elMenuBar.getChildNodes();
					for (int n = 0, nLen = nodes.getLength(); n < nLen; n++)
					{
						if (nodes.item(n) instanceof Element)
						{
							Menu menu = createMenu((Element)nodes.item(n), handlerAction, handlerValidation);
							if (menu != null)
							{
								menubar.getMenus().add(menu);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Creates a toggle button with an image for use in the GUI.
	 *
	 * @param strId The id string of this button.
	 * @param strTooltipId The tooltip id string.
	 * @param strImagePath The path string to an image icon. This path string will be
	 * appended to the central resource path.
	 * @return A {@link ToggleButton} object with image.
	 */
	public ToggleButton createImageToggleButton(String strId, String strTooltipId, String strImagePath)
	{
		ToggleButton button = new ToggleButton();
		button.setId(strId);
		button.setTooltip(Util.createTooltip(strTooltipId));

		ImageView view = new ImageView(App.RESOURCE_PATH + strImagePath);
		view.setFitWidth(TOOLBAR_ICONSIZE);
		view.setPreserveRatio(true);
		button.setGraphic(view);

		return button;
	}

	/**
	 * Creates a button with an image for use in the GUI.
	 *
	 * @param strId The id string of this button.
	 * @param strTooltipId The tooltip id string.
	 * @param strImagePath The path string to an image icon. This path string will be
	 * appended to the central resource path.
	 * @return A {@link Button} object with image.
	 */
	public Button createImageButton(String strId, String strTooltipId, String strImagePath)
	{
		Button button = new Button();
		button.setId(strId);
		button.setTooltip(Util.createTooltip(strTooltipId));

		ImageView view = new ImageView(App.RESOURCE_PATH + strImagePath);
		view.setFitWidth(TOOLBAR_ICONSIZE);
		view.setPreserveRatio(true);
		button.setGraphic(view);

		return button;
	}

	/**
	 * Creates a toolbar item from information in the given dom element.
	 *
	 * @param element An {@link Element} object containing information about the toolbar item to create.
	 * @param handlerAction An {@link EventHandler} to invoke in case of a toolbar item action.
	 * @return A {@link Node} object.
	 */
	public Node createToolBarItem(Element element, EventHandler<ActionEvent> handlerAction)
	{
		Node node = null;

		String strType = element.getTagName();
		if ("imagebutton".equals(strType) || "dualuseimagebutton".equals(strType))
		{
			Button button = "dualuseimagebutton".equals(strType) ? new DualUseImageButton() : new Button();
			button.setId(element.getAttribute("id"));

			if (button instanceof DualUseImageButton)
			{
				DualUseImageButton dualuse = ((DualUseImageButton)button);
				dualuse.setId1(element.getAttribute("id"));
				dualuse.setId2(element.getAttribute("id2"));
			}

			String strIcon = element.getAttribute("icon");
			if (strIcon != "")
			{
				ImageView view = new ImageView(App.RESOURCE_PATH + strIcon);
				view.setFitHeight(TOOLBAR_ICONSIZE);
				view.setPreserveRatio(true);
				button.setGraphic(view);

				if (button instanceof DualUseImageButton)
				{
					DualUseImageButton dualuse = ((DualUseImageButton)button);
					dualuse.setNode1(view);

					strIcon = element.getAttribute("icon2");
					if (strIcon != "")
					{
						view = new ImageView(App.RESOURCE_PATH + strIcon);
						view.setFitHeight(MENU_ICONSIZE);
						view.setPreserveRatio(true);
						dualuse.setNode2(view);
					}
				}
			}

			String strTooltipId = element.getAttribute("tooltip");
			if (strTooltipId != "")
			{
				Tooltip tooltip = new Tooltip();
				tooltip.setId(strTooltipId);
				tooltip.setWrapText(true);
				Loc.addIdTextObject(tooltip);
				button.setTooltip(tooltip);

				if (button instanceof DualUseImageButton)
				{
					DualUseImageButton dualuse = ((DualUseImageButton)button);
					dualuse.setTooltipId1(strTooltipId);

					strTooltipId = element.getAttribute("tooltip2");
					if (strTooltipId != "")
					{
						dualuse.setTooltipId2(strTooltipId);
					}
				}
			}

			button.setOnAction(handlerAction);

			node = button;
		}
		else if ("separator".equals(strType))
		{
			node = new Separator(Orientation.VERTICAL);
		}
		else
		{
			System.err.println("Unexpected toolbar item of type: " + strType);
		}

		return node;
	}

	/**
	 * Creates a tool bar from XML file.
	 *
	 * @param toolbar A {@link ToolBar} object to augment.
	 * @param strResName A string containing the name of the resource file.
	 * @param strSection A string containing the name of the section in the file.
	 * @param handlerAction An {@link EventHandler} to invoke in case of a menu item action.
	 * This will be passed to the menu items to create.
	 */
	public void readToolBar(ToolBar toolbar, String strResName, String strSection, EventHandler<ActionEvent> handlerAction)
	{
		toolbar.setId(strSection);

		try
		{
			Document doc = openXMLDocument(strResName);
			if (doc != null)
			{
				Element elToolBar = doc.getElementById(strSection);
				if (elToolBar != null)
				{
					NodeList nodes = elToolBar.getChildNodes();
					for (int n = 0, nLen = nodes.getLength(); n < nLen; n++)
					{
						if (nodes.item(n) instanceof Element)
						{
							Node node = createToolBarItem((Element)nodes.item(n), handlerAction);
							if (node != null)
							{
								toolbar.getItems().add(node);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Opens an XML document in the resources directory.
	 *
	 * @param strResName A String containing the name of the resourcefile ("xyz.xml").
	 * @return A {@link Document} object.
	 */
	Document openXMLDocument(String strResName)
	{
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(App.RESOURCE_PATH + strResName))
		{
			return Util.getDocumentBuilder().parse(is);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
