package de.unlixx.runpng.scene;

import java.util.ArrayList;
import java.util.List;

import de.unlixx.runpng.App;
import de.unlixx.runpng.util.Loc;
import de.unlixx.runpng.util.undo.Undoable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;

/**
 * Implementation of a FramesBox to show a sequence of FramePane objects.
 * This is a horizontal box with unlimited maximum width. So it needes
 * to be content of a ScrollPane.
 * The added FramePane objects can be dragged by the user to change
 * their order.
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
public class FramesBox extends HBox
{
	// TODO: Drag 'n Drop
	// TODO: Multiple selection
	// TODO: Delete by delete key

	protected final ToggleGroup m_toggles;
	protected boolean m_bAdjusting;
	protected ScrollPane m_scroller;

	protected ListChangeListener<Node> m_listenerChildren;

	protected EventHandler<ActionEvent> m_framesChangeEventHandler;
	protected boolean m_bDragListening;

	/**
	 * Constructor for this FramesBox. It needs a reference to the ScrollPane,
	 * which it is content of, in order to run a {@link #scrollIntoView(Node)}
	 * method.
	 *
	 * @param scroller A {@link ScrollPane} object
	 * which this FramesBox is content of.
	 * @param dSpacing A double containing the space between the
	 * {@link FramePane} cildren.
	 */
	public FramesBox(ScrollPane scroller, double dSpacing)
	{
		super(dSpacing);

		m_scroller = scroller;

		m_toggles = new ToggleGroup();

		doChangeListening();
		doDragListening();
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
		m_bAdjusting = bAdjusting;
	}

	/**
	 * Adds a FramePane to the end of the children list.
	 *
	 * @param pane An object of type {@link FramePane}.
	 */
	public void addFrame(FramePane pane)
	{
		addFrame(pane, -1);
	}

	/**
	 * Adds a FramePane to the given index of the children list. The following FramePanes
	 * will be shifted by 1.
	 *
	 * @param pane An object of type {@link FramePane}.
	 * @param nIdx The index where to add.
	 */
	public void addFrame(FramePane pane, int nIdx)
	{
		ObservableList<Node> list = getChildren();
		if (nIdx >= 0)
		{
			pane.setIndex(nIdx);
			list.add(nIdx, pane);

			refreshPaneNumbers();
		}
		else
		{
			pane.setIndex(list.size());
			list.add(pane);
		}

		pane.setToggleGroup(m_toggles);
	}

	/**
	 * Removes a FramePane from the children list.
	 *
	 * @param nIdx The index of the pane to remove.
	 * @return The removed {@link FramePane} object.
	 */
	public FramePane removeFrame(int nIdx)
	{
		ObservableList<Node> list = getChildren();
		FramePane pane = (FramePane)list.get(nIdx);
		if (pane.isSelected())
		{
			if (nIdx > 0)
			{
				m_toggles.selectToggle((Toggle)list.get(nIdx - 1));
			}
			else if (list.size() > nIdx + 1)
			{
				m_toggles.selectToggle((Toggle)list.get(nIdx + 1));
			}
			else
			{
				throw new RuntimeException(Loc.getString("exception.dontdeletelastframe"));
			}
		}

		pane.setToggleGroup(null);
		list.remove(nIdx);

		refreshPaneNumbers();

		return pane;
	}

	/**
	 * Gets a FramePane at the given index.
	 *
	 * @param nIdx The index of the pane to get.
	 * @return A {@link FramePane} object.
	 */
	public FramePane getFrame(int nIdx)
	{
		return (FramePane)getChildren().get(nIdx);
	}

	/**
	 * Gets the current number of FramePanes managed.
	 *
	 * @return An int containing the number of FramePanes.
	 */
	public int getFramesCount()
	{
		return getChildren().size();
	}

	/**
	 * Gets the currently selected FramePane.
	 *
	 * @return A {@link FramePane} object.
	 * Or null if none is selected.
	 */
	public FramePane getSelectedFrame()
	{
		return (FramePane)m_toggles.getSelectedToggle();
	}

	/**
	 * Gets the currently selected index.
	 *
	 * @return An int containing the index of the selected FramePane.
	 * Or -1 if none is selected.
	 */
	public int getSelectedIndex()
	{
		Toggle toggle = m_toggles.getSelectedToggle();
		if (toggle instanceof Node)
		{
			Node node = (Node)toggle;
			ObservableList<Node> list = getChildren();
			return list.indexOf(node);
		}

		return -1;
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
		ObservableList<Node> list = getChildren();
		if (nIdx >= 0 && nIdx < list.size())
		{
			FramePane pane = (FramePane)list.get(nIdx);
			m_toggles.selectToggle(pane);
			return pane;
		}

		return null;
	}

	/**
	 * Removes all FramePanes from the children list.
	 */
	public void clear()
	{
		ObservableList<Node> list = getChildren();
		for (Node node : list)
		{
			if (node instanceof Toggle)
			{
				((Toggle)node).setToggleGroup(null);
			}
		}

		list.clear();
	}

	/**
	 * Scrolls the given Node object, usually a FramePane, into the visible
	 * part of the ScrollPane which this FramesBox is content of. If the given
	 * Node is already completely shown in the viewport then this method does
	 * nothing.
	 *
	 * @param node A {@link Node} object.
	 */
	public void scrollIntoView(Node node)
	{
		double dWidthScroller = m_scroller.getBoundsInLocal().getWidth(),
				dWidthMe = getBoundsInParent().getWidth();

		if (dWidthMe > dWidthScroller)
		{
			double dMinX = node.getBoundsInParent().getMinX(),
					dMaxX = node.getBoundsInParent().getMaxX(),
					dHiddenWidth = dWidthMe - dWidthScroller,
					dHval = m_scroller.getHvalue(),
					dOffsX = dHiddenWidth * dHval,
					dSpacing = getSpacing();

			if (dMinX - dSpacing < dOffsX)
			{
				dHval = (dMinX - dSpacing) / dHiddenWidth;
				m_scroller.setHvalue(dHval);
			}
			else if (dMaxX + dSpacing > dOffsX + dWidthScroller)
			{
				dHval = 1 - (dWidthMe - dMaxX - dSpacing) / dHiddenWidth;
				m_scroller.setHvalue(dHval);
			}
		}
	}

	/**
	 * Implements a change listener for removing and adding of child nodes.
	 */
	protected void doChangeListening()
	{
		if (m_listenerChildren == null)
		{
			ObservableList<Node> list = getChildren();

			m_listenerChildren = new ListChangeListener<Node>()
			{
				@Override
				public void onChanged(Change<? extends Node> change)
				{
					if (!m_bAdjusting)
					{
						while (change.next())
						{
							if (change.wasRemoved())
							{
								List<? extends Node> listChange = change.getRemoved();
								ArrayList<FramePane> arr = new ArrayList<>();
								for (Node nodeChange : listChange)
								{
									arr.add((FramePane)nodeChange);
								}

								onFramesChange();
							}
							else if (change.wasAdded())
							{
								List<? extends Node> listChange = change.getAddedSubList();
								ArrayList<FramePane> arr = new ArrayList<>();
								for (Node nodeChange : listChange)
								{
									arr.add((FramePane)nodeChange);
								}

								onFramesChange();
							}
						}
					}
				}
			};

			list.addListener(m_listenerChildren);
		}
	}

	/**
	 * Sets an EventHandler to handle changes of the children list.
	 *
	 * @param handler The {@link EventHandler}.
	 */
	public void setOnFramesChange(EventHandler<ActionEvent> handler)
	{
		m_framesChangeEventHandler = handler;
	}

	/**
	 * Invokes an Eventhandler in case of changes in the children list.
	 */
	protected void onFramesChange()
	{
		if (m_framesChangeEventHandler != null)
		{
			m_framesChangeEventHandler.handle(new ActionEvent(this, ActionEvent.NULL_SOURCE_TARGET));
		}
	}

	/**
	 * Does the drag listening with multiple event filters.
	 */
	protected void doDragListening()
	{
		if (m_bDragListening)
		{
			return;
		}

		m_bDragListening = true;

		final int HOVER_TIMEOUT = 300;

		final DragContext dc = new DragContext();
		final Popup substitute = new Popup();
		final ImageView view = new ImageView();
		final ObservableList<Node> list = getChildren();

		substitute.getContent().add(view);

		// Listen to the substitute for hiding.
		substitute.setOnHiding(event ->
		{
			// Has the drag been started but not finished normally?
			// E. g. in case of escape key.
			if (dc.m_bStarted && !dc.m_bFinished)
			{
				// Then it has been escaped.
				dc.m_bEscaped = true;
				dc.m_bStarted = false;

				if (dc.m_nodeDraggee != null)
				{
					// Put the draggee back to it's initial index ...
					int n = list.indexOf(dc.m_nodeDraggee);
					if (n != dc.m_nInitialIndex)
					{
						list.remove(n);
						list.add(dc.m_nInitialIndex, dc.m_nodeDraggee);
					}

					// ... and make it visible again.
					dc.m_nodeDraggee.setVisible(true);
				}
			}
		});

		// Listen for mouse drag detected.
		addEventFilter(MouseEvent.DRAG_DETECTED, event ->
		{
			//System.out.println("Drag detected: " + event);

			// Only with primary mouse button.
			if (event.isPrimaryButtonDown())
			{
				m_bAdjusting = true;

				// Do we have a frame at this point?
				Point2D pt = new Point2D(event.getX(), event.getY());
				FramePane pFound = getFrameFromPoint(pt);
				if (pFound != null)
				{
					// Initialize the drag context.
					dc.reset();

					dc.m_nodeDraggee = pFound;

					// Create a snapshot for the substitute.
					SnapshotParameters params = new SnapshotParameters();
					WritableImage image = dc.m_nodeDraggee.snapshot(params, null);
					view.setImage(image);

					dc.m_nInitialIndex = list.indexOf(dc.m_nodeDraggee);
					dc.m_bStarted = true;

					Bounds bounds = dc.m_nodeDraggee.getBoundsInLocal(),
							boundsScreen = dc.m_nodeDraggee.localToScreen(bounds);

					// Position the substitue and show.
					substitute.setX(boundsScreen.getMinX());
					substitute.setY(boundsScreen.getMinY());
					substitute.show(getScene().getWindow());

					// Collect positions and sizes needed.
					dc.m_dMouseX = event.getScreenX();
					dc.m_dMouseY = event.getScreenY();
					dc.m_dDraggeeX = boundsScreen.getMinX();
					dc.m_dDraggeeY = boundsScreen.getMinY();
					dc.m_dWidth = boundsScreen.getWidth();
					dc.m_dHeight = boundsScreen.getHeight();

					// Set the original node invisible.
					dc.m_nodeDraggee.setVisible(false);
				}

				event.consume();
			}
		});

		// Listen for mouse dragged.
		addEventFilter(MouseEvent.MOUSE_DRAGGED, event ->
		{
			//System.out.println("Dragged: " + event);

			// Only with primary mouse button and only if a drag has been correctly started.
			if (event.isPrimaryButtonDown() && dc.m_bStarted)
			{
				event.consume();

				Bounds bounds = m_scroller.getBoundsInLocal();
				bounds = m_scroller.localToScreen(bounds);

				// Calculate the horizonal position of the substitute popup. The vertical position is fix.
				double dSpacing = getSpacing(),
						dSubstituteX = Math.max(bounds.getMinX() + dSpacing, Math.min(bounds.getMaxX() - dc.m_dWidth - dSpacing, dc.m_dDraggeeX + event.getScreenX() - dc.m_dMouseX)),
						dSubstituteY = dc.m_dDraggeeY;

				substitute.setX(dSubstituteX);


				int nIdxPane = list.indexOf(dc.m_nodeDraggee);

				// Checks whether the substitute popup hovers left outside.
				if (dSubstituteX <= bounds.getMinX() + dSpacing)
				{
					//System.out.println("bHoveringLeft");

					// If there is a left neighbor ...
					if (nIdxPane > 0)
					{
						// Delayed position change to don't scroll them all at once.
						if (dc.m_bHoveringLeft)
						{
							if (System.currentTimeMillis() - dc.m_lTimeHoveringStarted > HOVER_TIMEOUT)
							{
								list.remove(nIdxPane);
								list.add(nIdxPane - 1, dc.m_nodeDraggee);

								scrollIntoView(dc.m_nodeDraggee);

								dc.m_lTimeHoveringStarted = System.currentTimeMillis();
							}
						}
						else
						{
							dc.m_bHoveringLeft = true;
							dc.m_lTimeHoveringStarted = System.currentTimeMillis();

							list.remove(nIdxPane);
							list.add(nIdxPane - 1, dc.m_nodeDraggee);

							scrollIntoView(list.get(nIdxPane));
						}
					}

					return;
				}
				else // Done
				{
					dc.m_bHoveringLeft = false;
				}

				// Checks whether the substitute popup hovers right outside.
				if (dSubstituteX + dc.m_dWidth >= bounds.getMaxX() - dSpacing)
				{
					//System.out.println("bHoveringRight");

					// If there is a right neighbor ...
					if (nIdxPane < list.size() - 1)
					{
						// Delayed position change to don't scroll them all at once.
						if (dc.m_bHoveringRight)
						{
							if (System.currentTimeMillis() - dc.m_lTimeHoveringStarted > HOVER_TIMEOUT)
							{
								list.remove(nIdxPane);
								list.add(nIdxPane + 1, dc.m_nodeDraggee);

								scrollIntoView(dc.m_nodeDraggee);

								dc.m_lTimeHoveringStarted = System.currentTimeMillis();
							}
						}
						else
						{
							dc.m_bHoveringRight = true;
							dc.m_lTimeHoveringStarted = System.currentTimeMillis();

							list.remove(nIdxPane);
							list.add(nIdxPane + 1, dc.m_nodeDraggee);

							scrollIntoView(list.get(nIdxPane));
						}
					}

					return;
				}
				else // Done
				{
					dc.m_bHoveringRight = false;
				}

				// Does the index change left or right if applycable.
				Point2D ptSubstitute = screenToLocal(dSubstituteX + dc.m_dWidth / 2, dSubstituteY + dc.m_dHeight / 2);
				FramePane pFound = getFrameFromPoint(ptSubstitute);

				int nIdxFound = pFound != null ? list.indexOf(pFound) : -1;
				if (nIdxFound >= 0 && pFound != dc.m_nodeDraggee)
				{
					bounds = pFound.getBoundsInParent();

					// Wide enough over the left neighbor?
					if (nIdxPane > nIdxFound && ptSubstitute.getX() < bounds.getMinX() + bounds.getWidth())
					{
						// Remove the draggee from the list and insert it at the index of the left neighbor.
						list.remove(nIdxPane);
						list.add(nIdxFound, dc.m_nodeDraggee);

						scrollIntoView(dc.m_nodeDraggee);
					}
					// Or wide enough over the right neighbor?
					else if (nIdxPane < nIdxFound && ptSubstitute.getX() > bounds.getMinX())
					{
						// Remove the draggee from the list and insert it at the index of the right neighbor.
						list.remove(nIdxPane);
						list.add(nIdxFound, dc.m_nodeDraggee);

						scrollIntoView(dc.m_nodeDraggee);
					}
				}
			}
		});

		// Listen for mouse released.
		addEventFilter(MouseEvent.MOUSE_RELEASED, event ->
		{
			//System.out.println("Released: " + event);

			// Only if a drag has been correctly started.
			if (dc.m_bStarted)
			{
				m_bAdjusting = false;

				// If no escape flag set.
				if (!dc.m_bEscaped)
				{
					// First set the flags.
					dc.m_bFinished = true;
					dc.m_bStarted = false;

					// Hide the substitute and show the draggee again.
					substitute.hide();
					dc.m_nodeDraggee.setVisible(true);

					// Create an Undoable for this drag.
					final int nIdxNew = list.indexOf(dc.m_nodeDraggee);
					final int nIdxOld = dc.m_nInitialIndex;
					if (nIdxNew != nIdxOld)
					{
						final Node node = dc.m_nodeDraggee;
						scrollIntoView(node);

						refreshPaneNumbers();

						ArrayList<FramePane> arr = new ArrayList<>();
						arr.add((FramePane)node);
						onFramesChange();

						//fireFrameShift(nIdxOld, nIdxNew);

						Undoable<Node> undo = new Undoable<Node>(dc.m_nodeDraggee, "label.framemove")
						{
							@Override
							public void undoAction()
							{
								list.remove(nIdxNew);
								list.add(nIdxOld, node);
								refreshPaneNumbers();
								scrollIntoView(node);
							}

							@Override
							public void redoAction()
							{
								list.remove(nIdxOld);
								list.add(nIdxNew, node);
								refreshPaneNumbers();
								scrollIntoView(node);
							}
						};

						App.getMainApp().addUndo(undo);
					}
				}
			}
		});
	}

	/**
	 * Renumber all FramePanes.
	 */
	protected void refreshPaneNumbers()
	{
		final ObservableList<Node> list = getChildren();

		int n = 0;
		for (Node node : list)
		{
			FramePane pane = (FramePane)node;
			pane.setIndex(n++);

			if (pane.isSelected())
			{
				pane.onSelection();
			}
		}
	}

	/**
	 * Find a FramePane at the given point.
	 *
	 * @param pt An object of type {@link Point2D}.
	 * @return The {@link FramePane} from the given point.
	 * Or null if there is none.
	 */
	protected FramePane getFrameFromPoint(Point2D pt)
	{
		final ObservableList<Node> list = getChildren();

		for (Node node : list)
		{
			FramePane pane = (FramePane)node;

			Bounds bounds = pane.getBoundsInParent();
			if (bounds.contains(pt))
			{
				return pane;
			}
		}

		return null;
	}

	/**
	 * Container for drag related data.
	 */
	private static final class DragContext
	{
		public double m_dMouseX;
		public double m_dMouseY;
		public double m_dDraggeeX;
		public double m_dDraggeeY;
		public double m_dWidth;
		public double m_dHeight;

		public boolean m_bStarted;
		public boolean m_bEscaped;
		public boolean m_bFinished;

		public boolean m_bHoveringLeft;
		public boolean m_bHoveringRight;
		public long m_lTimeHoveringStarted;

		public int m_nInitialIndex;
		public Node m_nodeDraggee;

		public void reset()
		{
			m_dMouseX = m_dMouseY = m_dDraggeeX = m_dDraggeeY = m_dWidth = m_dHeight;
			m_bStarted = m_bEscaped = m_bFinished = m_bHoveringLeft = false;
			m_lTimeHoveringStarted = 0;
			m_nInitialIndex = 0;
			m_nodeDraggee = null;
		}
	}
}
