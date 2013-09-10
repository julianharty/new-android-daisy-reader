package org.androiddaisyreader.model;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * Tests for XmlUtilities class which was lacking tests until now.
 * 
 * I want to improve that class by writing suitable tests.
 * 
 * @author Julian Harty
 *
 */
public class XmlUtilitiesTest extends TestCase {
	
	public void testResettableInputStreamReturnsEncoding() throws IOException {
		try {
			String dontCare = XmlUtilities.obtainEncodingStringFromInputStream(null);
		} catch (NullPointerException npe) {
			// pass
		}
	}

}
