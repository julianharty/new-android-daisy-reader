package org.androiddaisyreader.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.androiddaisyreader.testutilities.DummyBookContext;
import org.xml.sax.SAXException;

public class Smil10SpecificationTest extends TestCase {
	private static final String SEQ_PAR_POSTAMBLE = "</par>" + "</seq>";

	private static final String SEQ_PAR_PREAMBLE = "<seq dur=\"0.0s\">" +
			"<par endsync=\"last\" id=\"par_12\">";

	private BookContext context;

	private static final String SMIL10PREAMBLE = 
		"<?xml version=\"1.0\" encoding=\"windows-1252\"?>" +
		"<!DOCTYPE smil PUBLIC \"-//W3C//DTD SMIL 1.0//EN\" \"http://www.w3.org/TR/REC-smil/SMIL10.dtd\">" +
		"<smil>" +
		"<head>" +
		"<meta name=\"dc:format\" content=\"Daisy 2.02\" />" +
		"</head>" +
		"<body>";
	private static final String SMIL10PROLOGUE = 
		"</body>" +
		"</smil>";
	
	private static final String SMILWITH1TEXTSECTION = 
		SMIL10PREAMBLE + SEQ_PAR_PREAMBLE + 
		"<text src=\"dummy.html#s8\" id=\"i10\" />" + 
		SEQ_PAR_POSTAMBLE + 
		SMIL10PROLOGUE;
	
	private static final String SMILWITH1BROKENLINKINTEXTSECTION = 
			SMIL10PREAMBLE + SEQ_PAR_PREAMBLE + 
			"<text src=\"dummy.html#broken_link\" id=\"i10\" />" + 
			SEQ_PAR_POSTAMBLE + 
			SMIL10PROLOGUE;
	
	private static final String SMILWITH2TEXTSECTIONS = 
		SMIL10PREAMBLE + SEQ_PAR_PREAMBLE + "<text src=\"dummy.html#s8\" id=\"i10\" />" + "<text src=\"dummy.html#s9\" id=\"i11\" />" + SEQ_PAR_POSTAMBLE + SMIL10PROLOGUE;
	
	private static final String SMILWITH1AUDIOSECTION = 
		SMIL10PREAMBLE +
		"<seq dur=\"1.666s\">" +
		"<audio src=\"meow.mp3\" clip-begin=\"npt=0.000s\" clip-end=\"npt=1.666s\" id=\"audio_0001\"/>" +
		"</seq>" +
		SMIL10PROLOGUE;

	private static final String SMILWITH2AUDIOSECTIONS = 
			SMIL10PREAMBLE +
			"<seq dur=\"4.317s\">" +
			"<audio src=\"meow.mp3\" clip-begin=\"npt=0.000s\" clip-end=\"npt=1.666s\" id=\"audio_0001\"/>" +
			"<audio src=\"meow.mp3\" clip-begin=\"npt=1.666s\" clip-end=\"npt=4.317s\" id=\"audio_0001\"/>" +
			"</seq>" +
			SMIL10PROLOGUE;

	private static final String EXPECTED_CONTENTS = "Hello tests";
	
	public void testParsingOfSimpleSmil10WithText() throws IOException, SAXException, ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMILWITH1TEXTSECTION.getBytes());
		Part[] parts = parseSmilContents(contents);

		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];
		assertEquals("The part should contain one snippet", 1, part.getSnippets().size());
		// TODO 20120207 revise once we implement processing of the snippets.
		assertEquals("The snippet name is incorrect", EXPECTED_CONTENTS, part.getSnippets().get(0).getText());
		assertEquals("Currently we expect only one snippet.", 1, part.getSnippets().size());
		assertEquals("The part should not contain any audio elements", 0, part.getAudioElements().size());
		}

	public void testErrorHandlingForBrokenSmilPointerToTextContents() throws IOException, SAXException, ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMILWITH1BROKENLINKINTEXTSECTION.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];
		assertEquals("The part should contain one snippet", 1, part.getSnippets().size());
		
		final Snippet snippet = part.getSnippets().get(0);
		if (snippet.hasText()) {
			assertEquals("The snippet name is incorrect", EXPECTED_CONTENTS, snippet.getText());
		}
	}
	
	public void testParsingOfSimpleSmil10WithAudio() throws IOException, SAXException, ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMILWITH1AUDIOSECTION.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];		
		assertEquals("The part should contain one audio element", 1, part.getAudioElements().size());
		assertEquals("The part should not contain any snippets", 0, part.getSnippets().size());
	}
	
	public void testParsingOfSimpleSmil10WithTwoAudioSections() throws IOException, SAXException, ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMILWITH2AUDIOSECTIONS.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected two parts", 2, parts.length);
		for (int item = 0; item < parts.length; item++) {
			Part part = parts[item];		
			assertEquals("The part should contain 1 audio element", 1, part.getAudioElements().size());
			assertEquals("The part should not contain any snippets", 0, part.getSnippets().size());
		}
	}
	
	public void testParsingOfSimpleSmil10WithTwoTextSections() throws IOException, SAXException, ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMILWITH2TEXTSECTIONS.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected two parts", 1, parts.length);
		Part part = parts[0];		
		assertEquals("The part should not contain any audio", 0, part.getAudioElements().size());
		assertEquals("The part should contain 2 snippets", 2, part.getSnippets().size());
	}
	
	private Part[] parseSmilContents(InputStream contents) throws IOException,
			SAXException, ParserConfigurationException {
		
		// TODO 20120214 (jharty): we need a way to create a book context for streams.
		context = new DummyBookContext("<h1 id=\"s8\"><p>" + EXPECTED_CONTENTS + "</p></h1>");
		return Smil10Specification.getParts(context, contents);
	}

}
