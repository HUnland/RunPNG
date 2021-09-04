package de.unlixx.runpng.png.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.unlixx.runpng.png.PngConstants;
import de.unlixx.runpng.png.PngProject;
import de.unlixx.runpng.png.PngProjectManager;
import de.unlixx.runpng.util.Progress;

/**
 * PngProjectInputStream based on ZipInputStream. This class reads
 * the zipped PngProject content from an input stream.
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
public class PngProjectInputStream extends ZipInputStream
{
	protected PngProjectManager m_manager;

	/**
	 * Constructor for this PngProjectInputStream.
	 *
	 * @param is The {@link InputStream} to read from.
	 */
	public PngProjectInputStream(InputStream is)
	{
		super(is);
	}

	/**
	 * Reads the whole PngProject content from the zip input stream.
	 *
	 * @param progress A {@link Progress} object to update the visual progress indicator.
	 * @return A {@link PngProject} object.
	 * @throws IOException In case of an IO problem.
	 * @throws DataFormatException In case of problems with the data format.
	 */
	public PngProject read(Progress<?> progress) throws IOException, DataFormatException
	{
		m_manager = new PngProjectManager();

		ZipEntry entry;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(PngConstants.BUFFER_32K);
		byte[] abBuffer = new byte[PngConstants.BUFFER_32K];

		while ((entry = getNextEntry()) != null)
		{
			String strName = entry.getName();

			bos.reset();

			int nRead;
			while ((nRead = read(abBuffer)) > 0)
			{
				bos.write(abBuffer, 0, nRead);
			}

			if ("meta.xml".equals(strName))
			{
				m_manager.setMetaData(bos.toString("utf-8"));
				progress.addProgress(bos.size());
			}
			else if (strName.endsWith(".png"))
			{
				PngChunkInputStream pcis = new PngChunkInputStream(new ByteArrayInputStream(bos.toByteArray()));
				m_manager.addNamedSequence(strName, pcis.read(progress));
			}

			bos.reset();
		}

		return m_manager.getProject();
	}

	@Override
	public void close() throws IOException
	{
		super.close();
	}
}
