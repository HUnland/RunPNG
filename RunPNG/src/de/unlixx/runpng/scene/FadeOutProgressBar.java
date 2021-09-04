package de.unlixx.runpng.scene;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

/**
 * This class implements an extension of the {@link ProgressBar} class
 * for the sake of changing the color of the gauge.
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
public class FadeOutProgressBar extends ProgressBar
{
	// Arbitrary for now.
	final int m_nAccentColor = 0x87ceeb;
	final int m_nAccentColorFinished = 0x7cfc00;

	/**
	 * Constructor of this class with an initial progress of 0;
	 */
	public FadeOutProgressBar()
	{
		this(0);
	}

	/**
	 * Constructor of this class with an initial progress;
	 *
	 * @param dProgress A double containing the initial progress.
	 */
	public FadeOutProgressBar(double dProgress)
	{
		super(dProgress);

		reset();
	}

	/**
	 * Resets this object for new use. If there is still a fadeout running
	 * then the task will be cancelled. If the progressProperty ist still bound
	 * to a {@link de.unlixx.runpng.util.Progress Progress} task or similar then
	 * it will be unbound. The progress color will be set to the value in m_nAccentColor.
	 */
	public void reset()
	{
		progressProperty().unbind();
		setProgress(0);

		// Pretty awkward to set the progress color that way.
		setStyle(String.format("-fx-accent: #%06x;", m_nAccentColor));
	}

	/**
	 * This method contains the fadeout task. The method will be called from the outside.
	 * Normally by a {@link de.unlixx.runpng.util.Progress Progress} task or similar
	 * when it reaches the end of task activity.
	 */
	public void startFadeOut()
	{
		// Pretty awkward to set the progress color that way.
		setStyle(String.format("-fx-accent: #%06x;", m_nAccentColorFinished));

		Task<Integer> task = new Task<Integer>()
		{
			@Override
			protected Integer call() throws Exception
			{
				int nA = 10;

				try
				{
					Thread.sleep(1500);
				}
				catch (InterruptedException e) {};

				while (!isCancelled())
				{
					updateValue(--nA);

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e) {};

					if (!isCancelled())
					{
						if (nA <= 0)
						{
							cancel();
						}
					}
				}

				return 0;
			}
		};

		final String strFormat = "-fx-accent: rgba(%d, %d, %d, 0.%1d);";
		final int nR = ((m_nAccentColorFinished >> 16) & 0xff),
				nG = ((m_nAccentColorFinished >> 8) & 0xff),
				nB = (m_nAccentColorFinished & 0xff);

		task.valueProperty().addListener(obs ->
		{
			int nA = task.getValue();

			// Pretty awkward to set the progress color that way.
			setStyle(String.format(strFormat, nR, nG, nB, nA));
		});

		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
}
