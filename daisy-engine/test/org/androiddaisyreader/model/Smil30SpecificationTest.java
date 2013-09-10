package org.androiddaisyreader.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.androiddaisyreader.testutilities.DummyBookContext;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class Smil30SpecificationTest extends TestCase {
	private BookContext context;

	private static final String SMIL30PREAMBLE = "<smil xmlns=\"http://www.w3.org/2001/SMIL20/\">"
			+ "<head>" + "</head>" + "<body>";

	private static final String SMIL10PROLOGUE = "</seq>" + "</body>" + "</smil>";

	private static final String SMIL_WITH_1_TEXT_SECTION = SMIL30PREAMBLE + "<seq>"
			+ "<par id=\"tcp1\">" + "<text id=\"text1\" src=\"AreYouReadyV3.xml#dtb1\" />"
			+ "</par>" + SMIL10PROLOGUE;

	private static final String SMIL_WITH_2_TEXT_SECTION = SMIL30PREAMBLE + "<seq>"
			+ "<par id=\"tcp1\">" 
			+ "<text id=\"text1\" src=\"AreYouReadyV3.xml#dtb1\" />"
			+ "<text id=\"text2\" src=\"AreYouReadyV3.xml#dtb2\" />"
			+ "</par>" + SMIL10PROLOGUE;

	private static final String SMIL_WITH_1_AUDIO_SECTION = SMIL30PREAMBLE
			+ "<seq dur=\"0:00:02.029\" fill=\"remove\" id=\"mseq\">" + "<par id=\"tcp1\">"
			+ "<audio clipBegin=\"0:00:00\" clipEnd=\"0:00:02.029\" src=\"speechgen0001.mp3\" />"
			+ "</par>" + SMIL10PROLOGUE;

	private static final String SMIL_WITH_2_AUDIO_SECTION = SMIL30PREAMBLE
			+ "<seq dur=\"0:00:04.878\" fill=\"remove\" id=\"mseq\">"
			+ "<par id=\"tcp1\">"
			+ "<audio clipBegin=\"0:00:00\" clipEnd=\"0:00:02.029\" src=\"speechgen0001.mp3\" />"
			+ "<audio clipBegin=\"0:00:02.029\" clipEnd=\"0:00:04.878\" src=\"speechgen0001.mp3\" />"
			+ "</par>" + SMIL10PROLOGUE;

	private static final String SMIL_WITH_1_AUDIO_1_TEXT = SMIL30PREAMBLE
			+ "<seq dur=\"0:00:02.029\" fill=\"remove\" id=\"mseq\">" + "<par id=\"tcp1\">"
			+ "<text id=\"text1\" src=\"AreYouReadyV3.xml#dtb1\" />"
			+ "<audio clipBegin=\"0:00:00\" clipEnd=\"0:00:02.029\" src=\"speechgen0001.mp3\" />"
			+ "</par>" + SMIL10PROLOGUE;

	private final static String SMIL_FILE_WITH_TWO_PARTS = SMIL30PREAMBLE
			+ "<seq dur=\"0:00:04.878\" fill=\"remove\" id=\"mseq\">"
			+ "<par id=\"tcp1\">"
			+ "<text id=\"text1\" src=\"AreYouReadyV3.xml#dtb1\" />"
			+ "<audio clipBegin=\"0:00:00\" clipEnd=\"0:00:02.029\" src=\"speechgen0001.mp3\" />"
			+ "</par>"
			+ "<par id=\"tcp2\">"
			+ "<text id=\"text2\" src=\"AreYouReadyV3.xml#dtb2\" />"
			+ "<text id=\"text2\" src=\"AreYouReadyV3.xml#dtb3\" />"
			+ "<audio clipBegin=\"0:00:02.029\" clipEnd=\"0:00:04.878\" src=\"speechgen0001.mp3\" />"
			+ "</par>" + SMIL10PROLOGUE;

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testParsingOfSimpleSmil30WithTwoPart() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMIL_FILE_WITH_TWO_PARTS.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected two part", 2, parts.length);
	}

	public void testParsingOfSimpleSmil30WithSimpleSection() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMIL_WITH_1_AUDIO_1_TEXT.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];
		assertEquals("The part should contain one audio element", 1, part.getAudioElements().size());
		assertEquals("The part should contain one snippets", 1, part.getSnippets().size());
	}

	public void testParsingOfSimpleSmil30WithAudio() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMIL_WITH_1_AUDIO_SECTION.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];
		assertEquals("The part should contain one audio element", 1, part.getAudioElements().size());
		assertEquals("The part should not contain any snippets", 0, part.getSnippets().size());
	}

	public void testParsingOfSimpleSmil30With2Audio() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMIL_WITH_2_AUDIO_SECTION.getBytes());
		Part[] parts = parseSmilContents(contents);
		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];
		assertEquals("The part should contain one audio element", 2, part.getAudioElements().size());
		assertEquals("The part should not contain any snippets", 0, part.getSnippets().size());
	}

	public void testParsingOfSimpleSmil30WithText() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMIL_WITH_1_TEXT_SECTION.getBytes());
		Part[] parts = parseSmilContents(contents);

		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];
		assertEquals("The part should contain one snippet", 1, part.getSnippets().size());
		assertEquals("The part should not contain any audio elements", 0, part.getAudioElements()
				.size());
	}

	public void testParsingOfSimpleSmil30With2Text() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream contents = new ByteArrayInputStream(SMIL_WITH_2_TEXT_SECTION.getBytes());
		Part[] parts = parseSmilContents(contents);

		assertEquals("Expected one part", 1, parts.length);
		Part part = parts[0];
		assertEquals("The part should contain one snippet", 2, part.getSnippets().size());
		assertEquals("The part should not contain any audio elements", 0, part.getAudioElements()
				.size());
	}

	private Part[] parseSmilContents(InputStream contents) throws IOException, SAXException,
			ParserConfigurationException {
		context = new DummyBookContext("<h1 id=\"s8\"><p>" + "AreYouReady" + "</p></h1>");
		return Smil30Specification.getParts(context, contents);
	}

}
