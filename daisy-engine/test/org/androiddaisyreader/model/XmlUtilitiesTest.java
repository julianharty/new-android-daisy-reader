package org.androiddaisyreader.model;

import java.io.IOException;
import java.io.InputStream;

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

	public void testCorrectExceptionThrownWhenInappropriateInputStreamUsed() throws IOException {
		InputStream bis = new InputStream() {

			@Override
			public int read() throws IOException {
				return 0;
			}

		};
		try {
			String dontCare = XmlUtilities.obtainEncodingStringFromInputStream(bis);
			fail ("Expected an IllegalArgumentException to be thrown.");
		} catch (IllegalArgumentException iae) {
			// pass
		}
	}
}
