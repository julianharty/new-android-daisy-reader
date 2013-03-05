package org.androiddaisyreader.model;

import org.xml.sax.helpers.AttributesImpl;

import junit.framework.TestCase;

/**
 * Unit tests to ensure we can parse timing values correctly from SMIL files.
 * files.
 * 
 * The specifications are unclear on how large numbers need be formatted, so I
 * expect to refine the tests, and the underlying code based on the contents 
 * of actual DAISY books.
 * 
 * @author Julian Harty
 *
 */
public class ExtractTimingValuesTest extends TestCase {

	private static final String INVALID_NUMBER_WITH_2_DECIMAL_POINTS = "npt=456.12.34s";
	private static final String NUMBER_WITHOUT_DECIMAL_POINT = "npt=10s";
	private static final String CDATA = "CDATA";
	private static final String CLIP_BEGIN = "clip-begin";
	private AttributesImpl attributes;
	protected void setUp() {
		attributes = new AttributesImpl();
		
	}
	public void testValidTiming() {
		attributes.addAttribute("", CLIP_BEGIN, "", CDATA, "npt=12.345s");
		int result = ExtractTimingValues.extractTimingAsMilliSeconds(CLIP_BEGIN, attributes);
		assertEquals("Expected the extracted timing should match", 12345, result);
	}
	
	public void testLargeValidTiming() {
		attributes.addAttribute("", CLIP_BEGIN, "", CDATA, "npt=123456.789s");
		int result = ExtractTimingValues.extractTimingAsMilliSeconds(CLIP_BEGIN, attributes);
		assertEquals("Expected the extracted timing should match", 123456789, result);
	}
	
	public void testNumberWithOneDecimalPlace() {
		attributes.addAttribute("", CLIP_BEGIN, "", CDATA, "npt=456.7s");
		int result = ExtractTimingValues.extractTimingAsMilliSeconds(CLIP_BEGIN, attributes);
		assertEquals("Expected the extracted timing should match", 456700, result);
	}

	public void testNumberWithTwoDecimalPlaces() {
		attributes.addAttribute("", CLIP_BEGIN, "", CDATA, "npt=456.89s");
		int result = ExtractTimingValues.extractTimingAsMilliSeconds(CLIP_BEGIN, attributes);
		assertEquals("Expected the extracted timing should match", 456890, result);
	}

	public void testNumberWithFourDecimalPlacesIsTrimmedToThree() {
		attributes.addAttribute("", CLIP_BEGIN, "", CDATA, "npt=456.1234s");
		int result = ExtractTimingValues.extractTimingAsMilliSeconds(CLIP_BEGIN, attributes);
		assertEquals("Expected the extracted timing should match", 456123, result);
	}
	
	public void testInvalidFormatRaisesNFE() {
		attributes.addAttribute("", CLIP_BEGIN, "", CDATA, INVALID_NUMBER_WITH_2_DECIMAL_POINTS);
		try {
			int result = ExtractTimingValues.extractTimingAsMilliSeconds(CLIP_BEGIN, attributes);
			fail("Expected a NumberFormatException for a number with 2 decimal points. Number = "
					+ INVALID_NUMBER_WITH_2_DECIMAL_POINTS);
		} catch (NumberFormatException nfe) {
			// Nothing to do, this is the expected behaviour.
		}
	}
	
	/**
	 * The DAISY 2.02 specification says all numbers should be in SS.S format.
	 * 
	 * However, I've ended up writing the code to also accept numbers without
	 * the decimal point. Doing so makes the application more tolerant and
	 * shouldn't adversely affect the behaviour AFAIK.
	 * 
	 * This is the relevant test of the new behaviour :)
	 */
	public void testNumberWithoutDecimal () {
		attributes.addAttribute("", CLIP_BEGIN, "", CDATA, NUMBER_WITHOUT_DECIMAL_POINT);
		int result = ExtractTimingValues.extractTimingAsMilliSeconds(CLIP_BEGIN, attributes);
		assertEquals("Number without a decimal point should be accepted.", 10000, result);
	}
}
