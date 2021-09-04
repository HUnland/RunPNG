package de.unlixx.runpng.scene.settings;

import java.util.ArrayList;
import java.util.List;

import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.scene.FramePane;
import de.unlixx.runpng.scene.FramesBox;
import de.unlixx.runpng.scene.settings.effects.AbstractEffectPane;
import de.unlixx.runpng.scene.settings.effects.BackgroundPane;
import de.unlixx.runpng.scene.settings.effects.FloatGeneratorPane;
import de.unlixx.runpng.scene.settings.effects.ForegroundPane;
import de.unlixx.runpng.scene.settings.effects.MaskPane;
import de.unlixx.runpng.util.IdTextContainer;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.Util;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Implementation of an EffectsSettingsPane as part of the tabbed side bar.
 * This pane gathers multiple effect panes. The user can show them by choosing
 * from a combo box.
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
public class EffectsSettingsPane extends AbstractSettingsPane implements EventHandler<ActionEvent>
{
	protected final Tab m_tab;

	protected EventHandler<ActionEvent> m_handlerEffectAction;

	protected ComboBox<IdTextContainer<AbstractSettingsPane>> m_comboEffects;

	protected StackPane m_stackEffects;

	protected List<AbstractSettingsPane> m_listEffects = new ArrayList<>();
	protected List<AbstractSettingsPane> m_listGenerators = new ArrayList<>();

	/**
	 * Constructor of this EffectsSettingsPane.
	 *
	 * @param tab The {@link Tab} which this pane is content of.
	 */
	public EffectsSettingsPane(Tab tab)
	{
		m_tab = tab;

		//setGridLinesVisible(true);

		Label label;

		BorderStroke stroke = new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.NONE, null, new BorderWidths(Util.SPACING));
		setBorder(new Border(stroke));

		label = createSectionLabel("label.effect");
		label.setId("label.effect");
		label.setFont(Util.DEFAULT_TITLE_FONT);
		add(label, 0, 0, 1, 1);
		setValignment(label, VPos.CENTER);
		Loc.addIdTextObject(label);

		m_comboEffects = new ComboBox<>();
		m_comboEffects.setMaxWidth(Double.MAX_VALUE);
		m_comboEffects.setTooltip(Util.createTooltip("tooltip.effect"));
		m_comboEffects.setOnAction(value -> handleEffectChosen(value));
		m_comboEffects.setButtonCell(new ListCell<IdTextContainer<AbstractSettingsPane>>());
		add(m_comboEffects, 1, 0, 1, 1);

		Loc.addChangeListener((localeOld, localeNew) ->
		{
			// TODO: Should happen automatically
			ListCell<IdTextContainer<AbstractSettingsPane>> cell = m_comboEffects.getButtonCell();
			IdTextContainer<AbstractSettingsPane> container = m_comboEffects.getValue();
			if (cell != null && container != null)
			{
				cell.setText(Loc.getString(container.getId()));
			}
		});

		m_stackEffects = new StackPane();

		ScrollPane scroller = new ScrollPane(m_stackEffects);
		scroller.setBackground(Background.EMPTY); // Looks like a border but isn't.
		scroller.setHbarPolicy(ScrollBarPolicy.NEVER);
		add(scroller, 0, 2, 2, 1);

		addSettingsPane(new MaskPane(this), m_listEffects);
		addSettingsPane(new BackgroundPane(this), m_listEffects);
		addSettingsPane(new ForegroundPane(this), m_listEffects);

		addSettingsPane(new FloatGeneratorPane(), m_listGenerators);

