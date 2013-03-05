package org.androiddaisyreader.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Represents the BookContext for a zipped book.
 * 
 * Note: Currently this doesn't check the contents of the zip file contain a
 * valid book. We can consider adding checks e.g. by passing in a 'check'
 * method in the constructor. For now we'll start simple :)
 * 
 * @author Julian Harty
 *
 */
public class ZippedBookContext implements BookContext {
	ZipFile zipContents;
	private ZipEntry entry;
	
	protected ZippedBookContext() {
		// Do nothing.
	}
	
	public ZippedBookContext(String zipFilename) throws IOException {
		zipContents = new ZipFile(zipFilename);
	}
	
	public InputStream getResource(String uri) throws IOException {
		ZipEntry entry;
		
		Enumeration<? extends ZipEntry> e = zipContents.entries();
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			System.out.println("Checking: " + entry);
			
			// Note: we're blindly stripping off any folder prefix and
			// assuming that each filename in the zip file is unique. These
			// assumptions may bite us in the end with some books.
			// TODO 20120218 (jharty): Consider ways to make the algorithm more robust.
			if (entry.getName().contains(uri)) {
				return zipContents.getInputStream(entry);
			}
		}
		return null;
	}

	public String getBaseUri() {
		return zipContents.getName();
	}

}
