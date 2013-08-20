package org.androiddaisyreader.daisy30;

import static org.androiddaisyreader.model.XmlUtilities.mapUnsupportedEncoding;
import static org.androiddaisyreader.model.XmlUtilities.obtainEncodingStringFromInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.ParserUtilities;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.model.XmlUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
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
	private ArrayList<XmlModel> listModel;
	private Daisy30Book.Builder bookBuilder = new Daisy30Book.Builder();
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

	private enum Meta {
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

	private static Map<String, Meta> metaMap = new HashMap<String, Meta>(Meta.values().length);
	static {
		for (Meta m : Meta.values()) {
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
			handleStartOfSpine(current, attributes);
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
		if (href.endsWith("xml")) {
			InputStream contents;
			try {
				contents = bookContext.getResource(href);
				listModel = XmlSpecification.readFromStream(contents);
			} catch (IOException e) {
			}
		}
		manifestItem.put(getId(attributes), getHref(attributes));
	}

	private void handleStartOfSpine(Element heading, Attributes attributes) {
		// Create the new header
		String smilHref = manifestItem.get(getIdRef(attributes));
		XmlModel model = getXmlModelBySmilHref(smilHref);
		attachSectionToParent(model);
	}

	private void attachSectionToParent(XmlModel model) {
		Daisy30Section.Builder builder = new Daisy30Section.Builder();
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
		Meta meta = metaMap.get(tagName.toLowerCase());

		if (meta == null) {
			return;
		}
		switch (meta) {
		case DATE:
			Date date = parseDate(content, null);
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

	private Date parseDate(String content, String scheme) {
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

	public Daisy30Book build() {
		return bookBuilder.build();
	}

	// public static Daisy30Book readFromFile(File file) throws IOException {
	// InputStream contents = new BufferedInputStream(new
	// FileInputStream(file));
	// String encoding = obtainEncodingStringFromInputStream(contents);
	// encoding = mapUnsupportedEncoding(encoding);
	// return readFromStream(contents, encoding);
	// }

	public static Daisy30Book readFromStream(InputStream contents, BookContext bookContext)
			throws IOException {
		String encoding = obtainEncodingStringFromInputStream(contents);
		encoding = mapUnsupportedEncoding(encoding);
		return readFromStream(contents, encoding, bookContext);

	}

	public static Daisy30Book readFromStream(InputStream contents, String encoding,
			BookContext bookContext) throws IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		OpfSpecification specification = new OpfSpecification(bookContext);
		try {
			XMLReader saxParser = factory.newSAXParser().getXMLReader();
			saxParser.setEntityResolver(XmlUtilities.dummyEntityResolver());
			saxParser.setContentHandler(specification);
			InputSource input = new InputSource(contents);
			input.setEncoding(encoding);
			saxParser.parse(input);
			return specification.build();

		} catch (Exception e) {
			throw new IOException("Couldn't parse the opf contents.", e);
		}
	}
}
