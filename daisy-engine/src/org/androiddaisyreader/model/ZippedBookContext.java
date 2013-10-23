package org.androiddaisyreader.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Represents the BookContext for a zipped book.
 * 
 * Note: Currently this doesn't check the contents of the zip file contain a
 * valid book. We can consider adding checks e.g. by passing in a 'check' method
 * in the constructor. For now we'll start simple :)
 * 
 * @author Julian Harty
 * 
 */
public class ZippedBookContext implements BookContext {
    ZipFile zipContents;

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
            // TODO 20120218 (jharty): Consider ways to make the algorithm more
            // robust.

            // 20130912: add "toLowerCase" to increase exactly when compare two
            // text.
            if (entry.getName().toLowerCase().contains(uri.toLowerCase())) {
                BufferedInputStream bis = new BufferedInputStream(zipContents.getInputStream(entry));
                return bis;
            }
        }
        return null;
    }

    public String getBaseUri() {
        return zipContents.getName();
    }

}
