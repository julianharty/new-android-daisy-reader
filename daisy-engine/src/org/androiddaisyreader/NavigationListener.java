package org.androiddaisyreader;

/**
 * Listens to navigation events.
 * 
 * Navigation events include going to previous and next sections & parts of a
 * book.
 * 
 * TODO's 20120220 (jharty): consider whether navigating to bookmarks should be included.
 * 
 * @author Julian Harty
 *
 */
public interface NavigationListener {

	/**
	 * Navigate to the next logical element of the book.
	 * 
	 * If there are no suitable elements, fire an AT_END event.
	 */
	public void next();
	
	/**
	 * Navigate to the previous logical element of the book.
	 * 
	 * If there are no suitable elements, fire an AT_START event.
	 */
	public void previous();
	
	/**
	 * GoTo a specified location in the book.
	 * 
	 * If the location is not found or is unreachable, fire either a NOT_FOUND
	 * event or UNREACHABLE_ELEMENT event.
	 * 
	 * @param location The location to go to.
	 */
	public void goTo(LocationInBook location);
	
	// TODO 20120220 (jharty): do we need to pass in a listener when instantiating this interface?
	// e.g. to allow us to report AT_END and AT_START of book events? 
	// Also do we need to allow the caller to specify the DAISY level they want
	// to use as a filter?
	
	public NavigationListener build(NavigationEventListener navigationEventListener);
}
