package de.unlixx.runpng.util;

import de.unlixx.runpng.scene.FadeOutProgressBar;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

/**
 * Progress extends {@link Task} in order to incorporate
 * a progress indicator. This task derivate will be used for longer lasting
 * processes like file read or write to don't stop the GUI thread for a long time.
 * Ok, long time is relative. In many cases this is done in less than one second.
 * For the use of this abstract class a protected &lt;T&gt; call() { return T; } method
 * must be created.
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
 * @param <T> The type of data to handle.
 */
public abstract class Progress<T> extends Task<T>
{
	final ProgressIndicator m_progress;
	long m_lWork;
	long m_lDone;

	BooleanProperty m_acknowledge = new SimpleBooleanProperty();

	/**
	 * Constructor of this Progress class.
	 *
	 * @param progress A {@link ProgressIndicator} object
	 * to bind to the progress property of this task.
	 * @param lWork A long integer for the declaration of max work to do.
	 */
	public Progress(ProgressIndicator progress, long lWork)
	{
		m_progress = progress;

		if (m_progress instanceof FadeOutProgressBar)
		{
			((FadeOutProgressBar)m_progress).reset();
		}
		else
		{
			m_progress.progressProperty().unbind();
		}

		m_progress.progressProperty().bind(progressProperty());
		m_progress.progressProperty().addListener(change ->
		{
			if (m_progress.getProgress() >= .999999) // 1 - Epsilon, just to be sure
			{
				if (m_progress instanceof FadeOutProgressBar)
				{
					((FadeOutProgressBar)m_progress).startFadeOut();
				}

				m_progress.progressProperty().unbind();
			}
		});

		updateProgress(0, Math.max(1, lWork));
	}

	/**
	 * Synchronization helper for data exchange between threads.
	 * This is invoked to check whether the opposite part has acknowledged.
	 *
	 * @return A boolean containing true if acknowledge ist set.
	 */
	public boolean getAcknowledge()
	{
		synchronized (m_acknowledge)
		{
			return m_acknowledge.get();
		}
	}

	/**
	 * Synchronization helper for data exchange between threads.
	 * This needs to be invoked to set the acknowledge flag to false.
	 */
	public void clearAcknowledge()
	{
		synchronized (m_acknowledge)
		{
			m_acknowledge.set(false);
		}
	}

	/**
	 * Synchronization helper for data exchange between threads.
	 * This needs to be invoked to set the acknowledge flag to true.
	 */
	public void acknowledge()
	{
		synchronized (m_acknowledge)
		{
			m_acknowledge.set(true);
		}
	}

	/**
	 * Updates the workDone and progress property with an absolute value.
	 *
	 * @param lDone The absolute workDone value.
	 */
	public void updateProgress(long lDone)
	{
		updateProgress(lDone, m_lWork);
	}

	@Override
	public void updateProgress(long lDone, long lWork)
	{
		m_lWork = Math.max(1, lWork);
		m_lDone = Math.min(lDone, m_lWork);

		super.updateProgress(m_lDone, m_lWork);
	}

	/**
	 * Updates the workDone and progress property with an incremental value.
	 *
	 * @param lWorked The incremental value added to the workDone property.
	 */
	public void addProgress(long lWorked)
	{
		m_lDone = Math.min(m_lDone + lWorked, m_lWork);

		updateProgress(m_lDone);
	}

	/**
	 * Gets the current workDone value.
	 *
	 * @return A long integer with the workDone value.
	 */
	public long getDone()
	{
		return m_lDone;
	}

	/**
	 * Gets the totalWork value.
	 *
	 * @return A long integer with the totalWork value.
	 */
	public long getWork()
	{
		return m_lWork;
	}
}
