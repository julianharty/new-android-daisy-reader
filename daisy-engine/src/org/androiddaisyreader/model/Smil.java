package org.androiddaisyreader.model;

import static org.androiddaisyreader.model.XmlUtilities.mapUnsupportedEncoding;
import static org.androiddaisyreader.model.XmlUtilities.obtainEncodingStringFromInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class Smil {

    public String format;

    public static class Builder {
        private Smil smil = new Smil();

        public Builder setFormat(String format) {
            smil.format = format;
            return this;
        }
    }

    /**
     * Gets the input source.
     * 
     * @param contents the contents
     * @return the input source
     */
    public static InputSource getInputSource(InputStream contents) {
        InputSource input = new InputSource(contents);
        try {
            String encoding = obtainEncodingStringFromInputStream(contents);
            encoding = mapUnsupportedEncoding(encoding);
            input.setEncoding(encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return input;
    }

    /**
     * Gets the XMLReader.
     * 
     * @return XMLReader
     */
    public static XMLReader getSaxParser() {
        XMLReader saxParser;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser().getXMLReader();
            saxParser.setEntityResolver(XmlUtilities.dummyEntityResolver());
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return saxParser;
    }

    public static enum Meta {
        TITLE {
            @Override
            public String toString() {
                return "dc:title";
            }
        },
        CREATOR {
            @Override
            public String toString() {
                return "dc:creator";
            }
        },
        LANGUAGE {
            @Override
            public String toString() {
                return "dc:language";
            }
        },
        CHARACTERSET {
            @Override
            public String toString() {
                return "ncc:charset";
            }
        },
        DATE {
            @Override
            public String toString() {
                return "dc:date";
            }
        },
        // Added by Logigear to resolve case: the daisy book is not audio.
        // Date: Jun-13-2013
        TOTALTIME {
            @Override
            public String toString() {
                return "ncc:totalTime";
            }
        },
        PUBLISHER {
            @Override
            public String toString() {
                return "dc:publisher";
            }
        }
        // Add more enums as we need them.
    }

    public static Date parseDate(String content, String scheme) {
        String format;

        if (scheme == null) {
            // Assume this structure, see
            // http://www.daisy.org/z3986/specifications/daisy_202.html#dtbclass
            // Note: Java uses MM for month, unlike ISO8601
            format = "yyyy-MM-dd";
        } else {
            format = scheme.replaceAll("m", "M");
        }

        DateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(content);
        } catch (ParseException pe) {
            throw new IllegalArgumentException(String.format(
                    "Problem parsing the date[%s] using scheme [%s]", content, scheme), pe);
        }

    }

}
