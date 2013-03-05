package org.androiddaisyreader.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.androiddaisyreader.testutilities.CreateDaisy202Book;
import org.androiddaisyreader.testutilities.NotImplementedException;
import org.androiddaisyreader.testutilities.SampleContent;

import junit.framework.TestCase;

public class NccSpecificationTest extends TestCase {
	ByteArrayInputStream bookContents = null;
	
	@Override
	protected void setUp() {
		bookContents = createNCC();
	}

	public static ByteArrayInputStream createNCC() {
		ByteArrayOutputStream out  = new ByteArrayOutputStream();
		CreateDaisy202Book eBookContents = null;
		try {
			eBookContents = new CreateDaisy202Book(out);
		} catch (NotImplementedException e) {
			e.printStackTrace();
		}
		eBookContents.writeXmlHeader();
		eBookContents.writeDoctype();
		eBookContents.writeXmlns();
		eBookContents.writeBasicMetadata();
		eBookContents.addTheseLevels("12231");
		eBookContents.writeEndOfDocument();
		return new ByteArrayInputStream(out.toByteArray());
	}
	
	// This is a spike, and intended to be replaced once we integrate this code with the main project.
	public void testReadFromFile() throws IOException {
		File inputFile = new File("/sdcard/files-used-for-testing/testfiles/minidaisyaudiobook/ncc.html");
		Daisy202Book thingy = NccSpecification.readFromFile(inputFile);
		assertEquals("A mini DAISY book for testing", thingy.getTitle());
		
		// TODO 201201 25 (jharty): the following test is ugly and uses a deprecated constructor.
		assertEquals(new Date(2011 - 1900, 8 - 1, 28), thingy.getDate());
	}
	
	public void testUsingValidSampleContent() throws IOException {
		ByteArrayInputStream content = new ByteArrayInputStream((SampleContent.simpleValidNccHtml).getBytes());
		Daisy202Book anotherThingy = NccSpecification.readFromStream(content, "utf-8");
		assertEquals(SampleContent.firstTitle, anotherThingy.getTitle());
		assertEquals(1, anotherThingy.sections.size());
		assertEquals(1, anotherThingy.getChildren().size());
	}
	
	public void testCorrectSectionsForTwoSectionContents() throws IOException {
		ByteArrayInputStream content = new ByteArrayInputStream((SampleContent.validIcelandicNccHtml).getBytes("utf-8"));
		Daisy202Book icelandicContents = NccSpecification.readFromStream(content, "utf-8");
		assertEquals(2, icelandicContents.getChildren().size());
	}
	
	public void testNestingOfSections() throws NotImplementedException, IOException {
		Daisy202Book book = NccSpecification.readFromStream(bookContents);
		assertEquals("Count should match the number of level 1 sections.", 2, book.getChildren().size());
	}
	
	public void testChildrenSections() throws IOException {
		Daisy202Book book = NccSpecification.readFromStream(bookContents);
		Navigable childrenOfFirstLevelOneSection = book.getChildren().get(0);
		assertEquals("There should be 2 level 2 children", 2, childrenOfFirstLevelOneSection.getChildren().size());
		assertEquals("There should be 1 level 3 child for the second level 2", 
				1, childrenOfFirstLevelOneSection.getChildren().get(1).getChildren().size());
		
	}
}
