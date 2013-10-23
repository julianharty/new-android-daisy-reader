package org.androiddaisyreader.model;

import org.xml.sax.Attributes;

/**
 * ParserUtilities are several static helper methods written to work with SAX
 * parsers.
 * 
 * @author julianharty
 * 
 */
public class ParserUtilities {

    // Possible bug between Android and Java...
    // On Android the element name is returned in localName, on the
    // workstation it's returned in 'name'
    // Adding a temporary workaround until I understand what's happening!
    public static String getName(String localName, String name) {
        if (localName.length() > 0) {
            return localName;
        }
        return name;
    }

    /**
     * Return the value for a given name if the name is matched. Matches are
     * case-insensitive.
     * 
     * The code has been adapted to work on a range of implementations of SAX
     * parser. For Android and Java 5 the name seems to be returned by
     * getLocalName(...). For Java 6 and Java 7 We need to use getQName(...)
     * instead. This implementation is basic and tries the first, and only calls
     * the second if the first returns a null or empty string.
     * 
     * @param nameToMatch
     * @param attributes
     * @return the value if found, else null.
     */
    public static String getValueForName(String nameToMatch, Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {

            // TODO 20120512 (jharty): this code may fail on Android, see my
            // comment above. We need to test this on Android soon.
            String name = attributes.getLocalName(i);

            if (name == null || name.length() == 0) {
                // Newer SAX parsers return the qualified name rather than the
                // localName.
                name = attributes.getQName(i);
            }

            if (name.equalsIgnoreCase(nameToMatch)) {
                return attributes.getValue(i);
            }
        }
        return null;
    }
}
