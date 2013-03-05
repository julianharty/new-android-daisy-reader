package org.androiddaisyreader.model;

import java.util.Collections;
import java.util.List;

public class Section implements Navigable {
	protected List<Navigable> navigables;
	protected int level;
	protected Navigable parent;
	protected String title;
	protected String id;
	protected String href;
	protected BookContext bookContext;

	public String getHref() {
		return href;
	}
	
	public int getLevel() {
		return level;
	}

	public String getTitle() {
		return title;
	}


	public Navigable getParent() {
		return parent;
	}

	public List<Navigable> getChildren() {
		return Collections.unmodifiableList(navigables);
	}
}
 