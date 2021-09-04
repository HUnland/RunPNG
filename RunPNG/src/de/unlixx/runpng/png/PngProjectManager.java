package de.unlixx.runpng.png;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.exceptions.Failure;

/**
 * The PngProjectManager controls reading and writing
 * from or to an animated png project.
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
public class PngProjectManager
{
	protected PngProject m_project;

	/**
	 * Constructor for PngProjectManager in case of reading a project.
	 */
	public PngProjectManager()
	{
		// Project needs to be read first
	}

	/**
	 * Constructor for PngProjectManager in case of writing a project.
	 *
	 * @param project A {@link PngProject} object.
	 */
	public PngProjectManager(PngProject project)
	{
		m_project = project;
	}

	/**
	 * Ensures the existence of a {@link PngProject}.
	 *
	 * @param document A {@link Document} object. Or null if not yet known.
	 */
	void ensureProject(Document document)
	{
		if (m_project == null)
		{
			m_project = new PngProject(document);
		}
		else if (document != null)
		{
			m_project.setMetaDocument(document);
		}
	}

	/**
	 * Gets the PngProject actually in use.
	 *
	 * @return A {@link PngProject} object.
	 * Or null if not yet created.
	 */
	public PngProject getProject()
	{
		return m_project;
	}

	/**
	 * Gets the project meta data, formatted as an xml string.
	 *
	 * @return An xml formatted string containing the project meta data.
	 * Or null if there is no project object created yet.
	 */
	public String getMetaDataXML()
	{
		if (m_project != null)
		{
			StringWriter out = new StringWriter();
			Document document = m_project.getMetaDocument();
			DocumentType docType = document.getDoctype();

			try
			{
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());

				transformer.transform(new DOMSource(document), new StreamResult(out));

				return out.toString();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Gets a list of all file descriptions actually stored in
	 * the meta data document of the PngProject.
	 *
	 * @return A {@link List} object.
	 * Or null if there is no project object created yet.
	 */
	public List<Element> getFileDescriptions()
	{
		if (m_project != null)
		{
			return m_project.getFileDescriptions();
		}

		return null;
	}

	/**
	 * Parses the given xml meta data into a Document object
	 * and passes it to the PngProject. Creates a PngProject
	 * if it not exists yet.
	 *
	 * @param strXML A string containing the xml meta data.
	 */
	public void setMetaData(String strXML)
	{
		try
		{
			DocumentBuilder builder = Util.getDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(strXML)));
			ensureProject(document);
		}
		catch (Exception e)
		{
			throw new Failure("failure.corruptrunpngproject");
		}
	}

	/**
	 * Adds a named bitmap sequence to the current project.
	 * Creates one if it not already exists.
	 *
	 * @param strName The name of the sequence.
	 * @param sequence a {@link Bitmap32Sequence} object.
	 */
	public void addNamedSequence(String strName, Bitmap32Sequence sequence)
	{
		ensureProject(null);
		m_project.addNamedSequence(strName, sequence);
	}

	/**
	 * Gets a bitmap sequence from the the current project.
	 *
	 * @param strName The name of the sequence.
	 * @return A {@link Bitmap32Sequence} object.
	 * Return null if it not exists or there is no project yet.
	 */
	public Bitmap32Sequence getNamedSequence(String strName)
	{
		if (m_project != null)
		{
			return m_project.getNamedSequence(strName);
		}

		return null;
	}
}
