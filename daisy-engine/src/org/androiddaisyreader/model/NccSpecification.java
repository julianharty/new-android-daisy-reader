package org.androiddaisyreader.model;

import static org.androiddaisyreader.model.XmlUtilities.mapUnsupportedEncoding;
import static org.androiddaisyreader.model.XmlUtilities.obtainEncodingStringFromInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.androiddaisyreader.model.DaisySection.Builder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser to handle the NCC files used by Daisy 2.02 books.
 * 
 * The NCC contains the central control structures for a Daisy 2.02 book e.g.
 * The meta-data, sections, and page numbers.
 * 
 * TODO 20120214 (jharty): I'd like to improve the way we report errors in the
 * contents. For instance we may encounter several problems in the ncc.html
 * file. It'd be good to capture the set of problems, store them for later
 * reporting (so we can discover ways to improve our code to handle the issues).
 * We should be able to tell the user what went wrong when trying to parse the
 * contents of the book and make it easy for them to send us details of the
 * problems (and optionally the ncc.html file that caused the problem).
 * 
 * My current idea would be to collect the issues and throw an exception when
 * the parsing has completed. Sometimes we may throw an exception sooner e.g. if
 * it's impractical to continue with the parsing.
 * 
 * @author jharty
 */
public class NccSpecification extends DefaultHandler {
    private Element current;
    private Stack<DaisySection.Builder> headingStack = new Stack<DaisySection.Builder>();
    // TODO 20120124 (jharty):replace with something that doesn't use Vector
    private StringBuilder buffer = new StringBuilder();
    private static final Integer NUM_LEVELS_AVAILABLE_IN_DAISY202 = 6;

    private DaisyBook.Builder bookBuilder = new DaisyBook.Builder();
    private String href;

    private enum Element {
        A, HTML, META, TITLE, H1, H2, H3, H4, H5, H6, SPAN;
        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private static Map<String, Element> elementMap = new HashMap<String, Element>(
            Element.values().length);
    static {
        for (Element e : Element.values()) {
            elementMap.put(e.toString(), e);
        }
    }

    private static Map<Element, Integer> levelMap = new HashMap<Element, Integer>(
            NUM_LEVELS_AVAILABLE_IN_DAISY202);
    static {
        levelMap.put(Element.H1, 1);
        levelMap.put(Element.H2, 2);
        levelMap.put(Element.H3, 3);
        levelMap.put(Element.H4, 4);
        levelMap.put(Element.H5, 5);
        levelMap.put(Element.H6, 6);
    }

