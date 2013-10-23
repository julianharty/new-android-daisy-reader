package org.androiddaisyreader.testutilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.androiddaisyreader.model.BookContext;

/**
 * Represents a dummy book for testing purposes.
 * 
 * @author jharty
 */
public class DummyBookContext implements BookContext {

    private String contents;

    public DummyBookContext(String contents) {
        this.contents = contents;
    }

    public InputStream getResource(String uri) throws FileNotFoundException {
        return new ByteArrayInputStream(contents.getBytes(Charset.forName("UTF-8")));
    }

    public String getBaseUri() {
        return File.separator;
    }

}
