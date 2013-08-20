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
		protected String name = "";
		protected String value;

		public int getIndex(String qName) {
			return 0;
		}

		public int getIndex(String uri, String localName) {
			return 0;
		}

		public int getLength() {
			return 1;
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
			return value;
		}

		public String getValue(String qName) {
			return null;
		}

		public String getValue(String uri, String localName) {
			return value;
		}
		
		/**
		 * Adds an attribute to the class. Currently the name is ignored.
		 * @param name
		 * @param value
		 * @return
		 */
		public boolean addAttribute(String name, String value) {
			this.name = name;
			this.value = value;
			return true;
		}
		
	};
	
	private class QNameAttributes extends TestAttributes {
		
		@Override
		public String getQName(int index) {
			return name;
		}
	}
		
	private class LocalNameAttributes extends TestAttributes {

		@Override
		public String getLocalName(int index) {
			return name;
		}

	}
	
	public void testNameCanBeFoundFromLocalName() {
		TestAttributes localNameImplementation = new LocalNameAttributes();
		localNameImplementation.addAttribute("name", "value1");
		assertEquals("value1", ParserUtilities.getValueForName("name", localNameImplementation));
	}

	public void testNameCanBeFoundFromQName() {
		TestAttributes qNameImplementation = new QNameAttributes();
		qNameImplementation.addAttribute("name", "value");
		assertEquals("value", ParserUtilities.getValueForName("name", qNameImplementation));
	}
	public void testNameCanBeFoundFromLocalNameOrQname() {
		TestAttributes qNameImplementation = new QNameAttributes();
		qNameImplementation.addAttribute("id", "value2");
		assertEquals("value2", ParserUtilities.getValueForName("id", qNameImplementation));
	}

}
