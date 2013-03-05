/**
 * Extract the XML encoding from XML content, to help parse content correctly.
 */
package org.androiddaisyreader.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Simple Utility class to extract the XML encoding from a text file.
 * 
 * @author Julian Harty
 */
public final class XmlUtilities {
	private static final int ENOUGH = 200;
	protected static final String XML_TRAILER = "\"?>";
	protected static final String EXTRACT_ENCODING_REGEX = ".*encoding=\"";
	protected static final String XML_FIRST_LINE_REGEX = "<\\?xml version=\"1\\.0\" encoding=\"(.*)\"?>";

	// Hide the constructor for this utility class.
	private XmlUtilities() { } ;
	/**
	 * Helper method to extract the XML file encoding
	 * @param line the first line of an XML file
	 * @return The value of the encoding in lower-case.
	 */
	protected static String extractEncoding(String line) {
		Pattern p = Pattern.compile(EXTRACT_ENCODING_REGEX);
		String matches[] = p.split(line);
		String value = matches[1];  // We want the value after encoding="
		// We don't need anything after the first " after the value
		String cleanup[] = value.split("\"");  
		String encoding = cleanup[0];
		return encoding.toLowerCase();
	}
	

	/**
	 * Helper method to map an unsupported XML encoding to a similar encoding.
	 * 
	 * Currently limited to processing windows-1252 encoding.
	 * @param encoding The encoding string e.g. "windows-1252"
	 * @return a similar, hopefully supported encoding, where we have a 
	 * suitable match, else the original encoding.
	 */
	public static String mapUnsupportedEncoding(String encoding) {
		if (encoding.equalsIgnoreCase("windows-1252")) {
			return "iso-8859-1";
		}
		return encoding;
	}
	
	/**
	 * Helper method to obtain the content encoding from an input stream.
	 * @param bis file to parse
	 * @return the encoding if we are able to extract and parse it, else the
	 * default value expected by the expat parser, i.e. "UTF-8"
	 * @throws IOException if there is a problem reading from the file.
	 */
	public static String obtainEncodingStringFromInputStream(InputStream bis) throws IOException {
		String encoding = "UTF-8";
		if (bis.markSupported()) {
			String line = null;
			// read the first line after setting the mark, then reset
			// before calling the parser.
			bis.mark(ENOUGH);
			DataInputStream dis = new DataInputStream(bis);
			line = dis.readLine();
			line = line.replace("'", "\"");
			if (line.matches(XML_FIRST_LINE_REGEX)) {
				encoding = extractEncoding(line);
			}
			bis.reset();
		}
		return encoding;
	}
	
	/**
	 * Provide a dummy XML resolver.
	 * @return the dummy resolver.
	 */
	public static EntityResolver dummyEntityResolver() {
		// Thanks to http://www.junlu.com/msg/202604.html 
		EntityResolver er = new EntityResolver() {
	    public InputSource resolveEntity(String publicId, String systemId)
	    {
	        return new InputSource(new StringReader(" "));
	    }
	};
	return er;
	}
}
