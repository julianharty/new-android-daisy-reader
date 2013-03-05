package org.androiddaisyreader.model;

import java.io.IOException;
import java.util.ArrayList;

public class Daisy202Section extends Section {
	

	private Daisy202Section() {
		// Use the Builder... not me...
	}
	
	public String getSmilFilename() {
		String[] values = splitHref(href);
		String smilFilename = values[0];
		return isSmilFilenameValid(smilFilename) ? smilFilename : null;
	}

	/**
	 * Splits a Smil href.
	 * The href should be into 2 parts, the filename and the id used to locate
	 * elements.
	 * 
	 * @return an array of string values split on the separator "#"
	 */
	private String[] splitHref(String href) {
		return href.split("#");
	}
	
	public Part[] getParts() {
		try {
			return Smil10Specification.getParts(bookContext, 
					bookContext.getResource(getSmilFilename()));
		} catch (IOException e) {
			// TODO 20120301 jharty: refactor once I've sorted out the custom exceptions
			throw new RuntimeException(e);
		}
	}
	
	
	public boolean hasParts() {
		return false;
	}

	/**
	 * Checks if the SmilHref seems Valid
	 * @param href in the format filename.smil#id
	 * @return true if the SmilHref seems valid, else false.
	 */
	private boolean isSmilHrefValid(String href) {
		String[] values = splitHref(href);
		if (values.length != 2) {
			return false;
		}
		if (!isSmilFilenameValid(values[0])) {
			return false;
		}
		// We can add more checks here. For now declare victory.
		return true;
	}

	/**
	 * Simple helper method to validate the smil filename.
	 * 
	 * We can enhance this to suit our needs.
	 * @param smilFilename
	 * @return true if the filename seems to represent a smil file, else false.
	 */
	public boolean isSmilFilenameValid(String smilFilename) {
		if (smilFilename.endsWith(".smil")) {
			return true;
		}
		return false;
	}
	
	public static class Builder {
		private Daisy202Section newInstance = new Daisy202Section();
		
		public Builder() {
			newInstance.navigables = new ArrayList<Navigable>();
		}
		
		public Builder addSection(Section section) {
			newInstance.navigables.add(section);
			return this;
		}
		
		public Builder setLevel(int level) {
			// Consider adding logic to protect the integrity of this Section
			newInstance.level = level;
			return this;
		}
		
		public Builder setParent(Navigable parent) {
			newInstance.parent = parent;
			return this;
		}
		
		public Builder setContext(BookContext context) {
			newInstance.bookContext = context;
			return this;
		}
		
		public Builder setTitle(String title) {
			newInstance.title = title;
			return this;
		}
		
		public Daisy202Section build() {
			return newInstance;
		}
		
		public int getLevel() {
			return newInstance.level;
		}

		public Builder setId(String id) {
			newInstance.id = id;
			return this;
			
		}

		public Builder setHref(String href) {
			if (newInstance.isSmilHrefValid(href)) {
				newInstance.href = href;
				return this;
			} else {
				throw new IllegalArgumentException(String.format("Smil Filename [%s] seems invalid", href));
			}
		}
	}
}