    private static Map<String, Smil.Meta> metaMap = new HashMap<String, Smil.Meta>(
            Smil.Meta.values().length);
    static {
        for (Smil.Meta m : Smil.Meta.values()) {
            metaMap.put(m.toString(), m);
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) {
        current = elementMap.get(ParserUtilities.getName(localName, name));
        if (current == null) {
            return;
        }

        switch (current) {
        case A:
            handleAnchor(attributes);
            break;
        case H1:
        case H2:
        case H3:
        case H4:
        case H5:
        case H6:
            buffer.setLength(0);
            href = null;
            handleStartOfHeading(current, attributes);
            break;
        case META:
            handleMeta(attributes);
            break;
        case SPAN:
            // TODO 20120124 (jharty): We need to handle page numbers at some
            // point.
            break;
        default:
            // do nothing for now for unmatched elements
            break;
        }
    }

    private void handleAnchor(Attributes attributes) {
        href = ParserUtilities.getValueForName("href", attributes);
    }

    private void handleStartOfHeading(Element heading, Attributes attributes) {
        // Create the new header
        DaisySection.Builder builder = new DaisySection.Builder();
        builder.setId(getId(attributes));
        builder.setLevel(levelMap.get(heading));

        attachParents(levelMap.get(heading));
        headingStack.push(builder);
    }

    private void attachParents(Integer level) {
        if (headingStack.empty()) {
            return;
        }
        Builder parent = headingStack.peek();
        if (parent.getLevel() >= level) {
            attachSectionToParent();
        } else {
            return;
        }
        attachParents(level);

    }

    private void attachSectionToParent() {
        DaisySection.Builder sibblingBuilder = headingStack.pop();
        Section sibbling = sibblingBuilder.build();
        if (headingStack.empty()) {
            bookBuilder.addSection(sibbling);
        } else {
            headingStack.peek().addSection(sibbling);
        }
    }

    private String getId(Attributes attributes) {
        return ParserUtilities.getValueForName("id", attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        buffer.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {

        // add current element type to the book model.
        current = elementMap.get(ParserUtilities.getName(localName, name));
        if (current == null) {
            return;
        }

        switch (current) {
        case H1:
        case H2:
        case H3:
        case H4:
        case H5:
        case H6:
            handleEndOfHeading(current);
            break;
        case HTML:
            while (!headingStack.empty()) {
                attachSectionToParent();
            }
            break;
        default:
            break;
        }
    }

    private void handleEndOfHeading(Element current) {
        Builder currentBuilder = headingStack.peek();
        int levelOnStack = currentBuilder.getLevel();
        Integer currentLevel = levelMap.get(current);
        if (levelOnStack != currentLevel) {
            throw new IllegalStateException(String.format(
                    "Expected the same level as [%s] found [%s]", currentLevel, currentBuilder));
        }

        currentBuilder.setTitle(buffer.toString());
        if (href != null) {
            currentBuilder.setHref(href);
        }
    }

    private void handleMeta(Attributes attributes) {
        String metaName = null;
        String content = null;
        String scheme = null;

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            // The following code fixes the failure in travis-ci. Newer parsers
            // don't return data for getLocalName. Instead they return the
            // Qualified Name.
            if (name.length() == 0) {
                name = attributes.getQName(i);
            }

            if (name.equalsIgnoreCase("name") || name.equalsIgnoreCase("Content-type")) {
                metaName = attributes.getValue(i);
            }

            if (name.equalsIgnoreCase("content")) {
                content = attributes.getValue(i);
            }

            if (name.equalsIgnoreCase("scheme")) {
                scheme = attributes.getValue(i);
            }
        }

        Smil.Meta meta = metaMap.get(metaName);
        if (meta == null) {
            return;
        }

        switch (meta) {
        case DATE:
            Date date = Smil.parseDate(content, scheme);
            bookBuilder.setDate(date);
            break;
        case TITLE:
            bookBuilder.setTitle(content);
            break;
        // Added by Logigear to resolve case: the daisy book is not audio.
        // Date: Jun-13-2013
        case TOTALTIME:
            bookBuilder.setTotalTime(content);
            break;
        case CREATOR:
            bookBuilder.setCreator(content);
            break;
        case PUBLISHER:
            bookBuilder.setPublisher(content);
            break;

        default:
            // this handles null (apparently :)
        }
    }

    public DaisyBook build() {
        return bookBuilder.build();
    }

    public static DaisyBook readFromFile(File file) throws IOException {
        InputStream contents = new BufferedInputStream(new FileInputStream(file));
        String encoding = obtainEncodingStringFromInputStream(contents);
        encoding = mapUnsupportedEncoding(encoding);
        return readFromStream(contents, encoding);
    }

    public static DaisyBook readFromStream(InputStream contents) throws IOException {
        String encoding = obtainEncodingStringFromInputStream(contents);
        encoding = mapUnsupportedEncoding(encoding);
        return readFromStream(contents, encoding);

    }

    public static DaisyBook readFromStream(InputStream contents, String encoding)
            throws IOException {
        NccSpecification specification = new NccSpecification();
        try {
            XMLReader saxParser = Smil.getSaxParser();
            saxParser.setContentHandler(specification);
            saxParser.parse(Smil.getInputSource(contents));
            contents.close();
            return specification.build();

        } catch (Exception e) {
            throw new IOException("Couldn't parse the ncc.html contents.", e);
        }
    }

}
