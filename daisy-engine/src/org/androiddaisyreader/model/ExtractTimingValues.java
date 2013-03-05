package org.androiddaisyreader.model;

import java.text.DecimalFormat;

import org.xml.sax.Attributes;

/**
 * Utility class to extract the timing values from the SMIL files.
 * 
 *  
 * Note: The npt= format is the required format for DAISY 2.02 books.
 * 
 * http://www.daisy.org/z3986/specifications/daisy_202.html#smil (and search 
 * for npt= in section 2.3.3.8 Attributes on the <audio> element)
 * See also http://www.w3.org/TR/REC-smil/ although this is not very
 * enlightening.
 * 
 * @author Julian Harty
 */
public class ExtractTimingValues {

	/**
	 * Extract the effective value of the time offset for a given element.  
	 * @param elementName the name of the element to extract the value for.
	 * @param attributes the set of attributes which include the expected name.
	 * @return the double representing the effective value of the time offset.
	 */
	@Deprecated
	static double extractTiming(String elementName, Attributes attributes) {
		String trimmedValue = getTrimmedValue(elementName, attributes);
		return Double.parseDouble(trimmedValue);
	}

	private static String getTrimmedValue(String elementName,
			Attributes attributes) {
		String rawValue = ParserUtilities.getValueForName(elementName, attributes);
		String trimmedValue = rawValue.replace("npt=", "").replace("s", "");
		return trimmedValue;
	}
	
	/**
	 * Extract the timing value as an int containing the number of mSecs.
	 * 
	 * Notes: 
	 * 1. I have a strong suspicion I'll end up revisiting this code as we
	 * add support for more locales and test the software with international 
	 * DAISY content. (However, the current old code works with a mix of books
	 * from various countries and languages so perhaps this'll work?)
	 * 
	 * 2. This code is probably unnecessarily complicated, I will seek existing
	 * java libraries which may more elegantly replace most of my code...
	 * 
	 * @param elementName The name we want to extract the value for
	 * @param attributes The set of attributes.
	 * @return the int value representing the number of miliseconds for elementName
	 */
	static int extractTimingAsMilliSeconds(String elementName, Attributes attributes) {
		Double temp = extractTiming(elementName, attributes);
		DecimalFormat milliseconds = new DecimalFormat("###.###");
		String formattedDouble = milliseconds.format(temp);
		String[] values = formattedDouble.split("\\.");
		
		if (values.length > 2 || values.length == 0) {
			throw new NumberFormatException(
					"Expected the number to be formatted with no more than 1 decimal point, got " 
					+ temp.toString());
		}
		
		if (values.length == 1) {
			return Integer.parseInt(values[0]) * 1000;
		}

		return Integer.parseInt(values[0]) * 1000 + extractMilliSeconds(values[1]);
	}

	/**
	 * The following code compensates for values such as .6 which is 600 mS
	 * and .15 which is 150 mS, etc.
	 * 
	 * @param decimalPart the string representing the digits to the right of
	 * the decimal point.
	 * @return the value as an integer.
	 */
	private static int extractMilliSeconds(String decimalPart) {

		int multiplier = 1;
		switch (decimalPart.length()) {
		case 3:
		case 0:
			multiplier = 1;
			break;
		case 2:
			multiplier = 10;
			break;
		case 1:
			multiplier = 100;
			break;
		default:
			throw new NumberFormatException("Unexpected number of digits after decimal point, got "
					+ decimalPart);
		}
		return Integer.parseInt(decimalPart) * multiplier;
	}
}

