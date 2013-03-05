package org.androiddaisyreader.model;

import java.util.List;

public interface Navigable {
	public List<? extends Navigable> getChildren();
}
