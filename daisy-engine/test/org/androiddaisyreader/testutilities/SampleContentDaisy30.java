package org.androiddaisyreader.testutilities;

public class SampleContentDaisy30 {
	public final static String firstTitle = "Test Book Title Daisy 3";
	
	public final static String simpleValidOpf = 
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<package xmlns=\"http://openebook.org/namespaces/oeb-package/1.0/\" unique-identifier=\"uid\" xml:lang=\"en\">" +
			"<metadata>" +
			"<dc-metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oebpackage=\"http://openebook.org/namespaces/oeb-package/1.0/\">" +
			"<dc:Creator>Gretchen McBride</dc:Creator>" +
			"<dc:Date>2008-05-09</dc:Date>" +
			"<dc:Description>Code Talkers</dc:Description>" +
			"<dc:Format>ANSI/NISO Z39.86-2005</dc:Format>" +
			"<dc:Language>EN-US</dc:Language>" +
			"<dc:Publisher>Pearson Scott Foresman</dc:Publisher>" +
			"<dc:Subject>Social Studies</dc:Subject>" +
			"<dc:Title>The Code Talkers</dc:Title>" +
			"<dc:Identifier id=\"uid\">ghBOOK1211209789</dc:Identifier>" +
			"</dc-metadata>" +
			"<x-metadata>" +
			"<meta name=\"dtb:multimediaContent\" content=\"text\" /> " +
			"<meta name=\"dtb:multimediaType\" content=\"textNCX\" />" +
			"<meta name=\"dtb:producedDate\" content=\"2008-05-09\" />" +
			"<meta name=\"dtb:producer\" content=\"gh, LLC\" />" +
			"<meta name=\"dtb:sourceDate\" content=\"2008-05-09\" />" +
			"<meta name=\"dtb:sourcePublisher\" content=\"Pearson Scott Foresman\" /> " +
			"<meta name=\"dtb:sourceTitle\" content=\"The Code Talkers\" />" +
			"<meta name=\"dtb:totalTime\" content=\"00:00.00\" />"+
			"<meta name=\"ghVerify\" content=\"EF3C37213231617CEC2795613A6288FC\" />" +
			"<meta name=\"gh:generator\" content=\"gh DTB Maker 4.0\" />" +
			"</x-metadata>"+
			"</metadata>"+
			"<manifest>" +
			"</manifest>" +
			"<spine>" +
			"<itemref idref=\"Smil_0\" />" +
			"</spine>" +
			"</package>"
			;
	
	public final static String SMIL_FILE_WITH_SINGLE_ITEM = 
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<smil xmlns=\"http://www.w3.org/2001/SMIL20/\" xml:lang=\"en\">" +
			"body" +
			"<seq id=\"baseseq\" dur=\"18:46.534\">" +
			
			"</seq>"+
			"</body>" +
			"<par  id=\"ghGen2\" class=\"doctitle\">" +
			"<text src=\"Thomas_Jefferson.xml#ghGen2\" />" +
			"</par>" +
			"<par  id=\"gh_pageRef1\" customTest=\"pagenum\" class=\"pagenum\">" +
			"<text src=\"Thomas_Jefferson.xml#gh_pageRef1\" /> " +
			"<audio src=\"audio/Page01.mp3\" clipBegin=\"00:00.00\" clipEnd=\"00:02.119\" />" +
			"</par>"+
			"</seq>" +
			"</smil>"
			;
}
