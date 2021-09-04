package de.unlixx.runpng.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Localization manager.
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
 */
public class Loc
{
	static final String BUNDLE_BASE = "de.unlixx.runpng.resources.lang";

	static ResourceBundle m_resources = ResourceBundle.getBundle(BUNDLE_BASE);
	static final ArrayList<Locale> m_listLocales = new ArrayList<>();
	static final ArrayList<ChangeListener> m_listListeners = new ArrayList<>();
	static final Locale m_localeFallback = Locale.ENGLISH;
	static Locale m_localeCurrent;

	/**
	 * Change listener for changes of the current locale.
	 *
	 */
	public static interface ChangeListener
	{
		/**
		 * This method will get invoked at implementors in case the locale changes.
		 *
		 * @param localeOld The old Locale object.
		 * @param localeNew The new Locale object.
		 */
		void localeChanged(Locale localeOld, Locale localeNew);
	}

	private static ArrayList<Object> m_listIdTextObjects = new ArrayList<>();

	static
	{
		// Search for installed language files
		String[] astrLang = Locale.getISOLanguages();
		for(String strLang : astrLang)
		{
			URL url = ClassLoader.getSystemResource("de/unlixx/runpng/resources/lang_" + strLang + ".properties");
			if (url != null)
			{
				m_listLocales.add(Locale.forLanguageTag(strLang));
			}
		}
	}

	/**
	 * This prevents erreneous instantiation.
	 */
	private Loc() { }

	/**
	 * This gets a fallback locale for the case that a user specific locale
	 * is not available.
	 *
	 * @return The fallback Locale which is Locale.ENGLISH actually.
	 */
	public static Locale getFallback()
	{
		return m_localeFallback;
	}

	/**
	 * Obtains a language specific string resource by key.
	 *
	 * @param strKey The key connected to the string resource.
	 * @return The obtained resource string or an empty string.
	 */
	public static String getString(String strKey)
	{
		try
		{
			return m_resources.getString(strKey);
		}
		catch (MissingResourceException e)
		{
			//System.err.println("Nix g'funden für " + strKey);
			return "";
		}
	}

	/**
	 * Obtains a language specific string resource by key and completes it
	 * with variable arguments.
	 *
	 * @param strKey The key connected to the string resource.
	 * @param args A variable arguments list.
	 * @return The obtained resource string or an empty string.
	 */
	public static String getString(String strKey, Object... args)
	{
		String str = getString(strKey);
		return String.format(str, args);
	}

	/**
	 * To obtain the list of currently installed language locales.
	 *
	 * @return The list of installed language locales.
	 */
	public static List<Locale> getLocalesList()
	{
		return m_listLocales;
	}

	/**
	 * This method checks the locales list for a specific language locale.
	 *
	 * @param locale The locale to find a language locale for.
	 * @return The language locale if found in the list or the fallback locale.
	 */
	public static Locale getListedLocale(Locale locale)
	{
		for (Locale loc : m_listLocales)
		{
			if (loc.getISO3Language().equals(locale.getISO3Language()))
			{
				return loc;
			}
		}

		return m_localeFallback;
	}

	/**
	 * This needs to be invoked when a locale change happens.
	 *
	 * @param locale The new Locale object.
	 */
	public static void localeChanged(Locale locale)
	{
		Locale localeOld = m_localeCurrent;

		if (m_localeCurrent == null
				|| !m_localeCurrent.getISO3Language().equals(locale.getISO3Language()))
		{
			m_localeCurrent = getListedLocale(locale);
		}
		else
		{
			return;
		}

		Locale.setDefault(m_localeCurrent);
		m_resources = ResourceBundle.getBundle(BUNDLE_BASE, m_localeCurrent);

		final Object[] aZeroArgs = new Object[0];

		/**
		 * Because of many different classes with String getId() and setText(String) methods
		 * which only have the super-class Object in common, this loop uses Java reflection
		 * to invoke these methods.
		 */
		for (Object obj : m_listIdTextObjects)
		{
			try
			{
				Method method = obj.getClass().getMethod("getId");
				String strId = (String)method.invoke(obj, aZeroArgs);
				if (strId != null)
				{
					method = obj.getClass().getMethod("setText", String.class);
					method.invoke(obj, getString(strId));
				}
			}
			catch (Exception e) { e.printStackTrace(); }
		}

		for (ChangeListener listener : m_listListeners)
		{
			listener.localeChanged(localeOld, m_localeCurrent);
		}
	}

	/**
	 * Gets the current ISO 639 two characters locale id. If no current locale has been set
	 * then it gets the id of the fallback language.
	 *
	 * @return A string containing a two characters locale id.
	 */
	public static String getLanguage()
	{
		if (m_localeCurrent != null)
		{
			return m_localeCurrent.getLanguage();
		}

		return m_localeFallback.getLanguage();
	}

	/**
	 * Gets the current ISO 639-2 three characters locale id. If no current locale has been set
	 * then it gets the id of the fallback language.
	 *
	 * @return A string containing a three characters locale id.
	 */
	public static String getISO3Language()
	{
		if (m_localeCurrent != null)
		{
			return m_localeCurrent.getISO3Language();
		}

		return m_localeFallback.getISO3Language();
	}

	/**
	 * Adds an id-text-object. This means an object which have a String getId()
	 * and a setText(String) method. The id of these objects must be the same as
	 * a text id in the resource bundle. In that case the text label can be changed
	 * according to a possible locale change.
	 *
	 * @param obj An object with a String getId() and a setText(String) method.
	 */
	public static void addIdTextObject(Object obj)
	{
		if (!m_listIdTextObjects.contains(obj))
		{
			m_listIdTextObjects.add(obj);
		}
	}

	/**
	 * Add a locale change listener to the list.
	 *
	 * @param listener A listener which implements the {@link Loc.ChangeListener} interface.
	 */
	public static void addChangeListener(ChangeListener listener)
	{
		m_listListeners.add(listener);
	}

	/**
	 * Remove a locale change listener from the list.
	 *
	 * @param listener A listener which implements the {@link Loc.ChangeListener} interface.
	 */
	public static void removeChangeListener(ChangeListener listener)
	{
		m_listListeners.remove(listener);
	}
}
