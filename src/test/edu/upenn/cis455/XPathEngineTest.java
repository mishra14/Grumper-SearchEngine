package test.edu.upenn.cis455;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

/**
 * This class tests the xpath engine to check the xpath validation
 * 
 * @author cis455
 *
 */
public class XPathEngineTest extends TestCase
{
	/**
	 * This method tests 5 valid xpath cases
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testXPathAllValid() throws UnknownHostException, IOException,
			SAXException, ParserConfigurationException
	{
		// get all the xPaths
		String xPath = "/node;"
				+ "/node[a/b/c[subnode]]/sub-subnode;"
				+ "/node[t e x  t ( )=\"someText\"];"
				+ "/node[@att=\"value\"];"
				+ "/node[contains(text(),\"someText\")];"
				+ "/node[filter[subfiler[lastfilter[contains(text(),\"someText\")]]]]";
		String[] xPaths = xPath.split(";");
		XPathEngine engine = XPathEngineFactory.getXPathEngine();
		engine.setXPaths(xPaths);
		boolean[] result = new boolean[xPaths.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = engine.isValid(i);
		}
		boolean[] expected = new boolean[xPaths.length];
		for (int i = 0; i < result.length; i++)
		{
			expected[i] = true;
		}
		assertArrayEquals(result, expected);
	}

	/**
	 * This method tests 5 invalidity cases for the xpaths - no axis, no step,
	 * //, unknown symbols and non closed brackets
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testXPathAllInvalid() throws UnknownHostException, IOException,
			SAXException, ParserConfigurationException
	{
		// get all the xPaths
		String xPath = "/;" + "node[a/b/c[subnode]]/sub-subnode;"
				+ "/node[text()=\"someText\";" + "/node//" + "/node&&";
		String[] xPaths = xPath.split(";");
		XPathEngine engine = XPathEngineFactory.getXPathEngine();
		engine.setXPaths(xPaths);
		boolean[] result = new boolean[xPaths.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = engine.isValid(i);
		}
		boolean[] expected = new boolean[xPaths.length];
		for (int i = 0; i < result.length; i++)
		{
			expected[i] = false;
		}
		assertArrayEquals(result, expected);
	}
}