		m_comboEffects.getSelectionModel().selectFirst();
		m_comboEffects.fireEvent(new ActionEvent());
	}

	@Override
	public void reset()
	{
		for (AbstractSettingsPane settings : m_listEffects)
		{
			settings.reset();
		}

		for (AbstractSettingsPane settings : m_listGenerators)
		{
			settings.reset();
		}
	}

	/**
	 * Internally used to add an effect or a generator pane.
	 *
	 * @param pane An {@link AbstractSettingsPane}
	 * derived object.
	 * @param list A {@link List} object to collect it.
	 */
	protected void addSettingsPane(AbstractSettingsPane pane, List<AbstractSettingsPane> list)
	{
		IdTextContainer<AbstractSettingsPane> container = new IdTextContainer<AbstractSettingsPane>(pane.getId(), pane);
		Loc.addIdTextObject(container);
		m_comboEffects.getItems().add(container);
		m_stackEffects.getChildren().add(pane);

		list.add(pane);
	}

	/**
	 * Handler for a choose event of the effects combo box.
	 *
	 * @param action An {@link ActionEvent} with the effects combo box as the source.
	 */
	protected void handleEffectChosen(ActionEvent action)
	{
		IdTextContainer<AbstractSettingsPane> container = m_comboEffects.getValue();
		if (container != null)
		{
			AbstractSettingsPane pane = container.getValue();

			ObservableList<Node> list = m_stackEffects.getChildren();
			for (Node node : list)
			{
				node.setVisible(pane.equals(node));
			}

			// TODO: This should happen automatically
			ListCell<IdTextContainer<AbstractSettingsPane>> cell = m_comboEffects.getButtonCell();
			if (cell != null)
			{
				cell.setText(Loc.getString(container.getId()));
			}
		}
	}

	/**
	 * Sets an event handler for effect changes by the user.
	 *
	 * @param handler The {@link EventHandler} to inform.
	 */
	public void setOnEffectChange(EventHandler<ActionEvent> handler)
	{
		m_handlerEffectAction = handler;
	}

	@Override
	public void handle(ActionEvent action)
	{
		if (m_handlerEffectAction != null)
		{
			m_handlerEffectAction.handle(action);
		}
	}

	/**
	 * Invoked to apply effects, if any, to the given FramePane's view image.
	 *
	 * @param pane A {@link FramePane}
	 */
	public void applyEffects(FramePane pane)
	{
		FileSettingsPane fileSettings = getApp().getFileSettings();

		Image image = fileSettings.applyFrameSize(pane);

		for (AbstractSettingsPane settings : m_listEffects)
		{
			if (settings instanceof AbstractEffectPane)
			{
				image = ((AbstractEffectPane)settings).applyEffect(pane.getIndex(), image);
			}
		}

		pane.setViewImage(image);
	}

	/**
	 * Invoked to apply effects, if any, to all FramePane's view images.
	 */
	public void applyEffects()
	{
		//System.out.printf("applyEffects(%d, %d)\n", getApp().getImageWidth(), getApp().getImageHeight());

		FramesBox framesBox = getApp().getFramesBox();

		for (int nFrame = 0, nFrames = framesBox.getFramesCount(); nFrame < nFrames; nFrame++)
		{
			applyEffects(framesBox.getFrame(nFrame));
		}
	}

	@Override
	public void appSizeChanged(int nAppWidth, int nAppHeight)
	{
		for (AbstractSettingsPane settings : m_listEffects)
		{
			settings.appSizeChanged(nAppWidth, nAppHeight);
		}

		for (AbstractSettingsPane settings : m_listGenerators)
		{
			settings.appSizeChanged(nAppWidth, nAppHeight);
		}

		applyEffects();
	}

	@Override
	public void collectProject(PngProject project)
	{
		for (AbstractSettingsPane settings : m_listEffects)
		{
			settings.collectProject(project);
		}

		for (AbstractSettingsPane settings : m_listGenerators)
		{
			settings.collectProject(project);
		}
	}

	@Override
	public void applyProject(PngProject project)
	{
		for (AbstractSettingsPane settings : m_listEffects)
		{
			settings.applyProject(project);
		}

		for (AbstractSettingsPane settings : m_listGenerators)
		{
			settings.applyProject(project);
		}

		applyEffects();
	}
}
