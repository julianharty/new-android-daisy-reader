package org.androiddaisyreader.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileSystemContext implements BookContext {

	private String directoryName; // TODO 20120214 (jharty): use more general uri rather than assuming files.
	private File directory;

	protected FileSystemContext() {
		// Do nothing.
	}
	
	public FileSystemContext(String directoryName) {
		directory = new File(directoryName);
		boolean isDirectory = directory.isDirectory();
		directory = null;
		if (isDirectory) {
			this.directoryName = directoryName;
		} else {
			throw new IllegalStateException("A valid directory is required");
		}
	}

	public InputStream getResource(String uri) throws FileNotFoundException {
		String fullName = directoryName + File.separator + uri;
		InputStream contents = new FileInputStream(fullName);
		return contents;
	}

	public String getBaseUri() {
		return directoryName;
	}

}
