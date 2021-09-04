package de.unlixx.runpng.scene.effects;

import javafx.event.ActionEvent;

public class EffectAction extends ActionEvent
{
	public EffectAction(AbstractEffectPane pane)
	{
		super(pane, ActionEvent.NULL_SOURCE_TARGET);
	}

	public AbstractEffectPane getEffectPane()
	{
		return (AbstractEffectPane)getSource();
	}
}
