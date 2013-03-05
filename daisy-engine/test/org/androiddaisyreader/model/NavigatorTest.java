package org.androiddaisyreader.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.androiddaisyreader.testutilities.CreateDaisy202Book;
import org.androiddaisyreader.testutilities.NotImplementedException;

public class NavigatorTest extends TestCase {

	private static final String SECTIONS_FOR_COMPLEX_NCC = "123123211123454566";
	private ByteArrayInputStream bookContents;
	private Navigator navigator;
	
	@Override
	protected void setUp() {
		bookContents = NccSpecificationTest.createNCC();
		Book book = null;
		try {
			book = NccSpecification.readFromStream(bookContents);
		} catch (IOException e) {
			// We cannot throw an exception from setUp so this is all we're doing...
			e.printStackTrace();
		}
		navigator = new Navigator(book);
	}
	
	public void testNavigationFromStartToEndOfBook() {
		int elements = 0;
		while (navigator.hasNext()) {
			Navigable n = navigator.next();
			assertTrue(n != null);
			elements++;
		}
		assertEquals("Expected 5 elements for a book with 12231 sections", 5, elements);
	}
	
	public void testNavigationBackwardsThroughBook() {
		int elements = 0;
		
		// First we need to reach the end of the book
		while (navigator.hasNext()) {
			Navigable n = navigator.next();
		}
		
		// We should be at the end of the book now.
		while (navigator.hasPrevious()) {
			Navigable n = navigator.previous();
			assertTrue(n != null);
			Section s = (Section)n;
			elements++;
		}
		assertEquals("Expected 5 elements for a book with 12231 sections", 5, elements);
	}
	
	public void testNavigationOfComplexDaisy202BookStructure() throws NotImplementedException, IOException {
		Daisy202Book book = createDaisy202Structure(SECTIONS_FOR_COMPLEX_NCC);
		
		Navigator navigator = new Navigator(book);
		navigator.gotoStartOfContent();
		
		int elements = 0;
		
		while (navigator.hasNext()) {
			Navigable n = navigator.next();
			assertSectionEquals(SECTIONS_FOR_COMPLEX_NCC, elements, n);
			elements++;
		}
		assertEquals("Expected to process all elements", 
				SECTIONS_FOR_COMPLEX_NCC.length(), elements);
	}

	public void testForwardAndBackwardNavigationOfComplexDaisy202BookStructure() throws NotImplementedException, IOException {
		Daisy202Book book = createDaisy202Structure(SECTIONS_FOR_COMPLEX_NCC);
		
		Navigator localNavigator = new Navigator(book);
		localNavigator.gotoStartOfContent();
		
		int position;
		ArrayList<Integer> levelsTraversed = new ArrayList<Integer>();
		
		for (int sectionsToTraverse = 0; sectionsToTraverse < SECTIONS_FOR_COMPLEX_NCC.length(); sectionsToTraverse++) {
			position = 0;
			while (localNavigator.hasNext() && position < sectionsToTraverse) {
				Navigable n = localNavigator.next();
				assertSectionEquals(SECTIONS_FOR_COMPLEX_NCC, position, n);
				levelsTraversed.add(((Section)n).getLevel());
				position++;
			}
			assertEquals(
					String.format("Didn't traverse the expected number of sections in: %s", 
							SECTIONS_FOR_COMPLEX_NCC),
							sectionsToTraverse, position);

			Navigable n;
			while (position > 0 && localNavigator.hasPrevious()) {
				position--;  // Useful to decrement now as we can use it as the array index.
				n = localNavigator.previous();
				int expectedValue = levelsTraversed.get(position);
				int levelFound = ((Section)n).getLevel();
				assertEquals("Expected the level returned to match that discovered from the forward navigation.", expectedValue, levelFound);
			}
			levelsTraversed.clear();
		}
	}
	
	public void testSmilFilenamesAreCaptured() {
		Navigable n = null;
		String smilFilename = null;
		while (navigator.hasNext()) {
			n = navigator.next();
			smilFilename = ((Daisy202Section)n).getSmilFilename();
			System.out.println(smilFilename);
			assertNotNull("", smilFilename);
		}
	}
	/**
	 * assertSectionEquals compare section contains the correct value.
	 * 
	 * The value of the section at position is compared with the contents of
	 * the navigable item.
	 * @param structure The string that represents the sequence of sections.
	 * @param position The position within the structure to compare against.
	 * @param n the Navigable item.
	 */
	void assertSectionEquals(String structure, int position, Navigable n) {
		assertEquals(
				String.format(
						"Section should be in correct sequence. Sequence [%s] element [%d]",
						structure, position), 
						Character.getNumericValue((structure.charAt(position))),
						((Section)n).getLevel());
	}

	/**
	 * Helper method to create a Daisy202 Ncc Structure.
	 * @param structure to create as a numeric string e.g. 11231
	 * @return a Daisy202Book structure
	 * @throws NotImplementedException
	 * @throws IOException
	 */
	Daisy202Book createDaisy202Structure(String structure) throws NotImplementedException,
			IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CreateDaisy202Book eBookContents = new CreateDaisy202Book(out);
		eBookContents.writeXmlHeader();
		eBookContents.writeDoctype();
		eBookContents.writeXmlns();
		eBookContents.writeBasicMetadata();
		eBookContents.addTheseLevels(structure);
		eBookContents.writeEndOfDocument();
		ByteArrayInputStream bookContents = new ByteArrayInputStream(out.toByteArray());
		Daisy202Book book = NccSpecification.readFromStream(bookContents);
		return book;
	}
}
