package org.androiddaisyreader.testutilities;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Creates an eBook in DAISY v2.02 format.
 * 
 * Note: Currently the functionality is rudimentary, we need to allow the
 * caller to add custom content e.g. to add corrupt headings so the error
 * handling of the parser(s) can be tested.
 * 
 * Current capabilities to inject bad data include:
 *   - addLevel(n) when n is outside the range 1 to 6 e.g. both 0 and 7 are wrong. 
 * 
 * TODO(jharty): Continue enhancing this class.
 */
public class CreateDaisy202Book extends CreateEBook {
	private static final String END_TAG = "\">";
	private int sectionsCreatedAutomatically = 0;

	public CreateDaisy202Book() throws NotImplementedException {
		super();
	}

	public CreateDaisy202Book(OutputStream out) throws NotImplementedException {
		super(out);
	}

	@Override
	public void writeXmlHeader() {
		new PrintStream(out).println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
	}
	
	@Override
	public void writeXmlns() {
		new PrintStream(out).println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
	}

	public void writeDoctype() {
		new PrintStream(out).println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"xhtml1-transitional.dtd\">");	
	}
	
	@Override
	public void writeBasicMetadata() {
		new PrintStream(out).println("  <head>");
		new PrintStream(out).println("    <meta name=\"dc:title\" content=\"basic title\"/>");
		new PrintStream(out).println("    <meta name=\"dc:format\" content=\"Daisy 2.02\"/>");
		new PrintStream(out).println("  </head><body>");
	}

	@Override
	public void writeEndOfDocument() {
		new PrintStream(out).println("</body></html>");
	}
	
	/**
	 * Adds the levels specified in the section string.
	 * 
	 * Sections are validated. Invalid characters e.g. 'A' and out-of-range
	 * values will be rejected with an exception.
	 * @param sections A sequence of sections to add in the range 1 through 6
	 * e.g. "1231"
	 */
	public void addTheseLevels(String sections) {
		for (int i = 0; i < sections.length(); i++) {
			String s = Character.toString(sections.charAt(i));
			int level = Integer.parseInt(s);
			if (level < 1 || (level > 6)) {
				throw new IllegalArgumentException(String.format(
						"The format string needs in the range 1 through 6: found:%d in %s",
						i, sections));
			}
			addLevel(level);
		}
	}

	public void addLevelOne() {
		addLevel(1);
	}
	
	public void addLevelTwo() {
		addLevel(2);
	}

	public void addLevelThree() {
		addLevel(3);
	}
	
	public void addLevelFour() {
		addLevel(4);
	}
	
	public void addLevelFive() {
		addLevel(5);
	}
	
	public void addLevelSix() {
		addLevel(6);
	}
	
	/**
	 * This is made public to facilitate injecting illegal levels.
	 * 
	 * Call addLevelOne() etc. to add levels correctly.
	 * @param level Legal values range from 1 to 6, any other value is illegal
	 * according to the Daisy 2.02 specification. 
	 */
	public void addLevel(int level) {
		int counter = sectionsCreatedAutomatically + 1;
		new PrintStream(out).print("<h" + level + " id=\"test_" + counter + END_TAG);
		new PrintStream(out).print("<a href=\"test_" + counter + ".smil#text_" + counter + END_TAG);
		new PrintStream(out).print(
				"This is a dummy level one entry that doesn't match a file</a></h" + level + ">");
		sectionsCreatedAutomatically++; // Now we can update the counter
	}
	
	/**
	 * Adds an entry for a SMIL file to the contents of ncc.html
	 * 
	 * Note: this does NOT check for correctness of the values being added. It
	 * is intended to facilitate testing of the DaisyReader and related code;
	 * not to generate real DAISY books.
	 * 
	 * @param level the level to include this smil file
	 * @param smilFilename the name to add to the ncc.html contents
	 *  the contents or the name?
	 * @param idToInsert the value of the this is appended as a 'fragment' to
	 *  the smilFilename in  ncc.html
	 */

	public void addSmilFileEntry(int level, String smilFilename, String idToInsert) {
		if (filenameSeemsInvalid(smilFilename)) {
			return;
		}
		int counter = sectionsCreatedAutomatically + 1;
		new PrintStream(out).print("<h" + level + " id=\"smil_" + counter + END_TAG);
		new PrintStream(out).print("<a href=\"" + smilFilename + "#" + idToInsert + END_TAG);
		// TODO(jharty): bug in the next line - write a test for it and then fix!
		new PrintStream(out).println("This is a dummy level one entry that doesn't match a file</a></h1>");
		sectionsCreatedAutomatically++; // Now we can update the counter
	}

	/**
	 * Helper method to detect filenames for smil files that seem invalid.
	 * 
	 * Extend as your tests guide you :)
	 * @param smilFilename The filename to test
	 * @return true if the filename seems invalid, else false.
	 */
	private boolean filenameSeemsInvalid(String smilFilename) {
		if (smilFilename.length() < "x.smil".length()) {
			return true;
		}
		return false;
	}


}
