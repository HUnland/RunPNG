package de.unlixx.runpng.scene;

/**
 * Enumeration for the use in {@link de.unlixx.runpng.scene.ThumbnailView ThumbnailView},
 * {@link de.unlixx.runpng.scene.settings.effects.ThumbnailPane ThumbnailPane} and others.
 * This is used as a member field to indicate an applied scale method.
 *
 * @author Hans-Josef Unland
 */
public enum ScaleApplied
{
	NONE("none"),
	SCALESYM("sym"),
	SCALEBOTH("both"),
	SCALEHORZ("horz"),
	SCALEVERT("vert");

	final String m_strName;

	/**
	 * Private constructor with the name of the enumerated type.
	 *
	 * @param strName A string containing the name.
	 */
	private ScaleApplied(String strName)
	{
		m_strName = strName;
	}

	/**
	 * Gets the name of the type.
	 *
	 * @return A string containing the name.
	 */
	public String getName()
	{
		return m_strName;
	}

	/**
	 * Gets an enumerated type by name.
	 *
	 * @param strName A string containing the name.
	 * @return An enumerated type.
	 */
	public static ScaleApplied getByName(String strName)
	{
		switch (strName)
		{
		case "sym": return SCALESYM;
		case "both": return SCALEBOTH;
		case "horz": return SCALEHORZ;
		case "vert": return SCALEVERT;
		}

		return NONE;
	}
}
