package org.androiddaisyreader.model;

import static org.androiddaisyreader.model.XmlUtilities.mapUnsupportedEncoding;
import static org.androiddaisyreader.model.XmlUtilities.obtainEncodingStringFromInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class OpfSpecification extends DefaultHandler {
    private Element current;
    // private Stack<Daisy30Section.Builder> headingStack = new
    // Stack<Daisy30Section.Builder>();
    private Map<String, String> manifestItem = new HashMap<String, String>();
    // TODO 20120124 (jharty):replace with something that doesn't use Vector
    private StringBuilder buffer = new StringBuilder();
    private List<XmlModel> listModel;
    private DaisyBook.Builder bookBuilder = new DaisyBook.Builder();
    private BookContext bookContext;

    public OpfSpecification(BookContext bookContext) {
        this.bookContext = bookContext;
    }

    private enum Element {
        A, METADATA, ITEM, ITEMREF, SPINE;
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

        if (name.contains("dc")) {
            buffer.setLength(0);
        }
        if (current == null) {
            return;
        }

        switch (current) {
        case ITEM:
            buffer.setLength(0);
            handleItemOfHeading(attributes);
            break;
        case ITEMREF:
            handleStartOfSpine(attributes);
            break;
        case SPINE:
            buffer.setLength(0);
            break;
        default:
            // do nothing for now for unmatched elements
            break;
        }
    }

    private void handleItemOfHeading(Attributes attributes) {
        String href = getHref(attributes);
        if (href.endsWith("xml") && bookBuilder != null) {
            InputStream contents;
            try {
                contents = bookContext.getResource(href);
                listModel = XmlSpecification.readFromStream(contents);
            } catch (IOException e) {
            }
        }
        manifestItem.put(getId(attributes), getHref(attributes));
    }

    private void handleStartOfSpine(Attributes attributes) {
        // Create the new header
        String smilHref = manifestItem.get(getIdRef(attributes));
        XmlModel model = getXmlModelBySmilHref(smilHref);
        attachSectionToParent(model);
    }

    private void attachSectionToParent(XmlModel model) {
        DaisySection.Builder builder = new DaisySection.Builder();
        if (model != null) {
            builder.setId(model.getId());
            builder.setLevel(model.getLevel());
            builder.setTitle(model.getText());
            builder.setHref(model.getSmilHref());
            Section sibbling = builder.build();
            bookBuilder.addSection(sibbling);
            listModel.remove(model);
        }
    }

    private XmlModel getXmlModelBySmilHref(String smilHref) {
        XmlModel result = null;
        for (XmlModel model : listModel) {
            if (model.getSmilHref().contains(smilHref)) {
                result = model;
                break;
            }
        }
        return result;
    }

    private String getId(Attributes attributes) {
        return ParserUtilities.getValueForName("id", attributes);
    }

    private String getHref(Attributes attributes) {
        return ParserUtilities.getValueForName("href", attributes);
    }

    private String getIdRef(Attributes attributes) {
        return ParserUtilities.getValueForName("idref", attributes);
    }

    private void handleMetadata(String tagName) {
        String content = buffer.toString();
        Smil.Meta meta = metaMap.get(tagName.toLowerCase());

        if (meta == null) {
            return;
        }
        switch (meta) {
        case DATE:
            Date date = Smil.parseDate(content, null);
            bookBuilder.setDate(date);
            break;
        case TITLE:
            bookBuilder.setTitle(content);
            break;
        // Added by Logigear to resolve case: the daisy book is not audio.
        // Date: Jun-13-2013
        case TOTALTIME:
            // bookBuilder.setTotalTime(content);
            // System.out.println("TOTALTIME " + content);
            break;
        case CREATOR:
            bookBuilder.setCreator(content);
            break;
        case PUBLISHER:
            bookBuilder.setPublisher(content);
            break;

        default:
            // this handles null (apparently :)
            break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        buffer.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        current = elementMap.get(ParserUtilities.getName(localName, name));
        if (name.contains("dc")) {
            handleMetadata(name);
        }
        if (current == null) {
            return;
        }
        switch (current) {
        case SPINE:
            while (listModel.size() > 0) {
                attachSectionToParent(listModel.get(0));
            }
            break;
        default:
            break;
        }

    }

    public DaisyBook build() {
        return bookBuilder.build();
    }

    public static DaisyBook readFromStream(InputStream contents, BookContext bookContext)
            throws IOException {
        String encoding = obtainEncodingStringFromInputStream(contents);
        encoding = mapUnsupportedEncoding(encoding);
        return readFromStream(contents, encoding, bookContext);

    }

    public static DaisyBook readFromStream(InputStream contents, String encoding,
            BookContext bookContext) throws IOException {
        OpfSpecification specification = new OpfSpecification(bookContext);
        try {
            XMLReader saxParser = Smil.getSaxParser();
            saxParser.setContentHandler(specification);
            saxParser.parse(Smil.getInputSource(contents));
            contents.close();
            return specification.build();

        } catch (Exception e) {
            throw new IOException("Couldn't parse the opf contents.", e);
        }
    }
}
