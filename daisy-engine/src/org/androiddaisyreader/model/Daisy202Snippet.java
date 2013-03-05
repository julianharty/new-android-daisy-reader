package org.androiddaisyreader.model;

import static org.androiddaisyreader.model.XmlUtilities.obtainEncodingStringFromInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Daisy202Snippet extends Snippet {
	private Document doc;
	private String id;

	// Prevent people from using the default constructor.
	@SuppressWarnings("unused")
	private Daisy202Snippet() {
	}
	
	/**
	 * Create a DAISY 2.02 snippet. 
	 * 
	 * Uses a jsoup document and the id part of a composite reference. This
	 * constructor should be significantly faster than the one that creates a
	 * jsoup document.
	 * @param doc the jsoup representation of the HTML document
	 * @param id the id used to get the text.
	 */
	Daisy202Snippet(Document doc, String id) {
		this.doc = doc;
		this.id = id;
	}

	/**
	 * Create a DAISY 2.02 snippet.
	 * Uses the book's context & a composite reference.
	 * 
	 * The context may be a file location, an index into a zip file, etc. The
	 * context is needed as composite references contain relative references.
	 * 
	 * A composite reference is formatted as follows:
	 *   fire_safety.html#dol_1_4_rgn_cnt_0043
	 *   An example of the reference in context follows:
	 *   <text src="fire_safety.html#dol_1_4_rgn_cnt_0043" id="rgn_txt_0004_0017"/>
	 * @param context
	 * @param compositeReference
	 */
	Daisy202Snippet(BookContext context, String compositeReference) {
		if (context == null) {
			throw new IllegalArgumentException("Programming error: context needs to be set");
		}
		
		String[] elements = parseCompositeReference(compositeReference);
		String uri = elements[0];
		this.id = elements[1];
		try {
			InputStream contents = context.getResource(uri);
			String encoding = obtainEncodingStringFromInputStream(contents);
			doc = Jsoup.parse(contents, encoding, context.getBaseUri());
		} catch (IOException ioe) {
			// TODO 20120214 (jharty): we need to consider more appropriate error reporting.
			throw new RuntimeException("TODO fix me", ioe);
		}
	}

	/**
	 * Split a composite reference into the constituent parts.
	 * 
	 *   A composite reference is formatted as follows:
	 *   fire_safety.html#dol_1_4_rgn_cnt_0043
	 *   An example of the reference in context follows:
	 *   <text src="fire_safety.html#dol_1_4_rgn_cnt_0043" id="rgn_txt_0004_0017"/>
	 * @param compositeReference to split
	 * @return 2 strings, the first [0] contains the relative filename, the
	 *   second [1] contains the id.
	 * @throws IllegalArgumentException if the composite reference doesn't
	 *   match the expected structure.
	 */
	public static String[] parseCompositeReference(String compositeReference) {
		String[] elements = compositeReference.split("#");
		if (elements.length != 2) {
			throw new IllegalArgumentException(
					"Expected composite reference in the form uri#id, got " + compositeReference);
		}
		return elements;
	}

	@Override
	public String getText() {
			return doc.getElementById(id).text(); 
	}

	@Override
	public boolean hasText() {
		final Element element = doc.getElementById(id);
		if (element == null || element.text() == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public String getId() {
		// TODO 20120214 (jharty): Consider keeping the composite reference as
		// the ID since these IDs are only truly unique in the context of the
		// filename...
		return id;
	}

}
