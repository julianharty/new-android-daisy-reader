package org.androiddaisyreader.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Book implements Navigable {
	// meta data
	protected String title;
	protected String author;
	protected List<Section> sections = new ArrayList<Section>();
	
	public String getAuthor() {
		return author;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean hasAuthor() {
		return author != null;
	}
	
	public boolean hasTitle() {
		return title != null;
	}
	
}
