package de.unlixx.runpng.util;

public class ProgressContainer<T>
{
	protected final T m_value;

	public ProgressContainer(T value)
	{
		m_value = value;
	}

	public T getValue()
	{
		return m_value;
	}

	public boolean hasInstance()
	{
		return m_value != null;
	}
}
