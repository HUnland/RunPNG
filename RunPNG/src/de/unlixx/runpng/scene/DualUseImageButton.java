package de.unlixx.runpng.scene;

import de.unlixx.runpng.util.Loc;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Implementation of a DualUseImageButton. This kind of Button has
 * a set of two image icons and two ids. It will be used in a rare
 * case where one Button at one place has to be used either for one
 * funtionality or for another one.
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
public class DualUseImageButton extends Button
{
	protected String m_strId1;
	protected Node m_node1;
	protected String m_strTooltipId1;
	protected String m_strId2;
	protected Node m_node2;
	protected String m_strTooltipId2;

	protected boolean m_bStatus2;

	/**
	 * Constructor for this DualUseImageButton. The initial id and
	 * the initial image will be set separately.
	 */
	public DualUseImageButton()
	{
	}

	/**
	 * Stores the id for status 1.
	 *
	 * @param str A string containing the id.
	 */
	public void setId1(String str)
	{
		m_strId1 = str;
	}

	/**
	 * Stores a graphical Node for status 1.
	 *
	 * @param node An object of type {@link Node}.
	 */
	public void setNode1(Node node)
	{
		m_node1 = node;
	}

	/**
	 * Stores the tooltip id for status 1.
	 *
	 * @param str A string containing the tooltip id.
	 */
	public void setTooltipId1(String str)
	{
		m_strTooltipId1 = str;
	}

	/**
	 * Stores the id for status 2.
	 *
	 * @param str A string containing the id.
	 */
	public void setId2(String str)
	{
		m_strId2 = str;
	}

	/**
	 * Stores a graphical Node for status 2.
	 *
	 * @param node An object of type {@link Node}.
	 */
	public void setNode2(Node node)
	{
		m_node2 = node;
	}

	/**
	 * Stores the tooltip id for status 2.
	 *
	 * @param str A string containing the tooltip id.
	 */
	public void setTooltipId2(String str)
	{
		m_strTooltipId2 = str;
	}

	/**
	 * Checks whether this button is in status 1.
	 *
	 * @return True if this button is in status 1.
	 */
	public boolean isStatus1()
	{
		return !m_bStatus2;
	}

	/**
	 * Switches this button to status 1.
	 */
	public void setStatus1()
	{
		if (m_bStatus2)
		{
			setId(m_strId1);

			if (m_node1 != null)
			{
				setGraphic(m_node1);
			}

			Tooltip tooltip = getTooltip();
			if (tooltip != null && m_strTooltipId1 != null)
			{
				tooltip.setText(Loc.getString(m_strTooltipId1));
			}

			m_bStatus2 = false;
		}
	}

	/**
	 * Switches this button to status 2.
	 */
	public void setStatus2()
	{
		if (!m_bStatus2)
		{
			setId(m_strId2);

			if (m_node2 != null)
			{
				setGraphic(m_node2);
			}

			Tooltip tooltip = getTooltip();
			if (tooltip != null && m_strTooltipId2 != null)
			{
				tooltip.setText(Loc.getString(m_strTooltipId2));
			}

			m_bStatus2 = true;
		}
	}
}
