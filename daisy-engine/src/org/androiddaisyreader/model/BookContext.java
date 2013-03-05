package org.androiddaisyreader.model;

import java.io.IOException;
import java.io.InputStream;

/**
 * BookContext provides a resource-independent way to access a book's contents.
 * 
 * The current design works for zipped content and files in a directory.
 * Potentially it may also support URI based content e.g. on a web server.
 * 
 * @author Julian Harty
 */
public interface BookContext {

	/**
	 * Get a named resource using this Book's Context.
	 * 
	 * The uri is relative to the Book's Context e.g. a filename without the
	 * folder name, or a filename within a zip file.
	 *  
	 * @param uri the name of the resource.
	 * @return an InputStream with the contents of the resource. 
	 * @throws IOException if the resource is not found or other
	 *         problems related to obtaining the contents.
	 */
	public InputStream getResource(String uri) throws IOException;
	
	/**
	 * Obtain the base URI for the Book's Context. e.g. the directory name
	 * or the name of the zip file.
	 * 
	 * @return the base URI, or null if none is available/relevant.
	 */
	public String getBaseUri();
	
}
