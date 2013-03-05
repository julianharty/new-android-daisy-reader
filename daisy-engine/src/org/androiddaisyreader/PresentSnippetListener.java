package org.androiddaisyreader;

public interface PresentSnippetListener {
	
	/**
	 * The section is a micro element e.g. a sentence or phrase.
	 * @param section
	 */
	public void present(TextSection section);
}
