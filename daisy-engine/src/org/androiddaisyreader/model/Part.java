package org.androiddaisyreader.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Part implements Navigable {
	private List<Snippet> snippets = new ArrayList<Snippet>();
	private List<Part> parts = new ArrayList<Part>();
	private List<Audio> audioElements = new ArrayList<Audio>();
	private List<String> textElements = new ArrayList<String>();
	private List<String> unhandledElements = new ArrayList<String>();
	private Image image;
	public String id;
	public String timingMode;
	
	private Part() {}

	public List<Audio> getAudioElements() {
		return Collections.unmodifiableList(audioElements);
	}
	
	public String getId() {
		return id;
	}
	
	public Image getImage() {
		return image;
	}
	
	public List<Snippet> getSnippets() {
		return Collections.unmodifiableList(snippets);
	}
	
	public boolean hasAudio() {
		return !audioElements.isEmpty();
	}
	
	public boolean hasImage() {
		return image != null;
	}
	
	public boolean hasSnippets() {
		return !snippets.isEmpty();
	}
	
	/**
	 * Builder class to construct a Part object correctly.
	 * 
	 * @author jharty
	 */
	public static class Builder {
		private Part newInstance = new Part();
		
		public Builder addAudio(Audio audioClip) {
			newInstance.audioElements.add(audioClip);
			return this;
		}
		
		public Builder addPart(Part part) {
			newInstance.parts.add(part);
			return this;
		}
		
		public Builder addSnippet(Snippet snippet) {
			newInstance.snippets.add(snippet);
			return this;
		}

		public Builder addUnhandledElement(String elementDetails) {
			newInstance.unhandledElements.add(elementDetails);
			return this;
		}
		
		public Builder setImage(Image image) {
			newInstance.image = image;
			return this;
		}
		
		public Part build() {
			return newInstance;
		}

		public Builder setId(String id) {
			newInstance.id = id;
			return this;
			
		}

		public Builder setTimingMode(String mode) {
			newInstance.timingMode = mode;
			return this;
		}

		public Builder addTextElement(String location) {
			newInstance.textElements.add(location);
			return this;
		}
	}

	public List<Navigable> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
