package de.unlixx.runpng.util;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

/**
 * Implementation of a Player derived from the abstract
 * {@link Task} class. This is used to run
 * a timed image sequence.
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
public class Player extends Task<Integer>
{
	final Thread m_thread;
	final int m_nFirst;
	final int m_nLast;
	final int m_nLoops;
	int m_nIndex;

	IntegerProperty m_timeout = new SimpleIntegerProperty();

	/**
	 * Constructor for this Player.
	 *
	 * @param nFirst An int containing the first index.
	 * @param nLast An int containing the lasst index.
	 * @param nIndex An int containing the current or start index.
	 * @param nLoops An int containing the number of loops to run.
	 */
	public Player(int nFirst, int nLast, int nIndex, int nLoops)
	{
		m_nFirst = nFirst;
		m_nLast = nLast;
		m_nIndex = Math.max(nIndex, m_nFirst);
		m_nLoops = nLoops;

		m_thread = new Thread(this);
		m_thread.setDaemon(true);
	}

	@Override
	protected Integer call() throws Exception
	{
		if (m_nFirst < 0)
		{
			cancel();
			return -1;
		}

		int nLoop = 0;

		do
		{
			setTimeout(-1); // For handshake.
			updateValue(m_nIndex); // Sets the index for which a timeout value is needed.

			// Keep the time "now" to calculate the correct time "then".
			// This is used to prevent a time slip because of waiting for the timeout value.
			long lTimeNow = System.currentTimeMillis(),
					lTimeThen = lTimeNow, lTime;

			try
			{
				int nTimeout;

				// Wait for the timeout value.
				while ((nTimeout = getTimeout()) < 0)
				{
					Thread.sleep(1);
				}

				lTimeThen = lTimeNow + nTimeout;
			}
			catch (InterruptedException e) {}

			if (!isCancelled())
			{
				lTime = lTimeThen - System.currentTimeMillis();
				if (lTime > 0)
				{
					// Sleep for the rest of the time.
					try
					{
						Thread.sleep(lTime);
					}
					catch (InterruptedException e) {}
				}

				if (!isCancelled())
				{
					// Update the index according first and last.
					// Or cancel if a loop limit has been reached.
					m_nIndex = m_nIndex < m_nLast ? m_nIndex + 1 : m_nFirst;

					if (m_nIndex == m_nFirst && m_nLoops > 0 && ++nLoop >= m_nLoops)
					{
						cancel();
					}
				}
			}
		}
		while (!isCancelled());

		return m_nIndex;
	}

	/**
	 * Gets the current timeout from the timeout property.
	 *
	 * @return An int containing the timeout in milliseconds.
	 */
	public synchronized int getTimeout()
	{
		synchronized (m_timeout)
		{
			return m_timeout.get();
		}
	}

	/**
	 * Sets the new timeout to the timeout property.
	 *
	 * @param nTimeout An int containing the timeout in milliseconds.
	 */
	public synchronized void setTimeout(int nTimeout)
	{
		synchronized (m_timeout)
		{
			m_timeout.set(nTimeout);
		}
	}

	/**
	 * Starts this task. Once started this task must not be reused anymore.
	 */
	public void start()
	{
		m_thread.start();
	}

	/**
	 * Stops this task if it is already running.
	 */
	public void stop()
	{
		if (isRunning())
		{
			cancel();
		}
	}
}
