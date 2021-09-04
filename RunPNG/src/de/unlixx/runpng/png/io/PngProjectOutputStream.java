package de.unlixx.runpng.png.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Element;

import de.unlixx.runpng.App;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.png.PngProjectManager;
import de.unlixx.runpng.util.Progress;

/**
 * PngProjectOutputStream based on ZipOutputStream. This class writes
 * the PngProject content zipped to an output stream.
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
public class PngProjectOutputStream extends ZipOutputStream
{
	PngProjectManager m_manager;

	/**
	 * Constructor for this PngProjectOutputStream.
	 *
	 * @param os The {@link OutputStream} to write to.
	 */
	public PngProjectOutputStream(OutputStream os)
	{
		super(os);

		setMethod(ZipOutputStream.DEFLATED);
		setLevel(9);
		setComment(App.APP_NAME);
	}

	/**
	 * Simple prediction of the step count for save.
	 *
	 * @return An int containing the step count.
	 */
	public int calcStepsForSave()
	{
		int nSteps = 0;

		List<Element> files = m_manager.getFileDescriptions();
		if (files != null)
		{
			for (Element file : files)
			{
				String strName = file.getAttribute("name"),
						strUsage = file.getAttribute("usage");

				switch (strUsage)
				{
				case "meta": nSteps++; break;
				case "sequence":
					Bitmap32Sequence sequence = m_manager.getNamedSequence(strName);
					nSteps += PngChunkOutputStream.calcStepsForSave(sequence) + 1;
					break;
				}
			}
		}

		return nSteps;
	}

	/**
	 * Writes the given project contents to the zip output stream.
	 *
	 * @param project The {@link PngProject} object to write it's contents.
	 * @param progress A {@link Progress} object to update the visual progress indicator.
	 * @throws IOException In case of an IO problem.
	 * @throws DataFormatException In case of problems with the data format.
	 */
	public void write(PngProject project, Progress<?> progress) throws IOException, DataFormatException
	{
		m_manager = new PngProjectManager(project);

		int nSteps = calcStepsForSave();
		progress.updateProgress(0, nSteps);

		List<Element> files = m_manager.getFileDescriptions();
		if (files != null)
		{
			for (Element file : files)
			{
				String strType = file.getAttribute("type"),
						strName = file.getAttribute("name"),
						strUsage = file.getAttribute("usage"),
						strComment = file.getAttribute("comment");

				if ("meta".equals(strUsage))
				{
					String strMeta = m_manager.getMetaDataXML();
					write(strMeta, strName, strComment, progress);
				}
				else if ("png".equals(strType))
				{
					Bitmap32Sequence sequence = m_manager.getNamedSequence(strName);
					if (sequence != null)
					{
						write(sequence, strName, strComment, progress);
					}
				}
			}
		}
	}

	/**
	 * Writes a text file to the zip output stream.
	 *
	 * @param strText A string containing the text.
	 * @param strFilename The filename string.
	 * @param strComment An optional comment string.
	 * @param progress A {@link Progress} object to update the visual progress indicator.
	 * @throws IOException In case of an IO problem.
	 */
	void write(String strText, String strFilename, String strComment, Progress<?> progress) throws IOException
	{
		addEntry(strText.getBytes(), strFilename, strComment, progress);
	}

	/**
	 * Writes a Bitmap32Sequence to the zip output stream.
	 *
	 * @param sequence The {@link Bitmap32Sequence} object.
	 * @param strFilename The filename string.
	 * @param strComment An optional comment string.
	 * @param progress A {@link Progress} object to update the visual progress indicator.
	 * @throws IOException In case of an IO problem.
	 * @throws DataFormatException In case of problems with the data format.
	 */
	void write(Bitmap32Sequence sequence, String strFilename, String strComment, Progress<?> progress) throws IOException, DataFormatException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (PngChunkOutputStream pcos = new PngChunkOutputStream(bos))
		{
			pcos.write(sequence, progress);
		}

		addEntry(bos.toByteArray(), strFilename, strComment, progress);
	}

	/**
	 * Creates the zip entry and writes the byte data to the zip output stream.
	 *
	 * @param abData An array of bytes to write.
	 * @param strFilename The filename string.
	 * @param strComment An optional comment string.
	 * @param progress A {@link Progress} object to update the visual progress indicator.
	 * @throws IOException In case of an IO problem.
	 */
	void addEntry(byte[] abData, String strFilename, String strComment, Progress<?> progress) throws IOException
	{
		ZipEntry entry = new ZipEntry(strFilename);

		entry.setComment(strComment);
		entry.setTime(System.currentTimeMillis());
		entry.setMethod(ZipEntry.DEFLATED);

		putNextEntry(entry);
		write(abData);
		closeEntry();

		progress.addProgress(1);
	}

	@Override
	public void close() throws IOException
	{
		finish();
		flush();

		super.close();
	}
}
