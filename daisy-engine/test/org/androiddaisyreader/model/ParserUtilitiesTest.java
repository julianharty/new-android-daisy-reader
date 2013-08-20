package org.androiddaisyreader.model;

import junit.framework.TestCase;

import org.androiddaisyreader.model.ParserUtilities;
import org.xml.sax.Attributes;

/**
 * Test Cases for the ParserUtilities class to test and demonstrate that class.
 * 
 * I'm using this class for a little forray into TDD style development to
 * address and apply changes learnt from using the current implementations
 * of SAX parser which behave differently from the implementation in Java 5
 * and Android runtimes.
 * 
 * @author julianharty
 */
public class ParserUtilitiesTest extends TestCase {
	
	private class TestAttributes implements Attributes {

		public int getIndex(String qName) {
			return 0;
		}

		public int getIndex(String uri, String localName) {
			return 0;
		}

		public int getLength() {
			return 0;
		}

		public String getLocalName(int index) {
			return null;
		}

		public String getQName(int index) {
			return null;
		}

		public String getType(int index) {
			return null;
		}

		public String getType(String qName) {
			return null;
		}

		public String getType(String uri, String localName) {
			return null;
		}

		public String getURI(int index) {
			return null;
		}

		public String getValue(int index) {
			return null;
		}

		public String getValue(String qName) {
			return null;
		}

		public String getValue(String uri, String localName) {
			return null;
		}
		
	};
	
	private class QNameAttributes extends TestAttributes {
		private String qNameValue;
		
		private QNameAttributes() {};
		QNameAttributes(String valueToReturn) {
			qNameValue = valueToReturn;
		}
		
		@Override
		public String getQName(int index) {
			return qNameValue;
		}
		
	}
	
	public void testNameCanBeFoundFromLocalNameOrQname() {
		Attributes qNameImplementation = new QNameAttributes("id");
		
		assertEquals("id", ParserUtilities.getValueForName("id", qNameImplementation));
	}

}
