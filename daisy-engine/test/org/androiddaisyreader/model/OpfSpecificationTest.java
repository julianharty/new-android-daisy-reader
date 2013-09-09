package org.androiddaisyreader.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import junit.framework.TestCase;

import org.androiddaisyreader.testutilities.SampleContentDaisy30;

public class OpfSpecificationTest extends TestCase {
	private static final String PATH_EBOOK_30 = "/sdcard/files-used-for-testing/testfiles/Are_you_ready_minidaisy3";
	private static final String OPF_NAME = "speechgen.opf";

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testUsingValidSampleContent() throws IOException {
		ByteArrayInputStream content = new ByteArrayInputStream(
				(SampleContentDaisy30.simpleValidOpf).getBytes());
		// This testcase must get title of daisybook, so we don't need bookcontext.
		DaisyBook daisybook = OpfSpecification.readFromStream(content, null);
		assertEquals("The Code Talkers", daisybook.getTitle());
		assertEquals(new Date(2008 - 1900, 5 - 1, 9), daisybook.getDate());
	}
	
	public void testReadFromPath() throws IOException {
		BookContext bookContext = openBook();
		InputStream contents = bookContext.getResource(OPF_NAME);
		DaisyBook daisyBook = OpfSpecification.readFromStream(contents, bookContext);
		assertEquals(1, daisyBook.sections.size());
		assertEquals(1, daisyBook.getChildren().size());
		assertEquals("Mini daisy 3", daisyBook.getTitle());
		
	}
	
	private BookContext openBook(){
		BookContext bookContext;
		File directory = new File(PATH_EBOOK_30 + File.separator + OPF_NAME);
		boolean isDirectory = directory.isDirectory();
		if (isDirectory) {
			bookContext = new FileSystemContext(PATH_EBOOK_30);
		} else {
			bookContext = new FileSystemContext(directory.getParent());
		}
		return bookContext;
	}

}
