package de.unlixx.runpng.png;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.unlixx.runpng.App;
import de.unlixx.runpng.bitmap.Bitmap32Sequence;
import de.unlixx.runpng.util.Util;
import de.unlixx.runpng.util.Version;

/**
 * Container class for a png project. Because an animated png file
 * stores only the final result of the creation, a png project
 * gathers all components and settings in use to store them in one
 * zipped file. This makes it possible to do changes later if needed.
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
public class PngProject
{
	HashMap<String, Bitmap32Sequence> m_namedSequences = new HashMap<>();

	Document m_document;

	/**
	 * Constructor for a png project to save. It creates a meta data
	 * document of type {@link Document} which contains
	 * it's own file description first. The application name and version
	 * will be stored in this document as well.
	 */
	public PngProject()
	{
		m_document = Util.getDocumentBuilder().newDocument();
		m_document.setXmlVersion("1.0");

		DOMImplementation domImpl = m_document.getImplementation();
	    DocumentType doctype = domImpl.createDocumentType("projectmeta", "-//unlixx.de//RunPNG//EN", "projectmeta.dtd");
	    m_document.appendChild(doctype);

	    Element root = m_document.createElement("projectmeta");
	    root.setAttribute("software", App.APP_NAME);
	    root.setAttribute("version", App.APP_VERSION.toString());
	    m_document.appendChild(root);

	    addFileDescription("xml", "meta", "meta.xml", null, "Project meta data");
	}

	/**
	 * Package private constructor for a png project in read.
	 *
	 * @param document A {@link Document} object. Or null if not yet known.
	 */
	PngProject(Document document)
	{
		m_document = document;
	}

	/**
	 * Sets a meta value in the meta document.
	 *
	 * @param strSection A string containing the section.
	 * @param strName A string containing the name.
	 * @param strValue A string containing the value.
	 */
	public void setMetaValue(String strSection, String strName, String strValue)
	{
		Element meta = getMetaElement(strSection, strName);
		if (meta == null)
		{
			meta = m_document.createElement("meta");
			m_document.getDocumentElement().appendChild(meta);

			meta.setAttribute("section", strSection);
			meta.setAttribute("name", strName);
		}

		meta.setAttribute("value", strValue);
	}

	/**
	 * Sets a meta value in the meta document.
	 *
	 * @param strSection A string containing the section.
	 * @param strName A string containing the name.
	 * @param nValue An int containing the value.
	 */
	public void setMetaValue(String strSection, String strName, int nValue)
	{
		setMetaValue(strSection, strName, "" + nValue);
	}

	/**
	 * Gets a meta value from the meta document.
	 *
	 * @param strSection A string containing the section.
	 * @param strName A string containing the name.
	 * @param strDefault A string containing the default in the case no value exists.
	 * @return A string containing the value. Or the default string if no element exists.
	 */
	public String getMetaValue(String strSection, String strName, String strDefault)
	{
		Element meta = getMetaElement(strSection, strName);
		if (meta != null)
		{
			return meta.getAttribute("value");
		}

		return strDefault;
	}

	/**
	 * Gets a meta value from the meta document.
	 *
	 * @param strSection A string containing the section.
	 * @param strName A string containing the name.
	 * @param nDefault An int containing the default in the case no value exists.
	 * @return An int containing the value. Or the default int if no element exists.
	 */
	public int getMetaValueInt(String strSection, String strName, int nDefault)
	{
		String str = getMetaValue(strSection, strName, null);
		return Util.toInt(str, nDefault);
	}

	/**
	 * Gets a meta element from the meta document.
	 *
	 * @param strSection A string containing the section.
	 * @param strName A string containing the name.
	 * @return An {@link Element} with the given section and name. Or null if no element exists.
	 */
	protected Element getMetaElement(String strSection, String strName)
	{
		NodeList nodes = m_document.getElementsByTagName("meta");
		for (int n = 0, nLen = nodes.getLength(); n < nLen; n++)
		{
			Element meta = (Element)nodes.item(n);
			if (meta.getAttribute("section").equals(strSection) && meta.getAttribute("name").equals(strName))
			{
				return meta;
			}
		}

		return null;
	}

	/**
	 * Adds a file description to the meta document.
	 *
	 * @param strType A string containing the type (xml, png, ...).
	 * @param strUsage A string describing the usage (meta, sequence, ...).
	 * @param strName A string with the storage filename.
	 * @param index An index or null if not needed.
	 * @param strComment An optional comment or null.
	 */
	public void addFileDescription(String strType, String strUsage, String strName, Integer index, String strComment)
	{
		Element file = m_document.createElement("file"),
				root = m_document.getDocumentElement();
		root.appendChild(file);
		file.setAttribute("type", strType);
		file.setAttribute("usage", strUsage);
		file.setAttribute("name", strName);

		if (index != null)
		{
			file.setAttribute("index", index.toString());
		}

		if (strComment != null)
		{
			file.setAttribute("comment", strComment);
		}
	}

	/**
	 * Gets the software name of the current document.
	 *
	 * @return A string containing the software name or the empty string.
	 */
	public String getMetaSoftwareName()
	{
		return m_document.getDocumentElement().getAttribute("software");
	}

	/**
	 * Gets the version number of the actual project.
	 *
	 * @return A Version onject.
	 */
	public Version getMetaVersion()
	{
		return Version.valueOf(m_document.getDocumentElement().getAttribute("version"));
	}

	/**
	 * Adds a named bitmap sequence, animated or not, to the internal hash map.
	 *
	 * @param strName The name of the sequence.
	 * @param sequence a {@link Bitmap32Sequence} object.
	 */
	public void addNamedSequence(String strName, Bitmap32Sequence sequence)
	{
		m_namedSequences.put(strName, sequence);
	}

	/**
	 * Gets a bitmap sequence from the internal hash map.
	 *
	 * @param strName The name of the sequence.
	 * @return A {@link Bitmap32Sequence} object.
	 * Or null if it not exists.
	 */
	public Bitmap32Sequence getNamedSequence(String strName)
	{
		return m_namedSequences.get(strName);
	}

	/**
	 * Sets a new meta data document.
	 *
	 * @param document A {@link Document} object.
	 */
	public void setMetaDocument(Document document)
	{
		m_document = document;
	}

	/**
	 * Gets the current meta data document.
	 *
	 * @return A {@link Document} object.
	 */
	public Document getMetaDocument()
	{
		return m_document;
	}

	/**
	 * Gets a list of all file descriptions actually stored in
	 * the meta data document.
	 *
	 * @return A {@link List} object.
	 */
	public List<Element> getFileDescriptions()
	{
		ArrayList<Element> list = new ArrayList<>();

		if (m_document != null)
		{
			NodeList nodes = m_document.getElementsByTagName("file");
			for (int n = nodes.getLength() - 1; n >= 0; n--)
			{
				list.add((Element)nodes.item(n));
			}
		}

		return list;
	}

	/**
	 * Gets a list with a subset of file descriptions actually stored in
	 * the meta data document, filtered by their usage attribute.
	 *
	 * @param strUsage A string containing the usage attribute to filter.
	 * @return A {@link List} object.
	 */
	public List<Element> getMetaFilesByUsage(String strUsage)
	{
		ArrayList<Element> list = new ArrayList<>();

		if (m_document != null)
		{
			NodeList nodes = m_document.getElementsByTagName("file");
			for (int n = nodes.getLength() - 1; n >= 0; n--)
			{
				Element element = (Element)nodes.item(n);
				if (element.getAttribute("usage").equals(strUsage))
				{
					list.add(element);
				}
			}
		}

		return list;
	}
}
