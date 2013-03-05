package org.androiddaisyreader.model;

import org.xml.sax.Attributes;

public class ParserUtilities {

	// Possible bug between Android and Java...
	// On Android the element name is returned in localName, on the
	// workstation it's returned in 'name'
	// Adding a temporary workaround until I understand what's happening!
	public static String getName(String localName, String name) {
		if (localName.length() > 0 ) {
			return localName;
		}
		return name;
	}
	
	public static String getValueForName(String nameToMatch, Attributes attributes) {
		for (int i = 0; i < attributes.getLength(); i++) {
			
			// TODO 20120512 (jharty): this code may fail on Android, see my
			// comment above. We need to test this on Android soon.
			String name = attributes.getLocalName(i);
			if (name.equalsIgnoreCase(nameToMatch)) {
				return attributes.getValue(i);
			}
		}
		return null;
	}
}
