/**
 * 
 */
package org.androiddaisyreader.testutilities;

import java.io.OutputStream;

/**
 * This abstract class represents a generic "Create eBook" set of helpers.
 * 
 * Initially it will be sub-classed with a couple of classes to create DAISY 3
 * and DAISY 2.02 books. Potentially it could be extended to create ePub and
 * other zipped books. Also potentially we can add support for RTF and plain
 * text books, if demands warrant.
 * 
 * Note: the choice of an OutputStream inherently makes the output linear and
 * means we need to write end tags, etc. careful (and in the correct order if
 * we want to generate a valid xml document representing a book). However, as
 * we have control over the order, we can take advantage of the linear nature
 * to generate out-of-order (i.e. invalid) content; which may help to test 
 * error handling of parsers, etc.
 * 
 * As ever, the design and implementation are expected to evolve as we learn
 * more about how to implement each format of book.
 */
public abstract class CreateEBook {
	protected OutputStream out;
	
	/**
	 * The overridden method needs to create an output stream.
	 * @throws NotImplementedException
	 */
	public CreateEBook() throws NotImplementedException {
		throw new NotImplementedException();
	}
	
	/**
	 * The 'contents' of the eBook will be written to the output stream.
	 * @param out The OutputStream to use e.g. ByteArrayOutputStream.
	 * @throws NotImplementedException
	 */
	public CreateEBook(OutputStream out) throws NotImplementedException {
		this.out = out;
	}

	public void writeXmlHeader() {
	}
	
	/**
	 * Write out the XML namespace statement.
	 */
	public void writeXmlns() {
	}

	/**
	 * Write the basic (minimal) meta tags.
	 */
	public void writeBasicMetadata() {
	}
	
	/**
	 * Write out the trailing xml tag.
	 */
	public void writeEndOfDocument() {
	}


}
