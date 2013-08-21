package org.androiddaisyreader.apps.metadata;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.androiddaisyreader.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import junit.framework.TestCase;

public class MetaDataHandlerTest extends TestCase {
	
	public void testWhatHappensWhenNullPassedToCreateTextNode() throws ParserConfigurationException {
		// This replicates the call to doc.createTextNode(...) where null is
		// input to the method. At this stage there's no need to call the
		// application code. Instead I'll copy some of the code from there to 
		// bootstrap my investigations.
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Text temp = doc.createTextNode(null);
		
		// If we get this far then we know we can create the text node.
		// So on to the next part of the code
		Element eTitle = doc.createElement(Constants.ATT_TITLE);
		eTitle.appendChild(temp);
		
		// given we've got this far, we've reproduced the essential calls from
		// the failing line of code. So perhaps there's another problem, or
		// one that's specific to the Android runtime (I've been testing using
		// J2SE 6).
	}

}
