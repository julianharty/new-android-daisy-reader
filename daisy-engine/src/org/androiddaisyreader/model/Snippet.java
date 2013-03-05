package org.androiddaisyreader.model;

public abstract class Snippet {
	
	/**
	 * @return the id that points to the contents.
	 */
	public abstract String getId();
	
	/**
	 * @return the text contents for this text element.
	 */
	public abstract String getText();

	/**
	 * Does the element have text?
	 * 
	 * @return true if the element has a text value, else false.
	 * The value may be empty.
	 */
	public abstract boolean hasText();
	
}
