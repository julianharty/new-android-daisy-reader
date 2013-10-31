package org.androiddaisyreader.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileSystemContext implements BookContext {
    // TODO 20120214 (jharty): use more general
    private String directoryName;

    protected FileSystemContext() {
        // Do nothing.
    }

    public FileSystemContext(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            throw new IllegalStateException("A valid directory is required");
        }
        this.directoryName = directoryName;
    }

    public InputStream getResource(String uri) throws FileNotFoundException {
        String fullName = directoryName + File.separator + uri;
        InputStream contents = new FileInputStream(fullName);
        // A BufferedInputStream adds functionality to another input
        // stream-namely, the ability to buffer the input and to support the
        // mark and reset methods.
        return new BufferedInputStream(contents);
    }

    public String getBaseUri() {
        return directoryName;
    }

}
