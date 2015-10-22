package test.edu.upenn.cis455;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.xml.sax.SAXException;

import edu.upenn.cis455.bean.DocumentRecord;
import edu.upenn.cis455.crawler.HttpClient;
import edu.upenn.cis455.xpath.DomParser;
import edu.upenn.cis455.xpath.XPath;
import edu.upenn.cis455.xpath.XPathParser;

/**
 * This class tests the Dom parsing logic by fetching the following document
 * from w3school website - <note> <to>Tove</to> <from>Jani</from>
 * <heading>Reminder</heading> <body>Don't forget me this weekend!</body>
 * </note>
 * 
 * Then it tests 2 xpaths against the document i.e. one valid and one invalid
 * 
 * @author cis455
 *
 */
public class DomParserTest extends TestCase
{

	/**
	 * This method tests the case with valid xpath
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testValidPathAndDocument() throws UnknownHostException,
			IOException, SAXException, ParserConfigurationException
	{
		XPath xPath = new XPath("/note/to");
		xPath.tokenize();
		XPathParser.parseXPath(xPath);
		URL sourceUrl = new URL("http://www.w3schools.com/xml/note.xml");
		HttpClient httpClient = new HttpClient(sourceUrl);
		DocumentRecord document = httpClient.getDocument();
		boolean result = DomParser.parseDom(document.getDocument(), xPath);
		assertEquals(result, true);
	}

	/**
	 * This method tests the case with invalid xpath
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */

	@Test
	public void testInValidPathAndValidDocument() throws UnknownHostException,
			IOException, SAXException, ParserConfigurationException
	{
		XPath xPath = new XPath("/note/non-present-field");
		xPath.tokenize();
		XPathParser.parseXPath(xPath);
		URL sourceUrl = new URL("http://www.w3schools.com/xml/note.xml");
		HttpClient httpClient = new HttpClient(sourceUrl);
		DocumentRecord document = httpClient.getDocument();
		boolean result = DomParser.parseDom(document.getDocument(), xPath);
		assertEquals(result, false);
	}
}
