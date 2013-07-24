package org.androiddaisyreader.metadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.utils.Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.Activity;

@SuppressLint("NewApi")
public class MetaDataHandler extends Activity {

	public NodeList ReadDataDownloadFromXmlFile(InputStream databaseInputStream, String link) {
		NodeList nList = null;
		try {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc;
				doc = dBuilder.parse(databaseInputStream);
				doc.getDocumentElement().normalize();
				nList = ReadDataDownloadFromXmlFile(doc.getElementsByTagName(Constants.ATT_BOOKS),
						link);
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, MetaDataHandler.this);
				throw ex;
			}
		} catch (PrivateException e) {
			e.writeLogException();
		}
		return nList;
	}

	private NodeList ReadDataDownloadFromXmlFile(NodeList nodeList, String link) {
		NodeList nList = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element eElement = (Element) nodeList.item(i);
			String actualType = eElement.getAttribute(Constants.ATT_TYPE);
			if (actualType.endsWith(Constants.TYPE_DOWNLOAD_BOOK)) {
				NodeList webNode = eElement.getElementsByTagName(Constants.ATT_WEBSITE);
				for (int j = 0; j < webNode.getLength(); j++) {
					Element e = (Element) webNode.item(j);
					String urlWebsite = e.getAttribute(Constants.ATT_URL);
					if (urlWebsite.equals(link)) {
						nList = e.getElementsByTagName(Constants.ATT_BOOK);
					}
				}
			}
		}
		return nList;
	}

	public NodeList ReadDataScanFromXmlFile(InputStream databaseInputStream) {
		NodeList nList = null;
		try {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc;
				doc = dBuilder.parse(databaseInputStream);
				doc.getDocumentElement().normalize();
				nList = doc.getElementsByTagName(Constants.ATT_BOOK);
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, MetaDataHandler.this);
				throw ex;
			}
		} catch (PrivateException e) {
			e.writeLogException();
		}
		return nList;
	}

	public void WriteDataToXmlFile(ArrayList<DaisyBook> daisybooks, String localPath) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element bookElement = doc.createElement(Constants.ATT_BOOKS);
			doc.appendChild(bookElement);
			// root elements
			for (DaisyBook daisybook : daisybooks) {
				Element book = doc.createElement(Constants.ATT_BOOK);
				bookElement.appendChild(book);

				Attr attr = doc.createAttribute(Constants.ATT_PATH);
				attr.setValue(daisybook.getPath());
				book.setAttributeNode(attr);

				// title elements
				Element eTitle = doc.createElement(Constants.ATT_TITLE);
				eTitle.appendChild(doc.createTextNode(daisybook.getTitle()));
				book.appendChild(eTitle);

				// author elements
				Element eAuthor = doc.createElement(Constants.ATT_AUTHOR);
				String author = daisybook.getAuthor() != null ? daisybook.getAuthor() : "";
				eAuthor.appendChild(doc.createTextNode(author));
				book.appendChild(eAuthor);

				// publisher elements
				Element ePublisher = doc.createElement(Constants.ATT_PUBLISHER);
				String publisher = daisybook.getPublisher() != null ? daisybook.getPublisher() : "";
				ePublisher.appendChild(doc.createTextNode(publisher));
				book.appendChild(ePublisher);

				// date elements
				Element eDate = doc.createElement(Constants.ATT_DATE);
				String date = daisybook.getDate() != null ? daisybook.getDate() : "";
				eDate.appendChild(doc.createTextNode(date));
				book.appendChild(eDate);
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File file = new File(localPath);
			if (!file.exists()) {
				file.createNewFile();
			}
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}
